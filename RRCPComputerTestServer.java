
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Alex
 */
public class RRCPComputerTestServer implements Runnable {
    
    private static boolean listening = true;
    private static int port = 548;
    private static int timeout = 5000;
    private static Thread t;
    private static RRCPComputerTestServer instance;
    private static ServerSocket server;

    public static RRCPComputerTestServer getInstance() {
        if (instance == null) {
            instance = new RRCPComputerTestServer();
        }
        return instance;
    }
    
    public static void setPort(int p) {
        port = p;
    }
    
    private RRCPComputerTestServer() {
        t = new Thread(this);
        RRCPComputerTestCommandHandler.getInstance();
    }
    
    public static void startServer(int port, int timeout) {
        RRCPComputerTestServer.port = port;
        RRCPComputerTestServer.timeout = timeout;
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
            server = new ServerSocket(this.port);
           while(listening) {
               Socket s = server.accept();
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
        
        private Socket s;
        private DataInputStream dis;
        private DataOutputStream dos;
        private long lastHeartBeat;
        public RRCPConnectionHandler(Socket s) {
            this.s = s;
            try {
                dis = new DataInputStream(this.s.getInputStream());
                dos = new DataOutputStream((this.s.getOutputStream()));
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
                RRCPComputerTestServer.listening = false;
            } catch (IOException ex) {
                System.err.println("Error closing ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
        }
        
        private void protocol() {
            try {
                this.lastHeartBeat = System.currentTimeMillis();
                while(System.currentTimeMillis() < this.lastHeartBeat+timeout && RRCPComputerTestServer.listening) {
                    while(dis.available() > 0) {
                        String command = dis.readUTF();
                        System.out.println("READING: "+command);
                        this.lastHeartBeat = System.currentTimeMillis();
                        if(command.equals("HEARTBEAT")) {
                            RRCPComputerTestCommandHandler.sendByte((byte)21, dos);
                            dos.flush();
                            this.lastHeartBeat = System.currentTimeMillis();
                        } else if(command.equals("QUIT")) { 
                            this.close();
                            break;
                        } else {
                            RRCPComputerTestCommandHandler.executeCommand(command, dis, dos);
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                    }
                }
                System.err.println("Client timed out!!!");
                RRCPComputerTestCommandHandler.onSocketClose();
            } catch (IOException ex) {
                System.err.println("Error reading data from client: \"" + ex.getMessage() + "\"");
            }         
        }        
    }
}