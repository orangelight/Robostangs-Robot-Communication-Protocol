Robostangs-Robot-Communication-Protocol (RRCP)
=======================================

Robostangs...?

Add RRCPServer  to the robot code.
Use RRCPClient to connect to the robot server from a client.

v1.1 coming soon....

Example command that echos a string you give it. "ECHO" is the name:
NOTE: The command must be static
```
static RRCPCommand echo = new RRCPCommand(("ECHO")) {
        @Override
        protected void execute(DataOutputStream dos, Object data) {
            String message = (String)data;
            System.out.println("ECHOING: "+message);
            this.sendString(message, dos);
        }
};
```
Example of how to send the ECHO command:
```
rrcpc.readStringPacket(rrcpc.sendCommandWithString("ECHO", "Test String"));
*rrcpc is an instance of RRCPCleint
```

To get a double in the execute method use: 
```
double d = (double)data
```
^This should get much simpler when they update Java versions

Set a RRCPCommand name to SOCKETCLOSED and it will be called when a client timeout or disconnects.
Android now supported!!!


Things to do
=======================================
- [x] Byte arrays
- [x] Maybe have robot server be able to send commands to client
- [x] Make reading date from server thread safe
- [x] Test out different timings of cleint and server
- [ ] Replace the address system or make it more hidden
- [ ] Maybe make integrated with network tables
- [ ] Make sure everything works
