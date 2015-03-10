package aoa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AoATestClient extends Thread
{
  PrintWriter out;
  BufferedReader in;
  Socket kkSocket;
  boolean connected;
  String host;
  
  private long lastSyncMsgAt = -1;  
  private long lastSyncMsg = -1;

  public AoATestClient(String hst) 
  {
    this.host = hst;
  }
 
  public long currentMs() {
      return System.currentTimeMillis();      
  }
  public long elapsedSinceLastSyncMessage() {
     return (int)(currentMs() - lastSyncMsgAt);
  }
  
  public long getSyncTime() {
     return lastSyncMsg + elapsedSinceLastSyncMessage();
  }
  
  public static void main(String[] args) 
  {    
    String host = "localhost";
    if (args != null && args.length ==1)
      host = args[0];
    AoATestClient cp = new AoATestClient(host);
    cp.start();    
  }
  
  public void run()
  {
    try
    {
      kkSocket = new Socket(host, 8081);
      out = new PrintWriter(kkSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
      connected = true;
      onData();
    }
    catch (Exception e)
    {
      System.out.println("[CLIENT] Error: "+e.getMessage());
      System.exit(1);
    }  
  }

  public void onData() 
  {       
    System.out.println("onData: "+in);//+" sync="+getSync());
    String fromServer = null;
    try
    {
      String pre="<sync time='";
      while ((fromServer = in.readLine()) != null) {       
                
        if (fromServer.startsWith(pre)) {
           fromServer = fromServer.substring(pre.length(),fromServer.length()-3);
                     
           //System.err.println("[CLIENT] ServerSync: "+serverSyncStamp+" :: clientSync="+getSyncTime());//+" elapsed="+getElapsed());
           //System.err.println("[CLIENT] ServerSync: "+serverSyncStamp+" :: clientSync="+getSyncTime());//+" elapsed="+getElapsed()
           System.err.print("[CLIENT] Sync="+getSyncTime());
           long serverSyncStamp = Long.parseLong(fromServer);
           lastSyncMsg = serverSyncStamp;
           lastSyncMsgAt = currentMs();
           System.err.println("  ServerSync: "+serverSyncStamp +" Local="+System.currentTimeMillis());
        }
        else
            System.err.println("NO MATCH!");
        
        //sendReply();
      }       
    }
    catch (Exception e)
    {
      System.out.println("[CLIENT] Error: "+e.getMessage());
      e.printStackTrace();
    }
  }
  
  public void sendReply() 
  { 
    try {
        String msg = "";//<sync time='"+getSync()+"'/>";
        out.println(msg);
        out.flush();
        System.out.println("[CLIENT] Sent: "+msg+" @ "+currentMs());
    }
    catch (Exception e)
    {
      System.out.println("[CLIENT] Error: "+e.getMessage());
    }
  }
}

