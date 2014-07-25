Robostangs-Robot-Communication-Protocol (RRCP)
=======================================

Robostangs...?

Add RRCPServer & RRCPCommandHandler to the robot code when They’re done.
Use RRCPClient to connect to the robot server from a client.
Use RRCPComputerTestServer & RRCPComputerTestCommandHandler if you dont have a robot cRIO to test on the computer.

Example command that echos a string you give it. "ECHO" is the name:
```
static RRCPCommand echo = new RRCPCommand(("ECHO")) {
        @Override
        public void exacute(DataInputStream dis, DataOutputStream dos) {
            this.sendString(this.readString(dis), dos);
    }
};
```
Set a RRCPCommand name to SOCKETCLOSED and it will be called when a cleint timesout or disconnects.
Android now supported!!!

Things to do
=======================================
- [ ] Maybe have robot server be able to send commands to client
- [ ] Make sure everything works
