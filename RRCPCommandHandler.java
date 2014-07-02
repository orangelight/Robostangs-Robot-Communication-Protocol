
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
                try {
                    dos.writeBoolean(true);
                    dos.flush();
                } catch (IOException ex) {
                    Logger.getLogger(RRCPCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "EXAMPLE COMMAND THAT READS ARRAY OF DOUBLES":
                try {
                    readCommandWithDoubleArray(dis);
                } catch (IOException ex) {
                    Logger.getLogger(RRCPCommandHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            default:
                System.err.println("Command not recognized: \"" + s + "\"");
                break;
        }
    }

    private static double[] readCommandWithDoubleArray(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        double[] d = new double[length];
        for (int i = 0; i < length; i++) {
            d[i] = dis.readDouble();
        }
        return d;

    }
}
