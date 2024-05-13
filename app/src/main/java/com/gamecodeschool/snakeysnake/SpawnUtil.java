package com.gamecodeschool.snakeysnake;
import android.util.Log;

class SpawnUtil {
    private Apple mApple;
    private static int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    private static final int NUM_POWER_UPS = 2;
    private static final double powerAppleProbability = 0.3;
    private static double individualProbability = powerAppleProbability / NUM_POWER_UPS;

    //constructor
    public SpawnUtil(Apple apple, int mNumBlocksHigh) {
        mApple = apple;
        this.mNumBlocksHigh = mNumBlocksHigh;
    }

    //determines whether a regular apple spawns
    public static boolean shouldSpawnApple() {
        double spawnProbability = 0.7; //70% chance everytime an apple is eaten to spawn a regular apple
        return Math.random() < spawnProbability; //if random < 0, spawns an apple
    }
    //determines if a power-up apples spawns
    public static boolean shouldSpawnPowerUp() {
        double spawnProbability = individualProbability;
        return Math.random() < spawnProbability;
    }

    public static boolean shouldSpawnPowerDown() {
        double spawnProbability = individualProbability;
        return Math.random() < spawnProbability;
    }
    //spawns a regular apple
    public void spawnApple() {
        if(mApple != null) {
            mApple.spawn();
        }
        else {
            Log.e("SnakeGame", "mApple is null");
        }
    }

    //spawn a power-up apple
    public void spawnPowerUp() {
        int minX = 0;
        int maxX = NUM_BLOCKS_WIDE;
        int minY = 0;
        int maxY = mNumBlocksHigh;

        mApple.spawn(minX, maxX, minY, maxY);
    }

    public void spawnPowerDown() {
        spawnPowerUp();
    }
}
