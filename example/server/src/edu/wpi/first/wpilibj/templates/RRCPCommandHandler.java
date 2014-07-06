
package edu.wpi.first.wpilibj.templates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


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
     * @param s Command
     * @param dis DataInputStream
     * @param dos DataOutputStream
     */
    public static void executeCommand(String s, DataInputStream dis, DataOutputStream dos) {
        if(s.equals("EXAMPLE COMMAND")) {
            System.out.println("TEST COMMAND");
        } else if(s.equals("ECHO")) {
            sendString(readString(dis), dos);
        } if(s.equals("FORWARD")) {
            RobotTemplate.setMotors(1, 1);
        } else if(s.equals("BACKWARD")) {
            RobotTemplate.setMotors(-1, -1);
        } else if(s.equals("LEFT")) {
            RobotTemplate.setMotors(-1, 1);
        } else if(s.equals("RIGHT")) {
            RobotTemplate.setMotors(1, -1);
        } else if(s.equals("STOP")) {
            RobotTemplate.setMotors(0, 0);
        } else {
            System.err.println("Command not recognized: \"" + s + "\"\nError incoming!!!");
        }
    }
    
    public static void onSocketClose() {
        RobotTemplate.setMotors(0, 0);
    }
    /**
     * Crap for sending & getting data from client
     * Don't delete anything under here!!!
    **/
    private static double[] readCommandWithDoubleArray(DataInputStream dis) {
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
