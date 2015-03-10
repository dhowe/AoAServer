package aoa.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

public class AoAServerThread extends Thread 
{
    static int ID = 0, MSG_IDX = 0;
    
    int id = 0;
    //msgIdx = 0;
    private String message;   

    BufferedWriter os;    
    ListenThread listener;
    Socket clientSocket;
    AoAMessageServer server;
    boolean running, listening;

    public AoAServerThread(AoAMessageServer msgServer, Socket cs) {
        this.id = ++ID;
        this.server = msgServer;
        this.clientSocket = cs;
        running = true;
    }

    public void run() {
        while (running) {
            try {
                if (listening && listener == null) {
                    this.listener = new ListenThread(new BufferedReader
                       (new InputStreamReader(clientSocket.getInputStream())));
                    this.listener.start();
                }
                if (message != null) {
                    if (os == null) {
                      os = new BufferedWriter(new OutputStreamWriter
                        (clientSocket.getOutputStream()));
                    }
                    os.write(message, 0, message.length());
                    cout(message);
                    os.flush(); 
                    message = null;
                }                
                Thread.sleep(10);
            } catch (SocketException e) {
                running = false;
                cout("client disconnected...");// + e.getMessage());
                server.removeThread(this);
            } catch (Exception e) {
                running = false;
                cout("unexpected error -> " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void cout(String msg) {
        System.err.println("[INFO] ServerThread#"+ id + ": " + msg);
    }

    public void kill() {
        //cout("kill() called...");
        running = false;
        try {
            if (listener != null) {
                listener.close();
                listener = null;
            }
            if (os != null)
                os.close();
            if (clientSocket != null)
                clientSocket.close();
            clientSocket = null;
        } 
        catch (IOException e) {
            System.err.println("I/O error while closing connections.");
        }
    }

    public synchronized void setMessage(String msg) {
        this.message = msg;
    }   
    
    class ListenThread extends Thread {
        BufferedReader is;

        public ListenThread(BufferedReader is) {
            //System.out.println("ListenThread.ListenThread("+is+")");
            this.is = is;
        }

        public void run() {
            
            //while (running) {
            String line;
            try {
                while ((line = is.readLine()) != null) {
                    if (line.charAt(0) != '<') continue;
                    if (!line.endsWith("/>")) continue;
                    line = line.substring(1,line.length()-2);                    
                    cout("RECEIVED: " + line);
                }
            } catch (SocketException e) {
                //System.out.println("disconnected");
            
            } catch (Exception e) {
                cout("ListenThread.unexpected error -> " + e.getMessage());
                e.printStackTrace();
            }
            
        }

        public void close() {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
            is = null;
        }
    }

}// end
