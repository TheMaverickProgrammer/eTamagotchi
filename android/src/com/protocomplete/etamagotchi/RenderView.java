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
    Bitmap egg;
    Bitmap poo;
    Sprite monsterSprite = null;
    Sprite bgSprite = null;
    Sprite hostSprite = null;
    Sprite attackSprite = null;
    Sprite eggSprite = null;
    Sprite pooSprite = null;
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

    public void joinBattle(String bc) {
      try {
        String IP = BattleCode.unpack(bc);

        if(bc.equals(getClientBattleCode())) {
          // we're fighting ourselves
          IP = "127.0.0.1";
        }

       battleThread = new BattleThread(IP, this.PORT, false, monster.getID(), monster.getHP(), monster.getMaxDamage());
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
      egg = getBitmapFromAsset(context.getAssets(), "egg.png");
      poo = getBitmapFromAsset(context.getAssets(), "poo.png");

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
        Long now = new Date().getTime();
        int tileID = (int) Math.floor(Math.random()*((int)NUM_COLS));
        String name = getMonsterNameFromID(tileID);
        String birthday = new SimpleDateFormat("MM/dd/yyyy").format(now);
        Long lastFedTimestamp = now;
        Long lastCareTimestamp = now;
        int HP = 1;
        int maxHP = 5;
        int minDamage = 1;
        int maxDamage = ThreadLocalRandom.current().nextInt(1, 3 + 1); // min = 1, max = 3
        int xpos = 0; // move around inbetween the frame
        int wins = 0;
        int losses = 0;

        monster = new Monster(tileID, birthday, name, lastFedTimestamp, lastCareTimestamp, HP, maxHP, minDamage, maxDamage, wins, losses);
      }

      ActionListener hourlyPerformer = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          // Real-time updates


          // get hungry
          monster.starve();
        }
      };

      Timer hourlyUpdate = new Timer(1000*60*60, hourlyPerformer);
      hourlyUpdate.start();
    }

    private static String getMonsterNameFromID(int ID) {
      String name = new String("???");

      String[] database = {
        "Pururumon", "Tsubumon", "Chibomon", "Leafmon", "Jyarimon" ,
        "Relemon"  , "Zerimon" , "Conomon", "Ketomon",   "Kiimon"  ,
        "Poromon"  , "Upamon"  , "DemiVeemon", "Minomon", "Gigimon",
        "Viximon"  , "Gummymon", "Kokomon" , "Hopmon" , "Yaamon",
        "Hawkmon"  , "Armadillomon","Veemon","Wormmon","Guilmon",
        "Renamon"  , "Terriermon", "Lopmon", "Monodramon", "Impmon",
        "Aquilamon", "Ankylomon","ExVeemon", "Stingmon", "Growlmon",
        "Kyubimon", "Gargomon", "Turuiemon", "Leomon", "Gaurdromon",
        "Silphymon", "Shakkoumon", "Paildramon", "Dinobeemon", "WarGrowlmon",
        "Taomon", "Rapidmon", "Antylamon", "Cyberdramon", "Andromon",
        "Valkyrimon", "Vikemon", "Imperialdramon", "GranKuwagamon", "Gallantmon",
        "Sakuyamon", "MegaGargomon", "MarineAngemon", "Justimon", "Beelzemon"
      };

      try{
        name = database[ID];
      }catch(Exception e) {
        System.out.println("There's no digimon in the database for " + ID + "\n");
      }

      return name;
    }

    /*
      This is a really basic evolution check
      We see if it's a baby, trainee, rookie, champion, ultimate, or mega by the row it's in
      Based on that, we see if it's had enough Wins/Losses to move up the ranks
    */
    private static boolean canEvolve(Monster monsterIn) {
      // 0 - 9 BABY
      if(monsterIn.getID() < 10) {
        if(monsterIn.getDaysOld() > 5) {
          return true;
        }
      } else if(monsterIn.getID() >= 10 && monsterIn.getID() < 20) {
        // 10 - 19 TRAINEE
        if(monsterIn.getDaysOld() > 12) {
          return true;
        }
      } else if(monsterIn.getID() >= 20 && monsterIn.getID() < 30) {
        // 20 - 29 ROOKIE
        if(monsterIn.getDaysOld() > 19 && monsterIn.getWins() >= 10) {
          return true;
        }
      } else if(monsterIn.getID() >= 30 && monsterIn.getID() < 40) {
        // 30 - 39 CHAMPION
        if(monsterIn.getDaysOld() > 25 && monsterIn.getWins() - monsterIn.getLosses() >= 30) {
          return true;
        }
      } else if(monsterIn.getID() >= 40 && monsterIn.getID() < 50) {
        // 40 - 49 ULTIMATE
        if(monsterIn.getDaysOld() > 40 && monsterIn.getWins() - monsterIn.getLosses() >= 60) {
          return true;
        }
      }

      // Megas cannot evolve further

      return false;
    }

    /*
    This is a very basic evolution.
    The immediate row in the spritesheet is the monster's next form
    Randomly boost stats.
    Winners get better boosts.
    NOTE: This is where things like overall care score and mood would affect status
          You can add this feature in
    */
    private static void evolve(Monster monsterIn) {
      // The spritesheet as 10 monsters per row
      int tileID =  monsterIn.getID()+10;
      String name = getMonsterNameFromID(tileID);
      String birthday = monsterIn.getBirthday();
      Long lastFedTimestamp = monsterIn.getLastFedTimestamp();
      Long lastCareTimestamp = monsterIn.getLastCareTimestamp();
      int HP = monsterIn.getHP();
      int maxHP = monsterIn.getMaxHP()+1;
      int minDamage = monsterIn.getMinDamage() + ThreadLocalRandom.current().nextInt(0, 1 + 1); // min = 0, max = 1
      int maxDamage = monsterIn.getMaxDamage() + ThreadLocalRandom.current().nextInt(1, 2 + 1); // min = 1, max = 2
      int wins = monsterIn.getWins();
      int losses = monsterIn.getLosses();

      if(losses > 0) {
        if((double)wins/(double)losses > 1) {
          // Boost stats
          minDamage += 1;
          maxDamage += 1;
        } else if((double)wins/(double)losses == 1) {
          if((double)wins/(double)losses > 1) {
            // Boost stats
            minDamage += 1;
            minDamage = Math.min(minDamage, maxDamage);
          }
        }
      } else {
        if(wins > 0) {
          // Boost stats
          minDamage += 1;
          maxDamage += 1;
        }
      }

      monster = new Monster(tileID, birthday, name, lastFedTimestamp, lastCareTimestamp, HP, maxHP, minDamage, maxDamage, wins, losses);
      myMonsterImage = getMonsterTileFromID(monster.getID());
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

          if(!isSpriteLoaded && monster != null && monsters != null
                && bg  != null && hostIcon != null && attackIcon != null
                && egg != null && poo != null) {
            monsterSprite = getMonsterSpriteFromID(monsters, monster.getID());
            monsterSprite.setScale(2);

            bgSprite = new Sprite(this, bg);

            hostSprite = new Sprite(this, hostIcon);
            hostSprite.setPosX((int)this.getWidth() - 50);
            hostSprite.setPosY((int)this.getHeight() - 50);

            attackSprite = new Sprite(this, attackIcon);
            attackSprite.setScale(2);

            eggSprite = new Sprite(this, egg);

            pooSprite = new Sprite(this, poo);

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
        if(monster.isEgg()) {
          if(isSpriteLoaded) {
            eggSprite.setPosX(getWidth()/2);
            eggSprite.setPosY(getHeight()/2);
            eggSprite.onDraw(canvas);
          }
        } else {
          // evolve if ready
          if(RenderView.canEvolve(monster)) {
            RenderView.evolve(monster);
          }

          // If the monster has not beed cleaned up, show poop
          // NOTE: This can affect other stats too like mood
          long lastFedTimestamp =  monster.getLastFedTimestamp();
          int hoursLastFed = monster.getHoursSinceLastFed();
          int hoursLastCared = monster.getHoursSinceLastCared();

          Random generator = new Random(hoursLastCared);

          double inc = 0;

          int extra = Math.max((hoursLastFed-hoursLastCared), 0);

          int poopCount = 30; // After too many, stop
          while(inc < (double)(hoursLastCared+extra)) {

            // draw poop
            double x = generator.nextDouble() * (0.5);
            double y = generator.nextDouble() * (0.5);

            if(isSpriteLoaded) {
              pooSprite.setPosX(int)((double)x*(((double)super.getWidth()/2.0)+30.0));
              pooSprite.setPosY(super.getHeight()/2) - (int)(y*(40.0/2.0));
              pooSprite.onDraw(canvas);
            }

            // The bigger (stronger) they are, the more the poop
            // The smaller, the less they poop
            inc += 3.0/monster.getMaxHP();

            poopCount--;

            if (poopCount == 0) break;
          }

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

          int renderHP = monster.getMaxHP();

          if(monster.isEgg()) {
            renderHP = 1;
          }

          double scale = 6.0/(double)monster.getMaxHP();

          for(int i = 0; i < renderHP; i++) {
            if(i < monster.getHP()) {
              paint.setStyle(Paint.Style.FILL);
            } else {
              // empty blocks
              paint.setStyle(Paint.Style.STROKE);
            }
            double left   = scale*10.0*((double)i+1.0)+(scale*40.0*(double)i);
            double right  = left + 30.0*scale;
            double top    = 30.0*scale;
            double bottom = top + 30.0*scale;
            canvas.drawRect((int)left, (int)top, (int)right, (int)bottom, paint);
          }
        }

        try{
          // give it that blocky 8bit movement
          Thread.sleep(500);
        }catch(InterruptedException e) {
          // Shouldnt happen...
        }
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
