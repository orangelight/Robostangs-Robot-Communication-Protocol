
import java.io.DataInputStream;
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
        RRCPCommandHandler.getInstance();
        RRCPCommandHandler.addCommand(this);
    }
    
    public abstract void exacute(DataInputStream dis, DataOutputStream dos);
    
    public String getName() {
        return name;
    }
    

    protected byte readByte(DataInputStream dis) {
        try {
            byte b = dis.readByte();
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    protected boolean readBoolean(DataInputStream dis) {
        try {
            boolean b = dis.readBoolean();
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return false;
    }

    protected int readInt(DataInputStream dis) {
        try {
            int i = dis.readInt();
            return i;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    protected double readDouble(DataInputStream dis) {
        try {
            double d = dis.readDouble();
            return d;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    protected String readString(DataInputStream dis) {
        try {
            String s = dis.readUTF();
            return s;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return "";
    }
    
    protected static double[] readDoubleArray(DataInputStream dis) {
        try {
            int length = dis.readInt();
            double[] d = new double[length];
            for (int i = 0; i < length; i++) {
                d[i] = dis.readDouble();
            }
            return d;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return null;
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
    
}
