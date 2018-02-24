import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BasicApp {

    static final double TILE_WIDTH = 480/10, TILE_HEIGHT = 384/6, NUM_COLS = 10, NUM_ROWS = 6;
    static BufferedImage tiles = null;

    static Timer timer;

    static int HP = 1;
    static int maxHP = 6;
    static int maxDamage = (int)(1.0+(Math.random()*1.0)); // 1 or 2 blocks
    static int xpos = 0; // move around inbetween the frame

    private static BufferedImage getMonsterTileFromID(int tileID) {
      // tiles are 16 x 16 pixels long
      int row = (int) Math.floor((double)tileID / (double)NUM_COLS);
      int col = tileID % (int) NUM_COLS;

      return tiles.getSubimage(col*(int)TILE_WIDTH, row*(int)TILE_HEIGHT, (int)TILE_WIDTH, (int)TILE_HEIGHT);
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
        JMenuItem m12 = new JMenuItem("P2P Battle");
        m1.add(m11);
        m1.add(m12);
        JMenuItem m21 = new JMenuItem("Reset");
        m2.add(m21);

        try {
          tiles = ImageIO.read(new File("./monsters.png"));
        } catch (Exception e) {
          // Not found, will try to throw an exception. Fail.
          return;
        }

        // Choose a random monster and store it for network battles
        // 16 cols x 12 rows
        final int tileID = (int) Math.floor(Math.random()*((int)NUM_COLS*NUM_ROWS));
        System.out.print("tileID: " + tileID);

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
            System.out.print("Dest IP: " + IP);
          }
        };

        doBattle.addActionListener(P2PAction);
        m12.addActionListener(P2PAction);

        // Drawable panel at the Center
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // make it move around I guess
                if(HP > 1) {
                  xpos = (int)(Math.sin(System.currentTimeMillis()*0.0002)*100.0);
                }

                g.drawImage(myMonster,
                            (super.getWidth()/2) - (int)(TILE_WIDTH/2) - xpos,
                            (super.getHeight()/2) - (int)(TILE_HEIGHT/2),
                            null);

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

        timer = new Timer(500, taskPerformer);
        timer.start();
    }
}
