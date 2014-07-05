
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
    private boolean heartBeatLock = false;

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

    private void setHeartBeatLock(boolean b) {
        this.heartBeatLock = b;
    }

    public synchronized byte readByte() {
        setHeartBeatLock(true);
        try {
            byte b = dis.readByte();
            setHeartBeatLock(false);
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    public boolean readBoolean() {
        setHeartBeatLock(true);
        try {
            boolean b = dis.readBoolean();
            setHeartBeatLock(false);
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }

        return false;
    }

    public int readInt() {
        setHeartBeatLock(true);
        try {
            int i = dis.readInt();
            setHeartBeatLock(false);
            return i;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }

        return -1;
    }

    public double readDouble() {
        setHeartBeatLock(true);
        try {
            double d = dis.readDouble();
            setHeartBeatLock(false);
            return d;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
        return -1.0;
    }

    public String readString() {
        setHeartBeatLock(true);
        try {
            String s = dis.readUTF();
            setHeartBeatLock(false);
            return s;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }

        return "";
    }

    public synchronized void sendCommand(String command) {
        setHeartBeatLock(true);
        try {
            dos.writeUTF(command);
            dos.flush();
            setHeartBeatLock(false);
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }

    public void sendCommandWithDouble(String command, double d) {
        setHeartBeatLock(true);
        try {
            dos.writeUTF(command);
            dos.writeDouble(d);
            dos.flush();
            setHeartBeatLock(false);
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }

    public void sendCommandWithInt(String command, int i) {
        setHeartBeatLock(true);
        try {
            dos.writeUTF(command);
            dos.writeInt(i);
            dos.flush();
            setHeartBeatLock(false);
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }

    public void sendCommandWithBoolean(String command, boolean b) {
        setHeartBeatLock(true);
        try {
            dos.writeUTF(command);
            dos.writeBoolean(b);
            dos.flush();
            setHeartBeatLock(false);
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    
    public void sendCommandWithString(String command, String s) {
        setHeartBeatLock(true);
        try {
            dos.writeUTF(command);
            dos.writeUTF(s);
            dos.flush();
            setHeartBeatLock(false);
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage() + "\"");
            this.close();
        }
    }
    
    public void sendCommandWithDoubleArray(String command, double d[]) {
        setHeartBeatLock(true);
        try {
            dos.writeUTF(command);
            dos.writeInt(d.length);
            for (int i = 0; i < d.length; i++) {
                dos.writeDouble(d[i]);
            }
            dos.flush();
            setHeartBeatLock(false);
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

    public void sendHeartBeat() {
        this.sendCommand("HEARTBEAT");
        if (this.readByte() == 21) {
            this.connected = true;
        } else {
            this.close();
        }
    }

    private class HeartBeatThread implements Runnable {
        public void run() {
            while (isConnected()) {
                if (!heartBeatLock) {
                    sendHeartBeat();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                }
            }
        }
    }
}