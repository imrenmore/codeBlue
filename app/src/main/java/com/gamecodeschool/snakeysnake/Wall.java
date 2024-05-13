package com.gamecodeschool.snakeysnake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import java.io.IOException;

class Wall extends MainObject {

    //holds the location of the wall segments
    private List<Point> wallSegments = new ArrayList<>();

    //size of each wall segment
    private int segmentSize;

    private MediaPlayer mediaPlayer;

    public Wall(Context context, Point gridDimensions, int segmentSize, int numberOfSegments) {
        this.segmentSize = segmentSize;
        initializeWall(gridDimensions, numberOfSegments);
        //calls the inisitialze wall method to populate the wallsegments randomly
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wall);
        //loads wall.png
        bitmap = Bitmap.createScaledBitmap(bitmap, segmentSize, segmentSize, false);
        // Initialize MediaPlayer with a sound from the assets folder
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd("creeper_explosion.ogg");
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();  // Handle exceptions appropriately
        }
    }

    protected void initializeWall(Point gridDimensions, int numberOfSegments) {
        //clears the old wallsegmnets from the list from pervious game
        wallSegments.clear();
        Random random = new Random();
        //random x and y coordinates withing the griddementsion
        for (int i = 0; i < numberOfSegments; i++) {
            int x = random.nextInt(gridDimensions.x);
            int y = random.nextInt(gridDimensions.y);
            //represents the wallsegments position
            wallSegments.add(new Point(x, y));
        }
    }
    //checks if snake collides with any wallsegments
    public boolean checkCollision(Point snakeHead) {
        for (Point segment : wallSegments) {
            if (segment.equals(snakeHead)) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();  // Play sound on collision
                }
                return true;
            }
        }
        return false;
    }
    @Override
    public void draw(Canvas canvas, Paint paint) {
        for (Point segment : wallSegments) {
            canvas.drawBitmap(bitmap, segment.x * segmentSize, segment.y * segmentSize, paint);
        }
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public boolean containsPoint(Point point) {
        return false;
    }

    @Override
    public void move() {

    }
}


