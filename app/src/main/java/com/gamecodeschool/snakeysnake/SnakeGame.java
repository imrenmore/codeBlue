package com.gamecodeschool.snakeysnake;
import android.content.Context;
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
import java.io.IOException;
import java.util.ArrayList;

//Interfaces
interface GameObject extends Drawable, Movable {
    Point getLocation();
}

interface Movable {
    void move();
}

interface Drawable{
    void draw(Canvas canvas, Paint paint);
    int getWidth();
    int getHeight();
    boolean containsPoint(Point point);
}

class SnakeGame extends SurfaceView implements Runnable {

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
    private static int NUM_BLOCKS_WIDE = 40;
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
    private Apple mApple;
    private Apple mGoldenApple;
    private Bitmap pauseButtonBitmap;

    // Constants for the pause button
    private final int pauseButtonWidth = 100;
    private final int pauseButtonHeight = 100;
    private final int pauseButtonMargin = 30;

//    //An image to represent background
//    private Bitmap mBitmapBackground;

    // Run at 10 frames per second
    private final long TARGET_FPS = 10;

    // There are 1000 milliseconds in a second
    private final long MILLIS_PER_SECOND = 1000;
    private Background background;
    private ArrayList<PowerUp> mPowerUps;
    private static final int BOOST_DURATION = 10000; //10 seconds

    public class Background {
        private Bitmap mBitmapBackground;
        int width = 2500;
        int height = 1200;
        public Background(Context context) {
            mBitmapBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            mBitmapBackground = Bitmap.createScaledBitmap(mBitmapBackground, width, height ,false);
        }

        public void draw(Canvas canvas){
            canvas.drawBitmap(mBitmapBackground, 0,0, null);
        }
    }

    // Overloaded constructor
    public SnakeGame(Context context, Point size, int initialScore) {
        this(context, size);  // Calls the existing constructor
        mScore = initialScore;  // Sets the initial score
    }

    // This is the constructor method that gets called
    // from com.gamecodeschool.snakeysnake.SnakeActivity
    public SnakeGame(Context context, Point size)  {
        super(context);

        // Initializes the drawing objects
        initializeDrawingTools();
        background = new Background(context);
        // Loads and sets the custom font
        setFont("Catfiles.otf");

        // Calculates the size of each block based on the screen size
        int blockSize = calculateBlockSize(size);
        // Initializes game entities - Snake and Apple
        initializeGameObjects(context, blockSize);
        // Sets up the sound engine for the game
        initializeSoundPool(context);
    }

    private void initializeDrawingTools() {
        // Gets the holder for the canvas and initializes the Paint object for drawing
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
    }

    private int calculateBlockSize(Point size) {
        // Work out how many pixels each block is and how many blocks fit into the height
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mNumBlocksHigh = size.y / blockSize;
        return blockSize;
    }

    private void initializeGameObjects(Context context, int blockSize) {
        // Loads the pause button graphic and initializes the Apple and Snake objects
        pauseButtonBitmap = loadScaledBitmap(context, R.drawable.pause_button, pauseButtonWidth, pauseButtonHeight);

        // Initialize game objects
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mGoldenApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mPowerUps = new ArrayList<>();
    }

    private Bitmap loadScaledBitmap(Context context, int resId, int width, int height) {
        // Loads a bitmap from resources and scales it to the specified width and height
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private void initializeSoundPool(Context context) {
        // Sets up the SoundPool for playing sound effects with its audio attributes
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mSP = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Loads sound files into the SoundPool
        loadSounds(context);
    }

    private void loadSounds(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            mEat_ID = mSP.load(assetManager.openFd("get_apple.ogg"), 0);
            mCrashID = mSP.load(assetManager.openFd("snake_death.ogg"), 0);
        } catch (IOException e) {
            Log.e("SnakeGame", "Error loading sound files", e);
        }
    }

    // Overloaded newGame method with custom initial score
    public void newGame(int initialScore) {
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        mApple.spawn();
        mScore = initialScore;  // Sets the initial score
        mNextFrameTime = System.currentTimeMillis();
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
                    // can change the speed
                    update(1);
                }
            }
            draw();
        }
    }

    // Check to see if it is time for an update
    public boolean updateRequired() {
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

    // Overloaded update method with custom speed parameter
    public void update(int speed) {
        // Adjust the update logic based on the speed parameter
        mSnake.move(speed);  // Assuming move can take speed as a parameter

        //Update power-ups
        updatePowerUps();

        //Check if the snake ate the apple
        checkAppleCollision();

        //Check if the snake died
        checkSnakeDeath();
    }

    //Update power-ups
    private void updatePowerUps() {
        if(mPowerUps != null) {
            for(PowerUp powerUp : mPowerUps) {
                powerUp.applyEffect(mSnake);
            }
        }
        else {
            Log.e("SnakeGame", "mPowerUps is null");
        }
    }

    //Did the head of the snake eat the apple?
    private void checkAppleCollision() {
        // Check if the head consumed a regular apple
        if(mGoldenApple != null && mSnake.checkDinner(mApple.getLocation())){
            // Determine whether to spawn a power-up or apple
            if(shouldSpawnApple()) {
                Log.e("SnakeGame", "spawning a regular apple");
                spawnApple();
            }
            else {
                Log.e("SnakeGame", "spawning a golden apple");
                spawnPowerUp();
            }
            mScore = mScore + 1; // Increment the score
            mSP.play(mEat_ID, 1, 1, 0, 0, 1); // Play a sound
        }

        // Check if the head consumed a golden apple
        if(mSnake != null && mGoldenApple != null) {
            if(mSnake.checkDinner(mGoldenApple.getLocation())) {
                mSnake.applySpeedBoost(2, BOOST_DURATION);
                // Determine whether to spawn a power-up or apple
                if(mPowerUps != null) {
                    if(shouldSpawnPowerUp()) {
                        Log.e("SnakeGame", "spawning a golden apple");
                        spawnPowerUp();
                    }
                    else {
                        Log.e("SnakeGame", "spawning a regular apple");
                        spawnApple();
                    }
                }
                else {
                    Log.e("SnakeGame", "mPowerUps is null");
                }
            }
        }
    }

    //Did the snake die?
    private void checkSnakeDeath() {
        if (mSnake.detectDeath()) {
            // Pause the game ready to start again
            mSP.play(mCrashID, 1, 1, 0, 0, 1);
            mPaused = true;
        }
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
            mPaused = true;
        }
    }

    // Do all the drawing
    public void draw() {
        // Check if the surface is valid before drawing
        if (mSurfaceHolder.getSurface().isValid()) {
            // Lock the canvas for drawing
            mCanvas = mSurfaceHolder.lockCanvas();

            // Draw the game's background
            drawBackground();
            // Draw the current score
            drawScore();
            // Draw the apple and snake
            mApple.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);

            // If the game is paused, draw the paused text overlay
            if (mPaused) {
                drawPausedText();
            }

            // Draw the pause button and the name text on the screen
            drawPauseButton();
            drawNames("Kiranjot, Imren, Marilyn, & Savannah");

            // Unlock the canvas and post the drawing to the screen
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);

            //check if an apple was eaten
            if (mSnake.checkDinner(mApple.getLocation())) {
                //Spawn another apple
                mApple.spawn();

                //increase the score
                mScore++;

                //play a sound
                mSP.play(mEat_ID, 1, 1, 0, 0, 1);

                //Randomly spawn a power-up
                if(shouldSpawnPowerUp()) {
                    spawnPowerUp();
                }
            }
        }
    }

    //determines whether a regular apple spawns
    boolean shouldSpawnApple() {
        double spawnProbability = 0.7; //70% chance everytime an apple is eaten to spawn a regular apple
        double random = Math.random();
        return random < spawnProbability; //if random == 0, spawns an apple
    }
    //determines if a power-up apples spawns
    boolean shouldSpawnPowerUp() {
        double spawnProbability = 0.3; //60% chance everytime an apple is eaten to spawn a power-up
        double random = Math.random();
        return random < spawnProbability; //if random == 0, spawns a power-up
    }
    //spawns a regular apple
    private void spawnApple() {
        if(mApple != null) {
            mApple.spawn();
        }
        else {
            Log.e("SnakeGame", "mApple is null");
        }
    }

    //spawn a power-up apple
    private void spawnPowerUp() {
        int minX = 0;
        int maxX = NUM_BLOCKS_WIDE;
        int minY = 0;
        int maxY = mNumBlocksHigh;

        mApple.spawn(minX, maxX, minY, maxY);
    }

    // Draws the background image
    private void drawBackground() {
        background.draw(mCanvas);
    }

    // Renders the current score on the screen
    private void drawScore() {
        mPaint.setColor(Color.argb(255, 255, 255, 255));
        mPaint.setTextSize(120);
        mCanvas.drawText("" + mScore, 20, 120, mPaint);
    }

    // Displays a "Paused" message overlay when the game is paused
    private void drawPausedText() {
        Paint outlinePaint = new Paint(mPaint);
        outlinePaint.setColor(Color.BLACK); // Outline color
        outlinePaint.setStyle(Paint.Style.STROKE); // Outline style
        outlinePaint.setStrokeWidth(8); // Outline width

        String tapToPlayText = getResources().getString(R.string.tap_to_play);

        mPaint.setTextSize(250);
        outlinePaint.setTextSize(250);

        // Draws the outline first
        mCanvas.drawText(tapToPlayText, 200, 700, outlinePaint);

        // Draws the main text, exactly over the outline
        mCanvas.drawText(tapToPlayText, 200, 700, mPaint);
    }


    // Draws the name text on the screen
    private void drawNames(String name) {
        mPaint.setTextSize(50);
        float nameWidth = mPaint.measureText(name);
        int xStart = mCanvas.getWidth() - pauseButtonWidth - pauseButtonMargin - (int) nameWidth - 20;
        int yStart = pauseButtonMargin + 50;
        mCanvas.drawText(name, xStart, yStart, mPaint);
    }

    // Renders the pause button in the top-right corner
    private void drawPauseButton() {
        int xStart = mCanvas.getWidth() - pauseButtonWidth - pauseButtonMargin;
        int yStart = pauseButtonMargin;
        mCanvas.drawBitmap(pauseButtonBitmap, xStart, yStart, null);
    }

    // Overloaded setFont method with font size
    public void setFont(String fontFileName, int fontSize) {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontFileName);
        mPaint.setTypeface(typeface);
        mPaint.setTextSize(fontSize);  // Set the text size
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

    // Overloaded onTouchEvent to include different types of input handling
    public boolean onTouchEvent(MotionEvent motionEvent, boolean specialCondition) {
        if (specialCondition) {
            // Handle touch event differently based on the special condition
            return true;  // Return early if condition is met
        }
        // Continue with existing touch event handling
        return onTouchEvent(motionEvent);
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