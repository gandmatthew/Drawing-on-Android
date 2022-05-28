package com.matthewgand.drawing;

import android.graphics.Path;

import java.io.Serializable;

public class Stroke extends Path implements Serializable {

    public int color;

    public int width;

    public Path path;

    public float x;
    public float y;

    public int action;

    public Stroke(int color, int width, Path path, int action) {
        this.color = color;
        this.width = width;
        this.path = path;
        this.action = action;
    }

    public Stroke(int color, int width, float x, float y, int action) {
        this.color = color;
        this.width = width;
        this.x = x;
        this.y = y;
        this.action = action;
    }
}
