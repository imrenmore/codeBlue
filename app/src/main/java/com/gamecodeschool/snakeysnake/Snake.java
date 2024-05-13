package com.gamecodeschool.snakeysnake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import java.util.ArrayList;

abstract class MainObject implements GameObject, Movable, Drawable {
    protected Point location;
    protected Bitmap bitmap;

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (bitmap != null && location != null) {
            canvas.drawBitmap(bitmap, location.x, location.y, paint);
        }
    }

    @Override
    public abstract void move();

    @Override
    public Point getLocation() {
        return location;
    }
}

class Snake extends MainObject {

    // The location in the grid of all the segments
    private final ArrayList<Point> segmentLocations;

    // How big is each segment of the snake?
    private final int mSegmentSize;

    // How big is the entire grid
    private final Point mMoveRange;

    // Where is the centre of the screen
    // horizontally in pixels?
    private final int halfWayPoint;
    private int w;
    private int h;

    // For tracking movement Heading
    private enum Heading {
        UP, RIGHT, DOWN, LEFT
    }

    // Start by heading to the right
    private Heading heading = Heading.RIGHT;

    // A bitmap for each direction the head can face
    private Bitmap mBitmapHeadRight;
    private Bitmap mBitmapHeadLeft;
    private Bitmap mBitmapHeadUp;
    private Bitmap mBitmapHeadDown;

    // A bitmap for the body
    private Bitmap mBitmapBody;

    private boolean isBoosted = false; //is the snake currently sped up?
    private boolean isSlowed = false; //is the snake currently slowed
    private long speedBoostLength = 0; //how long the speed boost lasts
    private boolean gameOver = false;
    private SnakeGame mSnakeGame;

     Snake(Context context, Point mr, int ss) {

        // Initialize our ArrayList
        this.segmentLocations = new ArrayList<>();

        // Initialize the segment size and movement
        // range from the passed in parameters
        this.mSegmentSize = ss;
        this.mMoveRange = mr;

        // Create and scale the bitmaps
        mBitmapHeadRight = BitmapFactory
                .decodeResource(context.getResources(),
                        R.drawable.alihead);

        // Create 3 more versions of the head for different headings
        mBitmapHeadLeft = BitmapFactory
                .decodeResource(context.getResources(),
                        R.drawable.alihead);

        mBitmapHeadUp = BitmapFactory
                .decodeResource(context.getResources(),
                        R.drawable.alihead);

        mBitmapHeadDown = BitmapFactory
                .decodeResource(context.getResources(),
                        R.drawable.alihead);

        // Modify the bitmaps to face the snake head
        // in the correct direction
        mBitmapHeadRight = Bitmap
                .createScaledBitmap(mBitmapHeadRight,
                        ss, ss, false);

        // A matrix for scaling
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);

        mBitmapHeadLeft = Bitmap
                .createBitmap(mBitmapHeadRight,
                        0, 0, ss, ss, matrix, true);

        // A matrix for rotating
        matrix.preRotate(-90);
        mBitmapHeadUp = Bitmap
                .createBitmap(mBitmapHeadRight,
                        0, 0, ss, ss, matrix, true);

        // Matrix operations are cumulative
        // so rotate by 180 to face down
        matrix.preRotate(180);
        mBitmapHeadDown = Bitmap
                .createBitmap(mBitmapHeadRight,
                        0, 0, ss, ss, matrix, true);

        // Create and scale the body
        mBitmapBody = BitmapFactory
                .decodeResource(context.getResources(),
                        R.drawable.body);

        mBitmapBody = Bitmap
                .createScaledBitmap(mBitmapBody,
                        ss, ss, false);

        // The halfway point across the screen in pixels
        // Used to detect which side of screen was pressed
        halfWayPoint = mr.x * ss / 2;
    }

    void setGame(SnakeGame mSnakeGame) {
         this.mSnakeGame = mSnakeGame;
    }

    // Overloaded reset method to reset with a custom length
    void reset(int w, int h, int length) {
        heading = Heading.RIGHT;
        segmentLocations.clear();

        // Start with a snake of the specified length
        for (int i = 0; i < length; i++) {
            segmentLocations.add(new Point(w / 2 - i, h / 2));
        }
    }

    // Get the snake ready for a new game
    public void setW(int w) {
        this.w = w;
    }
    public int getW(){
        return w;
    }
    public void setH(int h){
        this.h = h;
    }
    public int getH(){
        return h;
    }
    void reset(int w, int h) {
        // Reset the heading
        heading = Heading.RIGHT;

        // Delete the old contents of the ArrayList
        segmentLocations.clear();

        // Start with a single snake segment
        segmentLocations.add(new Point(w / 2, h / 2));

        //Game is Over, reset so the Game Over message doesnt show
        gameOver=false;
    }

    //method to activate the speed boost
    void activateSpeedBoost(long duration) {
         isBoosted = true;
         mSnakeGame.update(2);
         speedBoostLength = System.currentTimeMillis() + duration;
    }

    //method to activate speed decrease
    void activateSpeedDecrease(long duration) {
         isSlowed = true;
         mSnakeGame.update(0);
         speedBoostLength = System.currentTimeMillis() + duration;
    }

    public void applySpeedBoost(int steps, int boostDuration) {
        isBoosted = true;
        speedBoostLength = System.currentTimeMillis() + boostDuration;
        move(steps);
    }

    public void applySpeedDecrease(int steps, int boostDuration) {
        isBoosted = false;
        speedBoostLength = System.currentTimeMillis() + boostDuration;
        move(steps);
    }

    @Override
    public void move() {
        move(1); // Call move(double steps) with a default step of 1
    }

    // Overloaded move method to move multiple steps
    void move(int steps) {
        for (int i = 0; i < steps; i++) {
            moveSteps(steps);
        }
    }

    //Helper method to perform movement
    private void moveSteps(double steps) {
         // Move the body segments, from the back to the position of the segment in front
        for (int i = segmentLocations.size() - 1; i > 0; i--) {
            // Make it the same value as the next segment going forwards towards the head
            segmentLocations.get(i).x = segmentLocations.get(i - 1).x;
            segmentLocations.get(i).y = segmentLocations.get(i - 1).y;
        }

        // Move the head in the appropriate heading
        // Get the existing head position
        Point p = segmentLocations.get(0);
        // Move it appropriately
        switch (heading) {
            case UP:
                p.y--;
                break;
            case RIGHT:
                p.x++;
                break;
            case DOWN:
                p.y++;
                break;
            case LEFT:
                p.x--;
                break;
        }
    }

    boolean detectDeath(Wall mWall) {
         Point head = segmentLocations.get(0);
        // Check boundary collision
        boolean dead = head.x == -1 || head.x > mMoveRange.x || head.y == -1 || head.y > mMoveRange.y;
        // Check self-collision
        for (int i = segmentLocations.size() - 1; i > 0; i--) {
            if (head.equals(segmentLocations.get(i))) {
                dead = true;
                break;
            }
        }

        // Check wall collision
        if (mWall != null && mWall.checkCollision(head)) {
            dead = true;
            gameOver = true;
        }

        return dead;
    }

    boolean checkDinner(Point l) {
         if(!segmentLocations.isEmpty() && segmentLocations.get(0).x == l.x &&
                 segmentLocations.get(0).y == l.y) {
            segmentLocations.add(new Point(-10, -10));
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        // Don't run this code if ArrayList has nothing in it
        if (!segmentLocations.isEmpty()) {
            // All the code from this method goes here
            // Draw the head
            switch (heading) {
                case RIGHT:
                    canvas.drawBitmap(mBitmapHeadRight,
                            segmentLocations.get(0).x
                                    * mSegmentSize,
                            segmentLocations.get(0).y
                                    * mSegmentSize, paint);
                    break;

                case LEFT:
                    canvas.drawBitmap(mBitmapHeadLeft,
                            segmentLocations.get(0).x
                                    * mSegmentSize,
                            segmentLocations.get(0).y
                                    * mSegmentSize, paint);
                    break;

                case UP:
                    canvas.drawBitmap(mBitmapHeadUp,
                            segmentLocations.get(0).x
                                    * mSegmentSize,
                            segmentLocations.get(0).y
                                    * mSegmentSize, paint);
                    break;

                case DOWN:
                    canvas.drawBitmap(mBitmapHeadDown,
                            segmentLocations.get(0).x
                                    * mSegmentSize,
                            segmentLocations.get(0).y
                                    * mSegmentSize, paint);
                    break;
            }

            // Draw the snake body one block at a time
            for (int i = 1; i < segmentLocations.size(); i++) {
                canvas.drawBitmap(mBitmapBody,
                        segmentLocations.get(i).x
                                * mSegmentSize,
                        segmentLocations.get(i).y
                                * mSegmentSize, paint);
            }
            if (gameOver){
                drawGameOver(canvas, paint);
            }

        }
    }

    // Here prints the game over screen after the snake has died
    private void drawGameOver(Canvas canvas, Paint paint) {
            Paint gameOver = new Paint(paint);
            gameOver.setColor(Color.RED);
            gameOver.setTextSize(100);
            //adjusting the title of Game Over to be positioned above tap to play
            gameOver.setTextAlign(Paint.Align.CENTER);
            float x = (float) canvas.getWidth() / 2;
            float y = (float) canvas.getHeight() / 2 - gameOver.descent() - 50;
            canvas.drawText("Game Over", x , y, gameOver );
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
    public Point getLocation() {
        // Return the location of the snake head
        return segmentLocations.get(0);
    }

    //getter for isBoosted
    public boolean isBoosted() {
         return isBoosted;
    }
    //getter for isSlowed
    public boolean isSlowed() {
         return isSlowed;
    }

    // Handle changing direction
    void switchHeading(MotionEvent motionEvent) {
         if(motionEvent.getX() >= halfWayPoint) {
             heading = rotateClockwise(heading);
         }
         else {
             heading = rotateCounterClockwise(heading);
        }
    }

    /*
    Ordinal values:
    UP - 0
    DOWN - 1
    RIGHT - 2
    LEFT - 3
     */

    private Heading rotateClockwise(Heading currDirection) {
         int index = currDirection.ordinal();
         index = (index + 1) % 4;
         return Heading.values()[index];
    }

    private Heading rotateCounterClockwise(Heading currDirection) {
         int index = currDirection.ordinal();
         index = (index - 1 + 4) % 4;
         return Heading.values()[index];
    }
}