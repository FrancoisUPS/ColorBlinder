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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.thirdparty.color.ColorDifference;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class LineActivity extends Activity {
    static final double MAX_COLOR_DIFF = 6;
    static final double SEUIL_COLOR_DIFF = 0.2;

    private Uri imageUri;
    public static final String CST_IMGURI = "uri";
    private float upx, upy, downx, downy;
    private Canvas lineCanvas;

    private int[][] pixelsP1;
    private int[][] pixelsP2;

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
                    paint.setColor(Color.YELLOW);
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
                            for (int[] pixelCo : pixelsP1)
                            {
                                int pixel = bitmap.getPixel(pixelCo[0],pixelCo[1]);
                                colorsPassed.add(pixel);
                            }

                            Toast.makeText(LineActivity.this, "You did " + colorsPassed.size(), Toast.LENGTH_LONG).show();

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
            Bitmap tempBitmap = Bitmap.createScaledBitmap(bitmap, size.x - 30, size.y - 30, false);

            lineCanvas = new Canvas(tempBitmap);

            //Draw the image b itmap into the cavas
            lineCanvas.drawBitmap(bitmap, 0, 0, null);

            //Attach the canvas to the ImageView
            imgView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        } catch (IOException e) {
            Log.d("test", e.toString(), e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);

        Intent intent = getIntent();
        imageUri = intent.getParcelableExtra(CST_IMGURI);

        drawImage();
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
