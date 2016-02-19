/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sinnyclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class RRCPSkinnyClient {
    private Socket socket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private String hostIP;
    private int hostPort;
    private volatile boolean connected = false;
    private boolean connecting = false;
    private PacketHandler packetHandler;
    private int heartBeatDelay = 0;
    private boolean autoReconnect = true;

   

    public RRCPSkinnyClient(String host, int port, int timeout, boolean autoRe) {
        this.hostIP = host;
        this.hostPort = port;
        this.autoReconnect = autoRe;
    }

    public synchronized void connect() {
        if (isConnected()) {
            System.err.println("Error ID: 3 Already connected");
        } else if (isConnecting()) {
            System.err.println("Error ID: 4 Already connecting");
        } else {
            try {
                connecting = true;
                socket = new Socket(hostIP, hostPort);
                dataInput = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dataOutput = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                packetHandler = new PacketHandler();
                connected = true;
                System.out.println("CONNECTED TO SERVER!!!!");
            } catch (IOException ex) {
                System.err.println("Error ID: 0 "+ex.getMessage());
                close();
            } finally {
                connecting = false;
            }

        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isConnecting() {
        return connecting;
    }

    
    public void sendDistanceAndHeading(double Sdistance, double Sheading) {
        if (isConnected()) {
            try {
                dataOutput.write(1);
                dataOutput.writeDouble(Sdistance);
                dataOutput.writeDouble(Sheading);
                dataOutput.flush();
            } catch (IOException ex) {
                Logger.getLogger(RRCPSkinnyClient.class.getName()).log(Level.SEVERE, null, ex);
                this.close();
            }
        }
    }
    
    public void noTarget() {
        if (isConnected()) {
            try {
                dataOutput.write(2);
                dataOutput.flush();

            } catch (IOException ex) {
                Logger.getLogger(RRCPSkinnyClient.class.getName()).log(Level.SEVERE, null, ex);
                this.close();
            }
        }
    }
    


    private class PacketHandler implements Runnable {
        
        private Thread mainThread;
        
        public PacketHandler() {
            this.mainThread = new Thread(this);
            this.mainThread.start();
          
        }
        
        public void run() {
            while (isConnected()) {
                try {
                    while (dataInput.available() > 0) { //Reads packet when it comes in from input stream
                       if(dataInput.readByte()==21) {
                           connected = true;
                       }
                    }
                } catch (IOException ex) {
                    System.err.println("Error ID: 10 " + ex.getMessage());
                    close();
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    System.err.println("Error ID: 11 sleeping");
                }
            }
        }
        
        

    }

    private void close() {
        try {
            this.connected = false;
            this.connecting = false;
            if (socket != null) {
                this.socket.close();
            }
        } catch (IOException ex) {
            System.err.println("Error ID: 16 "+ ex.getMessage());
        } finally {
            if(autoReconnect) {
                this.connect();
            }
        }
    }
    
    public void disconnect() {
        if(autoReconnect) {
            this.autoReconnect = false;
            this.close();
            this.autoReconnect = true;
        } else this.close();
    }
 
}
