import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class RRCPClient {

    Socket s;
    DataInputStream dis;
    private DataOutputStream dos;
    private String host;
    private int port;
    private int timeout = 20000;
    private boolean connected = false;
    private Thread heartBeatThread;
    private boolean heartBeatLock = false;
    /**
     * Sets the robot server IP
     * Sets port to default port (548)
     * @param host Server IP
     */
    public RRCPClient(String host, int timeout) {
        this.host = host;
        port = 548;
    }
    /**
     * Sets IP to default server IP (10.5.48.2)
     * Sets port to default port (548)
     */
    public RRCPClient(int timeout) {
        host = "10.5.48.2";
        port = 548;
    }
    /**
     * Sets the robot server IP and port
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
            System.err.println("Error Connecting to Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
    }

    /**
     * Tells whether client is connected to robot server
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }
    /**
     * Locks or Unlocks heartbeat thread so it doesent mess stuff up
     * @param b set to true or false
     */
    public void setHeartBeatLock(boolean b) {
        this.heartBeatLock = b;
    }
    
    public byte readByte() {
        this.setHeartBeatLock(true);
        try {
            byte b = dis.readByte();
            this.setHeartBeatLock(false);
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
        }
        this.setHeartBeatLock(false);
        return -1;
    }
    public boolean readBoolean() {
        this.setHeartBeatLock(true);
        try {
            boolean b = dis.readBoolean();
            this.setHeartBeatLock(false);
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
        this.setHeartBeatLock(false);
        return false;
    }
    public int readInt() {
        this.setHeartBeatLock(true);
        try {
            int i = dis.readInt();
            this.setHeartBeatLock(false);
            return i;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
        this.setHeartBeatLock(false);
        return -1;
    }
    public double readDouble() {
        this.setHeartBeatLock(true);
        try {
            double d = dis.readDouble();
            this.setHeartBeatLock(false);
            return d;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
        this.setHeartBeatLock(false);
        return -1;
    }
    public String readString() {
        this.setHeartBeatLock(true);
        try {
            String s = dis.readUTF();
            this.setHeartBeatLock(false);
            return s;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
        this.setHeartBeatLock(false);
        return "";
    }
    public void sendCommand(String command) {
        try {
            dos.writeUTF(command);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
    }
    public void sendCommandWithDouble(String command, double d) {
        try {
            dos.writeUTF(command);
            dos.writeDouble(d);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
    }
    public void sendCommandWithInt(String command, int i) {
        try {
            dos.writeUTF(command);
            dos.writeInt(i);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
    }
    public void sendCommandWithBoolean(String command, boolean b) {
        try {
            dos.writeUTF(command);
            dos.writeBoolean(b);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
    }
    public void sendCommandWithDoubleArray(String command, double d[]) {
        heartBeatLock = true;
        try {
            dos.writeUTF(command);
            dos.writeInt(d.length);
            for(int i = 0; i < d.length; i++) {
                dos.writeDouble(d[i]);
            }
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
        heartBeatLock = false;
    }
    public void close() {
        try {
            this.connected = false;
            dos.close();
            dis.close();
            s.close();
        } catch (IOException ex) {
            System.err.println("Error closing socket: \"" + ex.getMessage()+"\"");
            this.close();
        }
    }
    public void sendHeartBeat() {
        try {
            this.sendCommand("HEARTBEAT");
            if(dis.readByte() == 21) this.connected = true;
            else { 
                this.close(); 
            }
        } catch (IOException ex) {
            Logger.getLogger(RRCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public InputStream getInputStream() throws IOException {
        return s.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException {
        return s.getOutputStream();
    }
    
    private class HeartBeatThread implements Runnable {

        @Override
        public void run() {
            while(isConnected()) {
                if(!heartBeatLock) {
                    sendHeartBeat();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage()+"\"");
                }
            }
        }
        
    }
}