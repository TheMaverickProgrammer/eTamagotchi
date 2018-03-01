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

import java.net.*;
import java.io.*;

class BattleThread extends Thread {
  private String otherMonsterIP = "";
  private String PORT = "";
  private Socket toClient = null; // When JOINING
  private ServerSocket myServer = null; // When HOSTING
  private boolean isHosting = false;
  private boolean isInBattle = false;
  private boolean battleIsDone = false;
  private int tileID = 0;
  private int otherTileID = 0;
  private int myHP = 0;
  private int myATK = 0;
  private int myWins = 0;
  private int myLosses = 0;
  private String battleResult = "";

  public BattleThread(String otherIP, String port, boolean host, int ID, int HP, int ATK) throws IOException {
      PORT = port;

      isHosting = host;

      tileID = ID;
      myHP = HP;
      myATK = ATK;

      if(isHosting) {
        myServer = new ServerSocket(Integer.parseInt(PORT));
        myServer.setSoTimeout(60000); // give it 1 minute to connect
      } else {
        otherMonsterIP = otherIP;
        myServer = null;
      }
   }

  private int simulateBattle(int otherHP, int otherATK) {
    while(myHP > 0 && otherHP > 0) {
      myHP = myHP - otherATK;
      otherHP = otherHP - myATK;
    }

    // Check who has the least amount of damage dealt. That is the winner.
    if(myHP < otherHP) {
      myLosses++;
      battleResult = "loser";
    } else if(myHP > otherHP) {
      myWins++;
      battleResult = "winner";
    } else {
      // DRAW
      battleResult = "draw";
    }

    // We cannot have negative health
    if(myHP < 0) myHP = 0;
    if(otherHP < 0) otherHP = 0;

    // If we won and our health was 0 after simulating, give a point back to winner
    if(battleResult == "winner" && myHP == 0) {
      myHP = 1;
    }

    return otherHP;
  }

  public int getAfterBattleHP() {
    return myHP;
  }

  public int getWins() {
    System.out.print("my wins: " + myWins + "\n");

    return myWins;
  }

  public int getLosses() {
    System.out.print("my losses: " + myLosses + "\n");

    return myLosses;
  }

  public boolean isBattleOver() {
    boolean result = battleIsDone;
    battleIsDone = false; // reset flag for the next battle
    return result; // update main thread...
  }

  public void setOtherMonsterIP(String IP) {
    otherMonsterIP = IP;
  }

  public boolean isInBattle() {
    return isInBattle;
  }

  public int getOtherTileID() {
    return otherTileID;
  }

  public boolean getIsHosting() {
    return isHosting && (!isInBattle && !battleIsDone);
  }

  public void run() {
    if(isHosting) {
      try {
        System.out.println("Waiting for client on port " +
        myServer.getLocalPort() + "...");

        Socket otherClient = myServer.accept();

        System.out.println("Just connected to " + otherClient.getRemoteSocketAddress());
        DataInputStream in = new DataInputStream(otherClient.getInputStream());

        String buffer = in.readUTF();
        System.out.println(buffer);

        // store our opponents tileID
        otherTileID = Integer.parseInt(buffer);

        // send OUR tileID to host and begin combat
        DataOutputStream out = new DataOutputStream(otherClient.getOutputStream());
        out.writeUTF(String.valueOf(tileID));

        isInBattle = true;

        // Read in THEIR stats
        buffer = in.readUTF();
        System.out.println("Server says " + buffer);

        int otherHP = Integer.parseInt(buffer);

        buffer = in.readUTF();
        System.out.println("Server says " + buffer);

        int otherATK = Integer.parseInt(buffer);

        // simulate battle
        otherHP = simulateBattle(otherHP, otherATK);

        if(battleResult.equals("loser")) {
          // we lost this round, tell the other they won
          out.writeUTF(String.valueOf("winner"));
        } else if(battleResult.equals("winner")){
          // we won, tell them they lost
          out.writeUTF(String.valueOf("loser"));
        } else {
          // DRAW
          out.writeUTF(String.valueOf("draw"));
        }

        // Tell the opponent their updated HP
        out.writeUTF(String.valueOf(otherHP));

        // wait 5 seconds
        try{
          Thread.sleep(5000);
        } catch(InterruptedException e) {

        }

        otherClient.close();
        myServer.close();
        myServer = null;

        isInBattle = false;
        battleIsDone = true;
        isHosting = false;
     } catch (SocketTimeoutException s) {
        System.out.println("Socket timed out!");
        isInBattle = false;
        isHosting = false;
        return;
     } catch (IOException e) {
        e.printStackTrace();
        isInBattle = false;
        isHosting = false;
        return;
     }
   } else {
     // We're sending things out to a host
     try {
       System.out.println("Connecting to " + otherMonsterIP + " on port " + PORT);
       toClient = new Socket(InetAddress.getByName(otherMonsterIP), Integer.parseInt(PORT));

       System.out.println("Just connected to " + toClient.getRemoteSocketAddress());
       OutputStream outToServer = toClient.getOutputStream();
       DataOutputStream out = new DataOutputStream(outToServer);

       // send OUR tileID to client
       out.writeUTF(String.valueOf(tileID));

       // wait for their tileID...
       InputStream inFromServer = toClient.getInputStream();
       DataInputStream in = new DataInputStream(inFromServer);

       String buffer = in.readUTF();
       System.out.println("Server says " + buffer);

       otherTileID = Integer.parseInt(buffer);
       isInBattle = true;

       // Send OUR stats over
       // current HP, ATK
       System.out.println("myHP: " + myHP + " myATK: " + myATK);
       out.writeUTF(String.valueOf(myHP));
       out.writeUTF(String.valueOf(myATK));

       // wait for the host to tell us who won...
       buffer = in.readUTF();
       System.out.println("Server says " + buffer + ".");

       if(buffer.equals("winner")) {
         // increase our stats
         myWins++;
       } else if(buffer.equals("loser")) {
         myLosses++;
       }

       // Update our health...
       buffer = in.readUTF();
       System.out.println("Server says " + buffer);

       myHP = Integer.parseInt(buffer);

       // wait 5 seconds to make viewers feel like a battle is happening...
       try{
         Thread.sleep(5000);
       } catch(InterruptedException e) {

       }

       toClient.close();
       isInBattle = false;
       battleIsDone = true;
    } catch (IOException e) {
       e.printStackTrace();
       isInBattle = false;
       return;
    }
   }
 }
}
