
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class RRCPClient {

    Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String host;
    private int port;
    private int timeout = 20000;
    private boolean connected = false;
    private Thread heartBeatThread;
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
    
    public byte readByte() {
        try {
            return dis.readByte();
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
        }
        return -1;
    }
    public boolean readBoolean() {
        try {
            return dis.readBoolean();
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
        return false;
    }
    public int readInt() {
        try {
            return dis.readInt();
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
        return -1;
    }
    public double readDouble() {
        try {
            return dis.readDouble();
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
        return -1;
    }
    public String readString() {
        try {
            return dis.readUTF();
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage()+"\"");
            this.close();
        }
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
    
    private class HeartBeatThread implements Runnable {

        @Override
        public void run() {
            while(isConnected()) {
                sendHeartBeat();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    System.err.println("Error sleeping: \"" + ex.getMessage()+"\"");
                }
            }
        }
        
    }
}