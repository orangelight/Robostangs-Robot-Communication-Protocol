
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

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
    private int timeout = 20;
    private boolean connected = false;
    private Thread heartBeatThread;
    private PacketHandler ph;

    /**
     * Sets the robot server IP Sets port to default port (548)
     *
     * @param host Server IP
     */
    public RRCPClient(String host, int timeout) {
        this.host = host;
        this.port = 548;
        this.timeout = timeout;
    }

    /**
     * Sets IP to default server IP (10.5.48.2) Sets port to default port (548)
     */
    public RRCPClient(int timeout) {
        host = "10.5.48.2";
        port = 548;
        this.timeout = timeout;
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
        this.timeout = timeout;
    }

    /**
     * Tries to connect to robot server with server host and port
     */
    public void connect() {
        try {
            s = new Socket(host, port);
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
        return (byte) ph.getPacket().getData();
    }

    public boolean readBooleanPacket() {
        return ((byte)ph.getPacket().getData() == 1) ? true : false;
    }

    public int readIntPacket() {
        return (int) ph.getPacket().getData();
    }

    public double readDoublePacket() {
        return (double) ph.getPacket().getData();
    }

    public String readStringPacket() {
        return (String) ph.getPacket().getData();
    }

    public double[] readDoubleArrayPacket() {
        return (double[]) ph.getPacket().getData();
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
                    System.err.println("Error reading packet ID from robot server: \"" + ex.getMessage() + "\"");
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
        }

        Packet getHeartBeat() {
            int i = 0;
            while (beatQueue.size() == 0) {
                ++i;
                if (i > timeout) {
                    System.err.println("SERVER DID NOT RESPOND!!!");
                    return new Packet((byte) 100);
                }
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
            int i = 0;
            while (packetQueue.size() == 0) {
                ++i;
                if (i > timeout + 15) {
                    System.err.println("Could not find packet! Packet lost!");
                    return null;
                }
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
            public Object data;

            public Packet(byte id) {
                this.id = id;
                if (id == 21) {
                    beatQueue.addFirst(this);
                } else if (id == 1) {
                    data = readByte();
                    packetQueue.addFirst(this);
                } else if (id == 2) {
                    data = readInt();
                    packetQueue.addFirst(this);
                } else if (id == 3) {
                    data = readByte();
                    packetQueue.addFirst(this);
                } else if (id == 4) {
                    data = readDouble();
                    packetQueue.addFirst(this);
                } else if (id == 5) {
                    data = readString();
                    packetQueue.addFirst(this);
                } else if (id == 6) {
                    data = readDoubleArray();
                    packetQueue.addFirst(this);
                } else if (id == 100) {
                } else {
                    data = null;
                    System.err.println("Packet not reconized!!!");
                }
            }

            public Object getData() {
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

        private double[] readDoubleArray() {
            try {
                int length = dis.readInt();
                double[] d = new double[length];
                for (int i = 0; i < length; i++) {
                    d[i] = dis.readDouble();
                }
                return d;
            } catch (IOException ex) {
                System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
            }
            return null;
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