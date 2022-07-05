package CityLink2RTR;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
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

import java.util.Scanner;

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
                ini.put("UDPSERVER", "name", "Server name 1");
                ini.put("UDPSERVER", "bindip", "127.0.0.1");
                ini.put("UDPSERVER", "port", 60600);

                ini.put("UDPCLIENT", "enabled", 0);
                ini.put("UDPCLIENT", "name", "Client name 1");
                ini.put("UDPCLIENT", "url", "127.0.0.1");
                ini.put("UDPCLIENT", "port", 60501);

                ini.put("SERIAL", "enabled", 0);
                ini.put("SERIAL", "name", "Serial port name 1");
                ini.put("SERIAL", "portname", "/dev/ttyUSB0");
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
                sec.get("name", i),
                sec.get("portname", i),
                Integer.parseInt(sec.get("baudrate", i)));
            serialPool.add(sPort);
            if (sPort.getIsEnabled() > 0)
              sPort.startSerialReader();
          }

        // Read all UDPSERVER sections and start threads
        sec = ini.get("UDPSERVER");
        for (int i = 0; i < sec.length("enabled"); ++i)
          {
            ServerUDPInstance udpServer = new ServerUDPInstance(Integer.parseInt(sec.get("enabled", i)),
                sec.get("name", i),
                sec.get("bindip", i),
                Integer.parseInt(sec.get("port", i)));
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
                sec.get("name", i),
                sec.get("url", i),
                Integer.parseInt(sec.get("port", i)));
            udpClientPool.add(udpClient);
            if (udpClient.getIsEnabled() > 0)
              udpClient.startUDPClient();

          }

        try
          {
            udpClientSendTimer.schedule(udpClientSendTimerTask, 200, 200);
            LOG.info("Retranslator initialise complete");
            try (Scanner in = new Scanner(System.in))
              {
                while (true)
                  {
                    try
                      {

                        String input = in.nextLine();
                        if (input != null)
                          {
                            if (input.equals("exit"))
                              {
                                StopRetranslator();
                              }
                            else if (input.equals("quit"))
                              {
                                StopRetranslator();
                              }
                            else if (input.equals("info"))
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

                      }
                    catch (Exception e)
                      {
                        //LOG.log(Level.SEVERE, e.getMessage(), e);
                      }

                    Thread.sleep(50);

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
        StringBuilder builder = new StringBuilder();
        String rtrName = CityLinkRTRMain.ini.get("RTR", "username");
        String rtrVer = CityLinkRTRMain.ini.get("RTR", "version");
        String javaVersion = System.getProperty("java.version");
        builder.append("Retranslator name:" + rtrName + "\r\n");
        builder.append("Software version:" + rtrVer + "\r\n");
        builder.append("JAVA SDK version:" + javaVersion + "\r\n");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();
        builder.append("Current time:" + formatter.format(date) + "\r\n");
        builder.append("Started at:" + formatter.format(CityLinkRTRMain.StartDate)
            + "\r\n");
        long delta = (date.getTime() - CityLinkRTRMain.StartDate.getTime()) / 1000;
        long d = delta / 86400;
        long h = (delta % 86400) / 3600;
        long m = ((delta % 86400) % 3600) / 60;
        long s = ((delta % 86400) % 3600) % 60;
        String dur = String.format("%dD %d:%02d:%02d", d, h, m, s);
        builder.append("Uptime:" + dur + "\r\n");
        
        if (CityLinkRTRMain.ini.get("HTTP", "enabled", int.class) > 0)
          {
            builder.append("HTTP web interface started on port "
                + CityLinkRTRMain.ini.get("HTTP", "httpport", int.class) + "\r\n");
          }

        builder.append("INPUT SERIAL:\r\n");
        for (int i = 0; i < CityLinkRTRMain.serialPool.size(); ++i)
          {
            if (CityLinkRTRMain.serialPool.get(i).getIsEnabled() > 0)
              {
                String pName = CityLinkRTRMain.serialPool.get(i).getName();
                String pState = CityLinkRTRMain.serialPool.get(i).getState();
                String pOk = String.valueOf(CityLinkRTRMain.serialPool.get(i).getPacketsOk());
                String pErr = String.valueOf(CityLinkRTRMain.serialPool.get(i).getPacketsErrors());

                builder.append("Serial port ");
                builder.append(pName);
                if (pState.equals("OK"))
                  builder.append(" [State:" + pState + "; ");
                else
                  builder.append(" [State:" + pState + "; ");
                builder.append("Packets OK:" + pOk + "; ");
                builder.append("Errors:" + pErr + "]\r\n");
              }
          }
        builder.append("INPUT UDP:\r\n");
        for (int i = 0; i < CityLinkRTRMain.udpServerPool.size(); ++i)
          {
            if (CityLinkRTRMain.udpServerPool.get(i).getIsEnabled() > 0)
              {
                String pName = CityLinkRTRMain.udpServerPool.get(i).getName();
                String pBindIP = CityLinkRTRMain.udpServerPool.get(i).getBindIP();
                String pPort = String.valueOf(CityLinkRTRMain.udpServerPool.get(i).getPort());
                String pOk = String.valueOf(CityLinkRTRMain.udpServerPool.get(i).getPacketsOk());
                String pErr = String.valueOf(CityLinkRTRMain.udpServerPool.get(i).getPacketsErrors());
                builder.append("UDP receiver");
                builder.append(pName);
                builder.append(" [BindIP:" + pBindIP + "; ");
                builder.append("Port:" + pPort + "; ");
                builder.append("Packets OK:" + pOk + "; ");
                builder.append("Errors:" + pErr + "]\r\n");
              }
          }

        builder.append("OUTPUT:\r\n");
        for (int i = 0; i < CityLinkRTRMain.udpClientPool.size(); ++i)
          {
            if (CityLinkRTRMain.udpClientPool.get(i).getIsEnabled() > 0)
              {
                String pName = CityLinkRTRMain.udpClientPool.get(i).getName();
                String pURL = CityLinkRTRMain.udpClientPool.get(i).getURL();
                String pPort = String.valueOf(CityLinkRTRMain.udpClientPool.get(i).getPort());
                String pOk = String.valueOf(CityLinkRTRMain.udpClientPool.get(i).getPacketsOk());
                String pErr = String.valueOf(CityLinkRTRMain.udpClientPool.get(i).getPacketsErrors());
                builder.append("UDP sender");
                builder.append(pName);
                builder.append(" [URL:" + pURL + "; ");
                builder.append("Port:" + pPort + "; ");
                builder.append("Packets OK:" + pOk + "; ");
                builder.append("Errors:" + pErr + "]\r\n");
              }
          }
        
        System.out.println(builder.toString());
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
