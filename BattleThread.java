import java.net.*;
import java.io.*;

class BattleThread extends Thread {
  private String otherMonsterIP = "";
  private String PORT = "";
  private Socket toClient = null; // When JOINING
  private ServerSocket myServer = null; // When HOSTING
  private boolean isHosting = false;

  public BattleThread(String otherIP, String port, boolean host) throws IOException {
      PORT = port;

      isHosting = host;

      if(isHosting) {
        myServer = new ServerSocket(Integer.parseInt(PORT));
        myServer.setSoTimeout(60000); // give it 1 minute to connect
      } else {
        otherMonsterIP = otherIP;
        myServer = null;
      }
   }

  public void setOtherMonsterIP(String IP) {
    otherMonsterIP = IP;
  }

  public void run() {
    if(isHosting) {
      try {
        System.out.println("Waiting for client on port " +
        myServer.getLocalPort() + "...");

        Socket otherClient = myServer.accept();

        System.out.println("Just connected to " + otherClient.getRemoteSocketAddress());
        DataInputStream in = new DataInputStream(otherClient.getInputStream());

        System.out.println(in.readUTF());
        DataOutputStream out = new DataOutputStream(otherClient.getOutputStream());
        out.writeUTF("Thank you for connecting to " + otherClient.getLocalSocketAddress()
           + "\nGoodbye!");
        otherClient.close();
     } catch (SocketTimeoutException s) {
        System.out.println("Socket timed out!");
        return;
     } catch (IOException e) {
        e.printStackTrace();
        return;
     }
   } else {
     // We're sending things out
     try {
       System.out.println("Connecting to " + otherMonsterIP + " on port " + PORT);
       toClient = new Socket(InetAddress.getByName(otherMonsterIP), Integer.parseInt(PORT));

       System.out.println("Just connected to " + toClient.getRemoteSocketAddress());
       OutputStream outToServer = toClient.getOutputStream();
       DataOutputStream out = new DataOutputStream(outToServer);

       out.writeUTF("Hello from " + toClient.getLocalSocketAddress());
       InputStream inFromServer = toClient.getInputStream();
       DataInputStream in = new DataInputStream(inFromServer);

       System.out.println("Server says " + in.readUTF());
       toClient.close();
    } catch (IOException e) {
       e.printStackTrace();
       return;
    }
   }
 }
}
