package aoa.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AoAMessageServer extends Thread
{
  private static final String CLOSE_TAG = "/>\n";
  static final int CMD_DELAY = 2000;
  static final int MSG_PORT = 8081;

  ServerSocket serverSocket;
  List serverThreads = new ArrayList();
  boolean running, sendingCmd;
  int count = 0, msgIdx = 0;
  int startupCount = 0;

    
  String[] cmds = { "blank", "largeFlip", "flashText", "green", "night", "elev"};

  /*String[] cmds = { "blank", "largeFlip", "elev", "red", "bw", 
    "flashText", "night", "green", "flashClips", "brown",  };
*/

  public AoAMessageServer(String[] args) {
 
    int interval = 3000; // default = 3sec
    int cmdFreq = 20;   // cmd every 1min
    
    if (args != null) { 
      if (args.length == 2) {
        interval = Integer.parseInt(args[0]);
        cmdFreq = Integer.parseInt(args[1]);
      }
      else if (args.length != 0) { 
        System.err.println("Invalid args! usage: " +
          getClass().getName()+" <interval> <freq>");
        System.exit(0);
      }
    }

    System.out.println("[INFO] AoA->MessageServer: eventFreq="
      + interval+" cmdFreq="+(interval*cmdFreq));

    final int modCheck = cmdFreq;
    Timer timer = new Timer();
    TimerTask syncTask = new TimerTask() {

      public synchronized void run() {        
        String cmd = getMsgHeader(++msgIdx);
        //String cmd = "<sync time='" + getSyncTime() + "'/>\n";
        
        if (startupCount < 10) {
          cmd += " cmd='blank' start='"+CMD_DELAY+"'";
        }
        else if (msgIdx % modCheck == 0) {
          cmd += " cmd='"+cmds[count]+"' start='"+CMD_DELAY+"'";
          if (++count == cmds.length)
            count = 0;
        }
       
         
        cmd += CLOSE_TAG;
        sendMessage(cmd);
        cmd = null;
      }
    };
    timer.scheduleAtFixedRate(syncTask, 1000, interval);

    this.start();
  }

  String getMsgHeader(int idx) {
    return "<sync id='"+idx+"' time='"+getSyncTime()+"'";
  }

  public static long getSyncTime() {
    return System.currentTimeMillis();
    // return (int)(System.currentTimeMillis() - startTime);
  }

  public void start() {
    try {
      serverSocket = new ServerSocket(MSG_PORT);
      running = true;
    } catch (IOException e) {
      System.err.println("Cannot create server socket "
          + "on port:  "+ MSG_PORT + ".  Exiting...");
      System.exit(0);
    }
    System.err.print("[INFO] AoAServer running on port: " + MSG_PORT + "\n");
    super.start();
  }

  public void run() {
    while (running) {
      try {
        Socket clientSocket = serverSocket.accept();
        AoAServerThread es = new AoAServerThread(this, clientSocket);
        es.start();
        serverThreads.add(es);
        System.err.print("[INFO] New client: " + clientSocket
            + " count=" + serverThreads.size() + "\n");
        startupCount = 0;
      } 
      catch (IOException e) {
        System.err.println("\n[ERROR] Unable to accept client connection: "
            + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public void removeThread(AoAServerThread st) {
    System.err.println("[INFO] Removing thread: " + st);
    serverThreads.remove(st);
    st.kill();
    System.err.println("[INFO] Threads: " + serverThreads);
  }

  public boolean sendMessage(String xml) {
    
    if (serverThreads == null || serverThreads.size()<1) 
      return false;
    
    for (Iterator i = serverThreads.iterator(); i.hasNext();) {
      AoAServerThread st = (AoAServerThread) i.next();
      st.setMessage(xml);
    }
    
    xml = null;
    return true;
  }

}
