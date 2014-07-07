
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
        RRCPComputerTestCommandHandler.getInstance();
        RRCPComputerTestCommandHandler.addCommand(this);
    }
    
    public abstract void exacute(DataInputStream dis, DataOutputStream dos);
    
    public String getName() {
        return name;
    }
    

    public byte readByte(DataInputStream dis) {
        try {
            byte b = dis.readByte();
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    public static boolean readBoolean(DataInputStream dis) {
        try {
            boolean b = dis.readBoolean();
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return false;
    }

    public static int readInt(DataInputStream dis) {
        try {
            int i = dis.readInt();
            return i;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    public static double readDouble(DataInputStream dis) {
        try {
            double d = dis.readDouble();
            return d;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    public static String readString(DataInputStream dis) {
        try {
            String s = dis.readUTF();
            return s;
        } catch (IOException ex) {
            System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
        }
        return "";
    }

    public static void sendByte(byte b, DataOutputStream dos) {
        try {
            dos.writeByte(b);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    public static void sendInt(int i, DataOutputStream dos) {
        try {
            dos.writeInt(i);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    public static void sendBoolean(boolean b, DataOutputStream dos) {
        try {
            dos.writeBoolean(b);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    public static void sendDouble(double d, DataOutputStream dos) {
        try {
            dos.writeDouble(d);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }

    public static void sendString(String s, DataOutputStream dos) {
        try {
            dos.writeUTF(s);
            dos.flush();
        } catch (IOException ex) {
            System.err.println("Error sending data to Client: \"" + ex.getMessage() + "\"");
        }
    }
}
