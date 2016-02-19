package sinnyrrcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class RRCPSkinnyServer {
  private static int port = 5801; //Port server is on. Should be 1180 for comp.
    private static int timeout = 2000; //Timeout for clients heartbeats
    private static RRCPSkinnyServer instance; //Instance of server
    private static ServerSocket server; //Socket that listens for incoming connections
    private static int delay = 5;
    private static RRCPConnectionListener clientListener;
    private static RRCPConnectionHandler ch;
    private static boolean clientConnected = false;

    /**
     * Creates new instance of server if their is none
     *
     * @return instance of server
     */
    public static RRCPSkinnyServer getInstance() {
        if (instance == null) {
            instance = new RRCPSkinnyServer();
        }
        return instance;
    }

    private RRCPSkinnyServer() {
        clientListener = new RRCPConnectionListener();
    }

    /**
     * Starts server for listening for incoming client connections
     *
     * @param port What port the server is ran on. Use 1180 for competitions
     * @param timeout Timeout for client heartbeat in milliseconds. Don't use
     * number less then 1000
     */
    public static void startServer(int port, int timeout) {
        RRCPSkinnyServer.port = port;
        RRCPSkinnyServer.timeout = timeout;
        clientListener.startListener();
    }

    /**
     * Starts server for listening for incoming clients connections Uses default
     * port of 548. Use 1180 in competitions Uses default timeout of 2000ms
     */
    public static void startServer() {
        clientListener.startListener();
    }
    
    /**
     * Stops server and disconnects all clients
     */
    public static void stopServer() {
        try {
            clientListener.stopListener();
            ch.close();
            server.close();
        } catch (IOException ex) {
            System.err.println("Error closing server: \"" + ex.getMessage() + "\"");
        }
        System.out.println("SERVER SHUT DOWN");
    }

    public static boolean isClientConnected() { return clientConnected; }
    
    private class RRCPConnectionListener implements Runnable {
        
        private Thread mainThread;
        private boolean listening; //true if listening for clients
        
        private RRCPConnectionListener() {
            mainThread = new Thread(this);
        }
        
        @Override
        public void run() {
            try {
                server = new ServerSocket(port);
                System.out.println("RRCP Server Started!!!");
                while (listening) {
                    
                    Socket s = server.accept();
                    if(!clientConnected) {
                    System.out.println("Client Connected");
                    ch = new RRCPConnectionHandler(s);
                    clientConnected = true;
                    new Thread(ch).start();
                    } else {
                        System.out.println("ERROR CLIENT ALREADY CONNECTED");
                    }
                    
                }
            } catch (IOException ex) {
                System.err.println("Error making client socket: \"" + ex.getMessage() + "\"");
            }
        }
        
        public void startListener() {
            listening = true;
            mainThread.start();
        }
        
        public void stopListener() {
           listening = false; 
        }
    }

    private class RRCPConnectionHandler implements Runnable {

        private Socket s; //Socket server uses to comunicate with client
        private DataInputStream dis;
        private DataOutputStream dos;
        private long lastHeartBeat; //Last heartbeat in system time

        public RRCPConnectionHandler(Socket s) {
            this.s = s;
            try {
                dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream()));
            } catch (IOException ex) {
                System.err.println("Error making data streams on ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
        }

        @Override
        public void run() {
            protocol();
            this.close();
        }

        private void close() {
            try {
                dis.close();
                dos.close();
                s.close();
            } catch (IOException ex) {
                System.err.println("Error closing ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
            clientConnected = false;
        }

        private void protocol() {
            try {
                this.lastHeartBeat = System.currentTimeMillis();
                while (System.currentTimeMillis() < this.lastHeartBeat + timeout) {
                    while (dis.available() > 0) {
                        byte id = dis.readByte();
                        this.lastHeartBeat = System.currentTimeMillis();
                         if (id == 0) {
                            this.close();
                            break;
                        } else if (id == 1) {
                            double distance = dis.readDouble();
                            double heading = dis.readDouble();
                            System.out.println("D: "+distance+" H: "+heading);
                            dos.write(21);
                            dos.flush();
                        } else if (id == 2) {
                            double distance = -1;
                            double heading = -361;
                            dos.write(21);
                            dos.flush();
                        }  else {
                            System.err.println("Error reading packet: " + id + " is not a known ID!");
                        }
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                    }
                }
                System.err.println("Client timed out!!!");              

            } catch (IOException ex) {
                Logger.getLogger(RRCPSkinnyServer.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Error reading data from client: \"" + ex.getMessage() + "\"");
            }
            RRCPSkinnyServer.onSocketClose();
        }
    }
    private static void onSocketClose() { //NTBR
       
    }


}