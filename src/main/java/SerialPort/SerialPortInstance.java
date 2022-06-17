package SerialPort;

public class SerialPortInstance
  {
    public int isEnabled;
    public String name;
    public int baudrate;
    public boolean isDataPresent;
    public int PacketErrors;
    public String State;

    public SerialPortInstance(int isEnabled, String name, int baudrate)
      {
        this.isEnabled = isEnabled;
        this.name = name;
        this.baudrate = baudrate;
        this.isDataPresent = false;
        this.PacketErrors = 0;
        this.State = "ERROR";
      }

    public void startSerialReader()
      {
        new Thread(new SerialPortReader(this.name, this.baudrate, this)).start();
      }

  }
