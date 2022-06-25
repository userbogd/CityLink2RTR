package SerialPort;

import java.util.logging.Logger;

import org.w3c.dom.html.HTMLOListElement;

import CityLink2RTR.CityLinkRTRMain;

public class SerialPortInstance
  {
    private int isEnabled;
    private String username,name;
    private int baudrate;
    private long PacketsOk,PacketsErrors;
    private String State;

    public SerialPortInstance(int isEnabled, String username, String name, int baudrate)
      {
        this.isEnabled = isEnabled;
        this.name = name;
        this.baudrate = baudrate;
        this.PacketsOk = 0;
        this.PacketsErrors = 0;
        this.State = "ERROR";
      }

    public void startSerialReader()
      {
        new Thread(new SerialPortReader(this.name, this.baudrate, this)).start();
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

    public synchronized String getUsername()
      {
        return username;
      }

    public synchronized void setUsername(String username)
      {
        this.username = username;
      }

    public synchronized String getName()
      {
        return name;
      }

    public synchronized void setName(String name)
      {
        this.name = name;
      }

    public synchronized int getBaudrate()
      {
        return baudrate;
      }

    public synchronized void setBaudrate(int baudrate)
      {
        this.baudrate = baudrate;
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

    public synchronized String getState()
      {
        return State;
      }

    public synchronized void setState(String state)
      {
        State = state;
      }



  }
