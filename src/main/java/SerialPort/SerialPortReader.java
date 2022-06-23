package SerialPort;

import java.nio.charset.StandardCharsets;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

import CityLink2RTR.CityLinkEventPacket;
import CityLink2RTR.CityLinkRTRMain;
import CityLink2RTR.Helpers;
import CityLink2RTR.MainEventBufer;

import java.util.Timer;
import java.util.TimerTask;

class RestartSerialAfterFail extends TimerTask
  {
    @Override
    public void run()
      {

      }
  }

public class SerialPortReader implements Runnable
  {
    public static final int EVENT_LENTH = 13;
    SerialPort comPort;
    SerialPortInstance sPortInst;
    int ReadyBytes;

    public SerialPortReader(String Port, int Baudrate, SerialPortInstance sPort)
      {
        sPortInst = sPort;
        try
          {
            comPort = SerialPort.getCommPort(Port);
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            comPort.setBaudRate(Baudrate);
            comPort.openPort();
          } catch (Exception e)
          {
            e.printStackTrace();
            System.out.print(e.getMessage());
          }
        
        
        Runtime.getRuntime().addShutdownHook(new Thread()
          {
            public void run()
              {
                try
                  {
                    Thread.sleep(200);
                    comPort.closePort();
                    System.out.println("Shutting down thread:" + comPort.getPortDescription());
                    // some cleaning up code...
                  } catch (InterruptedException e)
                  {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                  }
              }
          });
        
      }

    @Override
    public void run()
      {

        if (comPort.isOpen())
          {
            System.out.format("Serial port %s opened OK\r\n", comPort.getPortDescription());
            sPortInst.State = "OK";
          }
        else
          {
            System.out.format("Error. Can't open serial port %s\r\n", comPort.getSystemPortName());
            sPortInst.State = "ERROR";
            return;
          }
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
                //System.out.print(Helpers.bytesToHex(arr1) + "\r\n");  //raw data received
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
                            ++sPortInst.PacketsOk;
                            System.out.print(Helpers.bytesToHex(pt.rawByteArray) + "\r\n");
                          }
                        else
                          {
                            ++sPortInst.PacketsErrors;
                          }
                        ptr += EVENT_LENTH;
                      }
                    else
                      ++ptr;
                  }
                
              }
          } catch (Exception e)
          {
            System.out.println(e.getMessage());
          }
        comPort.closePort();

      }
  }
