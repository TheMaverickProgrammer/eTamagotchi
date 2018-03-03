package com.protocomplete.etamagotchi;

import android.content.res.AssetManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;

public class RenderView extends SurfaceView implements Runnable, OnTouchListener, Callback {
    Thread viewThread = null;
    SurfaceHolder holder = null;
    boolean isSpriteLoaded = false;
    boolean isThreadActive = false;
    Bitmap monsters;
    Sprite monsterSprite = null;

    public RenderView(Context context) {
      super(context);
      init(context);
    }
    public RenderView(Context context, AttributeSet attributes) {
      super(context, attributes);
      init(context);
    }

    public RenderView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
      Canvas canvas = surfaceHolder.lockCanvas();
      draw(canvas);
      surfaceHolder.unlockCanvasAndPost(canvas);
      isThreadActive = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    public void init(Context context) {
      monsters = getBitmapFromAsset(context.getAssets(), "monsters.png");

      holder = getHolder();
      holder.addCallback(this);

      this.setOnTouchListener(this);
    }

    public static Bitmap getBitmapFromAsset(AssetManager assetManager, String filePath) {
      InputStream istr;
      Bitmap bitmap = null;

      try {
          istr = assetManager.open(filePath);
          bitmap = BitmapFactory.decodeStream(istr);
      } catch (IOException e) {
          // TODO: handle exception
          e.printStackTrace();
      }

      return bitmap;
    }

    @Override
    public void run() {
      while(isThreadActive) {
        if(holder.getSurface().isValid()) {

          if(!isSpriteLoaded && monsters != null) {
            monsterSprite = new Sprite(this, monsters);
            monsterSprite.setVelX(2);
            monsterSprite.setVelY(2);
            isSpriteLoaded = true;
          }

          Canvas canvas = holder.lockCanvas();
          onDraw(canvas);
          holder.unlockCanvasAndPost(canvas);
        }
      }
    }

    @Override
    public void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      if(isSpriteLoaded) {
        monsterSprite.OnDraw(canvas);
      }
    }

    public void pause() {
      isThreadActive = false;

      while(true) {
        try {
          viewThread.join();
        } catch(InterruptedException e) {
          e.printStackTrace();
        }

        break;
      }

      viewThread = null;
    }

    public void resume() {
      isThreadActive = true;
      viewThread = new Thread(this);
      viewThread.start();
    }

    public boolean onTouch(View v, MotionEvent me) {
      //x=me.getX();//ontouch listener
      //y=me.getY();

      try {
        Thread.sleep(80);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      switch(me.getAction()) {
        case MotionEvent.ACTION_DOWN:
          break;

        case MotionEvent.ACTION_UP:
          break;

        case MotionEvent.ACTION_MOVE:
          break;

        default:
            break;
      }

      return true;
    }
}
