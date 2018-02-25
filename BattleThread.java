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

    // TODO: Check who has the least amount of damage dealt. That is the winner.

    // We cannot have negative health
    if(myHP < 0) myHP = 0;
    if(otherHP < 0) otherHP = 0;

    return otherHP;
  }

  public int getAfterBattleHP() {
    return myHP;
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

        // TODO: tell the opponent who won

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
     } catch (SocketTimeoutException s) {
        System.out.println("Socket timed out!");
        isInBattle = false;
        return;
     } catch (IOException e) {
        e.printStackTrace();
        isInBattle = false;
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

       // TODO: wait for the host to tell us who won...

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
