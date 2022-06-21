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

    public SerialPortReader(String Port, int Baudrate, SerialPortInstance sPort)
      {
        sPortInst = sPort;
        comPort = SerialPort.getCommPort(Port);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        comPort.setBaudRate(Baudrate);
        try
          {
            comPort.openPort();
          } catch (Exception e)
          {
            e.printStackTrace();
          }
      }

    @Override
    public void run()
      {

        if (comPort.isOpen())
          {
            System.out.format("Serial port %s OK\r\n", comPort.getSystemPortName());
            sPortInst.State = "OK";
          } else
          {
            System.out.format("Error. Can't open serial port %s\r\n", comPort.getSystemPortName());
            sPortInst.State = "ERROR";
            return;
          }
        try
          {
            while (true)
              {
                while (comPort.bytesAvailable() < EVENT_LENTH || comPort.bytesAvailable() % EVENT_LENTH != 0)
                  Thread.sleep(20);
                byte[] dt = new byte[comPort.bytesAvailable()];
                comPort.readBytes(dt, dt.length);
                for (int k = 0; k < dt.length; k += EVENT_LENTH)
                  {
                    CityLinkEventPacket pt = new CityLinkEventPacket();
                    System.arraycopy(dt, k, pt.rawByteArray, 0, EVENT_LENTH);
                    
                    int u[] = new int[EVENT_LENTH];
                    Helpers.SignedBytesToUnsignedInt(pt.rawByteArray, u);
                    if (Helpers.CheckCityLinkEventError(u) == 0)
                      {
                        MainEventBufer.PutEvent(pt);                        
                        ++sPortInst.PacketsOk;
                        System.out.print(Helpers.bytesToHex(pt.rawByteArray)+"\r\n");
                      } else
                      {
                        ++sPortInst.PacketsErrors;
                      }

                  }
              }
          } catch (Exception e)
          {
            System.out.println(e.getMessage());
          }
        comPort.closePort();

      }
  }
