
/**
 *
 * @author Alex
 */
public abstract class RRCPClientCommand {
    private String key;
    
    public RRCPClientCommand(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
    
    public abstract void execute(Object data);
}