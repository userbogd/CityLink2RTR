package SerialPort;

public class SerialPortInstance
  {
    private int isEnabled;
    private String username, name;
    private int baudrate, databytes;
    private long PacketsOk, PacketsErrors;
    private String State;
    private SerialPortReader sr;
    private boolean isRun;

    public SerialPortInstance(int isEnabled, String username, String name, int baudrate, int databytes)
      {
        this.isEnabled = isEnabled;
        this.name = name;
        this.baudrate = baudrate;
        this.databytes = (databytes == 13) ? 13 : 12;
        this.PacketsOk = 0;
        this.PacketsErrors = 0;
        this.State = "ERROR";
      }

    public synchronized boolean isRun()
      {
        return isRun;
      }

    public synchronized void setRun(boolean isRun)
      {
        this.isRun = isRun;
      }

    public void startSerialReader()
      {
        sr = new SerialPortReader(this.name, this.baudrate, this.databytes,  this);
        setRun(true);
        new Thread(sr).start();
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
