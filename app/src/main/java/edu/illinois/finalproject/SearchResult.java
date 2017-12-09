package edu.illinois.finalproject;

import android.graphics.Bitmap;

/**
 * Created by Davinwid on 12/8/2017.
 */

public class SearchResult {

    private String result;
    private Bitmap image;

    public SearchResult(String result, Bitmap image) {
        this.result = result;
        this.image = image;
    }

    public String getResult() {
        return result;
    }

    public Bitmap getImage() {
        return image;
    }
}
