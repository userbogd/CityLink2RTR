package SerialPort;

import org.apache.commons.math3.analysis.function.Log;

import com.fazecast.jSerialComm.SerialPort;

import CityLink2RTR.CityLinkEventPacket;
import CityLink2RTR.CityLinkRTRMain;
import CityLink2RTR.Helpers;
import CityLink2RTR.MainEventBufer;

public class SerialPortReader implements Runnable
  {
    public static final int EVENT_LENTH = 13;
    SerialPort comPort;
    String portName;
    int portBaudrate;
    SerialPortInstance sPortInst;
    int ReadyBytes;

    public SerialPortReader(String Port, int Baudrate, SerialPortInstance sPort)
      {
        portName = new String(Port);
        portBaudrate = Baudrate;
        sPortInst = sPort;
        Runtime.getRuntime().addShutdownHook(new Thread()
          {
            public void run()
              {
                try
                  {
                    System.out.println("Shutting down thread:" + comPort.getSystemPortName());
                    comPort.closePort();
                  }
                catch (Exception e)
                  {
                    e.printStackTrace();
                  }
              }
          });
      }

    @Override
    public void run()
      {
        while (true)
          {
            try
              {
                comPort = SerialPort.getCommPort(portName);
                comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
                comPort.setBaudRate(portBaudrate);
                comPort.openPort();
              }
            catch (Exception e)
              {
                System.out.format("Error. Can't open serial port %s\r\n", comPort.getSystemPortName());
                CityLinkRTRMain.LOG.severe(String.format("Can't open serial port %s", comPort.getSystemPortName()));
                
                e.printStackTrace();
                sPortInst.setState("ERROR");
              }

            if (comPort.isOpen())
              {
                System.out.format("Serial port %s opened OK\r\n", comPort.getPortDescription());
                sPortInst.setState("OK");
                try
                  {
                    while (true)
                      {
                        while (comPort.bytesAvailable() < EVENT_LENTH)
                          Thread.sleep(20);
                        while (ReadyBytes != comPort.bytesAvailable())
                          {
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
                catch (Exception e)
                  {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                  }
                System.out.format("Serial port %s ERROR\r\n", comPort.getSystemPortName());
                CityLinkRTRMain.LOG.severe(String.format("Can't open serial port %s", comPort.getSystemPortName()));
                try
                  {
                    comPort.closePort();
                    sPortInst.setState("ERROR");
                    Thread.sleep(10000);
                  }
                catch (InterruptedException e)
                  {
                    e.printStackTrace();
                  }
              }
            else
              {
                System.out.format("Error. Can't open serial port %s\r\n", comPort.getSystemPortName());
                CityLinkRTRMain.LOG.severe(String.format("Can't open serial port %s", comPort.getSystemPortName()));
                sPortInst.setState("ERROR");
                try
                  {
                    Thread.sleep(10000);
                  }
                catch (InterruptedException e)
                  {
                    e.printStackTrace();
                  }
              }
          }
      }
  }
