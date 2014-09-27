
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Alex Robostangs, Team 0548
 * @version 1.1
 */
public class RRCPClient {

    private Socket socket; //Socket used to connect to server
    private DataInputStream dis; //InputStream to get data from server
    private DataOutputStream dos; //OutputStream to send data to server
    private String host; //The IP of server
    private int port; //Port of server. sould be 1180 if use in comp.
    private int timeout; //Timeout for getting data from server. timeout in ms equals timeout*TIMEOUT_NUM
    private boolean connected = false; //true if conncted to server false if not connected
    private boolean connecting = false; //true of client is on proccese of connecting to server
    private Thread heartBeatThread; //Thread used to send a heartbeat to server every second
    private PacketHandler packetHandler; //Used to read all data from server and manage it in to packets from client to read
    private int heartBeatDelay = 0; //The time it takes for server to respond to heartbeat and for us to read it. Delay in ms = heartBeatDela*TIMEOUT_NUM
    private byte currentAddress = -1; //Used by cleint to set address to exbounding packets
    private final int TIMEOUT_NUM = 25; //Used to convert from timeout time to ms

    /**
     * Stores packet id's for reading and sending packets
     */
    private static enum PacketTypes {

        Command((byte) 8), Byte((byte) 1), Integer((byte) 2), Boolean((byte) 3), Double((byte) 4), String((byte) 5), DoubleArray((byte) 6), ByteArray((byte) 7), HeartBeat((byte) 21), ClientCommand((byte) 9);
        private byte id;

        private PacketTypes(byte b) {
            this.id = b;
        }

        public byte getID() {
            return id;
        }
    }

    /**
     * Sets the robot server IP Sets port to default port (548) Port must be set
     * to 1180 for competitions
     *
     * @param host Server IP
     * @param timeout timeout in milliseconds
     */
    public RRCPClient(String host, int timeout) {
        this(host, timeout, 548);
    }

    /**
     * Sets IP to default server IP (10.5.48.2) Sets port to default port (548)
     * Port must be set to 1180 for competitions
     *
     * @param timeout Set the timeout in milliseconds
     */
    public RRCPClient(int timeout) {
        this("10.5.48.2", timeout, 548);
    }

    /**
     * Sets the robot server IP and port Port must be set to 1180 for
     * competitions
     *
     * @param host Server IP
     * @param timeout Timeout in milliseconds
     * @param port Server Port
     */
    public RRCPClient(String host, int timeout, int port) {
        this.host = host;
        this.port = port;
        this.timeout = timeout / TIMEOUT_NUM;
    }

    /**
     * Tries to connect to robot server with server host and port, also starts
     * client
     */
    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connecting = true;
                    socket = new Socket(host, port);
                    dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    connected = true;
                    packetHandler = new PacketHandler(); //Starts teh packet handler
                    heartBeatThread = new Thread(new HeartBeatThread());
                    heartBeatThread.start(); //Starts heatbeats
                } catch (IOException ex) {
                    System.err.println("Error Connecting to Robot Server: \"" + ex.getMessage() + "\"");
                    close();
                } finally {
                    connecting = false;
                }

            }
        }).start();
    }

    /**
     * Tells whether client is connected to robot server
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Tells whether client is connecting to robot server
     *
     * @return true if connecting
     */
    public boolean isConnecting() {
        return connecting;
    }

    /**
     * Gets the last address assigned to packet send to server The values can
     * range from 0 to 50
     *
     * @return address of packet last sent
     */
    public byte getCurrentAddress() {
        return currentAddress;
    }

    private synchronized byte addCurrentAdress() {
        if (currentAddress == 50) {
            currentAddress = -1; //if last packet sent had address of 50 change currentAdress back to -1 to be changed to 0
        }
        packetHandler.packetQueue[++currentAddress] = null;
        return getCurrentAddress();
    }

    /**
     * Sends command to server if connect
     *
     * @param command Name of command sending to server
     * @return The address used to read returning data from server
     */
    public byte sendCommand(String command) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(PacketTypes.Command.getID());
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

    /**
     * Sends command with byte data to server if connected
     *
     * @param command Name of command sending to server
     * @param b value of byte data sent with the command
     * @return The address used to read returning data from server
     */
    public byte sendCommandWithByte(String command, byte b) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(PacketTypes.Byte.getID());
                dos.write(address);
                dos.writeUTF(command);
                dos.writeByte(b);
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

    /**
     *
     * Sends command with double data to server if connected
     *
     * @param command Name of command sending to server
     * @param d value of double data sent with the command
     * @return The address used to read returning data from server
     */
    public byte sendCommandWithDouble(String command, double d) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(PacketTypes.Double.getID());
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

    /**
     * Sends command with int data to server if connected
     *
     * @param command Name of command sending to server
     * @param i value of int data sent with command
     * @return The address used to read returning data from server
     */
    public byte sendCommandWithInt(String command, int i) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(PacketTypes.Integer.getID());
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

    /**
     * Sends command with boolean data to server if connected
     *
     * @param command Name of command sending to server
     * @param b value of boolean data sent with command
     * @return The address used to read returning data from server
     */
    public byte sendCommandWithBoolean(String command, boolean b) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(PacketTypes.Boolean.getID());
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

    /**
     * Sends command with String data to server if connected
     *
     * @param command Name of command sending to server
     * @param s value of String data sent with command
     * @return The address used to read returning data from server
     */
    public byte sendCommandWithString(String command, String s) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(PacketTypes.String.getID());
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

    /**
     * Sends command with double array data to server if connected
     *
     * @param command Name of command sending to server
     * @param d value of double array sent with command
     * @return The address used to read returning data from server
     */
    public byte sendCommandWithDoubleArray(String command, double d[]) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(PacketTypes.DoubleArray.getID());
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

    /**
     * Sends command with byte array data to server if connected
     *
     * @param command Name of command sending to server
     * @param b value of byte array sent with command
     * @return The address used to read returning data from server
     */
    public byte sendCommandWithByteArray(String command, byte b[]) {
        byte address = -2;
        if (isConnected()) {
            try {
                address = addCurrentAdress();
                dos.write(PacketTypes.ByteArray.getID());
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
                dos.write(PacketTypes.HeartBeat.getID()); //Sends byte with value of 21
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
        if (packetHandler.getHeartBeat().getID() == 21) { //Checks if packet has correct id
            this.connected = true;
        } else {
            this.close();
        }
    }

    private class HeartBeatThread implements Runnable {

        @Override
        public void run() {
            while (isConnected()) {
                sendHeartBeat(); //Sends heartbeat command every second
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
        }
    }

    private void setHeatBeatDelay(int i) {
        this.heartBeatDelay = i;
    }

    /**
     * Gets the heartbeat delay this is the amount of time it takes for the
     * server to respond and the client to read it
     *
     * @return Server delay in milliseconds
     */
    public int getDelay() {
        return TIMEOUT_NUM * this.heartBeatDelay;
    }

    /**
     * Reads byte from server with address
     *
     * @param address address used to identify packet for reading
     * @return The byte data read from server
     */
    public byte readBytePacket(byte address) {
        if (isConnected()) {
            Packet isNull = packetHandler.getPacket(address);
            if (isNull == null) {
                return -1;
            }
            return (Byte) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return -1;
    }

    /**
     * Reads boolean from server with address
     *
     * @param address address used to identify packet for reading
     * @return The boolean data read from server
     */
    public boolean readBooleanPacket(byte address) {
        if (isConnected()) {
            Packet isNull = packetHandler.getPacket(address);
            if (isNull == null) {
                return false;
            }
            return ((Byte) isNull.getData() == 1) ? true : false; //lol, not really reading a boolean... just byte values
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return false;
    }

    /**
     * Reads int from server with address
     *
     * @param address address used to identify packet for reading
     * @return The int data read from server
     */
    public int readIntPacket(byte address) {
        if (isConnected()) {
            Packet isNull = packetHandler.getPacket(address);
            if (isNull == null) {
                return -1;
            }
            return (Integer) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return -1;
    }

    /**
     * Reads double from server with address
     *
     * @param address address used to identify packet for reading
     * @return The double data read from server
     */
    public double readDoublePacket(byte address) {
        if (isConnected()) {
            Packet isNull = packetHandler.getPacket(address);
            if (isNull == null) {
                return -1.0;
            }
            return (Double) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return -1.0;
    }

    /**
     * Reads String from server with address
     *
     * @param address address used to identify packet for reading
     * @return The String data read from server
     */
    public String readStringPacket(byte address) {
        if (isConnected()) {
            Packet isNull = packetHandler.getPacket(address);
            if (isNull == null) {
                return "";
            }
            return (String) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return "";
    }

    /**
     * Reads double array from server with address
     *
     * @param address address used to identify packet for reading
     * @return The double array data read from server
     */
    public double[] readDoubleArrayPacket(byte address) {
        if (isConnected()) {
            Packet isNull = packetHandler.getPacket(address);
            if (isNull == null) {
                return new double[0];
            }
            return (double[]) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return new double[0];
    }

    /**
     * Reads byte array from server with address
     *
     * @param address address used to identify packet for reading
     * @return The byte array data read from server
     */
    public byte[] readByteArrayPacket(byte address) {
        if (isConnected()) {
            Packet isNull = packetHandler.getPacket(address);
            if (isNull == null) {
                return new byte[0];
            }
            return (byte[]) isNull.getData();
        }
        System.err.println("MUST BE CONNECTED TO ROBOT TO READ DATA!!!");
        return new byte[0];
    }

    /**
     * Closes client socket from server, which makes isConnected false. Also
     * called when their is an error sending data to server
     */
    public void close() {
        try {
            this.connected = false;
            if (socket != null) {
                this.dos.close();
                this.dis.close();
                this.socket.close();
            }
        } catch (IOException ex) {
            System.err.println("Error closing socket: \"" + ex.getMessage() + "\"");
        }
    }

    private class PacketHandler implements Runnable {

        private Packet[] packetQueue; //Where packets are stored with their address
        private Packet beatQueue; //Where heartbeat packet is stored
        private Thread mainThread; //Main thread for packetHandler

        public PacketHandler() {
            this.packetQueue = new Packet[51]; //Makes packet queue
            this.beatQueue = null;
            this.mainThread = new Thread(this);
            this.mainThread.start();
        }

        public void run() {
            while (isConnected()) {
                try {
                    while (dis.available() > 0) { //Reads packet when it comes in from input stream
                        new Packet(readByte()); //Makes new packet of id read from byte          
                    }
                } catch (IOException ex) {
                    System.err.println("Error reading packet ID from robot server: \"" + ex.getMessage() + "\"");
                }
                try {
                    Thread.sleep(TIMEOUT_NUM);
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
                    return new Packet((byte) 100); //Returns error packet
                }
                try {
                    Thread.sleep(TIMEOUT_NUM);
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
                    Thread.sleep(TIMEOUT_NUM);
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
            if (id == PacketTypes.HeartBeat.getID()) { //Determains what type of packet it is from id
                packetHandler.addPacketToBeatQueue(this);
            } else if (id == PacketTypes.Byte.getID()) {
                address = readByte();
                data = readByte();
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.Integer.getID()) {
                address = readByte();
                data = readInt();
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.Boolean.getID()) {
                address = readByte();
                data = readByte();
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.Double.getID()) {
                address = readByte();
                data = readDouble();
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.String.getID()) {
                address = readByte();
                data = readString();
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.DoubleArray.getID()) {
                address = readByte();
                data = readDoubleArray();
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.ByteArray.getID()) {
                address = readByte();
                data = readByteArray();
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.ClientCommand.getID()) {
                executeCommand(readString());
            } else if (id == 100) { //Error packet
            } else {
                data = null;
                System.err.println("Packet not reconized!!!"); //Unknow packet id
            }
        }

        public Object getData() {
            return data;
        }

        public byte getID() {
            return id;
        }
    }
    //The rest is used to read data from inputstream

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
    /*
     * 1.1
     */
    private ArrayList<RRCPClientCommand> commands;

    private void executeCommand(final String key) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (RRCPClientCommand command : commands) {
                    if (command.getKey().equals(key)) {
                        command.execute();
                        return;
                    }
                }
                System.err.println("Error reciving command: " + key + " command not reconized");
            }
        }).start();
    }

    public void addCommand(RRCPClientCommand command) {
        commands.add(command);
    }

    public String[] getCommandKeyList() {
        String[] s = new String[commands.size()];
        for (int i = 0; i < commands.size(); ++i) {
            s[i] = commands.get(i).getKey();
        }
        return s;
    }
}