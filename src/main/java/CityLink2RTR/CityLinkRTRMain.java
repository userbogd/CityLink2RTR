package CityLink2RTR;

import java.nio.charset.Charset;

import MonitorHTTPServer.*;
import SerialListener.SerialListener;
import UDPConnections.Packet;
import UDPConnections.PacketHandler;
import UDPConnections.ThreadedUDPServer;


public class CityLinkRTRMain {
	static ThreadedUDPServer UDPServ;
	static MonitorHTTPServer HTTP;
	//static SerialListener Serial1; 
	//static SerialListener Serial2;
	public int Counter;

	public static void main(String[] args) {


		
		
		UDPServ = new ThreadedUDPServer(60500);
		UDPServ.receive(new PacketHandler() {
			int Counter;

			@Override
			public void process(Packet packet) {
				String data = new String(packet.getData());
				Counter = 0;
				if (data.length() >= 13) {
					byte[] byteArray;
					byte[] b;
					byteArray = data.getBytes(Charset.forName("Windows-1251"));
					b = new byte[13];
					for (int k = 0; k < 1000; k += 13) {
						for (int kk = 0; kk < 13; ++kk)
							b[kk] = byteArray[k + kk];
						if (b[0] != 0) {
							System.out.println(bytesToHex(b));
							++Counter;
						}
					}
				}
				System.out.println("Packet size:" + Integer.toString(Counter));
			}
		});
		
		HTTP = new MonitorHTTPServer();
		//Serial1 = new SerialListener("COM3", 115200);
		new Thread(new SerialListener("/dev/ttyUSB0", 115200)).start();
		//Serial2  = new SerialListener("COM4", 115200);
		new Thread( new SerialListener("/dev/ttyUSB1", 115200)).start();
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

}
