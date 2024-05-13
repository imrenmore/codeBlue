package com.gamecodeschool.snakeysnake;

class PowerUp {
    private int x, y; //position of power-up
    private long duration; //duration of the power-up in milliseconds
    private PowerUpType type; //the type of power-up
    PowerUp(int x, int y, long duration, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.duration = duration;
        this.type = type;
    }

    //Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public long getDuration() {
        return duration;
    }

    public PowerUpType getType() {
        return type;
    }

    public void applyEffect(Snake snake) {
        if(type == PowerUpType.SPEED_UP) {
            snake.activateSpeedBoost(duration);
        }
        if(type == PowerUpType.SPEED_DOWN) {
            snake.activateSpeedDecrease(duration);
        }
    }
}

enum PowerUpType {
    SPEED_UP, SPEED_DOWN
}
