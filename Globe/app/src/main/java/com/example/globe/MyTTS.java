package com.example.globe;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class MyTTS {
    private TextToSpeech tts;

    TextToSpeech.OnInitListener initListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status==TextToSpeech.SUCCESS){
                tts.setLanguage(Locale.US);
            }
        }
    };

    public MyTTS(Context context){
        tts = new TextToSpeech(context,initListener);
    }

    public void speak(String message, String lan){
        switch(lan) {
            case "fr":
                tts.setLanguage(Locale.FRENCH);
                break;
            case "de":
                tts.setLanguage(Locale.GERMAN);
                break;
            case "it":
                tts.setLanguage(Locale.ITALIAN);
                break;
            case "zh":
                tts.setLanguage(Locale.CHINESE);
                break;
            case "ja":
                tts.setLanguage(Locale.JAPANESE);
                break;
            default:
                break;
        }
        tts.speak(message,TextToSpeech.QUEUE_ADD,null,null);
    }

    public void stop(){
        tts.stop();
    }
}
