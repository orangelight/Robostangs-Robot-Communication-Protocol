
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class RRCPClient {

    private Socket socket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private String hostIP;
    private int hostPort;
    private int packetTimeOut;
    private volatile boolean connected = false;
    private boolean connecting = false;
    private Thread heartBeatThread;
    private PacketHandler packetHandler;
    private int heartBeatDelay = 0;
    private volatile byte currentAddress = -1;
    private final int TIMEOUT_NUM = 5;
    private boolean autoReconnect = true;

    private static enum PacketTypes {

        Byte((byte) 1),
        Integer((byte) 2),
        Double((byte) 3),
        Long((byte) 4),
        Short((byte) 5),
        Float((byte) 6),
        Command((byte) 7),
        String((byte) 8),
        Boolean((byte) 9),
        HeartBeat((byte) 21),
        ClientCommand((byte) 30),
        ClientCommandDouble((byte) 31),
        ClientCommandDoubleArray((byte) 32),
        DoubleArray((byte) 10),
        ByteArray((byte) 11),
        IntegerArray((byte) 12),
        LongArray((byte) 13),
        ShortArray((byte) 14),
        FloatArray((byte) 15);

        private final byte id;

        private PacketTypes(byte b) {
            this.id = b;
        }

        public byte getID() {
            return id;
        }
    }

    public RRCPClient(String host, int port, int timeout, boolean autoRe) {
        this.hostIP = host;
        this.hostPort = port;
        this.packetTimeOut = timeout / TIMEOUT_NUM;
        this.autoReconnect = autoRe;
    }

    public synchronized void connect() {
        if (isConnected()) {

        } else if (isConnecting()) {

        } else {
            try {
                connecting = true;
                socket = new Socket(hostIP, hostPort);
                dataInput = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dataOutput = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                packetHandler = new PacketHandler();
                heartBeatThread = new Thread(new HeartBeatThread());
                connected = true;
                heartBeatThread.start();
            } catch (IOException ex) {
                Logger.getLogger(RRCPClient.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                connecting = false;
            }

        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public byte getCurrentAddress() {
        return currentAddress;
    }

    private synchronized byte addCurrentAddress() {
        if (currentAddress == 100) {
            currentAddress = -1; //if last packet sent had address of 50 change currentAdress back to -1 to be changed to 0
        }
        packetHandler.packetQueue[++currentAddress] = null;
        return getCurrentAddress();
    }

    public byte sendCommand(String command) {
        byte address;
        if (isConnected()) {
            try {
                address = addCurrentAddress();
                dataOutput.write(PacketTypes.Command.getID());
                addAddressCommand(address, command);
                dataOutput.flush();
                return address;
            } catch (IOException ex) {

            }
        } else {

        }
        return -2;
    }

    private void addAddressCommand(byte address, String command) throws IOException {
        dataOutput.write(address);
        dataOutput.writeUTF(command);
    }

    public byte sendCommandWithNumber(String command, Number n) {
        if (isConnected()) {
            try {
                byte address = addCurrentAddress();
                if (n instanceof Integer) {
                    dataOutput.write(PacketTypes.Integer.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeInt((int) n);
                    dataOutput.flush();
                    return address;
                } else if (n instanceof Double) {
                    dataOutput.write(PacketTypes.Double.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeDouble((double) n);
                    dataOutput.flush();
                    return address;
                } else if (n instanceof Short) {
                    dataOutput.write(PacketTypes.Short.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeDouble((short) n);
                    dataOutput.flush();
                    return address;
                } else if (n instanceof Byte) {
                    dataOutput.write(PacketTypes.Byte.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeDouble((byte) n);
                    dataOutput.flush();
                    return address;
                } else if (n instanceof Long) {
                    dataOutput.write(PacketTypes.Long.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeDouble((long) n);
                    dataOutput.flush();
                    return address;
                } else if (n instanceof Float) {
                    dataOutput.write(PacketTypes.Float.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeDouble((float) n);
                    dataOutput.flush();
                    return address;
                } else {
                    System.err.println("Really... I don't what to support that number type...");
                    return -2;
                }
            } catch (IOException ex) {
                return -2;
            }
        } else {
            return -2;
        }
    }

    public byte sendCommandWithNumberArray(String command, Object array) {
        if (isConnected()) {
            try {
                byte address = addCurrentAddress();
                if (array instanceof int[]) {
                    dataOutput.write(PacketTypes.IntegerArray.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeInt(((int[]) array).length);
                    for (int i = 0; i < ((int[]) array).length; i++) {
                        dataOutput.writeInt(((int[]) array)[i]);
                    }
                    dataOutput.flush();
                    return address;
                } else if (array instanceof double[]) {
                    dataOutput.write(PacketTypes.DoubleArray.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeInt(((double[]) array).length);
                    for (int i = 0; i < ((double[]) array).length; i++) {
                        dataOutput.writeDouble(((double[]) array)[i]);
                    }
                    dataOutput.flush();
                    return address;
                } else if (array instanceof short[]) {
                    dataOutput.write(PacketTypes.ShortArray.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeInt(((short[]) array).length);
                    for (int i = 0; i < ((short[]) array).length; i++) {
                        dataOutput.writeShort(((short[]) array)[i]);
                    }
                    dataOutput.flush();
                    return address;
                } else if (array instanceof byte[]) {
                    dataOutput.write(PacketTypes.ByteArray.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeInt(((byte[]) array).length);
                    for (int i = 0; i < ((byte[]) array).length; i++) {
                        dataOutput.write(((byte[]) array)[i]);
                    }
                    dataOutput.flush();
                    return address;
                } else if (array instanceof long[]) {
                    dataOutput.write(PacketTypes.LongArray.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeInt(((long[]) array).length);
                    for (int i = 0; i < ((long[]) array).length; i++) {
                        dataOutput.writeLong(((long[]) array)[i]);
                    }
                    dataOutput.flush();
                    return address;
                } else if (array instanceof float[]) {
                    dataOutput.write(PacketTypes.FloatArray.getID());
                    addAddressCommand(address, command);
                    dataOutput.writeInt(((float[]) array).length);
                    for (int i = 0; i < ((float[]) array).length; i++) {
                        dataOutput.writeFloat(((float[]) array)[i]);
                    }
                    dataOutput.flush();
                    return address;
                } else {
                    System.err.println("This doesn't support that number array");
                    return -2;
                }
            } catch (IOException ex) {
                return -2;
            }
        } else {
            return -2;
        }
    }

    public byte sendCommandWithBoolean(String command, boolean b) {
        byte address;
        if (isConnected()) {
            try {
                address = addCurrentAddress();
                dataOutput.write(PacketTypes.Boolean.getID());
                addAddressCommand(address, command);
                dataOutput.writeBoolean(b);
                dataOutput.flush();
                return address;
            } catch (IOException ex) {

            }
        } else {

        }
        return -2;
    }

    public byte sendCommandWithString(String command, String s) {
        byte address;
        if (isConnected()) {
            try {
                address = addCurrentAddress();
                dataOutput.write(PacketTypes.String.getID());
                addAddressCommand(address, command);
                dataOutput.writeUTF(s);
                dataOutput.flush();
                return address;
            } catch (IOException ex) {

            }
        } else {

        }
        return -2;
    }

    private class PacketHandler implements Runnable {
        
        private Packet[] packetQueue;
        private Packet beatQueue;
        private Thread mainThread;
        
        public PacketHandler() {
            this.packetQueue = new Packet[101]; //Makes packet queue
            this.beatQueue = null;
            this.mainThread = new Thread(this);
            this.mainThread.start();
        }
        
        public void run() {
            while (isConnected()) {
                try {
                    while (dataInput.available() > 0) { //Reads packet when it comes in from input stream
                        new Packet((byte)readNumber(PacketTypes.Byte.getID())); //Makes new packet of id read from byte
                    }
                } catch (IOException ex) {
                    
                }
                try {
                    Thread.sleep(TIMEOUT_NUM);
                } catch (InterruptedException ex) {
                    
                }
            }
        }
        
         private Packet getHeartBeat() {
            int i = 0;
            while (beatQueue == null) {
                if (i > packetTimeOut) {
                    System.err.println("SERVER DID NOT RESPOND!!!");
                    return new Packet((byte) 100); //Returns error packet
                }
                try {
                    Thread.sleep(TIMEOUT_NUM);
                } catch (InterruptedException ex) {
                    
                }
                ++i;
            }
            setHeartBeatDelay(i);
            Packet p = beatQueue;
            resetBeatQueue();
            return p;
        }

        private Packet getPacket(byte address) {
            int i = 0;
            while (packetQueue[address] == null) {
                ++i;
                if (i > packetTimeOut + 10) {
                    System.err.println("Could not find packet! Packet lost!");
                    return null;
                }
                try {
                    Thread.sleep(TIMEOUT_NUM);
                } catch (InterruptedException ex) {

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
    
    public Object readNumberPacket(byte address) {
        if(isConnected()) {
            Packet isNull = packetHandler.getPacket(address);
            if(isNull == null) {
                return null;
            }
            return isNull.getData();
        }
        return null;
    }
    
    private class Packet {

        private byte id;
        public Object data;
        private byte address;

        public Packet(byte id) {
            this.id = id;
            if (id == PacketTypes.HeartBeat.getID()) { //Determains what type of packet it is from id
                packetHandler.addPacketToBeatQueue(this);
            } else if (id <= PacketTypes.Float.getID()) { //Numbers
                address = (byte)readNumber(PacketTypes.Byte.getID());
                data = readNumber(id);
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.Boolean.getID()) {
                address = (byte)readNumber(PacketTypes.Byte.getID());
                data = readBoolean();
                packetHandler.addPacketToQueue(this);
            } else if (id == PacketTypes.String.getID()) {
                address = (byte)readNumber(PacketTypes.Byte.getID());
                data = readString();
                packetHandler.addPacketToQueue(this);
            } else if (id >= PacketTypes.DoubleArray.getID() && id <= PacketTypes.FloatArray.getID()) {
                address = (byte)readNumber(PacketTypes.Byte.getID());
                data = readNumberArray(id);
                packetHandler.addPacketToQueue(this);
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
    
    private Number readNumber(byte id) {
        try {
            if (id == PacketTypes.Byte.getID()|| id == PacketTypes.ByteArray.getID()) return dataInput.readByte();
            else if (id == PacketTypes.Integer.getID() || id == PacketTypes.IntegerArray.getID()) return dataInput.readInt();
            else if (id == PacketTypes.Double.getID() || id == PacketTypes.DoubleArray.getID()) return dataInput.readDouble();
            else if (id == PacketTypes.Long.getID() || id == PacketTypes.LongArray.getID()) return dataInput.readLong();
            else if (id == PacketTypes.Short.getID() || id == PacketTypes.ShortArray.getID()) return dataInput.readShort();
            else if (id == PacketTypes.Float.getID() || id == PacketTypes.FloatArray.getID()) return dataInput.readFloat();
            else return -1;
        } catch (IOException ex) {
            
        }
        return -1;
    }
    
    private Object readNumberArray(byte id) {
        int length = (int)readNumber(PacketTypes.Integer.getID());
        Number[] n = new Number[length];
        for (int i = 0; i < length; i++) {
            n[i] = readNumber(id);
        }
        return n;
    }
    
    private boolean readBoolean() {
        try {
            return dataInput.readBoolean();
        } catch (IOException ex) {
            
        }
        return false; 
    }
    
    private String readString() {
        try {
            return dataInput.readUTF();
        } catch (IOException ex) {
            
        }
        return null;
    }

    private void close() {
        try {
            this.connected = false;
            if (socket != null) {
                this.heartBeatThread.interrupt();
                this.socket.close();
            }
        } catch (IOException ex) {
            
        } finally {
            if(autoReconnect) {
                this.connect();
            }
        }
    }
    
    public void disconnect() {
        if(autoReconnect) {
            this.autoReconnect = false;
            this.close();
            this.autoReconnect = true;
        } else this.close();
    }
    //Heartbeat stuff

    private class HeartBeatThread implements Runnable {

        @Override
        public void run() {
            while (isConnected() && !Thread.currentThread().isInterrupted()) {
                if (sendHeartBeat()) {
                    Thread.sleep(250);
                } else {
                    Thread.currentThread().interrupt();
                    close();
                    break;
                }
            }
        }

        private boolean sendHeartBeat() {
            this.sendHeartBeatCommand();
            if (packetHandler.getHeartBeat().getID() == 21) {
                connected = true;
                return true;
            } else {
                return false;
            }
        }

        private void sendHeartBeatCommand() {
            if (isConnected()) {
                try {
                    dataOutput.write(PacketTypes.HeartBeat.getID());
                    dataOutput.flush();
                } catch (IOException ex) {

                }
            } else {

            }
        }
    }

    private void setHeartBeatDelay(int i) {
        this.heartBeatDelay = i;
    }

    public int getDelay() {
        return TIMEOUT_NUM * this.heartBeatDelay;
    }
}