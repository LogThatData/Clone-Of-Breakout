package data.logit.breakout;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

public class BreakoutGame extends Activity {

    BreakoutView breakoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        breakoutView = new BreakoutView(BreakoutGame.this);
        setContentView(breakoutView);
    }

    class BreakoutView extends SurfaceView implements Runnable {

        Thread gameThread = null;

        SurfaceHolder ourHolder;

        volatile boolean playing;
        boolean paused = true;

        Canvas canvas;
        Paint paint;

        long fps;

        private long timeThisFrame;

        int screenX;
        int screenY;

        Paddle paddle;
        Ball ball;
        Brick[] bricks = new Brick[200];
        int numBricks = 0;


        Random generator = new Random();
        int brickColorR = generator.nextInt(255);
        int brickColorG = generator.nextInt(255);
        int brickColorB = generator.nextInt(255);

        SoundPool soundPool;
        int beep1ID = -1;
        int beep2ID = -1;
        int beep3ID = -1;
        int loseLifeID = 1;
        int explodeID = -1;

        int score = 0;

        int lives = 3;

        Random bigPaddle = new Random();
        int biggerPaddle1 = bigPaddle.nextInt(8);
        int biggerPaddle2 = bigPaddle.nextInt(3);



        public BreakoutView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            Display display = getWindowManager().getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            paddle = new Paddle(screenX, screenY);
            ball = new Ball(screenX, screenY);
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

            try{
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                descriptor = assetManager.openFd("beep1.ogg");
                beep1ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep2.ogg");
                beep2ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep3.ogg");
                beep3ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("loseLife.ogg");
                loseLifeID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("explode.ogg");
                explodeID = soundPool.load(descriptor, 0);
            } catch (IOException e){
                Log.e("error", "failed to load sound files");
            }



            createBricksAndRestart();
        }

        public void createBricksAndRestart() {

            score = 0;
            lives = 3;

            ball.reset(screenX, screenY);
            ball.setSpeed();
            paddle.setSpeed();
            paddle.setLength();
            int brickWidth = screenX / 8;
            int brickHeight = screenY / 10;
            numBricks = 0;
            for(int column = 0; column < 8; column++){
                for(int row = 0; row < 3; row++){
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks++;
                }
            }
        }

        public void createBricksAndContinue() {

            lives = 3;
            ball.reset(screenX, screenY);
            ball.setFaster();
            paddle.setFasterAndBigger();
            paddle = new Paddle(screenX, screenY);
            int brickWidth = screenX / 8;
            int brickHeight = screenY / 10;
            numBricks = 0;

            for(int column = 0; column < 8; column++){
                for(int row = 0; row < 3; row++){
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks++;
                }
            }
        }


        @Override
        public void run() {
            while(playing) {
                long startFrameTime = System.currentTimeMillis();

                if(!paused) {
                    update();
                }

                drawFrame();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update() {
            //movement, collsion detection, etc.
            paddle.update(fps);

            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    if(RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();
                        score += 10;
                        soundPool.play(explodeID, 1, 1, 0, 0, 1);
                    }
                }
            }

            if(RectF.intersects(paddle.getRect(), ball.getRect())) {
                if (paddle.paddleMoving == paddle.LEFT) {
                    ball.xVelocity = -(Math.abs(ball.xVelocity));
                } else if (paddle.paddleMoving == paddle.RIGHT) {
                    ball.xVelocity = Math.abs(ball.xVelocity);
                }
                ball.reverseYVelocity();
                ball.clearObstacleY(paddle.getRect().top - 2);
                soundPool.play(beep1ID, 1, 1, 0, 0, 1);
            }

            if(ball.getRect().bottom > screenY) {
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);

                lives--;
                soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

            }
            if(ball.getRect().top < 0) {
                ball.reverseYVelocity();
                ball.clearObstacleY(12);
                soundPool.play(beep2ID, 1, 1, 0, 0, 1);
            }

            if(ball.getRect().left < 0) {
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            if(ball.getRect().right > screenX - 10) {
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 22);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            if(score == numBricks * 10){
                paused = true;
                createBricksAndRestart();
            }


            ball.update(fps);
        }

        public void drawFrame() {
            if(ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 26, 128, 182));

                paint.setColor(Color.argb(255, 255, 255, 255));

                //draw the paddle
                canvas.drawRect(paddle.getRect(), paint);
                //the ball
                canvas.drawRect(ball.getRect(), paint);
                //bricks
                for(int i = 0; i < numBricks; i++){
                    if(bricks[i].getVisibility()){
                        paint.setColor(Color.argb(255, brickColorR * i, brickColorG * i, brickColorB * i));
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }





                //HUD

                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(40);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);

                if(score == numBricks * 10) {
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE WON!", screenX / 4, screenY / 2, paint);
                    paused = true;
                }

                if(lives == 0) {
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE LOST!", screenX / 4, screenY / 2, paint);
                    paused = true;
                }
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }


        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
        }


        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    paused = false;

                    if(motionEvent.getX() > screenX / 2) {
                        paddle.setMovementState(paddle.RIGHT);
                    } else {
                        paddle.setMovementState(paddle.LEFT);
                    }

                    if(lives == 0) {
                        createBricksAndContinue();
                    }

                    break;
                case MotionEvent.ACTION_UP:

                    paddle.setMovementState(paddle.STOPPED);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(motionEvent.getX() < screenX / 2) {
                        paddle.setMovementState(paddle.LEFT);
                    } else {
                        paddle.setMovementState(paddle.RIGHT);
                    }
                    break;
            }
            return true;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        breakoutView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        breakoutView.pause();
    }

}
