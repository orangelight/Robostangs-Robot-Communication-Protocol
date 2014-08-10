
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RRCPClient {

    private Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String host;
    private int port;
    private int timeout;
    private boolean connected = false;
    private boolean connecting = false;
    private Thread heartBeatThread;
    private PacketHandler ph;
    private int heartBeatDelay = 0;
    private byte currentAddress = -1;
    private final int TIMEOUTNUM = 25;

    /**
     * Sets the robot server IP Sets port to default port (548)
     *
     * @param host Server IP
     * @param timeout timeout in milliseconds
     */
    public RRCPClient(String host, int timeout) {
        this(host, timeout, 548);    
    }

    /**
     * Sets IP to default server IP (10.5.48.2) Sets port to default port (548)
     * @param timeout Set the timeout in milliseconds
     */
    public RRCPClient(int timeout) {
        this("10.5.48.2", timeout, 548);        
    }

    /**
     * Sets the robot server IP and port
     *
     * @param host Server IP
     * @param port Server Port
     */
    public RRCPClient(String host, int timeout, int port) {
        this.host = host;
        this.port = port;
        this.timeout = timeout/TIMEOUTNUM;
    }

    /**
     * Tries to connect to robot server with server host and port, also starts
     * client
     */
    public void connect() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connecting = true;
                    s = new Socket(host, port);
                    dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                    connected = true;
                    ph = new PacketHandler();
                    heartBeatThread = new Thread(new HeartBeatThread());
                    heartBeatThread.start();
                } catch (IOException ex) {
                    System.err.println("Error Connecting to Robot Server: \"" + ex.getMessage() + "\"");
                    close();
                } finally {
                    connecting = false;
                }

            }
        });
        t.start();
    }

    /**
     * Tells whether client is connected to robot server
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }

    public boolean isConnecting() {
        return connecting;
    }
    
    public byte getCurrentAddress() {
        return currentAddress;
    }
    
    private synchronized byte addCurrentAdress() {
        if(currentAddress == 50) currentAddress = -1;
        ph.packetQueue[++currentAddress] = null;
        return getCurrentAddress();
    }

    public byte sendCommand(String command) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(8);
                dos.write(address);
                dos.writeUTF(command);
                dos.flush();
                return address;
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
        return address;
    }

    public byte sendCommandWithByte(String command, byte d) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(1);
                dos.write(address);
                dos.writeUTF(command);
                dos.writeByte(d);
                dos.flush();
                return address;
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
        return address;
    }

    public byte sendCommandWithDouble(String command, double d) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(4);
                dos.write(address);
                dos.writeUTF(command);
                dos.writeDouble(d);
                dos.flush();
                return address;
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
        return address;
    }

    public byte sendCommandWithInt(String command, int i) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(2);
                dos.write(address);
                dos.writeUTF(command);
                dos.writeInt(i);
                dos.flush();
                return address;
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
        return address;
    }

    public byte sendCommandWithBoolean(String command, boolean b) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(3);
                dos.write(address);
                dos.writeUTF(command);
                dos.writeBoolean(b);
                dos.flush();
                return address;
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
        return address;
    }

    public byte sendCommandWithString(String command, String s) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(5);
                dos.write(address);
                dos.writeUTF(command);
                dos.writeUTF(s);
                dos.flush();
                return address;
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
        return address;
    }

    public byte sendCommandWithDoubleArray(String command, double d[]) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(6);
                dos.write(address);
                dos.writeUTF(command);
                dos.writeInt(d.length);
                for (int i = 0; i < d.length; i++) {
                    dos.writeDouble(d[i]);
                }
                dos.flush();
                return address;
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
        return address;
    }
    
    public byte sendCommandWithByteArray(String command, byte b[]) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(7);
                dos.write(address);
                dos.writeUTF(command);
                dos.writeInt(b.length);
                for (int i = 0; i < b.length; i++) {
                    dos.writeByte(b[i]);
                }
                dos.flush();
                return address;
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
        return address;
    }

    private void sendHeatBeatCommand() {
        if (isConnected()) {
            try {
                dos.write(21);
                dos.flush();
            } catch (IOException ex) {
                System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
                this.close();
            }
        } else {
            System.err.println("MUST BE CONNECTED TO ROBOT TO SEND COMMANDS!!!");
        }
    }

    private void sendHeartBeat() {
        this.sendHeatBeatCommand();
        if (ph.getHeartBeat().getID() == 21) {
            this.connected = true;
        } else {
            this.close();
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
    
    private void setHeatBeatDelay(int i) {
        this.heartBeatDelay = i;
    }
    
    public int getDelay() {
        return TIMEOUTNUM*this.heartBeatDelay;
    }
    public byte readBytePacket(byte address) {
        if (isConnected()) {
            Packet isNull = ph.getPacket(address);
            if (isNull == null) {
                return -1;
            }
            return (Byte) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return -1;
    }

    public boolean readBooleanPacket(byte address) {
        if (isConnected()) {
            Packet isNull = ph.getPacket(address);
            if (isNull == null) {
                return false;
            }
            return ((Byte) isNull.getData() == 1) ? true : false;
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return false;
    }

    public int readIntPacket(byte address) {
        if (isConnected()) {
            Packet isNull = ph.getPacket(address);
            if (isNull == null) {
                return -1;
            }
            return (Integer) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return -1;
    }

    public double readDoublePacket(byte address) {
        if (isConnected()) {
            Packet isNull = ph.getPacket(address);
            if (isNull == null) {
                return -1.0;
            }
            return (Double) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return -1.0;
    }

    public String readStringPacket(byte address) {
        if (isConnected()) {         
            Packet isNull = ph.getPacket(address);
            if (isNull == null) {
                return "";
            }
            return (String) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return "";
    }

    public double[] readDoubleArrayPacket(byte address) {
        if (isConnected()) {
            Packet isNull = ph.getPacket(address);
            if (isNull == null) {
                return new double[0];
            }
            return (double[]) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return new double[0];
    }
    
    public byte[] readByteArrayPacket(byte address) {
        if (isConnected()) {
            Packet isNull = ph.getPacket(address);
            if (isNull == null) {
                return new byte[0];
            }
            return (byte[]) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return new byte[0];
    }

    public void close() {
        try {
            this.connected = false;
            if (s != null) {
                this.dos.close();
                this.dis.close();
                this.s.close();
            }
        } catch (IOException ex) {
            System.err.println("Error closing socket: \"" + ex.getMessage() + "\"");
        }
    }

    private class PacketHandler implements Runnable {

        private Packet[] packetQueue;
        private Packet beatQueue;
        private Thread mainThread;

        public PacketHandler() {
            this.packetQueue = new Packet[51];
            this.beatQueue = null;
            this.mainThread = new Thread(this);
            this.mainThread.start();
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
                    Thread.sleep(TIMEOUTNUM);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
        }

        private Packet getHeartBeat() {
            int i = 0;
            while (beatQueue == null) {
                if (i > timeout) {
                    System.err.println("SERVER DID NOT RESPOND!!!");
                    return new Packet((byte) 100);
                }
                try {
                    Thread.sleep(TIMEOUTNUM);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
                ++i;
            }
            setHeatBeatDelay(i);
            Packet p = beatQueue;
            resetBeatQueue();
            return p;
        }
        
        private Packet getPacket(byte address) {
            int i = 0;
            while (packetQueue[address] == null) {
                ++i;
                if (i > timeout + 10) {
                    System.err.println("Could not find packet! Packet lost!");
                    return null;
                }
                try {
                    Thread.sleep(TIMEOUTNUM);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
            Packet p = packetQueue[address];
            packetQueue[address] = null;
            return p;
        }

        public void addPacketToBeatQueue(Packet p) {
            beatQueue = p;
        }

        public void addPacketToQueue(Packet p) {
            packetQueue[p.address] = p;
        }
        
        public void resetBeatQueue() {
            beatQueue = null;
        }
    }

    private class Packet {

        private byte id;
        public Object data;
        private byte address;
        public Packet(byte id) {
            this.id = id;
            if (id == 21) {
                ph.addPacketToBeatQueue(this);
            } else if (id == 1) {
                address = readByte();
                data = readByte();
                ph.addPacketToQueue(this);
            } else if (id == 2) {
                address = readByte();
                data = readInt();
                ph.addPacketToQueue(this);
            } else if (id == 3) {
                address = readByte();
                data = readByte();
                ph.addPacketToQueue(this);
            } else if (id == 4) {
                address = readByte();
                data = readDouble();
                ph.addPacketToQueue(this);
            } else if (id == 5) {
                address = readByte();
                data = readString();
                ph.addPacketToQueue(this);
            } else if (id == 6) {
                address = readByte();
                data = readDoubleArray();
                ph.addPacketToQueue(this);
            } else if (id == 7) {
                address = readByte();
                data = readByteArray();
                ph.addPacketToQueue(this);
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
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
        }
        return new byte[0];
    }
}