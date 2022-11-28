package com.example.globe;

import android.os.Parcel;
import android.os.Parcelable;

public class Language implements Parcelable {
    // Event attributes
    private String  Title, Icon, Short, Tag;

    // Constructor for parcelable
    protected Language(Parcel in) {
        Title = in.readString();
        Icon = in.readString();
        Short = in.readString();
        Tag = in.readString();
    }

    // Constructor
    public Language(String title, String icon, String aShort, String tag) {
        Title = title;
        Icon = icon;
        Short = aShort;
        Tag = tag;
    }

    // Empty constructor
    protected Language(){}


    // Interface that must be implemented and provided as a public CREATOR field that generates
    // instances of your Parcelable class from a Parcel.
    public static final Creator<Language> CREATOR = new Creator<Language>() {
        @Override
        public Language createFromParcel(Parcel in) {
            return new Language(in);
        }

        @Override
        public Language[] newArray(int size) {
            return new Language[size];
        }
    };

    // Getters
    public String getTitle() { return Title; }

    public String getIcon() { return Icon; }

    public String getShort() { return Short; }

    public String getTag() { return Tag; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Title);
        dest.writeString(Icon);
        dest.writeString(Short);
        dest.writeString(Tag);
    }
}
