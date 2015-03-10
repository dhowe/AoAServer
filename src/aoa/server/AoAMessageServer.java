package aoa.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
 * TODO: wrap as service?
 */
public class AoAMessageServer extends Thread // singleton?
{
  private static final String CLOSE_TAG = "/>\n";
  static final int CMD_DELAY = 2000;
  static final int MSG_PORT = 8081;

  ServerSocket serverSocket;
  List serverThreads = new ArrayList();
  boolean running, sendingCmd;
  int count = 0, msgIdx = 0;
    
  String[] cmds = { "largeFlip", "red", "green", }; 

  /*String[] cmds = { "blank", "largeFlip", "elev", "red", "bw", 
    "flashText", "night", "green", "flashClips", "brown",  };
*/

  String getMsgHeader(int idx) {
    return "<sync id='"+idx+"' time='"+getSyncTime()+"'";
  }

  public AoAMessageServer(String[] args) {
 
    int interval = 3000; // default = 3sec
    int cmdFreq = 10;   // cmd every 30 sec
    
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
        if (msgIdx % modCheck == 0) {
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

/*    Timer refreshtimer = new Timer();
    TimerTask refresh = new TimerTask() {
      public synchronized void run() {
        msgIdx++;
        String cmd = //"<sync id='" + msgIdx + "' time='" + getSyncTime()
            getMsgHeader() + "' cmd='"+cmds[count]+"' start='"+CMD_DELAY+CLOSE_TAG;
        //String cmd = "<sync time='" + getSyncTime()
         // + "' cmd='" + cmds[count] + "' start='" + CMD_DELAY + "'/>\n";
        sendMessage(cmd);

        if (++count == cmds.length)
          count = 0;
      }
    };
    refreshtimer.scheduleAtFixedRate(refresh, START_DELAY, (long) interval);*/
    this.start();
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
    System.err.print("[INFO] MsgServer running on port: " + MSG_PORT + "\n");
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
