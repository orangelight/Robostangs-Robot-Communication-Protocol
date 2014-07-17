
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;


/**
 *
 * @author Alex
 */
public class RRCPCommandHandler {

    private static RRCPCommandHandler instance;
    private static Vector commandlist;
    
    public static RRCPCommandHandler getInstance() {
        if (instance == null) {
            instance = new RRCPCommandHandler();
        }
        return instance;
    }
    
    private RRCPCommandHandler() {
        commandlist = new Vector();
    }
    public static void addCommand(RRCPCommand rrcpcommand) {
        commandlist.addElement(rrcpcommand);
        commandlist.trimToSize();
    }
    /**
     * This class and method is what you edit to make commands
     * @param s Command
     * @param dis DataInputStream
     * @param dos DataOutputStream
     */
    public static void executeCommand(String s, DataInputStream dis, DataOutputStream dos) {
        for(int i = 0; i < commandlist.size(); i++) {
            RRCPCommand rrcpcommand = (RRCPCommand) commandlist.elementAt(i);
            if(rrcpcommand.getName().equals(s)) { 
                rrcpcommand.exacute(dis, dos);
                return;
            }
        }
        System.err.println("Command not recognized: \"" + s + "\"\nError incoming!!!");
    }
}