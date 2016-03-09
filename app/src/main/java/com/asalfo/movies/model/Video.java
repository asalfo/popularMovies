package com.asalfo.movies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by asalfo on 03/02/16.
 */
public class Video  implements Parcelable{
    private String id;
    private String languague;
    private String key;
    private String name;
    private String site;
    private String size;
    private String type;


    public static final Parcelable.Creator<Video> CREATOR
            = new Parcelable.Creator<Video>() {
        public Video createFromParcel(Parcel in) {
            return new Video (in);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };


    public Video(String id, String languague, String key, String name, String site, String size, String type) {
        this.id = id;
        this.languague = languague;
        this.key = key;
        this.name = name;
        this.site = site;
        this.size = size;
        this.type = type;
    }

    private Video(Parcel in) {
        this.id = in.readString();
        this.languague = in.readString();
        this.key = in.readString();
        this.name = in.readString();
        this.site = in.readString();
        this.size = in.readString();
        this.type = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(languague);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
        dest.writeString(size);
        dest.writeString(type);


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguague() {
        return languague;
    }

    public void setLanguague(String languague) {
        this.languague = languague;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
