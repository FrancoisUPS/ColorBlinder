package com.ups.m2dl.colorblinder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class LineActivity extends Activity {
    private Uri imageUri;
    public static final String CST_IMGURI = "uri";
    private float upx, upy, downx, downy;
    private Canvas lineCanvas;
    private int[][] pixelsP1;
    private int maxScore = 0;
    private MediaPlayer mp;

    private static final String[] WIN_MESSAGE = {"Well, your %1$s is nothing near the amazing %2$s your friend has scored", "Not bad, but your %1$s still doesn't beat the best score of %2$s", "So close! You almost beat the best with a %1$s" };

    private static final String BASE_PATH = "ColorBlinder/photos/";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public void takePhoto() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String storageDirectory = BASE_PATH + timestamp + ".jpg";

        File photo = new File(Environment.getExternalStorageDirectory(), storageDirectory);
        photo.getParentFile().mkdirs();

        imageUri = Uri.fromFile(photo);

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                drawImage();
            }
        }
    }
    protected void drawImage() {
        getContentResolver().notifyChange(imageUri, null);
        ContentResolver cr = getContentResolver();

        final Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);

            ImageView imgView = (ImageView) findViewById(R.id.imageView);
            imgView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);
                    paint.setStrokeWidth(10);
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            downx = event.getX();
                            downy = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            upx = event.getX();
                            upy = event.getY();
                            lineCanvas.drawLine(downx, downy, upx, upy, paint);
                            pixelsP1 = line((int) downx, (int) downy, (int) upx, (int) upy);

                            ((ImageView) findViewById(R.id.imageView)).invalidate();

                            Set<Integer> colorsPassed = new HashSet<Integer>();
                            for (int[] pixelCo : pixelsP1) {
                                if (pixelCo[0] < bitmap.getWidth() && pixelCo[1] < bitmap.getWidth()) {
                                    int pixel = bitmap.getPixel(pixelCo[0], pixelCo[1]);
                                    colorsPassed.add(pixel);
                                }
                            }

                            String message = "";
                            int score = colorsPassed.size();
                            if(score > maxScore) {
                                message = "You beat the best score with a marvelous %1$s";
                                maxScore = score;
                            } else {
                                int result = Math.round(( score / maxScore ) * WIN_MESSAGE.length);
                                Log.d("score ", String.valueOf(result));
                                if(result < 0)
                                    result = 0;

                                if(result >= WIN_MESSAGE.length)
                                    result = WIN_MESSAGE.length - 1;

                                message = WIN_MESSAGE[result];
                            }
                            message = String.format(message, score, maxScore);
                            Toast.makeText(LineActivity.this, message, Toast.LENGTH_LONG).show();

                            break;
                        case MotionEvent.ACTION_CANCEL:
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            //Create a new image bitmap and attach a brand new canvas to it
            Bitmap tempBitmap = Bitmap.createScaledBitmap(bitmap, size.x, size.y, true);

            lineCanvas = new Canvas(tempBitmap);

            //Draw the image b itmap into the cavas
            lineCanvas.drawBitmap(tempBitmap, 0, 0, null);

            //Attach the canvas to the ImageView
            imgView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        } catch (IOException e) {
            Log.d("test", e.toString(), e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mp = MediaPlayer.create(this, R.raw.background);
        mp.setLooping(true);
        mp.start();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_line);

        takePhoto();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mp.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mp.start();
    }

    //http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/
    private int[][] line(int x, int y, int x2, int y2) {

        int w = x2 - x;
        int h = y2 - y;

        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;

        if (w < 0) dx1 = -1;
        else if (w > 0) dx1 = 1;
        if (h < 0) dy1 = -1;
        else if (h > 0) dy1 = 1;
        if (w < 0) dx2 = -1;
        else if (w > 0) dx2 = 1;

        int longest = Math.abs(w);
        int shortest = Math.abs(h);

        if (!(longest > shortest)) {
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if (h < 0) dy2 = -1;
            else if (h > 0) dy2 = 1;
            dx2 = 0;
        }

        int numerator = longest >> 1;

        int[][] pixels = new int[longest][2];

        for (int i = 0; i < longest; i++) {

            pixels[i][0] = x;
            pixels[i][1] = y;

            numerator += shortest;
            if (!(numerator < longest)) {
                numerator -= longest;
                x += dx1;
                y += dy1;
            } else {
                x += dx2;
                y += dy2;
            }
        }

        return pixels;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_line, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
