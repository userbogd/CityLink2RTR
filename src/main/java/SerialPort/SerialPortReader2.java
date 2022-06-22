package SerialPort;

import java.io.IOException;
import java.io.InputStream;

import gnu.io.*;


public class SerialPortReader2 
  {

    public SerialPortReader2()
      {
        
      }

    static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }
    
   public  static void listPorts()
    {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()) );
        }        
    }
    
    public void connect (String portname) throws Exception
    {
      CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("COM3");
      if ( portIdentifier.isCurrentlyOwned() )
      {
          System.out.println("Error: Port is currently in use");
      }
      else
      {
          CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
          
          if ( commPort instanceof SerialPort )
          {
              SerialPort serialPort = (SerialPort) commPort;
              serialPort.setSerialPortParams(19200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
              
              InputStream in = serialPort.getInputStream();
              (new Thread(new SerialReader(in))).start();


          }
          else
          {
              System.out.println("Error: Only serial ports are handled by this example.");
          }
      }  
    }
    
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                    System.out.print(new String(buffer,0,len));
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }
    


  }
