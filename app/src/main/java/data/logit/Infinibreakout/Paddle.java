package data.logit.Infinibreakout;

import android.graphics.RectF;
/**
 * Created by AdminProgram on 8/10/2015.
 */
public class Paddle {
    private RectF rect;

    private float length;
    private float height;

    //x = far left, y = top
    private float x;
    private float y;

    // speed in pixels/second paddle
    private float paddleSpeed = 350;

    // ways the paddle can move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    public int paddleMoving = STOPPED;

    public Paddle(int screenX, int screenY) {

        height = 20;

        x = screenX / 2 - length / 2;
        y = screenY - 30;

        rect = new RectF(x, y, x + length / 2, y + height);

        paddleSpeed = 350;
    }

    public void reset(int screenX, int screenY) {
            rect.left = screenX / 2;
            rect.top = screenY - 30;
            rect.right = screenX / 2 + length;
            rect.bottom = screenY - 10;

    }

    public void setLength(){ length = 130; }
    public void setSpeed() {
        paddleSpeed = 350;
    }
    public RectF getRect() {
        return rect;
    }
    public void setMovementState(int state) {
        paddleMoving = state;
    }

    public void update(long fps) {
        if (paddleMoving == LEFT) {
            x = x - paddleSpeed / fps;
        }
        if(paddleMoving == RIGHT) {
            x = x + paddleSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }
    public void setBigger() {
        length *= 2;
    }
    public void setFaster() {
        paddleSpeed *= 1.5;

    }


    public void setSmaller() {
        length /= 2;
    }



}
