
import java.io.DataOutputStream;
import java.util.ArrayList;

/**
 *
 * @author Alex
 */
public class RRCPComputerTestCommandHandler {

    private static RRCPComputerTestCommandHandler instance;
    private static ArrayList<RRCPCommand> commandlist;
    private static RRCPCommand closeSocketCommand;
    
    protected static RRCPComputerTestCommandHandler getInstance() {
        if (instance == null) {
            instance = new RRCPComputerTestCommandHandler();
        }
        return instance;
    }
    
    private RRCPComputerTestCommandHandler() {
        commandlist = new ArrayList<>();
    }
    
    protected static void addCommand(RRCPCommand rrcpcommand) {
        if(rrcpcommand.getName().equals("SOCKETCLOSED")) closeSocketCommand = rrcpcommand; 
        else {
            commandlist.add(rrcpcommand);
            commandlist.trimToSize();
        }
    }
    
    protected static void executeCommand(String s, DataOutputStream dos, Object data) {
        for(RRCPCommand rrcpcommand : commandlist) {
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