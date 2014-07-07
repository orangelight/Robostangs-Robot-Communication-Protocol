
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class RRCPComputerTestCommandHandler {

    private static RRCPComputerTestCommandHandler instance;
    private static Vector<RRCPCommand> commandlist;
    
    public static RRCPComputerTestCommandHandler getInstance() {
        if (instance == null) {
            instance = new RRCPComputerTestCommandHandler();
        }
        return instance;
    }
    
    private RRCPComputerTestCommandHandler() {
        commandlist = new Vector<RRCPCommand>();
    }
    public static void addCommand(RRCPCommand rrcpcommand) {
        commandlist.add(rrcpcommand);
        commandlist.trimToSize();
    }
    /**
     * This class and method is what you edit to make commands
     * @param s Command
     * @param dis DataInputStream
     * @param dos DataOutputStream
     */
    public static void executeCommand(String s, DataInputStream dis, DataOutputStream dos) {
        for(RRCPCommand rrcpcommand : commandlist) {
            if(rrcpcommand.getName().equals(s)) rrcpcommand.exacute(dis, dos);
        }
    }
    
    public static void onSocketClose() {
        
    }
}