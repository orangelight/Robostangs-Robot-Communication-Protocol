
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Alex
 */
public class RRCPServer {
    private static int port = 5800; //Port server is on. Should be 1180 for comp.
    private static int timeout = 750; //Timeout for clients heartbeats
    private static RRCPServer instance; //Instance of server
    private static ServerSocket server; //Socket that listens for incoming connections
    private static ArrayList<RRCPCommand> commandlist; //List where commands are stored
    private static RRCPCommand closeSocketCommand; //Command that is ran when client is dissconncted
    private static int delay = 5;
    private static RRCPConnectionListener clientListener;

    /**
     * Creates new instance of server if their is none
     *
     * @return instance of server
     */
    public static RRCPServer getInstance() {
        if (instance == null) {
            instance = new RRCPServer();
        }
        return instance;
    }

    private RRCPServer() {
        commandlist = new ArrayList<>();
        connectionList = new LinkedList<>();
        clientListener = new RRCPConnectionListener();
    }

    /**
     * Starts server for listening for incoming client connections
     *
     * @param port What port the server is ran on. Use 5800 for competitions
     * @param timeout Timeout for client heartbeat in milliseconds. Don't use
     * number less then 1000
     */
    public static void startServer(int port, int timeout) {
        RRCPServer.port = port;
        RRCPServer.timeout = timeout;
        clientListener.startListener();
    }

    /**
     * Starts server for listening for incoming clients connections Uses default
     * port of 548. Use 5800 in competitions Uses default timeout of 2000ms
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
            server.close();
            connectionList.clear();
        } catch (IOException ex) {
            System.err.println("Error closing server: \"" + ex.getMessage() + "\"");
        }
        System.out.println("SERVER SHUT DOWN");
    }

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
                    System.out.println("Client Connected");
                    RRCPConnectionHandler ch = new RRCPConnectionHandler(s);
                    new Thread(ch).start();
                    connectionList.add(ch);
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
                s.close();
            } catch (IOException ex) {
                System.err.println("Error closing ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
        }

        private void protocol() {
            try {
                this.lastHeartBeat = System.currentTimeMillis();
                while (System.currentTimeMillis() < this.lastHeartBeat + timeout && RRCPServer.clientListener.listening) {
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
                        } else if (id == 9) {
                            execute(dis.readByte(), dis.readUTF(), dis.readLong());
                        } else if (id == 10) {
                            execute(dis.readByte(), dis.readUTF(), dis.readShort());
                        } else if (id == 11) {
                            execute(dis.readByte(), dis.readUTF(), dis.readFloat());
                        } else if (id == 12) {
                            execute(dis.readByte(), dis.readUTF(), this.readIntegerArray());
                        } else if (id == 13) {
                            execute(dis.readByte(), dis.readUTF(), this.readLongArray());
                        } else if (id == 14) {
                            execute(dis.readByte(), dis.readUTF(), this.readShortArray());
                        } else if (id == 15) {
                            execute(dis.readByte(), dis.readUTF(), this.readFloatArray());
                        } else {
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
                System.err.println("Error reading data from client: \"" + ex.getMessage() + "\"");
            }
            connectionList.remove(this);
            RRCPServer.onSocketClose();
        }

        private void execute(final byte address, final String s, final Object o) {
            new Thread(new Runnable() {
                @Override
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
        
        private int[] readIntegerArray() {
            try {
                int length = dis.readInt();
                int[] i = new int[length];
                for (int x = 0; x < length; x++) {
                    i[x] = dis.readInt();
                }
                return i;
            } catch (IOException ex) {
                System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
            }
            return new int[0];
        }
        
        private long[] readLongArray() {
            try {
                int length = dis.readInt();
                long[] l = new long[length];
                for (int i = 0; i < length; i++) {
                    l[i] = dis.readLong();
                }
                return l;
            } catch (IOException ex) {
                System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
            }
            return new long[0];
        }
        
        private short[] readShortArray() {
            try {
                int length = dis.readInt();
                short[] s = new short[length];
                for (int i = 0; i < length; i++) {
                    s[i] = dis.readShort();
                }
                return s;
            } catch (IOException ex) {
                System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
            }
            return new short[0];
        }
        
        private float[] readFloatArray() {
            try {
                int length = dis.readInt();
                float[] f = new float[length];
                for (int i = 0; i < length; i++) {
                    f[i] = dis.readFloat();
                }
                return f;
            } catch (IOException ex) {
                System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
            }
            return new float[0];
        }

        /*
         * 1.1
         */
        private void sendCommand(String command) {
            try {
                dos.writeByte(30);
                dos.writeUTF(command);
                dos.flush();
            } catch (IOException ex) {
                System.err.println("Error sending command to Client: \"" + ex.getMessage() + "\"");
            }
        }
        
        private void sendCommandWithDouble(String command, double d) {
            try {
                dos.writeByte(31);
                dos.writeUTF(command);
                dos.writeDouble(d);
                dos.flush();
            } catch (IOException ex) {
                System.err.println("Error sending command to Client: \"" + ex.getMessage() + "\"");
            }
        }
        
        private void sendCommandWithDoubleArray(String command, double d[]) {
            try {
                dos.writeByte(32);
                dos.writeUTF(command);
                 dos.writeInt(d.length);
                for (int i = 0; i < d.length; i++) {
                    dos.writeDouble(d[i]);
                }
                dos.flush();
            } catch (IOException ex) {
                System.err.println("Error sending command to Client: \"" + ex.getMessage() + "\"");
            }
        }
    }

    protected static void addCommand(RRCPCommand rrcpcommand) {
        if (rrcpcommand.getName().equals("SOCKETCLOSED")) {
            closeSocketCommand = rrcpcommand;
        } else {
            commandlist.add(rrcpcommand);
            commandlist.trimToSize();
        }
    }

    private static void executeCommand(String s, DataOutputStream dos, byte address, Object data) {
        for (RRCPCommand rrcpcommand : commandlist) {
            if (rrcpcommand.getName().equals(s)) {
                rrcpcommand.serverExecute(dos, data, address);
                return;
            }
        }
        System.err.println("Command not recognized: \"" + s + "\"\nError incoming!!!");
    }

    private static void onSocketClose() {
        if (closeSocketCommand != null) {
            closeSocketCommand.execute(null);
        }
    }
    /*
     * 1.1
     */
    private static LinkedList<RRCPConnectionHandler> connectionList;

    public static void sendCommand(String command) {
        for (RRCPConnectionHandler rrcpCon : connectionList) {
            rrcpCon.sendCommand(command);
        }
    }
    
    public static void sendCommandWithDouble(String command, double d) {
        for (RRCPConnectionHandler rrcpCon : connectionList) {
            rrcpCon.sendCommandWithDouble(command, d);
        }
    }
    
    public static void sendCommandWithDoubleArray(String command, double d[]) {
        for (RRCPConnectionHandler rrcpCon : connectionList) {
            rrcpCon.sendCommandWithDoubleArray(command, d);
        }
    }

    private static int getOpenIndex() {
        if (connectionList.isEmpty()) {
            return 0;
        } else {
            for (int i = 0; i < connectionList.size(); ++i) {
                if (connectionList.get(i) == null) {
                    return i;
                }
            }
            return connectionList.size();
        }
    }
}