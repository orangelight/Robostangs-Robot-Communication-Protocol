
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author Alex
 */
public class RRCPServer implements Runnable {
    
    private static boolean listening = true;
    private static int port = 548;
    private static int timeout = 5000;
    private static Thread t;
    private static RRCPServer instance;
    private static ServerSocketConnection server;

    public static RRCPServer getInstance() {
        if (instance == null) {
            instance = new RRCPServer();
        }
        return instance;
    }
    
    public static void setPort(int p) {
        port = p;
    }
    
    private RRCPServer() {
        t = new Thread(this);
        RRCPCommandHandler.getInstance();
    }
    
    public static void startServer(int port, int timeout) {
        RRCPServer.port = port;
        RRCPServer.timeout = timeout;
        listening = true;
        t.start();
    }
    
    public static void startServer() {
        t.start();
    }
    
    public static void stopServer() {
        try {
            listening = false;
            server.close();
        } catch (IOException ex) {
            System.err.println("Error closing server: \"" + ex.getMessage() + "\"");
        }
        System.out.println("SERVER SHUT DOWN");
    }
    
    public void run() {
        try {
           server = (ServerSocketConnection) Connector.open("serversocket://:" + RRCPServer.port);
           while(listening) {
               SocketConnection s = (SocketConnection) server.acceptAndOpen();
               System.out.println("Client Connected");
               RRCPConnectionHandler ch = new RRCPConnectionHandler(s);
               Thread tch = new Thread(ch);
               tch.start();
           }
        } catch (IOException ex) {
            System.err.println("Error making client socket: \"" + ex.getMessage() + "\"");
        }
    }
       
    private class RRCPConnectionHandler implements Runnable {
        
        private SocketConnection s;
        private DataInputStream dis;
        private DataOutputStream dos;
        private long lastHeartBeat;
        public RRCPConnectionHandler(SocketConnection s) {
            this.s = s;
            try {
                dis = new DataInputStream(this.s.openInputStream());
                dos = new DataOutputStream((this.s.openOutputStream()));
            } catch (IOException ex) {
                System.err.println("Error making data streams on ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
        }

        public void run() {
            protocol();
            this.close();
        }
        
        private void close() {
            try {
                dis.close();
                s.close();
                RRCPServer.listening = false;
            } catch (IOException ex) {
                System.err.println("Error closing ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
        }
        
        private void protocol() {
            try {
                this.lastHeartBeat = System.currentTimeMillis();
                while(System.currentTimeMillis() < this.lastHeartBeat+timeout && RRCPServer.listening) {
                    while(dis.available() > 0) {
                        String command = dis.readUTF();
                        System.out.println("READING: "+command);
                        this.lastHeartBeat = System.currentTimeMillis();
                        if(command.equals("HEARTBEAT")) {
                            RRCPCommandHandler.sendByte((byte)21, dos);
                            dos.flush();
                            this.lastHeartBeat = System.currentTimeMillis();
                        } else if(command.equals("QUIT")) { 
                            this.close();
                            break;
                        } else {
                            RRCPCommandHandler.executeCommand(command, dis, dos);
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                    }
                }
                System.err.println("Client timed out!!!");
            } catch (IOException ex) {
                System.err.println("Error reading data from client: \"" + ex.getMessage() + "\"");
            }         
        }        
    }
}