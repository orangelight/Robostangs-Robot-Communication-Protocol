
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

        Command((byte) 8),
        Byte((byte) 1),
        Integer((byte) 2),
        Boolean((byte) 3),
        Double((byte) 4),
        String((byte) 5),
        DoubleArray((byte) 6),
        ByteArray((byte) 7),
        HeartBeat((byte) 21),
        ClientCommand((byte) 30),
        ClientCommandDouble((byte) 31),
        ClientCommandDoubleArray((byte) 32),
        Long((byte) 9),
        Short((byte) 10),
        Float((byte) 11),
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
                heartBeatThread.start();
                connected = true;
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

        @Override
        public void run() {

        }

    }

    private class HeartBeatThread implements Runnable {

        @Override
        public void run() {

        }

    }
}