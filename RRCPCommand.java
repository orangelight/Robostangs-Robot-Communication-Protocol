
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Alex
 */
public abstract class RRCPCommand {
    private String name;
    private byte currentAddress;
    DataOutputStream dos;
    public RRCPCommand(String n) {
        this.name = n;
        RRCPComputerTestServer.getInstance();
        RRCPComputerTestServer.addCommand(this);
    }
    protected synchronized void serverExecute(DataOutputStream dos, Object data, byte address) {
        currentAddress = address;
        this.dos = dos;
        execute(data);
    }
    protected abstract void execute(Object data);
    
    public String getName() {
        return name;
    }
    
    protected void sendByte(byte b) {
        try {
            dos.writeByte(1);
            dos.write(currentAddress);
            dos.writeByte(b);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    protected void sendInt(int i) {
        try {
            dos.writeByte(2);
            dos.write(currentAddress);
            dos.writeInt(i);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    protected void sendBoolean(boolean b) {
        try {
            dos.writeByte(3);
            dos.write(currentAddress);
            dos.writeBoolean(b);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    protected void sendDouble(double d) {
        try {
            dos.writeByte(4);
            dos.write(currentAddress);
            dos.writeDouble(d);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    protected void sendString(String s) {
        try {
            dos.writeByte(5);
            dos.write(currentAddress);
            dos.writeUTF(s);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }
    
    protected void sendDoubleArray(double d[]) {
        try {
            int length = d.length;
            dos.write(6);
            dos.write(currentAddress);
            dos.writeInt(length);
            for (int i = 0; i < d.length; i++) {
                dos.writeDouble(d[i]);
            }
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
    }
    
    protected void sendByteArray(byte b[]) {
        try {
            int length = b.length;
            dos.write(7);
            dos.write(currentAddress);
            dos.writeInt(length);
            for (int i = 0; i < b.length; i++) {
                dos.write(b[i]);
            }
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
    }
}