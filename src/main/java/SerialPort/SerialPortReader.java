package SerialPort;

import java.nio.charset.StandardCharsets;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import CityLink2RTR.CityLinkRTRMain;

public class SerialPortReader implements Runnable {

	SerialPort comPort;

	public SerialPortReader(String Port, int Baudrate, SerialPortInstance sPort) {
		comPort = SerialPort.getCommPort(Port);
		comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
		comPort.setBaudRate(Baudrate);
		comPort.openPort();
	}

	@Override
	public void run() {

		if (comPort.isOpen()) {
			System.out.format("Serial port %s OK\r\n", comPort.getSystemPortName());
		} else {
			System.out.format("Error. Can't open serial port %s\r\n", comPort.getSystemPortName());
			return;
		}
		try {
			while (true) {
				while (comPort.bytesAvailable() == 0)
					Thread.sleep(100);
				byte[] readBuffer = new byte[comPort.bytesAvailable()];
				comPort.readBytes(readBuffer, readBuffer.length);
				String s = new String(readBuffer, StandardCharsets.UTF_8);
				System.out.println(s);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		comPort.closePort();
		
		
	}
}
