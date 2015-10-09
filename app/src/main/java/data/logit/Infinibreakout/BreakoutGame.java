package data.logit.Infinibreakout;

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
        Ball[] balls = new Ball[200];
        Brick[] bricks = new Brick[200];
        int numBricks = 0;
        int numBalls = 0;

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
            balls[1] = new Ball(screenX, screenY);
            numBalls = 1;
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

            balls[1].reset(screenX, screenY);
            balls[1].setSpeed();
            numBalls = 1;
            paddle.setSpeed();
            paddle.setLength();
            paddle.reset(screenX, screenY);
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
            if(numBalls <= 4) {
                paddle.setBigger();
                paddle.setFaster();
            } else if (numBalls <= 7){
                paddle.setFaster();
            }
            balls[numBalls].reset(screenX, screenY);
            numBalls++;
            score = 0;
            lives += 4;

            paddle.reset(screenX, screenY);
            balls[numBalls] = new Ball(screenX + 2, screenY);
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
            //movement, collision detection, etc.
            paddle.update(fps);
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    for(int j = 1; j <= numBalls; j++){
                    if(RectF.intersects(bricks[i].getRect(), balls[j].getRect())) {
                        bricks[i].setInvisible();
                        balls[j].reverseYVelocity();
                        score += 10;
                        soundPool.play(explodeID, 1, 1, 0, 0, 1);
                    }

                    }
                }
            }
            for(int i = 1; i <= numBalls; i++) {
                if (RectF.intersects(paddle.getRect(), balls[i].getRect())) {
                    if (paddle.paddleMoving == paddle.LEFT) {
                        balls[i].xVelocity = -(Math.abs(balls[i].xVelocity + new Random().nextInt(30)));
                    } else if (paddle.paddleMoving == paddle.RIGHT) {
                        balls[i].xVelocity = Math.abs(balls[i].xVelocity + new Random().nextInt(30));
                    }
                    balls[i].reverseYVelocity();
                    balls[i].clearObstacleY(paddle.getRect().top - 2);
                    soundPool.play(beep1ID, 1, 1, 0, 0, 1);
                }
            }
            for(int i = 1; i <= numBalls; i++) {
                if (balls[i].getRect().bottom > screenY) {
                    balls[i].reverseYVelocity();
                    balls[i].clearObstacleY(screenY - 2);

                    lives--;
                    soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

                }
                if (balls[i].getRect().top < 0) {
                    balls[i].reverseYVelocity();
                    balls[i].clearObstacleY(12);
                    soundPool.play(beep2ID, 1, 1, 0, 0, 1);
                }

                if (balls[i].getRect().left < 0) {
                    balls[i].reverseXVelocity();
                    balls[i].clearObstacleX(2);
                    soundPool.play(beep3ID, 1, 1, 0, 0, 1);
                }

                if (balls[i].getRect().right > screenX - 10) {
                    balls[i].reverseXVelocity();
                    balls[i].clearObstacleX(screenX - 22);
                    soundPool.play(beep3ID, 1, 1, 0, 0, 1);
                }
            }


            if(score == numBricks * 10 && numBricks != 0){
                paused = true;
                createBricksAndContinue();
            }

            for(int i = 1; i <= numBalls; i++) {
                balls[i].update(fps);
            }
        }

        public void drawFrame() {
            if(ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 26, 128, 182));

                paint.setColor(Color.argb(255, 255, 255, 255));

                //draw the paddle
                canvas.drawRect(paddle.getRect(), paint);
                //the balls
                for(int i = 1; i <= numBalls; i++) {
                    canvas.drawRect(balls[i].getRect(), paint);
                }
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
                        createBricksAndRestart();
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
