package com.matthewgand.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class DrawView extends View implements Serializable {

    private static final float TOLERANCE = 4;
    private float mX, mY;
    private Path path;

    private Paint brush;

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);

    private int currentColor;
    private int strokeWidth;

    private int backgroundColor;

    /*
    Paths ArrayList will store the brush data and color.
    Strokes ArrayList will store the brush location and color.

    Two ArrayLists, because we cannot Serialize the Path class (Android).
     */
    private ArrayList<Stroke> paths = new ArrayList<>();
    private ArrayList<Stroke> strokes = new ArrayList<>();

    // Not sure why we need this constructor
    public DrawView(Context context) {
        this(context, null);
    }

    // Not sure why we need this constructor
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        /*
        Setting up the brush, similar to turtle in Python.
         */

        brush = new Paint();

        brush.setAntiAlias(true);
        brush.setDither(true);
        brush.setColor(Color.BLUE);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeCap(Paint.Cap.ROUND);
    }

    public void init(int height, int width) {
        /*
        Not really sure what this code does, my best guess is that it creates a canvas for us to draw on?
         */
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();

        currentColor = Color.BLUE;
        strokeWidth = 20;
    }

    public void setColor(int color) { currentColor = color; }

    public void setStrokeWidth(int width) { strokeWidth = width; }

    public void undo() {
        try {
            strokes.remove(strokes.size() - 1);
            paths.remove(paths.size() - 1);
            invalidate();
        } catch (ArrayIndexOutOfBoundsException e) {
            // Except error if strokes or path is empty.
        }
    }

    public void save(String filename) {
        try {
            /*
            Create a better file saving system.
             */
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(folder, filename + ".draw");

            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(strokes);

            Toast.makeText(getContext(), "File saved as " + filename + "!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(getContext(), "Unable to save file!", Toast.LENGTH_SHORT).show();
        }
    }

    public void load(String filename) {
        try {
            // Clears the current drawing on the screen, by clearing the list.
            strokes.clear();
            paths.clear();

            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(folder, filename);

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            ArrayList<Stroke> strokes = (ArrayList<Stroke>) ois.readObject();

            Toast.makeText(getContext(), "File " + filename + " loaded!", Toast.LENGTH_SHORT).show();

            // Loading all strokes and paths to the canvas.
            for (Stroke s : strokes) {
                switch (s.action) {
                    case MotionEvent.ACTION_DOWN:
                        actionDown(s.x, s.y);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        actionMove(s.x, s.y);
                        break;

                    case MotionEvent.ACTION_UP:
                        actionUp(s.x, s.y);
                        break;
                }
            }

        } catch (IOException e) {
            Toast.makeText(getContext(), "Unable to load!", Toast.LENGTH_SHORT).show();

        } catch (ClassNotFoundException e) {
            Toast.makeText(getContext(), "File not found!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();

        // Possibly add the ability to change the wallpaper of a canvas, we also need to save it.
        backgroundColor = Color.WHITE;
        canvas.drawColor(backgroundColor);

        for (Stroke p : paths) {
            brush.setColor(p.color);
            brush.setStrokeWidth(p.width);
            canvas.drawPath(p.path, brush);
        }
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                actionMove(x, y);
                break;

            case MotionEvent.ACTION_UP:
                actionUp(x, y);
                break;
        }
        return true;
    }

    private void actionDown(float x, float y) {
        // Creates new object of starting path and adds it to the list of paths.
        path = new Path();

        Stroke p = new Stroke(currentColor, strokeWidth, path, MotionEvent.ACTION_DOWN);
        paths.add(p);

        // Creates new object of starting x, y and adds it to the list of strokes.
        Stroke s = new Stroke(currentColor, strokeWidth, x, y, MotionEvent.ACTION_DOWN);
        strokes.add(s);

        path.reset();

        path.moveTo(x, y);

        mX = x;
        mY = y;

        invalidate();
    }

    private void actionMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);

            // Create new object of Stroke.
            Stroke s = new Stroke(currentColor, strokeWidth, x, y, MotionEvent.ACTION_MOVE);
            strokes.add(s);

            mX = x;
            mY = y;
        }

        invalidate();
    }

    private void actionUp(float x, float y) {
        path.lineTo(mX, mY);

        // Create new object of Stroke.
        Stroke s = new Stroke(currentColor, strokeWidth, mX, mY, MotionEvent.ACTION_UP);
        strokes.add(s);

        invalidate();
    }
}
