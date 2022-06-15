package SerialListener;

import java.nio.charset.StandardCharsets;
import com.fazecast.jSerialComm.SerialPort;

public class SerialListener implements Runnable {

	SerialPort comPort;

	public SerialListener(String Port, int Baudrate) {
		
		try {
			comPort = SerialPort.getCommPort(Port);
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
			comPort.setBaudRate(Baudrate);
			comPort.openPort();
			System.out.format("Open serial port %s\r\n", Port);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
	}

	@Override
	public void run() {

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
