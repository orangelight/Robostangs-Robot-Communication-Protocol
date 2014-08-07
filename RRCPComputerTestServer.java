
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Alex
 */
public class RRCPComputerTestServer implements Runnable {

    private static boolean listening = true;
    private static int port = 548;
    private static int timeout = 5000;
    private static Thread mainThread;
    private static RRCPComputerTestServer instance;
    private static ServerSocket server;
    private static ArrayList<RRCPCommand> commandlist;
    private static RRCPCommand closeSocketCommand;

    public static RRCPComputerTestServer getInstance() {
        if (instance == null) {
            instance = new RRCPComputerTestServer();
        }
        return instance;
    }

    private RRCPComputerTestServer() {
        commandlist = new ArrayList<>();
        mainThread = new Thread(this);
    }

    public static void startServer(int port, int timeout) {
        RRCPComputerTestServer.port = port;
        RRCPComputerTestServer.timeout = timeout;
        listening = true;
        mainThread.start();
    }

    public static void startServer() {
        mainThread.start();
    }

    public static void stopServer() {
        try {
            listening = false;
            server.close();
        } catch (IOException ex) {
            System.err.println("Error closing server: \"" + ex.getMessage() + "\"");
        }
        System.out.println("SERVER SHUT DOWN");
    }

    public void run() {
        try {
            server = new ServerSocket(this.port);
            while (listening) {
                Socket s = server.accept();
                System.out.println("Client Connected");
                RRCPConnectionHandler ch = new RRCPConnectionHandler(s);
                Thread tch = new Thread(ch);
                tch.start();
            }
        } catch (IOException ex) {
            System.err.println("Error making client socket: \"" + ex.getMessage() + "\"");
        }
    }

    private class RRCPConnectionHandler implements Runnable {

        private Socket s;
        private DataInputStream dis;
        private DataOutputStream dos;
        private long lastHeartBeat;

        public RRCPConnectionHandler(Socket s) {
            this.s = s;
            try {
                dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(this.s.getOutputStream()));
            } catch (IOException ex) {
                System.err.println("Error making data streams on ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
        }

        public void run() {
            protocol();
            this.close();
        }

        private void close() {
            try {
                dis.close();
                s.close();
            } catch (IOException ex) {
                System.err.println("Error closing ConnectionHandler: \"" + ex.getMessage() + "\"");
            }
        }

        private void protocol() {
            try {
                this.lastHeartBeat = System.currentTimeMillis();
                while (System.currentTimeMillis() < this.lastHeartBeat + timeout && RRCPComputerTestServer.listening) {
                    while (dis.available() > 0) {
                        byte id = dis.readByte();
                        this.lastHeartBeat = System.currentTimeMillis();
                        if (id == 21) {
                            dos.write((byte) 21);
                            dos.flush();
                            this.lastHeartBeat = System.currentTimeMillis();
                            System.out.println("Heartbeat Received");
                            break;
                        } else if (id == 0) {
                            this.close();
                            break;
                        } else if (id == 1) {
                            execute(dis.readUTF(), dis.readByte());
                        } else if (id == 2) {
                            execute(dis.readUTF(), dis.readInt());
                        } else if (id == 3) {
                            execute(dis.readUTF(), dis.readBoolean());
                        } else if (id == 4) {
                            execute(dis.readUTF(), dis.readDouble());
                        } else if (id == 5) {
                            execute(dis.readUTF(), dis.readUTF());
                        } else if (id == 6) {
                            execute(dis.readUTF(), this.readDoubleArray());
                        } else if (id == 7) {
                            execute(dis.readUTF(), this.readByteArray());
                        } else if (id == 8) {
                            execute(dis.readUTF(), null);
                        }
                    }
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ex) {
                        System.err.println("Error sleeping: \"" + ex.getMessage() + "\"");
                    }
                }
                System.err.println("Client timed out!!!");
            } catch (IOException ex) {
                System.err.println("Error reading data from client: \"" + ex.getMessage() + "\"");
            }
            RRCPComputerTestServer.onSocketClose();
        }

        private void execute(final String s, final Object o) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RRCPComputerTestServer.executeCommand(s, dos, o);
                }
            }).start();
        }

        private double[] readDoubleArray() {
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
            return new double[0];
        }
        
        private byte[] readByteArray() {
            try {
                int length = dis.readInt();
                byte[] b = new byte[length];
                for (int i = 0; i < length; i++) {
                    b[i] = dis.readByte();
                }
                return b;
            } catch (IOException ex) {
                System.err.println("Error reading data from Client: \"" + ex.getMessage() + "\"");
            }
            return new byte[0];
        }
    }
    
    protected static void addCommand(RRCPCommand rrcpcommand) {
        if(rrcpcommand.getName().equals("SOCKETCLOSED")) closeSocketCommand = rrcpcommand; 
        else {
            commandlist.add(rrcpcommand);
            commandlist.trimToSize();
        }
    }
    
    private static void executeCommand(String s, DataOutputStream dos, Object data) {
        for(RRCPCommand rrcpcommand : commandlist) {
            if(rrcpcommand.getName().equals(s)) { 
                rrcpcommand.exacute(dos, data);
                return;
            }
        }
        System.err.println("Command not recognized: \"" + s + "\"\nError incoming!!!");
    }
    
    private static void onSocketClose() {
        if(closeSocketCommand !=  null) closeSocketCommand.exacute(null, null);
    }
}