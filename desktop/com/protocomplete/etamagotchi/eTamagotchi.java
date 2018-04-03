package com.protocomplete.etamagotchi;

/*

"eTamagotchi"

MIT License

Copyright (c) 2018 Maverick Peppers

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.*; // DEBUG
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Random; // For seeded generator

public class eTamagotchi extends Thread {

    // Java Swing GUI drawing stuff
    static final double TILE_WIDTH = 480/10, TILE_HEIGHT = 384/6, NUM_COLS = 10, NUM_ROWS = 6;
    static BufferedImage tiles = null, attackImg = null, hostImg = null, poop = null;
    static Timer timer;

    // Server/client stuff
    static final String PORT = "9999"; // if hosting, this is the port to use
    static BattleThread battleThread = null;

    // Monster Stuff
    static Monster monster = null;
    static BufferedImage myMonsterImage = null;

    private static BufferedImage getMonsterTileFromID(int tileID) {
      // tiles are 16 x 16 pixels long
      int row = (int) Math.floor((double)tileID / (double)NUM_COLS);
      int col = tileID % (int) NUM_COLS;

      return tiles.getSubimage(col*(int)TILE_WIDTH, row*(int)TILE_HEIGHT, (int)TILE_WIDTH, (int)TILE_HEIGHT);
    }

    private static BufferedImage createTransformed(BufferedImage image, AffineTransform at)
    {
        BufferedImage newImage = new BufferedImage(
            image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    private static BufferedImage createFlipped(BufferedImage image)
    {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(-1, 1));
        at.concatenate(AffineTransform.getTranslateInstance(-image.getWidth(), 0));
        return createTransformed(image, at);
    }

    private static void showStats() {
      String content = "Digimon: " + monster.getName()
                      + "\nBday: " + monster.getBirthday() + " - " + monster.getDaysOld() + " days old!"
                      + "\nHP : " + monster.getHP() + "/" + monster.getMaxHP()
                      + "\nATK: " + monster.getMaxDamage()
                      + "\nKDR: " + monster.getWins() + " / " + monster.getLosses();

      JOptionPane.showMessageDialog(null, content);
    }

    private static void showBattleCode() {
      String bc = BattleCode.pack("127.0.0.1");

      String content = "Your monster battle code is " + bc;

      JOptionPane.showMessageDialog(null, content);
    }

    private static String getMonsterNameFromID(int ID) {
      String name = new String("???");

      switch(ID) {
        case 0:
          name = new String("Birdmon");
        break;
        case 1:
          name = new String("super birdmon");
        break;
      }
      return name;
    }

    private static void saveMonster() {
      try{
        MonsterWriter.write(monster, new File("./"), "digimon.xml");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private static void loadMonster() {
      try {
        monster = MonsterReader.read(new File("./"), "digimon.xml");

        if(monster == null) {
          System.out.print("Monster could not be read");
        }
      } catch(Exception e) {
        e.printStackTrace();
        monster = null;
      }
    }

    private static String getResource(String resource) {
      return "com/protocomplete/etamagotchi/" + resource;
    }

    public static void main(String[] args) throws IOException {
        //Creating the Frame
        JFrame frame = new JFrame("eTamagotchi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);

        //Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("Action");
        JMenu m2 = new JMenu("Help");
        mb.add(m1);
        mb.add(m2);
        JMenuItem m11 = new JMenuItem("Feed");
        JMenuItem m12 = new JMenuItem("Clean");
        JMenuItem m13 = new JMenuItem("HOST P2P Battle");
        JMenuItem m14 = new JMenuItem("JOIN P2P Battle");
        m1.add(m11);
        m1.add(m12);
        m1.add(m13);
        m1.add(m14);
        JMenuItem m21 = new JMenuItem("Stats");
        JMenuItem m22 = new JMenuItem("Battle Code");
        JMenuItem m23 = new JMenuItem("Save");
        m2.add(m21);
        m2.add(m22);
        m2.add(m23);

        try {
          tiles = ImageIO.read(new File(eTamagotchi.getResource("monsters.png")));
          hostImg = ImageIO.read(new File(eTamagotchi.getResource("hosting.png")));
          attackImg = ImageIO.read(new File(eTamagotchi.getResource("attack.png")));
          poop = ImageIO.read(new File(eTamagotchi.getResource("poop.png")));
        } catch (Exception e) {
          // Not found, will try to throw an exception. Fail.
          e.printStackTrace();
          return;
        }

        // Monster setup
        eTamagotchi.loadMonster();

        if(monster == null) {
          Long now = new Date().getTime();
          int tileID = (int) Math.floor(Math.random()*((int)NUM_COLS*NUM_ROWS));
          String name = getMonsterNameFromID(tileID);
          String birthday = new SimpleDateFormat("MM/dd/yyyy").format(now);
          Long lastFedTimestamp = now;
          Long lastCareTimestamp = now;
          int HP = 1;
          int maxHP = 6;
          int minDamage = 1;
          int maxDamage = ThreadLocalRandom.current().nextInt(1, 3 + 1); // min = 1, max = 3
          int xpos = 0; // move around inbetween the frame
          int wins = 0;
          int losses = 0;

          monster = new Monster(tileID, birthday, name, lastFedTimestamp, lastCareTimestamp, HP, maxHP, minDamage, maxDamage, wins, losses);
          myMonsterImage = getMonsterTileFromID(monster.getID());

        } else {
          myMonsterImage = getMonsterTileFromID(monster.getID());
        }

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel();
        JButton feed = new JButton("Feed");
        JButton clean = new JButton("Clean");
        JButton doBattle = new JButton("Battle");
        panel.add(feed); // Components Added using Flow Layout
        panel.add(clean);
        panel.add(doBattle); // Components Added using Flow Layout

        // Make menu items and buttons do stuff
        ActionListener feedAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              monster.eat();
          }
        };

        feed.addActionListener(feedAction);
        m11.addActionListener(feedAction);

        ActionListener cleanAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              monster.care();
          }
        };

        clean.addActionListener(cleanAction);
        m12.addActionListener(cleanAction);

        ActionListener P2PAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String bc = JOptionPane.showInputDialog(frame, "Enter other monster's battle code", null);
            String IP = BattleCode.unpack(bc);

            System.out.print("Dest IP: " + IP + "\n");

            if(IP == null) {
              // Canceled
              return;
            }

            // Finally start the battle thread in the background
            try {
             battleThread = new BattleThread(IP, PORT, false, monster.getID(), monster.getHP(), monster.getMaxDamage());
             battleThread.start();
           } catch (IOException ex) {
             ex.printStackTrace();
           }
          }
        };

        doBattle.addActionListener(P2PAction);
        m13.addActionListener(P2PAction);

        ActionListener HostAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try {
             battleThread = new BattleThread("", PORT, true, monster.getID(), monster.getHP(), monster.getMaxDamage());
             battleThread.start();
            } catch (IOException ex) {
             ex.printStackTrace();
            }
          }
        };

        m14.addActionListener(HostAction);

        ActionListener StatsClickedAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            showStats();
          }
        };

        m21.addActionListener(StatsClickedAction);

        ActionListener BattleCodeClickedAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            showBattleCode();
          }
        };

        m22.addActionListener(BattleCodeClickedAction);

        ActionListener SaveClickedAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            saveMonster();
          }
        };

        m23.addActionListener(SaveClickedAction);

        // Drawable panel at the Center
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // This is our main loop for game logic which is bad design
                boolean inBattle = false;

                if(battleThread != null) {
                  if(battleThread.getIsHosting()) {
                    g.drawImage(hostImg, super.getWidth() - 45, 50, null);
                  }

                  if(battleThread.isInBattle()) {
                      inBattle = true;
                      BufferedImage otherMonsterImg = getMonsterTileFromID(battleThread.getOtherTileID());
                      g.drawImage(createFlipped(myMonsterImage),
                                (super.getWidth()/2) - (int)(TILE_WIDTH/2) - 50,
                                (super.getHeight()/2) - (int)(TILE_HEIGHT/2),
                                null);
                      g.drawImage(otherMonsterImg,
                                (super.getWidth()/2) - (int)(TILE_WIDTH/2) + 50,
                                (super.getHeight()/2) - (int)(TILE_HEIGHT/2),
                                null);

                      // randomly add attack effects
                      int rand = ThreadLocalRandom.current().nextInt(0, 2 + 1);

                      for(int i = 0; i < rand; i++) {
                        g.drawImage(attackImg,
                                    (int)((double)Math.random()*(((double)super.getWidth()/2.0)+30.0)),
                                    (super.getHeight()/2) - (int)(Math.random()*(40.0/2.0)),
                                    null);
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
                  // Check health over time fed
                  // 24 hours should deplete max bars of health
                  double barHours = 24.0/monster.getMaxHP();
                  long lastFedTimestamp =  monster.getLastFedTimestamp();
                  int hours = monster.getHoursSinceLastFed();

                  Random generator = new Random(lastFedTimestamp);
                  double inc = 0;

                  while(inc < (double)hours) {

                    // draw poop
                    double x = generator.nextDouble() * (0.5);
                    double y = generator.nextDouble() * (0.5);

                    g.drawImage(poop,
                                (int)((double)x*(((double)super.getWidth()/2.0)+30.0)),
                                (super.getHeight()/2) - (int)(y*(40.0/2.0)),
                                null);

                    inc += barHours;
                  }


                  // make it move around I guess
                  int xpos = 0;

                  if(monster.getHP() > 1) {
                    xpos = (int)(Math.sin(System.currentTimeMillis()*0.0002)*100.0);
                  }

                  g.drawImage(myMonsterImage,
                              (super.getWidth()/2) - (int)(TILE_WIDTH/2) - xpos,
                              (super.getHeight()/2) - (int)(TILE_HEIGHT/2),
                              null);
                }

                if(monster.getHP() > 1) {
                  g.setColor(Color.BLACK);
                } else {
                  g.setColor(Color.RED);
                }

                for(int i = 0; i < monster.getMaxHP(); i++) {
                  if(i < monster.getHP()) {
                    g.fillRect(10*(i+1)+(40*i), 10, 30, 30);
                  } else {
                    // empty blocks
                    g.drawRect(10*(i+1)+(40*i), 10, 30, 30);
                  }
                }
                g.dispose();
            }
        };

        canvas.setBackground(Color.WHITE);

        ActionListener taskPerformer = new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            // repaint every half second for that choppy effect
            canvas.repaint();
          }
        };

        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, canvas);
        frame.setVisible(true);
        frame.setResizable(false);

        timer = new Timer(500, taskPerformer);
        timer.start();
    }
}
