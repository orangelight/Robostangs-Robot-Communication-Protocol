
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
    
    protected static void executeCommand(String s, DataOutputStream dos, Object data) {
        for(int i = 0; i < commandlist.size(); i++) {
            RRCPCommand rrcpcommand = (RRCPCommand) commandlist.elementAt(i);
            if(rrcpcommand.getName().equals(s)) { 
                rrcpcommand.exacute(dos, data);
                return;
            }
        }
        System.err.println("Command not recognized: \"" + s + "\"\nError incoming!!!");
    }
    
    protected static void onSocketClose() {
        if(closeSocketCommand !=  null) closeSocketCommand.exacute(null, null);
    }
}