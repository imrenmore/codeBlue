package com.gamecodeschool.snakeysnake;

import com.gamecodeschool.snakeysnake.SnakeGame;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.Random;

class Apple extends MainObject {
    private SnakeGame mSnakeGame;

    // The location of the apple on the grid
    // Not in pixels
    private Point location = new Point();

    // The range of values we can choose from
    // to spawn an apple
    private final Point mSpawnRange;
    private final int mSize;

    // An image to represent the apple
    private Bitmap mBitmap;
    private Bitmap mBitmapApple;
    private Bitmap mGoldenApple;

    private long spawnTime;
    private static final long MAX_TIME = 8000;

    // Set up the apple in the constructor
    Apple(Context context, Point sr, int s) {
        // Make a note of the passed in spawn range
        this.mSpawnRange = sr;
        // Make a note of the size of an apple
        this.mSize = s;
        // Hide the apple off-screen until the game starts
        location.x = -10;
        intializeBitmap(context, s);
    }

    //initialize mSnakeGame
    public void setmSnakeGame(SnakeGame mSnakeGame) {
        this.mSnakeGame = mSnakeGame;
    }

    private void intializeBitmap(Context context, int s) {
        // Load the image to the bitmap
        // Create the default bitmap object
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, s, s, false);

        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
        // Resize the bitmap
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);
        // Load image to the bitmap
        mGoldenApple = BitmapFactory.decodeResource(context.getResources(),R.drawable.golden_apple);
        // Resize the bitmap
        mGoldenApple = Bitmap.createScaledBitmap(mGoldenApple, s, s, false);
    }

    // This is called every time a normal apple is eaten
    public void spawn(){
        // Choose two random values and place the apple
        Random random = new Random();
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;
        spawnTime = System.currentTimeMillis();

        if(isGoldenApple()) {
            mBitmap = mGoldenApple;
        }
        else {
            mBitmap = mBitmapApple;
        }
    }

    // This is called when a power-up apple is spawned
    public void spawn(int minX, int maxX, int minY, int maxY) {
        Random random = new Random();
        location.x = random.nextInt(maxX - minX + 1) + minX;
        location.y = random.nextInt(maxY - minY + 1) + minY;
        spawnTime = System.currentTimeMillis();

        if(isGoldenApple()) {
            mBitmap = mGoldenApple;
        }
        else {
            mBitmap = mBitmapApple;
        }
    }

    public boolean needsRespawn() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - spawnTime > MAX_TIME);
    }

    // Let SnakeGame know where the apple is
    // SnakeGame can share this with the snake
    @Override
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location){
        this.location = location;
    }

    // Draw the apple
    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(mBitmap, location.x * mSize, location.y * mSize, paint);
    }

    //check if the apple that spawned is golden
    private boolean isGoldenApple() {
        if(mSnakeGame != null) {
            boolean isGolden = SpawnUtil.shouldSpawnPowerUp();
            Log.d("Apple","isGoldenApple: " + isGolden);
            return isGolden;
        }
        else {
            Log.e("Apple", "SnakeGame object is null");
            return false;
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

    // Apple doesn't move like Snake, so an empty implementation
    @Override
    public void move() {
        // No movement for Apple
    }

}