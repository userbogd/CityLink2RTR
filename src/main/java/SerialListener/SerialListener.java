package SerialListener;

import java.nio.charset.StandardCharsets;

import com.fazecast.jSerialComm.SerialPort;

public class SerialListener implements Runnable {
	SerialPort comPort1;

	public SerialListener() {
		try {
		comPort1 = SerialPort.getCommPort("COM3");
		comPort1.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
		comPort1.setBaudRate(115200);
		comPort1.openPort();
		System.out.println("Open serial port");
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		run();
	}

	@Override
	public void run() {

		try {
			while (true) {
				while (comPort1.bytesAvailable() == 0)
					Thread.sleep(100);
				byte[] readBuffer = new byte[comPort1.bytesAvailable()];
				int numRead = comPort1.readBytes(readBuffer, readBuffer.length);
				String s = new String(readBuffer, StandardCharsets.UTF_8);
				System.out.println(s);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		comPort1.closePort();
	}
}
