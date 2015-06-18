using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace RRCP
{
    public class RRCPClient
    {

        private System.Net.Sockets.TcpClient socket;
        private string hostIp;
        private int timeout;
        private int hostPort;
        private volatile bool connected = false, connecting = false;
        private BinaryReader dataInput;
        private BinaryWriter dataOutput;
        private Object connectLock = new Object(), outputLock = new Object(), addressLock = new Object();
        private Thread heartBeatThread;
        private int heartBeatDelay = 0;
        private volatile sbyte currentAddress = -1;
        private const int TIMEOUT_NUM = 5;
        private Boolean autoReconnect = false;
        public PacketHandler packetHandler;

        public enum PacketTypes : sbyte
        {
            Byte = 1,
            Integer,
            Double,
            Long,
            Short,
            Float,
            Command,
            String,
            Boolean,
            DoubleArray,
            ByteArray,
            IntegerArray,
            LongArray,
            ShortArray,
            FloatArray,
            HeartBeat = 21,
            ClientCommand = 30,
            ClientCommandDouble,
            ClientCommandDoubleArray
        }

        public RRCPClient(string host, int timeout, int port)
        {
            this.hostIp = host;
            this.hostPort = port;
            this.timeout = timeout;
        }

        public void Connect()
        {
            lock (connectLock)
            {
                if (isConnected() || isConnecting())
                {

                }
                else
                {
                    try
                    {
                        connecting = true;
                        socket = new System.Net.Sockets.TcpClient(hostIp, hostPort);
                        dataInput = new BinaryReader(socket.GetStream(), System.Text.Encoding.UTF8);
                        dataOutput = new BinaryWriter(socket.GetStream(), System.Text.Encoding.UTF8);
                        packetHandler = new PacketHandler(this);
                        heartBeatThread = new Thread(HeartBeatRun);
                        heartBeatThread.Start();
                        connected = true;
                    }
                    catch (Exception e)
                    {
                        connected = false;
                    }
                    finally
                    {
                        connecting = false;
                    }
                }
            }
        }

        public void Dissconnect()
        {
            if (isConnected())
            {
                dataOutput.Close();
                dataInput.Close();
                socket.Close();
            }
        }
        public bool isConnected()
        {
            return connected;
        }

        public bool isConnecting()
        {
            return connecting;
        }

        public sbyte getCurrentAddress()
        {
            return currentAddress;
        }

        private sbyte addCurrentAddress()
        {
            lock (addressLock)
            {
                if (currentAddress == 100)
                {
                    currentAddress = -1; //if last packet sent had address of 50 change currentAdress back to -1 to be changed to 0
                }
                packetHandler.packetQueue[++currentAddress] = null;
                return getCurrentAddress();
            }
        }
        private void addAddressCommand(sbyte address, string command)
        {
            dataOutput.Write(address);
            byte[] b = BitConverter.GetBytes((ushort)command.Length);
            Array.Reverse(b);
            dataOutput.Write(b);
            foreach(char c in command) 
            {
                dataOutput.Write(c);
            }
        }
        public sbyte sendCommand(string command)
        {
            if (isConnected())
            {
                sbyte address;
                lock (outputLock)
                {
                    address = addCurrentAddress();
                    dataOutput.Write((sbyte)PacketTypes.Command);
                    addAddressCommand(address, command);
                    dataOutput.Flush();
                }
                return address;
            }
            else
            {

            }
            return -2;
        }
        public sbyte sendCommandWithNumber(string command, ValueType v)
        {
            if (isConnected())
            {
                sbyte address = addCurrentAddress();
                if (v is int)
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.Integer);
                        addAddressCommand(address, command);
                        dataOutput.Write((int)v);
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (v is double)
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.Double);
                        addAddressCommand(address, command);
                        byte[] b = BitConverter.GetBytes((double)v);
                        Array.Reverse(b);
                        dataOutput.Write(b);
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (v is short)
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.Short);
                        addAddressCommand(address, command);
                        dataOutput.Write((short)v);
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (v is sbyte)
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.Byte);
                        addAddressCommand(address, command);
                        dataOutput.Write((sbyte)v);
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (v is long)
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.Long);
                        addAddressCommand(address, command);
                        dataOutput.Write((long)v);
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (v is float)
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.Float);
                        addAddressCommand(address, command);
                        dataOutput.Write((float)v);
                        dataOutput.Flush();
                    }
                    return address;
                }
                else
                {
                    return -2;
                }
            }
            else
            {
                return -2;
            }
        }

        public sbyte sendCommandWithNumberArray(string command, Object array)
        {
            if (isConnected())
            {
                sbyte address = addCurrentAddress();
                if (array is int[])
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.IntegerArray);
                        addAddressCommand(address, command);
                        dataOutput.Write(((int[])array).Length);
                        for (int i = 0; i < ((int[])array).Length; i++)
                        {
                            dataOutput.Write(((int[])array)[i]);
                        }
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (array is double[])
                {
                    Debug.WriteLine("Writing array");
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.DoubleArray);
                        addAddressCommand(address, command);
                        //dataOutput.Write(((double[])array).Length);
                        byte[] l = BitConverter.GetBytes(((double[])array).Length);
                        Array.Reverse(l);
                        dataOutput.Write(l);
                        for (int i = 0; i < ((double[])array).Length; i++)
                        {
                            byte[] b = BitConverter.GetBytes(((double[])array)[i]);
                            Array.Reverse(b);
                            dataOutput.Write(b);
                        }
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (array is short[])
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.ShortArray);
                        addAddressCommand(address, command);
                        dataOutput.Write(((short[])array).Length);
                        for (int i = 0; i < ((short[])array).Length; i++)
                        {
                            dataOutput.Write(((short[])array)[i]);
                        }
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (array is sbyte[])
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.ByteArray);
                        addAddressCommand(address, command);
                        dataOutput.Write(((byte[])array).Length);
                        for (int i = 0; i < ((sbyte[])array).Length; i++)
                        {
                            dataOutput.Write(((byte[])array)[i]);
                        }
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (array is long[])
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.LongArray);
                        addAddressCommand(address, command);
                        dataOutput.Write(((long[])array).Length);
                        for (int i = 0; i < ((long[])array).Length; i++)
                        {
                            dataOutput.Write(((long[])array)[i]);
                        }
                        dataOutput.Flush();
                    }
                    return address;
                }
                else if (array is float[])
                {
                    lock (outputLock)
                    {
                        dataOutput.Write((sbyte)PacketTypes.FloatArray);
                        addAddressCommand(address, command);
                        dataOutput.Write(((float[])array).Length);
                        for (int i = 0; i < ((float[])array).Length; i++)
                        {
                            dataOutput.Write(((float[])array)[i]);
                        }
                        dataOutput.Flush();
                    }
                    return address;
                }
                else
                {
                    return -2;
                }
            }
            else
            {
                return -2;
            }
        }

        public sbyte sendCommandWithBoolean(string command, bool b)
        {
            sbyte address;
            if (isConnected())
            {

                lock (outputLock)
                {
                    address = addCurrentAddress();
                    dataOutput.Write((sbyte)PacketTypes.Boolean);
                    addAddressCommand(address, command);
                    dataOutput.Write(b);
                    dataOutput.Flush();
                }
                return address;

            }
            else
            {

            }
            return -2;
        }

        public sbyte sendCommandWithString(string command, string s)
        {
            sbyte address;
            if (isConnected())
            {

                lock (outputLock)
                {
                    address = addCurrentAddress();
                    dataOutput.Write((sbyte)PacketTypes.String);
                    addAddressCommand(address, command);
                    dataOutput.Write(s);
                    dataOutput.Flush();
                }
                return address;

            }
            else
            {

            }
            return -2;
        }

        public class PacketHandler
        {
            public Packet[] packetQueue;
            public Packet beatQueue;
            private Thread mainThread;
            public RRCPClient client;

            public PacketHandler(RRCPClient client)
            {
                this.packetQueue = new Packet[101]; //Makes packet queue
                this.beatQueue = null;
                this.client = client;
                this.mainThread = new Thread(run);
                this.mainThread.Start();
            }

            public void run()
            {
                while (client.isConnected())
                {
                    try
                    {
                        new Packet((sbyte)client.readNumber((sbyte)PacketTypes.Byte), client); //Makes new packet of id read from byte
                    }
                    catch (Exception ex)
                    {

                    }
                    Thread.Sleep(TIMEOUT_NUM);
                }
            }

            public Packet getPacket(sbyte address)
            {
                int i = 0;
                while (packetQueue[address] == null)
                {
                    ++i;
                    if (i > client.timeout + 10)
                    {
                        return null;
                    }
                    Thread.Sleep(TIMEOUT_NUM);
                }
                Packet p = packetQueue[address];
                packetQueue[address] = null;
                return p;
            }

            public Packet getHeartBeat()
            {
                int i = 0;
                while (beatQueue == null)
                {

                    if (i > client.timeout)
                    {

                        return new Packet((sbyte)100, client); //Returns error packet
                    }
                    Thread.Sleep(TIMEOUT_NUM);
                    ++i;
                }
                client.setHeartBeatDelay(i);
                Packet p = beatQueue;
                resetBeatQueue();
                return p;
            }

            public void addPacketToQueue(Packet p)
            {
                packetQueue[p.getAddress()] = p;
            }

            public void addPacketToBeatQueue(Packet p)
            {
                beatQueue = p;
            }

            public void resetBeatQueue()
            {
                beatQueue = null;
            }

        }

        public ValueType readNumber(sbyte id)
        {
            try
            {
                if (id == (sbyte)PacketTypes.Byte || id == (sbyte)PacketTypes.ByteArray) return dataInput.ReadSByte();
                else if (id == (sbyte)PacketTypes.Integer || id == (sbyte)PacketTypes.IntegerArray) return dataInput.ReadInt32();
                else if (id == (sbyte)PacketTypes.Double || id == (sbyte)PacketTypes.DoubleArray) {
                    byte[] b = dataInput.ReadBytes(8);
                    Array.Reverse(b);
                    return BitConverter.Int64BitsToDouble(BitConverter.ToInt64(b, 0));
                }
                else if (id == (sbyte)PacketTypes.Long || id == (sbyte)PacketTypes.LongArray) return dataInput.ReadInt64();
                else if (id == (sbyte)PacketTypes.Short || id == (sbyte)PacketTypes.ShortArray) return dataInput.ReadInt16();
                else if (id == (sbyte)PacketTypes.Float || id == (sbyte)PacketTypes.FloatArray) return dataInput.ReadSingle();
                else return -1;
            }
            catch (IOException ex)
            {

            }
            return -1;
        }

        public Object readNumberArray(sbyte id)
        {
            int length = (int)readNumber((sbyte)PacketTypes.Integer);
            Object[] n = new Object[length];
            for (int i = 0; i < length; i++)
            {
                n[i] = readNumber(id);
            }
            return n;
        }

        public bool readBoolean()
        {
            try
            {
                return dataInput.ReadBoolean();
            }
            catch (IOException ex)
            {

            }
            return false;
        }

        public string readString()
        {
            try
            {
                 byte[] b = dataInput.ReadBytes(2);
                 Array.Reverse(b);
                 Debug.WriteLine(new string(dataInput.ReadChars(BitConverter.ToUInt16(b, 0))));
                 return new string(dataInput.ReadChars(BitConverter.ToUInt16(b, 0)));
            }
            catch (IOException ex)
            {

            }
            return null;
        }

        public Object readPacket(sbyte address)
        {
            if (isConnected())
            {
                if (address == -2)
                {
                    return null;
                }
                Packet isNull = packetHandler.getPacket(address);
                if (isNull == null)
                {
                    return null;
                }
                return isNull.getData();
            }
            return null;
        }

        private void close()
        {
            this.connected = false;
            if (socket != null)
            {
                this.socket.Close();
            }
            if (autoReconnect)
            {
                this.Connect();
            }
        }

        private void HeartBeatRun()
        {
            while (isConnected() && sendHeartBeat())
            {
                Debug.WriteLine("Heart Beat");
                Thread.Sleep(250);
            }
            Debug.WriteLine("Heart Beat Dead!");
            close();
        }

        private bool sendHeartBeat()
        {
            lock (outputLock)
            {
                try
                {
                    Debug.WriteLine("Heart Beat Send");
                    dataOutput.Write((sbyte)21);
                    dataOutput.Flush(); 
                }
                catch (Exception e)
                {
                    return false;
                }
            }
            if (packetHandler.getHeartBeat().getID() == 21)
            {
                connected = true;
                return true;
            }
            else
            {
                return false;
            }
        }

        private void setHeartBeatDelay(int i)
        {
            this.heartBeatDelay = i;
        }

        public int getDelay()
        {
            return TIMEOUT_NUM * this.heartBeatDelay;
        }

        public class Packet
        {
            private sbyte id;
            private Object data;
            private sbyte address;

            public Packet(sbyte id, RRCPClient client)
            {
                Debug.WriteLine("Getting Something " + id);
                this.id = id;
                if (id == (sbyte)RRCPClient.PacketTypes.HeartBeat)
                { //Determains what type of packet it is from id 
                    client.packetHandler.addPacketToBeatQueue(this);
                }
                else if (id <= (sbyte)RRCPClient.PacketTypes.Float)
                { //Numbers
                    address = (sbyte)client.readNumber((sbyte)RRCPClient.PacketTypes.Byte);
                    data = client.readNumber(id);
                    client.packetHandler.addPacketToQueue(this);
                }
                else if (id == (sbyte)RRCPClient.PacketTypes.Boolean)
                {
                    address = (sbyte)client.readNumber((sbyte)RRCPClient.PacketTypes.Byte);
                    data = client.readBoolean();
                    client.packetHandler.addPacketToQueue(this);
                }
                else if (id == (sbyte)RRCPClient.PacketTypes.String)
                {
                    address = (sbyte)client.readNumber((sbyte)RRCPClient.PacketTypes.Byte);
                    data = client.readString();
                    client.packetHandler.addPacketToQueue(this);
                }
                else if (id >= (sbyte)RRCPClient.PacketTypes.DoubleArray && id <= (sbyte)RRCPClient.PacketTypes.FloatArray)
                {
                    address = (sbyte)client.readNumber((sbyte)RRCPClient.PacketTypes.Byte);
                    data = client.readNumberArray(id);
                    client.packetHandler.addPacketToQueue(this);
                }
                else if (id == 100)
                { //Error packet
                }
                else
                {
                    data = null;
                }
            }

            public Object getData()
            {
                return data;
            }

            public sbyte getID()
            {
                return id;
            }

            public sbyte getAddress()
            {
                return address;
            }
        }
    }

    
}
