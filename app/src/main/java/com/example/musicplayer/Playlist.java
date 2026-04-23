package com.example.musicplayer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;

public class Playlist implements Iterable<Song>, Parcelable {

    private String name;
    private ArrayList<Song> list = new ArrayList<>();

    public Playlist(String name){
        this.name = name;
    }

    protected Playlist(Parcel in) {
        name = in.readString();
        list = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public void addSong(Song song){
        list.add(song);
    }

    public void removeSong(Song song){
        list.remove(song);
    }

    public Song[] getSongs(){
        return list.toArray(new Song[0]);
    }

    public String getName(){
        return name;
    }

    public String[] getTitles(){
        String[] titles = new String[list.size()];
        int index = 0;
        for (Song s : list){
            titles[index++] = s.getTitle();
        }
        return titles;
    }

    @NonNull
    @Override
    public Iterator<Song> iterator() {
        return new Iterator<Song>() {
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < list.size();
            }

            @Override
            public Song next() {
                return list.get(index++);
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(list);
    }

    public int size(){
        return list.size();
    }
}
