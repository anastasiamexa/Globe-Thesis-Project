package com.example.globe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.List;

public class SelectImage extends AppCompatActivity {

    // Define the pic id
    private static final int pic_id = 123;
    private static final int gallery_pic_id = 200;
    private static final int REQUEST_CODE_STORAGE = 124;
    private static final int REQUEST_CODE_CAMERA = 125;

    // Define the button and imageview type variable
    Button camera_open_id;
    Button gallery_open_id;
    ImageView click_image_id;
    Bitmap photo;
    Uri selectedImageUri;
    Boolean from_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_image);

        // By ID we can get each component
        // which id is assigned in XML file
        // get Buttons and imageview.
        camera_open_id = findViewById(R.id.camera_button);
        gallery_open_id = findViewById(R.id.gallery_button);
        click_image_id = findViewById(R.id.click_image);

        // handle the Choose Image button to trigger
        // the image chooser function
        gallery_open_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request for storage permission
                if(ContextCompat.checkSelfPermission(SelectImage.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SelectImage.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
                } else {
                    imageChooser();
                }
            }
        });

        // Camera_open button is for open the camera
        // and add the setOnClickListener in this button
        camera_open_id.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                // Request for camera permission
                if(ContextCompat.checkSelfPermission(SelectImage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SelectImage.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
                } else {
                    // Create the camera_intent ACTION_IMAGE_CAPTURE
                    // it will open the camera for capture the image
                    Intent camera_intent
                            = new Intent(MediaStore
                            .ACTION_IMAGE_CAPTURE);

                    // Start the activity with camera_intent,
                    // and request pic id
                    startActivityForResult(camera_intent, pic_id);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            imageChooser();
        }
        if (requestCode == REQUEST_CODE_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // Create the camera_intent ACTION_IMAGE_CAPTURE
            // it will open the camera for capture the image
            Intent camera_intent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, photo);
            // Start the activity with camera_intent,
            // and request pic id
            startActivityForResult(camera_intent, pic_id);
        }
    }

    // this function is triggered when
    // the Select Image Button is clicked
    void imageChooser() {
        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), gallery_pic_id);
    }

    // This method will help to retrieve the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == pic_id) {
                // BitMap is data structure of image file
                // which stores the image in memory
                photo = (Bitmap) data.getExtras().get("data");
                // Set the image in imageview for display
                click_image_id.setImageBitmap(photo);

                // Set the boolean variable to true
                from_camera = true;
            }

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == gallery_pic_id) {
                // Get the url of the image from data
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Set the boolean variable to false
                    from_camera = false;
                    click_image_id.setImageURI(selectedImageUri);
                }
            }
        }
    }

    public void runTextRecognition(View view) throws IOException {
        InputImage image;
        if (from_camera){
            image = InputImage.fromBitmap(photo, 0);
        } else {
            image = InputImage.fromFilePath(this, selectedImageUri);
        }
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text texts) {
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast(getString(R.string.no_text_found));
            return;
        } else {
            String results = "";
            for (int i = 0; i < blocks.size(); i++) {
                List<Text.Line> lines = blocks.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<Text.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        results = results + " " +elements.get(k).getText();
                    }
                }
            }
            showToast(results);
            if (!results.equals("")) {
                Intent intent = new Intent(this, Translation.class);
                intent.putExtra("image text", results);
                startActivity(intent);
            } else {
                showToast(getString(R.string.no_text_recognised));
            }
        }
    }
}