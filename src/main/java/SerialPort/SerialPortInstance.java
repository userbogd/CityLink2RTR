package SerialPort;

public class SerialPortInstance
  {
    public int isEnabled;
    public String name;
    public int baudrate;
    public long PacketsOk;
    public long PacketsErrors;
    public String State;

    public SerialPortInstance(int isEnabled, String name, int baudrate)
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

  }
