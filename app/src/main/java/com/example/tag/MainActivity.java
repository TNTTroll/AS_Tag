package com.example.tag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Context thisContext;
    ConstraintLayout cl;
    DisplayMetrics displaymetrics;

    Mover image, reset, select, minus, plus;
    TextView showSize;

    int size = 3;
    int mixes;
    int inactivePlate;

    Mover[] buttons = new Mover[size * size];

    Bitmap black;
    Bitmap localPicture = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisContext = this;
        displaymetrics = getResources().getDisplayMetrics();

        cl = (ConstraintLayout) findViewById(R.id.layout);

        ImageButton bg = (ImageButton) findViewById(R.id.bg);
        bg.setEnabled(false);

        reset = (Mover) findViewById(R.id.reset);
        reset.setVisibility(View.GONE);
        reset.setOnClickListener(this);

        select = (Mover) findViewById(R.id.select);
        select.setOnClickListener(this);

        image = (Mover) findViewById(R.id.image);
        image.setEnabled(false);
        image.setVisibility(View.GONE);

        minus = (Mover) findViewById(R.id.minus);
        minus.setOnClickListener(this);

        plus = (Mover) findViewById(R.id.plus);
        plus.setOnClickListener(this);

        createPlates(0);
    }

    public static final int PICK_IMAGE = 1;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                localPicture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                inactivePlate = buttons.length - 1;
            }
            catch (IOException ignored) {}

            preparePicture();
        }
    }

    @SuppressLint("IntentReset")
    @Override
    public void onClick(View v) {

        for (Mover button : buttons) {
            if ( button.getTag() == v.getTag() ) {

                int move = findPlate( button.getName() );
                if (move != -1) {
                    Mover old = cl.findViewWithTag("btn_" + inactivePlate);

                    int save = old.getPos();

                    old.setIcon( button.getIcon() );
                    old.setPos( button.getPos() );

                    button.setIcon( new BitmapDrawable(getResources(), black) );
                    button.setPos( save );

                    inactivePlate = button.getName();
                }

                break;
            }
        }

        if (v.getId() == R.id.reset)
            restartGame();

        else if (v.getId() == R.id.select) {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            @SuppressLint("IntentReset") Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE);

        } else if (v.getId() == R.id.minus) {
            if (size > 2)
                createPlates(-1);

        } else if (v.getId() == R.id.plus) {
            if (size < 5)
                createPlates(1);

        } else {
            if (checkPlates()) {
                for (Mover button : buttons)
                    button.setVisibility(View.GONE);

                image.setVisibility(View.VISIBLE);

                reset.setVisibility(View.VISIBLE);

                minus.setVisibility(View.GONE);
                showSize.setVisibility(View.GONE);
                plus.setVisibility(View.GONE);
            }
        }
    }

    private void preparePicture() {

        BitmapDrawable blackPicture = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), getResId("black", R.drawable.class), null);
        black = blackPicture.getBitmap();

        BitmapDrawable getPicture = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), getResId("pic", R.drawable.class), null);
        Bitmap picture = getPicture.getBitmap();

        if (localPicture != null)
            picture = localPicture;

        int imageHeight = picture.getHeight();
        int imageWidth = picture.getWidth();

        int need = Math.min(imageWidth, imageHeight) / 2 - 1;
        Bitmap usePicture = Bitmap.createBitmap(picture, imageWidth / 2 - need, imageHeight / 2 - need, need * 2, need * 2);
        need *= 2;

        image.setParam(-1, new BitmapDrawable(getResources(), usePicture), -1);

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                int index = i*size+j;

                Mover btn = cl.findViewWithTag("btn_" + index);

                Bitmap crop = Bitmap.createBitmap(usePicture, j*(need / size), i*(need / size), need / size, need / size);

                btn.setPos(index);

                if (index != inactivePlate)
                    btn.setIcon(new BitmapDrawable(getResources(), crop));
                else
                    btn.setIcon(new BitmapDrawable(getResources(), black));

                buttons[index] = btn;
            }

        generatePlates();
    }

    private boolean checkPlates() {
        for (int index = 0; index < buttons.length; index++)
            if (buttons[index].getPos() != index)
                return false;

        return true;
    }

    private void generatePlates() {

        int last = -1;
        for (int index = 0; index < mixes; index++) {
            List<Integer> candidates = new ArrayList<Integer>();

            for (int move = 0; move < buttons.length; move++) {
                if (findPlate(buttons[move].getName()) != -1)
                    candidates.add(move);
            }

            Mover candidate = buttons[candidates.get(new Random().nextInt(candidates.size()) )];

            Mover old = cl.findViewWithTag("btn_" + inactivePlate);

            int save = old.getPos();

            old.setIcon( candidate.getIcon() );
            old.setPos( candidate.getPos() );

            candidate.setIcon( new BitmapDrawable(getResources(), black) );
            candidate.setPos( save );

            inactivePlate = candidate.getName();

            if (last == candidate.getName())
                mixes += 1;

            last = old.getName();
        }
    }

    private void createPlates(int changeSize) {

        size += changeSize;

        showSize = (TextView) findViewById(R.id.size);
        showSize.setText("" + size);

        try {
           for (Mover button : buttons)
               cl.removeView(button);
        } catch (Exception ignored) {}

        buttons = new Mover[size * size];
        inactivePlate = buttons.length - 1;
        mixes = size * 10;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int index = i * size + j;
                final Mover btn = new Mover(this);

                btn.setTag("btn_" + index);

                btn.setParam(index, new BitmapDrawable(getResources(), black), index);
                btn.setOnClickListener(this);

                int pxPieceSize = (int) (displaymetrics.widthPixels * 0.8) / size;

                int xOffset = (int) (displaymetrics.widthPixels * 0.2) / 2;
                int yOffset = (int) (displaymetrics.heightPixels * 0.5) / 2;

                btn.setX(j * pxPieceSize + xOffset);
                btn.setY(i * pxPieceSize + yOffset);

                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(pxPieceSize, pxPieceSize);
                btn.setLayoutParams(layoutParams);

                cl.addView(btn);

                buttons[index] = btn;
            }
        }

        preparePicture();
    }

    private void restartGame() {
        for (Mover button : buttons)
            button.setVisibility(View.VISIBLE);

        image.setVisibility(View.GONE);

        reset.setVisibility(View.GONE);

        minus.setVisibility(View.VISIBLE);
        showSize.setVisibility(View.VISIBLE);
        plus.setVisibility(View.VISIBLE);

        generatePlates();
    }

    public int findPlate(int index) {

        try {
            if (buttons[index + 1].getName() == inactivePlate && buttons[index].getName() % size != size - 1)
                return buttons[index + 1].getName();
        } catch(Exception ignored) {}

        try {
            if (buttons[index - 1].getName() == inactivePlate && buttons[index].getName() % size != 0)
                return buttons[index - 1].getName();
        } catch(Exception ignored) {}

        try {
            if (buttons[index - size].getName() == inactivePlate)
                return buttons[index - size].getName();
        } catch(Exception ignored) {}

        try {
            if (buttons[index + size].getName() == inactivePlate)
                return buttons[index + size].getName();
        } catch(Exception ignored) {}

        return -1;
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            return -1;
        }
    }
}