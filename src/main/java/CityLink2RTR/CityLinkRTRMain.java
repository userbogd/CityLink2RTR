package CityLink2RTR;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

import MonitorHTTPServer.*;
import SerialPort.SerialPortInstance;
import UDPConnections.ClientUDPInstance;
import UDPConnections.Packet;
import UDPConnections.PacketHandler;
import UDPConnections.ThreadedUDPServer;

import java.util.Timer;
import java.util.TimerTask;

class SendUDPClientRoutine extends TimerTask
  {
    public static final int MAX_EVENTS_INPACKET = 100;
    public static final int EVENT_LENTH = 13;
    public static final int UDP_PACKET_MAX_SIZE = 1300;
	public void run()
      {
        byte[] sendArr;
        sendArr = new byte[UDP_PACKET_MAX_SIZE];
        for (int i = 0; i < CityLinkRTRMain.mainDataBuffer.size() && i < MAX_EVENTS_INPACKET; ++i)
          {
            System.arraycopy(CityLinkRTRMain.mainDataBuffer.poll().rawByteArray, 0, sendArr, EVENT_LENTH * i, EVENT_LENTH);
          }
        for (int i = 0; i < CityLinkRTRMain.udpPool.size(); ++i)
          {
            CityLinkRTRMain.udpPool.get(i).sendUDPClient(sendArr);
          }
      }
  }

public class CityLinkRTRMain
  {
	static ThreadedUDPServer UDPServ;
    static MonitorHTTPServer HTTP;
    public static final String INI_FILE_NAME = "rtrconfig.ini";
    public static Ini ini;
    public static final String LOG_FILE_NAME = "rtr.%u.log";
    public static Date StartDate;
    private static Logger log = Logger.getLogger(CityLinkRTRMain.class.getName());
    public static List<SerialPortInstance> serialPool;
    public static List<ClientUDPInstance> udpPool;
    public static Queue<CityLinkEventPacket> mainDataBuffer;
    
    public CityLinkRTRMain()
      {

      }

    public static void main(String[] args)
      {
        System.out.println("Start retranslator");
        StartDate = new Date(); // fix start date

        serialPool = new ArrayList<>();
        udpPool = new ArrayList<>();
        mainDataBuffer = new LinkedList<>();

        Timer udpClientSendTimer = new Timer();
        TimerTask udpClientSendTimerTask = new SendUDPClientRoutine();

        String filename = (args.length > 0) ? args[0] : INI_FILE_NAME;
        File conf = new File(filename);
        if (!conf.exists())
          {
            System.out.println("Conf file not found. Created default");
            try
              {
                conf.createNewFile();
                ini = new Ini(conf);
                ini.getConfig().setMultiSection(true);
                ini.getConfig().setMultiOption(true);
                ini.put("RTR", "version", "1.00");
                ini.put("RTR", "name", "Retranslator #1 at location");

                ini.put("HTTP", "enabled", 1);
                ini.put("HTTP", "httpport", 81);
                ini.put("HTTP", "refreshrate", 5);

                ini.put("UDPSERVER", "enabled", 0);
                ini.put("UDPSERVER", "udpport", 60500);

                ini.put("UDPCLIENT", "enabled", 0);
                ini.put("UDPCLIENT", "name", "Name1");
                ini.put("UDPCLIENT", "url", "127.0.0.1");
                ini.put("UDPCLIENT", "port", 60501);

                ini.put("SERIAL", "enabled", 0);
                ini.put("SERIAL", "name", "/dev/ttyUSB0");
                ini.put("SERIAL", "baudrate", 19200);

                ini.store();
              } catch (InvalidFileFormatException e)
              {
                e.printStackTrace();
              } catch (IOException e)
              {
                e.printStackTrace();
              }
          }

        try
          {
            ini = new Ini(conf);
            ini.getConfig().setMultiSection(true);
            ini.getConfig().setMultiOption(true);
          } catch (IOException e)
          {
            e.printStackTrace();
            System.out.println("Default ini file creation ERROR");
            return;
          }

        if (ini.get("UDPSERVER", "enabled", int.class) > 0)
          {
            UDPServ = new ThreadedUDPServer(ini.get("UDPSERVER", "port", int.class));
            UDPServ.receive(new PacketHandler()
              {
                @Override
                public void process(Packet packet)
                  {
                    String data = new String(packet.getData());
                    if (data.length() >= 13)
                      {
                        byte[] byteArray;
                        byteArray = data.getBytes(Charset.forName("Windows-1251"));
                        for (int k = 0; k < 1300; k += 13)
                          {
                            if (byteArray[k] != 0)
                              {
                                CityLinkEventPacket pt = new CityLinkEventPacket();
                                System.arraycopy(byteArray, k, pt.rawByteArray, 0, 13);
                                PutEventToMainBuffer(pt);
                              }
                          }
                      }
                  }
              });
          }
        
        


        if (ini.get("HTTP", "enabled", int.class) > 0)
          HTTP = new MonitorHTTPServer(ini.get("HTTP", "port", int.class));

        // Read all SERIAL sections and start threads
        Section sec = ini.get("SERIAL");
        System.out.format("Found and intialize %d serial ports\r\n",sec.length("enabled"));
        for (int i = 0; i < sec.length("enabled"); ++i)
          {
            SerialPortInstance sPort = new SerialPortInstance(Integer.parseInt(sec.get("enabled", i)),
                sec.get("name", i), Integer.parseInt(sec.get("baudrate", i)));
            serialPool.add(sPort);
            sPort.startSerialReader();
          }

        // Read all UDPCLIENT sections and start threads
        sec = ini.get("UDPCLIENT");
        System.out.format("Found %d udp client destinations\r\n",sec.length("enabled"));
        for (int i = 0; i < sec.length("enabled"); ++i)
          {
            ClientUDPInstance udpClient = new ClientUDPInstance(Integer.parseInt(sec.get("enabled", i)),
                sec.get("name", i), sec.get("url", i), Integer.parseInt(sec.get("port", i)));
            udpPool.add(udpClient);
            udpClient.startUDPClient();
          }
        udpClientSendTimer.schedule(udpClientSendTimerTask, 200, 200);
        System.out.println("Retranslator initialise complete");
      }
    
    public static synchronized void PutEventToMainBuffer(CityLinkEventPacket pkt)
    {
    	mainDataBuffer.offer(pkt);
    }

  }
