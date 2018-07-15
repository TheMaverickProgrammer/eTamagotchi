package com.protocomplete.etamagotchi;

import android.content.Context;
import android.content.res.AssetManager;
import android.annotation.TargetApi;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Build;
import android.util.Log;
import android.util.AttributeSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;

public class RenderView extends SurfaceView implements Runnable, OnTouchListener {
    // Process & Activity stuff
    Thread viewThread = null;
    SurfaceHolder holder;
    boolean isThreadActive = true;
    Context context = null;

    // Graphic loading stuff
    final double TILE_WIDTH = 480/10, TILE_HEIGHT = 384/6, NUM_COLS = 10, NUM_ROWS = 6;
    Bitmap tiles = null;
    Bitmap attackIcon;
    Bitmap monsters;
    Bitmap hostIcon;
    Bitmap bg;
    Sprite monsterSprite = null;
    Sprite bgSprite = null;
    Sprite hostSprite = null;
    Sprite attackSprite = null;
    boolean isSpriteLoaded = false;
    Paint paint;

    // P2P
    final String PORT = "9999"; // if hosting, this is the port to use
    BattleThread battleThread = null;
    Monster monster = null;

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

    private Sprite getMonsterSpriteFromID(Bitmap source, int id) {
      // tiles are 16 x 16 pixels long
      int row = (int) Math.floor((double)id / (double)NUM_COLS);
      int col = id % (int) NUM_COLS;

      return new Sprite(this, source, col*(int)TILE_WIDTH, row*(int)TILE_HEIGHT, (int)TILE_WIDTH, (int)TILE_HEIGHT);
    }

    public void feedMonster() {
      if(monster == null) {
        return;
      }

      monster.eat();
    }

    public void hostBattle() {
      try {
       battleThread = new BattleThread("", this.PORT, true, monster.getID(), monster.getHP(), monster.getMaxDamage());
       battleThread.start();
      } catch (IOException ex) {
       ex.printStackTrace();
      }
    }

    public void joinBattle(String ip) {
      try {
       battleThread = new BattleThread(ip, this.PORT, false, monster.getID(), monster.getHP(), monster.getMaxDamage());
       battleThread.start();
     } catch (IOException ex) {
       ex.printStackTrace();
     }
    }

    public void train() {

    }

    public void init(Context context) {
      // Rendering setup
      this.context = context;

      // graphics
      monsters = getBitmapFromAsset(context.getAssets(), "monsters.png");
      bg = getBitmapFromAsset(context.getAssets(), "bg.jpg");
      hostIcon = getBitmapFromAsset(context.getAssets(), "hosting.png");
      attackIcon = getBitmapFromAsset(context.getAssets(), "attack.png");

      // callback
      holder = getHolder();

      paint = new Paint();
      paint.setColor(Color.BLACK);
      paint.setStrokeWidth(2);
      paint.setStyle(Paint.Style.STROKE);

      this.setOnTouchListener(this);

      // Monster setup
      loadMonster();

      if(this.monster == null) {
        int tileID = (int) Math.floor(Math.random()*((int)NUM_COLS*NUM_ROWS));
        String name = getMonsterNameFromID(tileID);
        int HP = 1;
        int maxHP = 6;
        int minDamage = 1;
        int maxDamage = ThreadLocalRandom.current().nextInt(1, 3 + 1); // min = 1, max = 3
        int xpos = 0; // move around inbetween the frame
        int wins = 0;
        int losses = 0;

        this.monster = new Monster(tileID, name, HP, maxHP, minDamage, maxDamage, wins, losses);
      }
    }

    public String getMonsterNameFromID(int ID) {
      return new String("Digimon");
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

          if(!isSpriteLoaded && monster != null && monsters != null && bg !=null && hostIcon != null && attackIcon != null) {
            monsterSprite = getMonsterSpriteFromID(monsters, monster.getID());
            monsterSprite.setScale(2);

            bgSprite = new Sprite(this, bg);

            hostSprite = new Sprite(this, hostIcon);
            hostSprite.setPosX((int)this.getWidth() - 50);
            hostSprite.setPosY((int)this.getHeight() - 50);

            attackSprite = new Sprite(this, attackIcon);
            attackSprite.setScale(2);

            isSpriteLoaded = true;
          }
          Canvas canvas = holder.lockCanvas();
          onDraw(canvas);
          holder.unlockCanvasAndPost(canvas);
        } else {
          Log.d("Android: ", "holder not valid!");
        }
      }
    }

    @Override
    public void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      // draw white bg
      paint.setColor(Color.WHITE);
      paint.setStyle(Paint.Style.FILL);
      canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

      // draw bg
      if(bg != null){
        paint.setAlpha(25); // barely visible
        bgSprite.onDraw(canvas, paint);
        paint.setAlpha(255); // fully opaque
      }

      boolean inBattle = false;

      if(battleThread != null) {
        if(battleThread.getIsHosting()) {
          hostSprite.onDraw(canvas);
        }

        if(battleThread.isInBattle()) {
            inBattle = true;

            // flip sprite
            Sprite flippedMonsterSprite = monsterSprite.flipX();
            flippedMonsterSprite.setPosX(this.getWidth()/2 - ((int)(TILE_WIDTH/2) + 100));
            flippedMonsterSprite.setPosY(this.getHeight()/2 - (int)(TILE_HEIGHT/2));

            Sprite otherMonster = getMonsterSpriteFromID(monsters, battleThread.getOtherTileID());
            otherMonster.setScale(2);
            otherMonster.setPosX(this.getWidth()/2 - ((int)(TILE_WIDTH/2) - 100));
            otherMonster.setPosY(this.getHeight()/2 - (int)(TILE_HEIGHT/2));

            flippedMonsterSprite.onDraw(canvas);
            otherMonster.onDraw(canvas);

            // randomly add attack effects
            int rand = ThreadLocalRandom.current().nextInt(0, 2 + 1);

            for(int i = 0; i < rand; i++) {
              attackSprite.setPosX((int)((double)Math.random()*(((double)this.getWidth()/2.0)+30.0)));
              attackSprite.setPosY((this.getHeight()/2) - (int)(Math.random()*(40.0/2.0)));
              attackSprite.onDraw(canvas);
            }
        }

        if(battleThread.isBattleOver()){
          // update our stats post battle
          monster.updateHP(battleThread.getAfterBattleHP());

          if(battleThread.getWins() > 0) {
            monster.victory();
          }

          if(battleThread.getLosses() > 0) {
            monster.defeat();
          }
        }
      }

      if(!inBattle) {
        int xPos = getWidth()/2;
        // make it move around I guess
        if(monster.getHP() > 1) {
          xPos += (int)(Math.sin(System.currentTimeMillis()*0.0002)*getWidth()/4);
          xPos -= (int)TILE_WIDTH/2;
        }

        if(isSpriteLoaded) {
          monsterSprite.setPosX(xPos);
          monsterSprite.setPosY(getHeight()/2);
          monsterSprite.onDraw(canvas);
        }

        if(monster.getHP() > 1) {
          paint.setColor(Color.BLACK);
        } else {
          paint.setColor(Color.RED);
        }

        for(int i = 0; i < monster.getMaxHP(); i++) {
          if(i < monster.getHP()) {
            paint.setStyle(Paint.Style.FILL);
          } else {
            // empty blocks
            paint.setStyle(Paint.Style.STROKE);
          }
          int left = 10*(i+1)+(40*i);
          int right = left + 30;
          int top = 30;
          int bottom = top + 30;
          canvas.drawRect(left, top, right, bottom, paint);
        }
      }

      try{
        // give it that blocky 8bit movement
        Thread.sleep(500);
      }catch(InterruptedException e) {
        // Shouldnt happen...
      }
    }

    public void onPause() {
      isThreadActive = false;

      try {
        viewThread.join();
      } catch(InterruptedException e) {
        e.printStackTrace();
      }

      viewThread = null;
    }

    public void onResume() {
      isThreadActive = true;
      viewThread = new Thread(this);
      viewThread.start();
    }

    public void saveMonster() {
      try{
        MonsterWriter.write(this.monster, this.context.getFilesDir(), "digimon.xml");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public void loadMonster() {
      try {
        this.monster = MonsterReader.read(this.context.getFilesDir(), "digimon.xml");
      } catch(Exception e) {
        e.printStackTrace();
        this.monster = null;
      }
    }

    public boolean onTouch(View v, MotionEvent me) {
      // monsterSprite.setPosX((int)me.getX());
      // monsterSprite.setPosY((int)me.getY());

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
