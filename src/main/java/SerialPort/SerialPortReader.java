package SerialPort;

import java.nio.charset.StandardCharsets;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

import CityLink2RTR.CityLinkEventPacket;
import CityLink2RTR.CityLinkRTRMain;
import CityLink2RTR.MainEventBufer;

public class SerialPortReader implements Runnable
  {
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
          } 
        catch (Exception e)
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
                while (comPort.bytesAvailable() == 0)
                  Thread.sleep(100);
                byte[] readBuffer = new byte[comPort.bytesAvailable()];
                comPort.readBytes(readBuffer, readBuffer.length);
                String s = new String(readBuffer, StandardCharsets.UTF_8);
                System.out.println(s);
                CityLinkEventPacket pkt = new CityLinkEventPacket();
                MainEventBufer.PutEvent(pkt);
              }
          } catch (Exception e)
          {
            System.out.println(e.getMessage());
          }
        comPort.closePort();

      }
  }
