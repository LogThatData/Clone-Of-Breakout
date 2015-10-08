package data.logit.breakout;

import android.graphics.RectF;
import java.util.Random;
/**
 * Created by AdminProgram on 8/10/2015.
 */
public class Ball {

    RectF rect;
    float xVelocity;
    float yVelocity;
    float ballWidth = 10;
    float ballHeight = 10;

    public Ball(int screenX, int screenY) {
        setSpeed();


        rect = new RectF();
    }
    public void setSpeed(){
        xVelocity = 200;
        yVelocity = -400;
    }

    public RectF getRect() {
        return rect;
    }

    public void update(long fps){
        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top + (yVelocity / fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top + ballHeight;
    }

    public void reverseYVelocity(){
        yVelocity = -yVelocity;
    }

    public void reverseXVelocity() {
        xVelocity = -xVelocity;
    }

    public void setRandomXVelocity(){
        Random generator = new Random();
        int answer = generator.nextInt(2);

        if(answer == 0) {
            reverseXVelocity();
        }
    }

    public void clearObstacleY(float y) {
        rect.bottom = y;
        rect.top = y - ballHeight;
    }

    public void clearObstacleX(float x){
        rect.left = x;
        rect.right = x + ballWidth;
    }

    public void reset(int x, int y) {
        rect.left = x / 2;
        rect.top = y - 20;
        rect.right = x / 2 + ballWidth;
        rect.bottom = y - 2 - ballHeight;
    }

    public void setFaster() {
        xVelocity *= Math.sqrt(1.5);
        yVelocity *= Math.sqrt(1.5);
    }
}
