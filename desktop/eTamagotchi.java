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
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class eTamagotchi extends Thread {

    // Java Swing GUI drawing stuff
    static final double TILE_WIDTH = 480/10, TILE_HEIGHT = 384/6, NUM_COLS = 10, NUM_ROWS = 6;
    static BufferedImage tiles = null, attackImg = null, hostImg = null;
    static Timer timer;

    // Server/client stuff
    static final String PORT = "9999"; // if hosting, this is the port to use
    static BattleThread battleThread = null;

    // Monster Stuff
    static int HP = 1;
    static int maxHP = 6;
    static int maxDamage = ThreadLocalRandom.current().nextInt(1, 2 + 1); // min = 1, max = 2
    static int xpos = 0; // move around inbetween the frame
    static int wins = 0;
    static int losses = 0;

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
      String content = "Digimon: ???\nHP : "
                      + HP + "/" + maxHP
                      + "\nATK: " + maxDamage
                      + "\nKDR: " + wins + " / " + losses;
      JOptionPane.showMessageDialog(null, content);
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
        JMenuItem m12 = new JMenuItem("HOST P2P Battle");
        JMenuItem m13 = new JMenuItem("JOIN P2P Battle");
        m1.add(m11);
        m1.add(m12);
        m1.add(m13);
        JMenuItem m21 = new JMenuItem("Stats");
        JMenuItem m22 = new JMenuItem("Reset");
        m2.add(m21);
        // m2.add(m22); NOTE: reset app not implemented

        try {
          tiles = ImageIO.read(new File("./monsters.png"));
          hostImg = ImageIO.read(new File("./hosting.png"));
          attackImg = ImageIO.read(new File("./attack.png"));
        } catch (Exception e) {
          // Not found, will try to throw an exception. Fail.
          return;
        }

        // Choose a random monster and store it for network battles
        // 16 cols x 12 rows
        final int tileID = (int) Math.floor(Math.random()*((int)NUM_COLS*NUM_ROWS));
        System.out.print("tileID: " + tileID + "\n");

        final BufferedImage myMonster = getMonsterTileFromID(tileID);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel();
        JButton feed = new JButton("Feed");
        JButton doBattle = new JButton("Do Battle");
        panel.add(feed); // Components Added using Flow Layout
        panel.add(doBattle); // Components Added using Flow Layout

        // Make menu items and buttons do stuff
        ActionListener feedAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              HP++;
              if(HP > maxHP) {
                HP = maxHP;
              }
          }
        };

        feed.addActionListener(feedAction);
        m11.addActionListener(feedAction);

        ActionListener P2PAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String IP = JOptionPane.showInputDialog(frame, "Enter IP address of monster", null);
            System.out.print("Dest IP: " + IP + "\n");

            if(IP == null) {
              // Canceled
              return;
            }

            // TODO: BattleThread connect
            // Finally start the battle thread in the background
            try {
             battleThread = new BattleThread(IP, PORT, false, tileID, HP, maxDamage);
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
             battleThread = new BattleThread("", PORT, true, tileID, HP, maxDamage);
             battleThread.start();
            } catch (IOException ex) {
             ex.printStackTrace();
            }
          }
        };

        m12.addActionListener(HostAction);

        ActionListener StatsClickedAction = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            showStats();
          }
        };

        m21.addActionListener(StatsClickedAction);

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
                      g.drawImage(createFlipped(myMonster),
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
                    HP = battleThread.getAfterBattleHP();
                    wins += battleThread.getWins();
                    losses += battleThread.getLosses();
                  }
                }

                if(!inBattle) {
                  // make it move around I guess
                  if(HP > 1) {
                    xpos = (int)(Math.sin(System.currentTimeMillis()*0.0002)*100.0);
                  }

                  g.drawImage(myMonster,
                              (super.getWidth()/2) - (int)(TILE_WIDTH/2) - xpos,
                              (super.getHeight()/2) - (int)(TILE_HEIGHT/2),
                              null);
                }

                if(HP > 1) {
                  g.setColor(Color.BLACK);
                } else {
                  g.setColor(Color.RED);
                }

                for(int i = 0; i < maxHP; i++) {
                  if(i < HP) {
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
