package com.example.androidpaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;

public class DrawThread extends Thread {
    private SurfaceHolder surfaceHolder;

    private volatile boolean running = true;
    private volatile boolean gameOver = false;   //завершение игры
    private volatile boolean pause = false;      //постановка игры на паузу
    private volatile int viewWidth = 0;
    private volatile int viewHeight = 0;

    public void setViewWidth(int w){
        viewWidth = w;
    }

    public int getViewWidth(){
        return viewWidth;
    }

    public void setViewHeight(int h){
        viewHeight = h;
    }

    public int getViewHeight(){
        return viewHeight;
    }

    private Paint backgroundPaint = new Paint();
    private Bitmap bitmap;
    {
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);
    }
    private MotionEvent event = null;
    public void setMotionEvent(MotionEvent e){
        event = e;
    }

    private Sprite playerBird;
    private ArrayList<Sprite> enemyBirds;
    private Sprite apple;
    private int level = 1;
    private int countEnemyBird = 5;

    private int points = 0;
    private int speedPlayer = 100;
    private int speedMaxPlayer = 300;
    private final int timerInterval = 30;

    public static double getRandomInt(double min, double max){ //рандомное число
        double x = (int)(Math.random()*((max-min)+1))+min;
        return x;
    }

    public DrawThread(Context context, SurfaceHolder surfaceHolder){
        Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.raw.player);
        int w = b.getWidth()/5;
        int h = b.getHeight()/3;
        Rect firstFrame = new Rect(0, 0, w, h);

        playerBird = new Sprite(10, 0, 0, speedPlayer, firstFrame, b);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i ==0 && j == 0) {
                    continue;
                }

                if (i ==2 && j == 3) {
                    continue;
                }
                playerBird.addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
            }
        }


        b = BitmapFactory.decodeResource(context.getResources(), R.raw.enemy);
        w = b.getWidth()/5;
        h = b.getHeight()/3;
        firstFrame = new Rect(4*w, 0, 5*w, h);

        enemyBirds = new ArrayList<Sprite>();
        for(int i = 0; i < countEnemyBird; i++){
            enemyBirds.add(new Sprite(2000,
                    getRandomInt(100, viewHeight),
                    getRandomInt(-150, -50), 0, firstFrame, b));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i ==0 && j == 4) {
                    continue;
                }

                if (i ==2 && j == 0) {
                    continue;
                }

                for(int k = 0; k < enemyBirds.size(); k++){
                    enemyBirds.get(k).addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
                }
            }
        }

        b = BitmapFactory.decodeResource(context.getResources(), R.raw.apple);
        w = b.getWidth();
        h = b.getHeight();
        firstFrame = new Rect(0, 0, w, h);

        apple = new Sprite(1500, 400, -100, 0, firstFrame, b);

        this.surfaceHolder = surfaceHolder;
    }

    public void requestStop(){
        running = false;
    }

    protected void draw(Canvas canvas) {
        canvas.drawARGB(250, 255, 255, 255);
        playerBird.draw(canvas);
        for(int i = 0; i < enemyBirds.size(); i++){
            enemyBirds.get(i).draw(canvas);
        }
        apple.draw(canvas);

        Paint p = new Paint();

        p.setAntiAlias(true);
        p.setTextSize(55.0f);
        p.setColor(Color.BLACK);
        canvas.drawText("Level: " + level + "  Score: " + points + "", viewWidth - 700, 70, p);

        if(gameOver == true){
            p.setTextSize(80.0f);
            p.setColor(Color.RED);
            canvas.drawText("GAME OVER", viewWidth /4, viewHeight / 2, p);
        }

        if(pause == true){
            p.setTextSize(80.0f);
            p.setColor(Color.RED);
            canvas.drawText("PAUSE", viewWidth /3, viewHeight / 2, p);
        }
    }

    protected void update () {
        playerBird.update(timerInterval);
        for(int i = 0; i < enemyBirds.size(); i++){
            enemyBirds.get(i).update(timerInterval);
        }
        apple.update(timerInterval);

        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVy(-playerBird.getVy());
            points--;
        }

        else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVy(-playerBird.getVy());
            points--;
        }

        boolean flag = false;
        if(apple.getX() < - apple.getFrameWidth()){
            teleportObject(apple);
        }

        if(apple.intersect(playerBird)){
            teleportObject(apple);
            points += 100;
            if(speedPlayer <= speedMaxPlayer) {
                speedPlayer += 20;
                flag = true;
            }
        }

        for(int i = 0; i < enemyBirds.size(); i++){
            if (enemyBirds.get(i).getX() < - enemyBirds.get(i).getFrameWidth()) {
                teleportObject(enemyBirds.get(i));
                points +=10;
            }

            if (enemyBirds.get(i).intersect(playerBird)) {
                teleportObject(enemyBirds.get(i));
                points -= 40;
                if(speedPlayer > 100) {
                    speedPlayer -= 20;
                    flag = true;
                }
            }
        }

        if((level == 1) && (points >= 500)){
            points = 0;
            level++;
            for(int i = 0; i < enemyBirds.size(); i++){
                enemyBirds.get(i).setVx(enemyBirds.get(i).getVx() - getRandomInt(100, 300));
            }
        }else if((level == 2) && (points >= 700)){
            points = 0;
            level++;
            for(int i = 0; i < enemyBirds.size(); i++){
                enemyBirds.get(i).setVx(enemyBirds.get(i).getVx() - getRandomInt(100, 300));
            }
        }else if((level == 3) && (points >= 1200)){
            points = 0;
            level++;
            for(int i = 0; i < enemyBirds.size(); i++){
                enemyBirds.get(i).setVx(enemyBirds.get(i).getVx() - getRandomInt(100, 300));
            }
        }else if((level == 4) && (points >= 1500)){
            points = 0;
            level++;
            for(int i = 0; i < enemyBirds.size(); i++){
                enemyBirds.get(i).setVx(enemyBirds.get(i).getVx() - getRandomInt(100, 300));
            }
        }else if((level == 5) && (points >= 2000)){
            points = 0;
            level++;
            for(int i = 0; i < enemyBirds.size(); i++){
                enemyBirds.get(i).setVx(enemyBirds.get(i).getVx() - getRandomInt(100, 300));
            }
        }

        if((level != 1) && (points <= -100)){
            points = 0;
            level--;
            if(speedPlayer > 150) {
                speedPlayer -= 100;
                flag = true;
            }
        }else if((level == 1) && (points <= -100)){
            points = 0;
            gameOver = true;
        }

        if((flag == true) && (playerBird.getVy() < 0) && (speedPlayer > -speedMaxPlayer))
            playerBird.setVy(-speedPlayer);
        else if((flag == true) && (playerBird.getVy() >= 0) && (speedPlayer < speedMaxPlayer))
            playerBird.setVy(speedPlayer);
    }

    private void teleportObject (Sprite sprite) {
        sprite.setX(viewWidth + Math.random() * 500);
        sprite.setY(Math.random() * (viewHeight - sprite.getFrameHeight()));
    }

    public boolean onTouchEventAction() {
        if(event == null)
            return false;
        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN)  {
            if (event.getY() < playerBird.getBoundingBoxRect().top) {
                if(playerBird.getVy() < 0)
                    playerBird.setVy(playerBird.getVy());
                else
                    playerBird.setVy(playerBird.getVy() * (-1));
                points--;
            }
            else if (event.getY() > (playerBird.getBoundingBoxRect().bottom)) {
                if(playerBird.getVy() < 0)
                    playerBird.setVy(playerBird.getVy() * (-1));
                else
                    playerBird.setVy(playerBird.getVy());
                points--;
            }

            for(int i = 0; i < enemyBirds.size(); i++){
                if(enemyBirds.get(i).clicked(event.getX(), event.getY(), 50)){
                    teleportObject(enemyBirds.get(i));
                    points += 50;
                }
            }
        }

        return true;
    }

    public void pause(){
        pause = !pause;
    }

    @Override
    public void run(){
        while(running){
            Canvas canvas = surfaceHolder.lockCanvas();
            if(canvas != null){
                try{
                    draw(canvas);
                    if((pause != true) && (gameOver != true)){
                        update();
                        onTouchEventAction();
                    }
                    if(gameOver == true){
                        draw(canvas);
                        running = false;
                    }
                }finally {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

}
