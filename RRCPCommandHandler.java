
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class RRCPCommandHandler {

    private static RRCPCommandHandler instance;

    public static RRCPCommandHandler getInstance() {
        if (instance == null) {
            instance = new RRCPCommandHandler();
        }
        return instance;
    }
    /**
     * This class and method is what you edit to make commands
     *
     * @param s Command
     * @param dis DataInputStream
     * @param dos DataOutputStream
     */
    static int i = 0;

    public static void executeCommand(String s, DataInputStream dis, DataOutputStream dos) {
        switch (s) {
            case "EXAMPLE COMMAND":
                System.out.println(++i);
                break;
            case "EXAMPLE COMMAND THAT SENDS DATA BACK":
                sendBoolean(true, dos);
                break;
            case "EXAMPLE COMMAND THAT SENDS DATA BACK1":
                sendDouble(0.99, dos);
                break;
            case "EXAMPLE COMMAND THAT READS ARRAY OF DOUBLES":
                System.out.println(readCommandWithDoubleArray(dis)[0]);
                break;
            case "ECHO":
                sendString(readString(dis), dos);
                break;
            default:
                System.err.println("Command not recognized: \"" + s + "\"\nError incoming!!!");
                break;
        }
    }
    //Crap for senteding a getting data

    private static double[] readCommandWithDoubleArray(DataInputStream dis) {
        try {
            int length = dis.readInt();
            double[] d = new double[length];
            for (int i = 0; i < length; i++) {
                d[i] = dis.readDouble();
            }
            return d;
        } catch (IOException ex) {
            Logger.getLogger(RRCPCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public byte readByte(DataInputStream dis) {
        try {
            byte b = dis.readByte();
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    public static boolean readBoolean(DataInputStream dis) {
        try {
            boolean b = dis.readBoolean();
            return b;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
        }
        return false;
    }

    public static int readInt(DataInputStream dis) {
        try {
            int i = dis.readInt();
            return i;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    public static double readDouble(DataInputStream dis) {
        try {
            double d = dis.readDouble();
            return d;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
        }
        return -1;
    }

    public static String readString(DataInputStream dis) {
        try {
            String s = dis.readUTF();
            return s;
        } catch (IOException ex) {
            System.err.println("Error reading data from Robot Server: \"" + ex.getMessage() + "\"");
        }
        return "";
    }

    public static void sendByte(byte b, DataOutputStream dos) {
        try {
            dos.writeByte(b);
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(RRCPCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sendInt(int i, DataOutputStream dos) {
        try {
            dos.writeInt(i);
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(RRCPCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sendBoolean(boolean b, DataOutputStream dos) {
        try {
            dos.writeBoolean(b);
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(RRCPCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sendDouble(double d, DataOutputStream dos) {
        try {
            dos.writeDouble(d);
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(RRCPCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sendString(String s, DataOutputStream dos) {
        try {
            dos.writeUTF(s);
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(RRCPCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}