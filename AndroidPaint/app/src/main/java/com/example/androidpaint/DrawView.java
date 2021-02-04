package com.example.androidpaint;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread = new DrawThread(getContext(), getHolder());

    public DrawView(Context context){
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        //create SurfaceView
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        super.onSizeChanged(width, height, drawThread.getViewWidth(), drawThread.getViewHeight());
        drawThread.setViewWidth(width);
        drawThread.setViewHeight(height);
    }

    public void pause(){
        drawThread.pause();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        //destroy SurfaceView
        drawThread.requestStop();
        boolean retry = true;
        while(retry){
            try{
                drawThread.join();
                retry = false;
            }catch(InterruptedException e){
                //...
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        drawThread.setMotionEvent(event);
        return false;
    }
}