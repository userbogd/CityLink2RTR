package CityLink2RTR;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

import MonitorHTTPServer.*;
import SerialPort.SerialPortInstance;
import UDPConnections.ClientUDPInstance;
import UDPConnections.ServerUDPInstance;
import UDPConnections.ThreadedUDPServer;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
        int readyEvents = (MainEventBufer.getSize() > MAX_EVENTS_INPACKET) ? MAX_EVENTS_INPACKET
            : MainEventBufer.getSize();
        sendArr = new byte[readyEvents * EVENT_LENTH];
        for (int i = 0; i < readyEvents; ++i)
          {
            System.arraycopy(MainEventBufer.PollEvent().rawByteArray, 0, sendArr, EVENT_LENTH * i, EVENT_LENTH);
          }
        if (readyEvents > 0)
          {
            for (int i = 0; i < CityLinkRTRMain.udpClientPool.size(); ++i)
              {
                if (CityLinkRTRMain.udpClientPool.get(i).getIsEnabled() > 0)
                  {
                    CityLinkRTRMain.udpClientPool.get(i).sendUDPClient(sendArr);
                    CityLinkRTRMain.udpClientPool.get(i)
                        .setPacketsOk(CityLinkRTRMain.udpClientPool.get(i).getPacketsOk() + readyEvents);
                  }
              }
          }
      }
  }

public class CityLinkRTRMain
  {
    static ThreadedUDPServer UDPServ;
    static MonitorHTTPServer HTTP;
    public static final String INI_FILE_NAME = "rtrconfig.ini";
    public static Ini ini;
    public static Date StartDate;
    public static List<SerialPortInstance> serialPool;
    public static List<ClientUDPInstance> udpClientPool;
    public static List<ServerUDPInstance> udpServerPool;
    public static MainEventBufer MB;
    public String version;

    static
      {
        try (InputStream is = CityLinkRTRMain.class.getClassLoader().getResourceAsStream("logging.properties"))
          {
            LogManager.getLogManager().readConfiguration(is);

          }
        catch (IOException e)
          {
            e.printStackTrace();
          }
      }
    public static final Logger LOG = Logger.getLogger(CityLinkRTRMain.class.getName());

    public CityLinkRTRMain()
      {

      }

    public static void main(String[] args)
      {
        // LogManager().readConfiguration(CityLinkRTRMain.class.getResourceAsStream("logging.properties"));
        LOG.info("Start retranlator");
        StartDate = new Date(); // fix start date

        MB = new MainEventBufer();
        serialPool = new ArrayList<>();
        udpClientPool = new ArrayList<>();
        udpServerPool = new ArrayList<>();

        Timer udpClientSendTimer = new Timer();
        TimerTask udpClientSendTimerTask = new SendUDPClientRoutine();



        String filename = (args.length > 0) ? args[0] : INI_FILE_NAME;
        File conf = new File(filename);
        if (!conf.exists())
          {
            LOG.warning("Config file does not exists.Recreated default");
            try
              {
                conf.createNewFile();
                ini = new Ini(conf);
                ini.getConfig().setMultiSection(true);
                ini.getConfig().setMultiOption(true);
                ini.put("RTR", "version", "1.00");
                ini.put("RTR", "username", "Retranslator #1 at location");

                ini.put("HTTP", "enabled", 1);
                ini.put("HTTP", "httpport", 8181);
                ini.put("HTTP", "refreshrate", 5);

                ini.put("UDPSERVER", "enabled", 0);
                ini.put("UDPSERVER", "username", "Server name 1");
                ini.put("UDPSERVER", "port", 60600);

                ini.put("UDPCLIENT", "enabled", 0);
                ini.put("UDPCLIENT", "username", "Client name 1");
                ini.put("UDPCLIENT", "url", "127.0.0.1");
                ini.put("UDPCLIENT", "port", 60501);

                ini.put("SERIAL", "enabled", 0);
                ini.put("SERIAL", "username", "Serial port name 1");
                ini.put("SERIAL", "name", "/dev/ttyUSB0");
                ini.put("SERIAL", "baudrate", 19200);

                ini.store();
              }
            catch (InvalidFileFormatException e)
              {
                e.printStackTrace();
              }
            catch (IOException e)
              {
                e.printStackTrace();
              }
          }

        try
          {
            ini = new Ini(conf);
            ini.getConfig().setMultiSection(true);
            ini.getConfig().setMultiOption(true);
          }
        catch (IOException e)
          {
            e.printStackTrace();
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return;
          }

        if (ini.get("HTTP", "enabled", int.class) > 0)
          {
            int port = ini.get("HTTP", "httpport", int.class);
            HTTP = new MonitorHTTPServer(port);
          }

        // Read all SERIAL sections and start threads
        Section sec = ini.get("SERIAL");
        for (int i = 0; i < sec.length("enabled"); ++i)
          {
            SerialPortInstance sPort = new SerialPortInstance(Integer.parseInt(sec.get("enabled", i)),
                sec.get("username", i), sec.get("name", i), Integer.parseInt(sec.get("baudrate", i)));
            serialPool.add(sPort);
            if (sPort.getIsEnabled() > 0)
              sPort.startSerialReader();
          }

        // Read all UDPSERVER sections and start threads
        sec = ini.get("UDPSERVER");
        for (int i = 0; i < sec.length("enabled"); ++i)
          {
            ServerUDPInstance udpServer = new ServerUDPInstance(Integer.parseInt(sec.get("enabled", i)),
                sec.get("username", i), Integer.parseInt(sec.get("port", i)));
            udpServerPool.add(udpServer);
            if (udpServer.getIsEnabled() > 0)
              {
                udpServer.startUDPServer();
              }
          }

        // Read all UDPCLIENT sections and start threads
        sec = ini.get("UDPCLIENT");
        for (int i = 0; i < sec.length("enabled"); ++i)
          {
            ClientUDPInstance udpClient = new ClientUDPInstance(Integer.parseInt(sec.get("enabled", i)),
                sec.get("username", i), sec.get("url", i), Integer.parseInt(sec.get("port", i)));
            udpClientPool.add(udpClient);
            if (udpClient.getIsEnabled() > 0)
              udpClient.startUDPClient();

          }
        
        udpClientSendTimer.schedule(udpClientSendTimerTask, 200, 200);
        LOG.info("Retranslator initialise complete");

        try
          {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
            while (true)
              {
                
                try
                  {
                    String com = new String(reader.readLine());
                    if (com.length() > 0)
                      {
                        if (com.equals("exit"))
                          {
                            StopRetranslator();
                          }
                        else if (com.equals("info"))
                          {
                            PrintInfo();
                          }
                        else
                          {
                            System.out.println("This is CityLink UDP retranslator. Supported commands:\r\n"
                                + " exit  -  shutdown retranslator\r\n"
                                + " info  -  get information and statistics\r\n"
                                + " help  -  this help\r\n");
                          }
                      }
                    
                  
                  Thread.sleep(50);
                  }
                catch (Exception e)
                  {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                  }   
              }
          }
        catch (Exception e)
          {
            LOG.log(Level.SEVERE, e.getMessage(), e);
          }
        
        
        
        

      }

    public static void PrintInfo()
      {

      }

    public static void StopRetranslator()
      {
        LOG.info("Shutting down retranslator main thread...");
        try
          {
            for (int i = 0; i < CityLinkRTRMain.udpClientPool.size(); ++i)
              {
                if (CityLinkRTRMain.udpClientPool.get(i).getIsEnabled() > 0)
                  CityLinkRTRMain.udpClientPool.get(i).closeUDPClient();
              }

            for (int i = 0; i < CityLinkRTRMain.serialPool.size(); ++i)
              {
                if (CityLinkRTRMain.serialPool.get(i).getIsEnabled() > 0)
                  {
                    CityLinkRTRMain.serialPool.get(i).setRun(false);
                  }
              }
          }
        catch (Exception e)
          {
            LOG.log(Level.SEVERE, e.getMessage(), e);
          }
        LOG.info("Waiting external threads trmination...");
        try
          {
            Thread.sleep(1000);
          }
        catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        LOG.info("Application terminated");
        System.exit(0);
      }

  }
