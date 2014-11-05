
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Alex
 */
public class RRCPComputerTestServer implements Runnable {

    private static boolean listening = true; //true if listening for clients
    private static int port = 548; //Port server is on. Should be 1180 for comp.
    private static int timeout = 2000; //Timeout for clients heartbeats
    private static Thread mainThread; //Main thread server runs on
    private static RRCPComputerTestServer instance; //Instance of server
    private static ServerSocket server; //Socket that listens for incoming connections
    private static ArrayList<RRCPCommand> commandlist; //List where commands are stored
    private static RRCPCommand closeSocketCommand; //Command that is ran when client is dissconncted
    private static int delay = 5;
    
    /**
     * Creates new instance of server if their is none
     * @return instance of server
     */
    public static RRCPComputerTestServer getInstance() {
        if (instance == null) {
            instance = new RRCPComputerTestServer();
        }
        return instance;
    }

    private RRCPComputerTestServer() {
        commandlist = new ArrayList<>();
        connectionList = new ArrayList<>();
        mainThread = new Thread(this);
    }
    /**
     * Starts server for listening for incoming client connections 
     * @param port What port the server is ran on. Use 1180 for competitions 
     * @param timeout Timeout for client heartbeat in milliseconds. Don't use number less then 1000
     */
    public static void startServer(int port, int timeout) {
        RRCPComputerTestServer.port = port;
        RRCPComputerTestServer.timeout = timeout;
        listening = true;
        mainThread.start();
    }
    /**
     * Starts server for listening for incoming clients connections
     * Uses default port of 548. Use 1180 in competitions
     * Uses default timeout of 2000ms
     */
    public static void startServer() {
        listening = true;
        mainThread.start();
    }
    /**
     * Stops server and disconnects all clients
     */
    public static void stopServer() {
        try {
            listening = false;
            server.close();
            connectionList.clear();
        } catch (IOException ex) {
            System.err.println("Error closing server: \"" + ex.getMessage() + "\"");
        }
        System.out.println("SERVER SHUT DOWN");
    }
    /**
     * DO NOT USE!!!!!
     */
    public void run() {
        try {
            server = new ServerSocket(this.port);
            while (listening) {
                Socket s = server.accept();
                System.out.println("Client Connected");
                int index = getOpenIndex();
                RRCPConnectionHandler ch = new RRCPConnectionHandler(s, index);
                Thread tch = new Thread(ch);
                tch.start();
                connectionList.add(index, ch);
            }
        } catch (IOException ex) {
            System.err.println("Error making client socket: \"" + ex.getMessage() + "\"");
        }
    }

    private class RRCPConnectionHandler implements Runnable {

        private Socket s; //Socket server uses to comunicate with client
        private DataInputStream dis;
        private DataOutputStream dos;
        private long lastHeartBeat; //Last heartbeat in system time
        int connectionListIndex;

        public RRCPConnectionHandler(Socket s, int i) {
            this.s = s;
            this.connectionListIndex = i;
            try {
                dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream()));
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
                this.lastHeartBeat = System.currentTimeMillis();
                while (System.currentTimeMillis() < this.lastHeartBeat + timeout && RRCPComputerTestServer.listening) {
                    while (dis.available() > 0) {
                        byte id = dis.readByte();
                        this.lastHeartBeat = System.currentTimeMillis();
                        if (id == 21) {
                            dos.write((byte) 21);
                            dos.flush();
                            this.lastHeartBeat = System.currentTimeMillis();
                            System.out.println("Heartbeat Received");
                            break;
                        } else if (id == 0) {
                            this.close();
                            break;
                        } else if (id == 1) {
                            execute(dis.readByte(), dis.readUTF(), dis.readByte());
                        } else if (id == 2) {
                            execute(dis.readByte(), dis.readUTF(), dis.readInt());
                        } else if (id == 3) {
                            execute(dis.readByte(), dis.readUTF(), dis.readBoolean());
                        } else if (id == 4) {
                            execute(dis.readByte(), dis.readUTF(), dis.readDouble());
                        } else if (id == 5) {
                            execute(dis.readByte(), dis.readUTF(), dis.readUTF());
                        } else if (id == 6) {
                            execute(dis.readByte(), dis.readUTF(), this.readDoubleArray());
                        } else if (id == 7) {
                            execute(dis.readByte(), dis.readUTF(), this.readByteArray());
                        } else if (id == 8) {
                            execute(dis.readByte(), dis.readUTF(), null);
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
                System.err.println("Error reading data from client: \"" + ex.getMessage() + "\"");
            }
            connectionList.remove(connectionListIndex);
            RRCPComputerTestServer.onSocketClose();
        }
        
        private void execute(final byte address, final String s, final Object o) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RRCPComputerTestServer.executeCommand(s, dos, address, o);
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
        
        /*
         * 1.1
         */
        private void sendCommand(String command) {
            try {
                dos.writeByte(9);
                dos.writeUTF(command);
                dos.flush();
            } catch (IOException ex) {
                System.err.println("Error sending command to Client: \"" + ex.getMessage() + "\"");
            }
        }
    }
    
    protected static void addCommand(RRCPCommand rrcpcommand) {
        if(rrcpcommand.getName().equals("SOCKETCLOSED")) closeSocketCommand = rrcpcommand; 
        else {
            commandlist.add(rrcpcommand);
            commandlist.trimToSize();
        }
    }
  
    private static void executeCommand(String s, DataOutputStream dos, byte address,  Object data) {
        for(RRCPCommand rrcpcommand : commandlist) {
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
    
    /*
     * 1.1
     */
    private static ArrayList<RRCPConnectionHandler> connectionList;
    
    public static void sendCommand(String command) {
        for(RRCPConnectionHandler rrcpCon : connectionList) {
            rrcpCon.sendCommand(command);
        }
    }
    
    private static int getOpenIndex() {
        if(connectionList.isEmpty()) return 0;
        else {
            for(int i = 0; i < connectionList.size(); ++i) {
                if(connectionList.get(i) == null) return i;
            }
            return connectionList.size();
        }
    }
}