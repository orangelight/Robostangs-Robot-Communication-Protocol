
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class RRCPClient {

    private Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String host;
    private int port;
    private int timeout = 20000;
    private boolean connected = false;
    private Thread heartBeatThread;
    private static PacketHandler ph;

    /**
     * Sets the robot server IP Sets port to default port (548)
     *
     * @param host Server IP
     */
    public RRCPClient(String host, int timeout) {
        this.host = host;
        port = 548;
    }

    /**
     * Sets IP to default server IP (10.5.48.2) Sets port to default port (548)
     */
    public RRCPClient(int timeout) {
        host = "10.5.48.2";
        port = 548;
    }

    /**
     * Sets the robot server IP and port
     *
     * @param host Server IP
     * @param port Server Port
     */
    public RRCPClient(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
    }

    /**
     * Tries to connect to robot server with server host and port
     */
    public void connect() {
        try {
            s = new Socket(host, port);
            s.setSoTimeout(timeout);
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
            connected = true;
            ph = new PacketHandler();
            heartBeatThread = new Thread(new HeartBeatThread());
            heartBeatThread.start();

        } catch (IOException ex) {
            System.err.println("Error Connecting to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }

    /**
     * Tells whether client is connected to robot server
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }

    public synchronized void sendCommand(String command) {
        try {
            dos.writeUTF(command);
            dos.flush();
            
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    public void sendCommandWithDouble(String command, double d) {
        try {
            dos.writeUTF(command);
            dos.writeDouble(d);
            dos.flush();
            
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    
    public void sendCommandWithInt(String command, int i) {
        try {
            dos.writeUTF(command);
            dos.writeInt(i);
            dos.flush();
            
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    public void sendCommandWithBoolean(String command, boolean b) {
        try {
            dos.writeUTF(command);
            dos.writeBoolean(b);
            dos.flush();
            
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    
    public void sendCommandWithString(String command, String s) {
        try {
            dos.writeUTF(command);
            dos.writeUTF(s);
            dos.flush();
            
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    
    public void sendCommandWithDoubleArray(String command, double d[]) {
        try {
            dos.writeUTF(command);
            dos.writeInt(d.length);
            for (int i = 0; i < d.length; i++) {
                dos.writeDouble(d[i]);
            }
            dos.flush();
            
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    
    public void close() {
        try {
            this.connected = false;
            dos.close();
            dis.close();
            s.close();
        } catch (IOException ex) {
            System.err.println("Error closing socket: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    
    private void sendHeartBeat() {
        this.sendCommand("HEARTBEAT");
        if (ph.getHeartBeat().getID() == 21) {
            this.connected = true;
        } else {
            this.close();
        }
    }
    
    public byte readBytePacket() {
        return ph.getPacket().getData()[0];
    }
    
    public boolean readBooleanPacket() {
        return (ph.getPacket().getData()[0] == 0) ? false : true;
    }
    public int readIntPacket() {
        return new BigInteger(ph.getPacket().getData()).intValue();
    }
    
    public double readDoublePacket() {
        return ByteBuffer.wrap(ph.getPacket().getData()).getDouble();
    }
    
    public String readStringPacket() {
        return new String(ph.getPacket().getData());
    }
    
    public class PacketHandler implements Runnable {
        private LinkedList<Packet> packetQueue;
        private LinkedList<Packet> beatQueue;
        private Thread t;
        public PacketHandler() {
            packetQueue = new LinkedList<Packet>();
            beatQueue = new LinkedList<Packet>();
            t = new Thread(this);
            t.start();
        }
        public void run() {
            while (isConnected()) {
                try {
                    while (dis.available() > 0) {
                        new Packet(readByte());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RRCPClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
        }
        Packet getHeartBeat() {
            while (beatQueue.size() == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
            Packet p = beatQueue.getFirst();
            beatQueue.removeFirst();
            return p;
        }
        Packet getPacket() {
            while (packetQueue.size() == 0) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
            Packet p = packetQueue.getFirst();
            packetQueue.removeFirst();
            return p;
        }
        public class Packet {
            private byte id;
            public byte[] data;
            public Packet(byte id) {
                this.id = id;
                if (id == 21) {
                    beatQueue.addFirst(this);
                } else if (id == 1) {
                    data = new byte[1];
                    data[0] = readByte();
                    packetQueue.addFirst(this);
                } else if (id == 2) {
                    data = ByteBuffer.allocate(4).putInt(readInt()).array();
                    packetQueue.addFirst(this);
                } else if (id == 3) {
                    data = new byte[1];
                    data[0] = readByte();
                    packetQueue.addFirst(this);
                } else if (id == 4) {
                    data = ByteBuffer.allocate(8).putDouble(readDouble()).array();
                    packetQueue.addFirst(this);
                } else if (id == 5) {
                    data = readString().getBytes();
                    packetQueue.addFirst(this);
                } else {
                    data = null;
                    System.err.println("Packet not reconized!!!");
                }
            }
            public byte[] getData() {
                return data;
            }
            public byte getID() {
                return id;
            }
        }
        private byte readByte() {
            try {
                byte b = dis.readByte();
                
                return b;
            } catch (IOException ex) {
                System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
            }
            return -1;
        }
        private int readInt() {
            try {
                int i = dis.readInt();
                
                return i;
            } catch (IOException ex) {
                System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
                close();
            }
            return -1;
        }
        private double readDouble() {
            
            try {
                double d = dis.readDouble();
                
                return d;
            } catch (IOException ex) {
                System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
                close();
            }
            return -1.0;
        }
        private String readString() {
            try {
                String s = dis.readUTF();
                
                return s;
            } catch (IOException ex) {
                System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
                close();
            }
            return "";
        }
    }
    private class HeartBeatThread implements Runnable {
        public void run() {
            while (isConnected()) {
                sendHeartBeat();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
        }
    }
}