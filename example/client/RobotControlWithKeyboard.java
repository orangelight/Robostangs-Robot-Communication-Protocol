
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 *
 * @author Alex
 */
public class RobotControlWithKeyboard {

    static RRCPClient rrcpc;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        rrcpc = new RRCPClient("localhost", 20000);
        rrcpc.connect();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(100, 100);
        JPanel panel = new JPanel();
        
        panel.getInputMap().put(KeyStroke.getKeyStroke("W"), "forward");
        panel.getActionMap().put("forward", forward);
        panel.getInputMap().put(KeyStroke.getKeyStroke("released W"), "released forward");
        panel.getActionMap().put("released forward", released);
        
        panel.getInputMap().put(KeyStroke.getKeyStroke("S"), "backward");
        panel.getActionMap().put("backward", backward);
        panel.getInputMap().put(KeyStroke.getKeyStroke("released S"), "released backward");
        panel.getActionMap().put("released backward", released);
        
        panel.getInputMap().put(KeyStroke.getKeyStroke("A"), "left");
        panel.getActionMap().put("left", left);
        panel.getInputMap().put(KeyStroke.getKeyStroke("released A"), "released left");
        panel.getActionMap().put("released left", released);
        
        panel.getInputMap().put(KeyStroke.getKeyStroke("D"), "right");
        panel.getActionMap().put("right", right);
        panel.getInputMap().put(KeyStroke.getKeyStroke("released D"), "released right");
        panel.getActionMap().put("released right", released);
        
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
    
    static boolean pressed = false;
    
    static Action forward = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!pressed) {
                rrcpc.sendCommand("FORWARD");
                pressed = true;
            }
        }
    };
    
    static Action released = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            pressed = false;
            rrcpc.sendCommand("STOP");
        }
    };

    static Action backward = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!pressed) {
                rrcpc.sendCommand("BACKWARD");
                pressed = true;
            }
        }
    };
    
    static Action left = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!pressed) {
                rrcpc.sendCommand("LEFT");
                pressed = true;
            }
        }
    };
    
    static Action right = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!pressed) {
                rrcpc.sendCommand("RIGHT");
                pressed = true;
            }
        }
    };

}
