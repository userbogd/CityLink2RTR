package SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.fazecast.jSerialComm.SerialPort;

import CityLink2RTR.CityLinkEventPacket;
import CityLink2RTR.Helpers;
import CityLink2RTR.MainEventBufer;

public class SerialPortReader implements Runnable
  {
    public static final int EVENT_LENTH = 13;
    public static final int PORT_RESTART_INTERVAL = 5;
    SerialPort comPort;
    String portName;
    int portBaudrate;
    SerialPortInstance sPortInst;
    int ReadyBytes;

    static
      {
        try (InputStream is = SerialPortReader.class.getClassLoader().getResourceAsStream("logging.properties"))
          {
            LogManager.getLogManager().readConfiguration(is);
          }
        catch (IOException e)
          {
            e.printStackTrace();
          }
      }
    public static final Logger LOG = Logger.getLogger(SerialPortReader.class.getName());

    public SerialPortReader(String Port, int Baudrate, SerialPortInstance sPort)
      {
        portName = new String(Port);
        portBaudrate = Baudrate;
        sPortInst = sPort;
      }

    public void stop()
      {
        try
          {
            LOG.info(String.format("Stop thread of serial reader %s", portName));
            comPort.closePort();
          }
        catch (Exception e)
          {
            LOG.log(Level.SEVERE, e.getMessage(), e);
          }

      }
    
    @Override
    public void run()
      {
        while (sPortInst.isRun())
          {
            try
              {
                comPort = SerialPort.getCommPort(portName);
                comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
                comPort.setBaudRate(portBaudrate);
                comPort.openPort();
                if (comPort.isOpen())
                  {
                    LOG.info(String.format("Serial port %s opened OK", comPort.getPortDescription()));
                    sPortInst.setState("OK");
                    byte[] arr = {'A','T','Z'};
                    comPort.writeBytes(arr, 3);
                    while (true)
                      {
                        while (comPort.bytesAvailable() < EVENT_LENTH)
                          {
                            if (comPort.bytesAvailable() == -1)
                              throw new java.lang.RuntimeException("Port is closed unexpected!");
                            if (!sPortInst.isRun())
                              {
                                stop();
                                return;
                              }
                            Thread.sleep(20);
                          }
                        while (ReadyBytes != comPort.bytesAvailable())
                          {
                            if (comPort.bytesAvailable() == -1)
                              throw new java.lang.RuntimeException("Port is closed unexpected!");
                            Thread.sleep(10);
                            ReadyBytes = comPort.bytesAvailable();
                          }
                        byte[] arr1 = new byte[comPort.bytesAvailable()];
                        comPort.readBytes(arr1, arr1.length);
                        int ptr = 0;
                        while (ptr <= (arr1.length - EVENT_LENTH))
                          {
                            if (arr1[ptr] == 0x34 && arr1[ptr + 10] == 0x0D)
                              {
                                CityLinkEventPacket pt = new CityLinkEventPacket();
                                System.arraycopy(arr1, ptr, pt.rawByteArray, 0, EVENT_LENTH);
                                int tmp[] = new int[EVENT_LENTH];
                                Helpers.SignedBytesToUnsignedInt(pt.rawByteArray, tmp);
                                if (Helpers.CheckCityLinkEventError(tmp) == 0)
                                  {
                                    MainEventBufer.PutEvent(pt);
                                    sPortInst.incPacketsOK();
                                    System.out.print(Helpers.bytesToHex(pt.rawByteArray) + "\r\n");
                                  }
                                else
                                  {
                                    sPortInst.incPacketsError();
                                  }
                                ptr += EVENT_LENTH;
                              }
                            else
                              ++ptr;
                          }
                      }
                  }
                else
                  {
                    throw new java.lang.RuntimeException(String.format("Can't open port %s", portName));
                  }
              }
            catch (Exception e)
              {
                LOG.log(Level.SEVERE, e.getMessage(), e);
                comPort.closePort();
                sPortInst.setState("ERROR");
              }

            try
              {
                int i = PORT_RESTART_INTERVAL * 10;
                while (--i > 0 && sPortInst.isRun())
                  Thread.sleep(100);
              }
            catch (InterruptedException ex)
              {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
              }
          }

        stop();

      }

  }
