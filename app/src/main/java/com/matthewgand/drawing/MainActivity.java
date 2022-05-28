package com.matthewgand.drawing;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private DrawView drawing;

    private ImageButton save, color, undo, load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawing = (DrawView) findViewById(R.id.draw_view);
        undo = (ImageButton) findViewById(R.id.btn_undo);
        save = (ImageButton) findViewById(R.id.btn_save);
        color = (ImageButton) findViewById(R.id.btn_color);
        load = (ImageButton) findViewById(R.id.btn_load);

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawing.undo();
            }
        });

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Possibly add a way to change the color of the pen.
                 */
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                EditText input = new EditText(v.getContext());

                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                builder.setTitle("Enter file name: ");

                builder.setPositiveButton("okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String filename = input.getText().toString();
                        drawing.save(filename);
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String[] files = folder.list();

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setTitle("Choose a file");
                builder.setItems(files, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        drawing.load(files[which]);
                    }
                });
                builder.show();
            }
        });

        ViewTreeObserver vto = drawing.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                drawing.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = drawing.getMeasuredWidth();
                int height = drawing.getMeasuredHeight();
                drawing.init(height, width);
            }
        });

    }
}