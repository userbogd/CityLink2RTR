package UDPConnections;

public class ClientUDPInstance
  {
    public int isEnabled;
    public String name;
    public String URL;
    public int Port;
    public boolean isDataPresent;
    public int PacketErrors;
    public ThreadedUDPClient cl;

    public ClientUDPInstance(int isEnabled, String name, String uRL, int port)
      {
        this.isEnabled = isEnabled;
        this.name = name;
        this.URL = new String(uRL);
        this.Port = port;
      }

    public void startUDPClient()
      {
        this.cl = new ThreadedUDPClient(URL, Port);
      }

    public void sendUDPClient(byte[] data)
      {
        this.cl.send(data);
      }
  }
