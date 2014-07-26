
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
    private static RRCPCommand closeSocketCommand;
    
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
        if(rrcpcommand.getName().equals("SOCKETCLOSED")) closeSocketCommand = rrcpcommand; 
        else {
            commandlist.addElement(rrcpcommand);
            commandlist.trimToSize();
        }
    }
    
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
    
    protected static void onSocketClose() {
        if(closeSocketCommand !=  null) closeSocketCommand.exacute(null, null);
    }
}