package UDPConnections;

import CityLink2RTR.CityLinkEventPacket;
import CityLink2RTR.Helpers;
import CityLink2RTR.MainEventBufer;

public class ServerUDPInstance
  {
    private int isEnabled;
    private String name;
    private int Port;
    private long PacketsOk, PacketsErrors;
    private ThreadedUDPServer sr;

    public ServerUDPInstance(int isEnabled, String name, int port)
      {
        this.isEnabled = isEnabled;
        this.name = name;
        this.Port = port;
      }

    public void startUDPServer()
      {
        this.sr = new ThreadedUDPServer(Port);
        this.sr.receive(new PacketHandler()
          {
            @Override
            public void process(Packet packet)
              {
                byte[] dt = new byte[1300];
                dt = packet.getData();
                for (int k = 0; k < 1300; k += 13)
                  {
                    if (dt[k] != 0)
                      {
                        CityLinkEventPacket pt = new CityLinkEventPacket();
                        System.arraycopy(dt, k, pt.rawByteArray, 0, 13);
                        int u[] = new int[13];
                        Helpers.SignedBytesToUnsignedInt(pt.rawByteArray, u);
                        if (Helpers.CheckCityLinkEventError(u) == 0)
                          {
                            MainEventBufer.PutEvent(pt);
                            ++PacketsOk;
                            // System.out.println(Helpers.bytesToHex(pt.rawByteArray));
                          }
                        else
                          {
                            ++PacketsErrors;
                          }
                      }
                  }
              }
          });
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
