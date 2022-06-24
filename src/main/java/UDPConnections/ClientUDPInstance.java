package UDPConnections;

public class ClientUDPInstance
  {
    private int isEnabled;
    private String name, URL;
    private int Port;
    private long PacketsOk, PacketsErrors;
    private ThreadedUDPClient cl;

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
    
    public void closeUDPClient()
      {
        this.cl.close();;
      }

    public synchronized void incPacketsOK()
      {
        ++this.PacketsOk;
      }
   
    public synchronized void incPacketsError()
      {
        ++this.PacketsErrors;
      } 
    
    public synchronized int getIsEnabled()
      {
        return isEnabled;
      }

    public synchronized void setIsEnabled(int isEnabled)
      {
        this.isEnabled = isEnabled;
      }

    public synchronized String getName()
      {
        return name;
      }

    public synchronized void setName(String name)
      {
        this.name = name;
      }

    public synchronized String getURL()
      {
        return URL;
      }

    public synchronized void setURL(String uRL)
      {
        URL = uRL;
      }

    public synchronized int getPort()
      {
        return Port;
      }

    public synchronized void setPort(int port)
      {
        Port = port;
      }

    public synchronized long getPacketsOk()
      {
        return PacketsOk;
      }

    public synchronized void setPacketsOk(long packetsOk)
      {
        PacketsOk = packetsOk;
      }

    public synchronized long getPacketsErrors()
      {
        return PacketsErrors;
      }

    public synchronized void setPacketsErrors(long packetsErrors)
      {
        PacketsErrors = packetsErrors;
      }
  
  
  }
