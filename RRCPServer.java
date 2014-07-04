
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class RRCPServer implements Runnable{
    
    private static boolean listening = true;
    private static int port = 548;
    private static int timeout = 5000;
    private static Thread t;
    private static RRCPServer instance;
    private static ServerSocket server;

    public static RRCPServer getInstance() {
        if (instance == null) {
            instance = new RRCPServer();
        }
        return instance;
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
            Logger.getLogger(RRCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("SERVER SHUT DOWN");
    }
    public void run() {
        try {
            server = new ServerSocket(this.port);
           while(listening) {
               Socket s = server.accept();
               System.out.println("Client Connected");
               RRCPConnectionHandler ch = new RRCPConnectionHandler(s);
               Thread tch = new Thread(ch);
               tch.start();
           }
        } catch (IOException ex) {
            Logger.getLogger(RRCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private class RRCPConnectionHandler implements Runnable {
        
        private Socket s;
        private DataInputStream dis;
        private DataOutputStream dos;
        long lastHeartBeat;
        public RRCPConnectionHandler(Socket s) {
            this.s = s;
            try {
                dis = new DataInputStream(this.s.getInputStream());
                dos = new DataOutputStream((this.s.getOutputStream()));
            } catch (IOException ex) {
                Logger.getLogger(RRCPServer.class.getName()).log(Level.SEVERE, null, ex);
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
                s.close();
                RRCPServer.listening = false;
            } catch (IOException ex) {
                Logger.getLogger(RRCPServer.class.getName()).log(Level.SEVERE, null, ex);
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
                        Logger.getLogger(RRCPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.err.println("Client timed out!!!");
            } catch (IOException ex) {
                Logger.getLogger(RRCPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
                    
        }
        
    }
}
