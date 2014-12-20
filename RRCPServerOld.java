
import edu.wpi.first.wpilibj.Timer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

/**
 * @deprecated 
 * @author Alex
 */
public class RRCPServer implements Runnable {
    
    private static boolean listening = true;
    private static int port = 548;
    private static double timeout = 3;//Changed from 5 to 3
    private static Thread mainThread;
    private static RRCPServer instance;
    private static ServerSocketConnection server;
    private static Vector commandlist;
    private static RRCPCommand closeSocketCommand;

    public static RRCPServer getInstance() {
        if (instance == null) {
            instance = new RRCPServer();
        }
        return instance;
    }
    
    private RRCPServer() {
        commandlist = new Vector();
        mainThread = new Thread(this);
    }
    
    public static void startServer(int port, int timeout) {
        RRCPServer.port = port;
        RRCPServer.timeout = timeout;
        listening = true;
        mainThread.start();
    }
    
    public static void startServer() {
        mainThread.start();
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
        private double lastHeartBeat;
        public RRCPConnectionHandler(SocketConnection s) {
            this.s = s;
            try {
                dis = new DataInputStream(new BufferedInputStream(s.openInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(this.s.openOutputStream()));
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
            } catch (IOException ex) {
                System.err.println("Error closing ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
        }
        
        private void protocol() {
            try {
                this.lastHeartBeat = Timer.getFPGATimestamp();
                while(Timer.getFPGATimestamp() <  this.lastHeartBeat+timeout && RRCPServer.listening) {
                    while (dis.available() > 0) {
                        byte id = dis.readByte();
                        if (id == 21) {
                            dos.write((byte) 21);
                            dos.flush();
                            this.lastHeartBeat = Timer.getFPGATimestamp();
                            System.out.println("Heartbeat Received");
                            break;
                        } else if (id == 0) {
                            this.close();
                            break;
                        } else if (id == 1) {
                            execute(dis.readByte(), dis.readUTF(), Byte.valueOf(dis.readByte()));
                        } else if (id == 2) {
                            execute(dis.readByte(),dis.readUTF(), Integer.valueOf(dis.readInt()));
                        } else if (id == 3) {
                            execute(dis.readByte(),dis.readUTF(), Boolean.valueOf(dis.readBoolean()));
                        } else if (id == 4) {
                            execute(dis.readByte(),dis.readUTF(), Double.valueOf(dis.readDouble()));
                        } else if (id == 5) {
                            execute(dis.readByte(),dis.readUTF(), dis.readUTF());
                        } else if (id == 6) {
                            execute(dis.readByte(),dis.readUTF(), this.readDoubleArray());
                        } else if (id == 7) {
                            execute(dis.readByte(),dis.readUTF(), this.readByteArray());
                        } else if (id == 8) {
                            execute(dis.readByte(), dis.readUTF(), null);
                        } else {
                            System.err.println("Error reading packet: " + id + " is not a know ID!");
                        }
                    }
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ex) {
                        System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                    }
                }
                System.err.println("Client timed out!!!");
            } catch (IOException ex) {
                System.err.println("Error reading data from client: \"" + ex.getMessage() + "\"");
            }    
            RRCPServer.onSocketClose();
        }
         private void execute(final byte address, final String s, final Object o) {
            new Thread(new Runnable() {
                public void run() {
                    RRCPServer.executeCommand(s, dos, address, o);
                }
            }).start();
        }
         
        private double[] readDoubleArray() {
            try {
                int length = dis.readInt();
                double[] d = new double[length];
                for (int i = 0; i < length; i++) {
                    d[i] = dis.readDouble();
                }
                return d;
            } catch (IOException ex) {
                System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
            }
            return new double[0];
        }
        
        private byte[] readByteArray() {
            try {
                int length = dis.readInt();
                byte[] b = new byte[length];
                for (int i = 0; i < length; i++) {
                    b[i] = dis.readByte();
                }
                return b;
            } catch (IOException ex) {
                System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
            }
            return new byte[0];
        }
    }
    
    protected static void addCommand(RRCPCommand rrcpcommand) {
        if(rrcpcommand.getName().equals("SOCKETCLOSED")) closeSocketCommand = rrcpcommand; 
        else {
            commandlist.addElement(rrcpcommand);
            commandlist.trimToSize();
        }
    }
    
    private static void executeCommand(String s, DataOutputStream dos, byte address, Object data) {
        for(int i = 0; i < commandlist.size(); i++) {
            RRCPCommand rrcpcommand = (RRCPCommand) commandlist.elementAt(i);
            if(rrcpcommand.getName().equals(s)) { 
                rrcpcommand.serverExecute(dos, data, address);
                return;
            }
        }
        System.err.println("Command not recognized: \"" + s + "\"\nError incoming!!!");
    }
    
     private static void onSocketClose() {
        if(closeSocketCommand !=  null) closeSocketCommand.execute(null, null);
    }
}