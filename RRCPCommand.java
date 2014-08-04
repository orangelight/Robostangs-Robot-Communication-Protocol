
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Alex
 */
public abstract class RRCPCommand {
    private String name;
    public RRCPCommand(String n) {
        this.name = n;
        RRCPComputerTestServer.getInstance();
        RRCPComputerTestServer.addCommand(this);
    }
    
    public abstract void exacute(DataOutputStream dos, Object data);
    
    public String getName() {
        return name;
    }
    
    protected void sendByte(byte b, DataOutputStream dos) {
        try {
            dos.writeByte(1);
            dos.writeByte(b);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    protected void sendInt(int i, DataOutputStream dos) {
        try {
            dos.writeByte(2);
            dos.writeInt(i);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    protected void sendBoolean(boolean b, DataOutputStream dos) {
        try {
            dos.writeByte(3);
            dos.writeBoolean(b);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    protected void sendDouble(double d, DataOutputStream dos) {
        try {
            dos.writeByte(4);
            dos.writeDouble(d);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    protected void sendString(String s, DataOutputStream dos) {
        try {
            dos.writeByte(5);
            dos.writeUTF(s);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }
    
    protected void sendDoubleArray(double d[], DataOutputStream dos) {
        try {
            int length = d.length;
            dos.write(6);
            dos.writeInt(length);
            for (int i = 0; i < d.length; i++) {
                dos.writeDouble(d[i]);
            }
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
    }
    
    protected void sendByteArray(byte b[], DataOutputStream dos) {
        try {
            int length = b.length;
            dos.write(7);
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