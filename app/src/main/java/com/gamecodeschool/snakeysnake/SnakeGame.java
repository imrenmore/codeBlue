package com.gamecodeschool.snakeysnake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// added these for pause button
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

// added this for font
import android.graphics.Typeface;

// added this for debug and errors
import android.util.Log;

import com.gamecodeschool.snakeysnake.R;

import java.io.IOException;

class SnakeGame extends SurfaceView implements Runnable{

    // Objects for the game loop/thread
    private Thread mThread = null;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    // for playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    // How many points does the player have
    private int mScore;

    // Objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    // A snake ssss
    private com.gamecodeschool.snakeysnake.Snake mSnake;
    // And an apple
    private com.gamecodeschool.snakeysnake.Apple mApple;

    private Bitmap pauseButtonBitmap;

    // Constants for the pause button
    private final int pauseButtonWidth = 100;
    private final int pauseButtonHeight = 100;
    private final int pauseButtonMargin = 30;

    //An image to represent background
    private Bitmap mBitmapBackground;

    // This is the constructor method that gets called
    // from com.gamecodeschool.snakeysnake.SnakeActivity
    public SnakeGame(Context context, Point size) {
        super(context);

        // Initializes the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        // Loads and sets the custom font
        setFont("Catfiles.otf");

        // Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        mNumBlocksHigh = size.y / blockSize;

        // for pause button
        pauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause_button);
        pauseButtonBitmap = Bitmap.createScaledBitmap(pauseButtonBitmap, pauseButtonWidth, pauseButtonHeight, false);

        // Initialize the SoundPool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSP = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the sounds in memory
            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);

        } catch (IOException e) {
            // Error
            Log.e("SnakeGame", "Error loading sound files", e);
        }

        // Call the constructors of our two game objects
        mApple = new com.gamecodeschool.snakeysnake.Apple(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

        mSnake = new com.gamecodeschool.snakeysnake.Snake(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

    }


    // Called to start a new game
    public void newGame() {

        // reset the snake
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);

        // Get the apple ready for dinner
        mApple.spawn();

        // Reset the mScore
        mScore = 0;

        // Setup mNextFrameTime so an update can triggered
        mNextFrameTime = System.currentTimeMillis();
    }


    // Handles the game loop
    @Override
    public void run() {
        while (mPlaying) {
            if(!mPaused) {
                // Update 10 times a second
                if (updateRequired()) {
                    update();
                }
            }

            draw();
        }
    }


    // Check to see if it is time for an update
    public boolean updateRequired() {

        // Run at 10 frames per second
        final long TARGET_FPS = 10;
        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        // Are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            mNextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }

        return false;
    }


    // Update all the game objects
    public void update() {

        // Move the snake
        mSnake.move();

        // Did the head of the snake eat the apple?
        if(mSnake.checkDinner(mApple.getLocation())){
            // This reminds me of Edge of Tomorrow.
            // One day the apple will be ready!
            mApple.spawn();

            // Add to  mScore
            mScore = mScore + 1;

            // Play a sound
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);
        }

        // Did the snake die?
        if (mSnake.detectDeath()) {
            // Pause the game ready to start again
            mSP.play(mCrashID, 1, 1, 0, 0, 1);

            mPaused =true;
        }

    }


    // Do all the drawing
    public void draw() {
        // Get a lock on the mCanvas
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();

            // Fill the screen with a background
            mCanvas.drawColor(Color.WHITE);
            //created a bitmap for background image to be drawn
            Bitmap mBitmapBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            mBitmapBackground = Bitmap.createScaledBitmap(mBitmapBackground, 2500, 1200, false);
            mCanvas.drawBitmap(mBitmapBackground, 0,0, null);




            // Set the size and color of the mPaint for the text
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            mPaint.setTextSize(120);

            // Draw the score
            mCanvas.drawText("" + mScore, 20, 120, mPaint);

            // Draw the apple and the snake
            mApple.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);

            // Draw some text while paused
            if(mPaused){

                // Set the size and color of the mPaint for the text
                mPaint.setColor(Color.argb(255, 255, 255, 255));
                mPaint.setTextSize(250);

                // Draw the message
                // We will give this an international upgrade soon
                //mCanvas.drawText("Tap To Play!", 200, 700, mPaint);
                mCanvas.drawText(getResources().
                                getString(R.string.tap_to_play),
                        200, 700, mPaint);
            }

            // Draw the pause button
            drawPauseButton(mCanvas);

            // Unlock the mCanvas and reveal the graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    // function to draw pause button
    private void drawPauseButton(Canvas canvas) {
        // Calculate the position of the pause button
        int xStart = canvas.getWidth() - pauseButtonWidth - pauseButtonMargin;
        int yStart = pauseButtonMargin;

        // Draw the pause button bitmap
        canvas.drawBitmap(pauseButtonBitmap, xStart, yStart, null);
    }

    // function to set font
    public void setFont(String fontFileName) {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontFileName);
        mPaint.setTypeface(typeface);
    }

    private void togglePause() {
        // If the game is paused, resume it, otherwise pause it
        mPaused = !mPaused;

        // If resuming, reset the next frame time to avoid instant update
        if (!mPaused) {
            mNextFrameTime = System.currentTimeMillis();
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int pauseButtonXStart = mSurfaceHolder.getSurfaceFrame().width() - 100; // Top-right corner for pause button
        int pauseButtonYStart = 0; // Top of the screen
        int pauseButtonYEnd = 100; // Height of the pause button

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= pauseButtonXStart && motionEvent.getY() >= pauseButtonYStart && motionEvent.getY() <= pauseButtonYEnd) {
                    // Toggle pause if the pause button area is tapped
                    togglePause();
                    return true;
                } else if (mPaused) {
                    mPaused = false;
                    newGame();

                    // Don't want to process snake direction for this tap
                    return true;
                }

                // Let the com.gamecodeschool.snakeysnake.Snake class handle the input
                if (!mPaused) {
                    mSnake.switchHeading(motionEvent);
                }
                break;

            default:
                break;

        }
        return true;
    }


    // Stop the thread
    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }


    // Start the thread
    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}