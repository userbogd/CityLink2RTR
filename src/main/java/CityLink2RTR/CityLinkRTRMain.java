package CityLink2RTR;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.prefs.Preferences;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

import MonitorHTTPServer.*;
import SerialListener.SerialListener;
import UDPConnections.Packet;
import UDPConnections.PacketHandler;
import UDPConnections.ThreadedUDPServer;

public class CityLinkRTRMain {
	static ThreadedUDPServer UDPServ;
	static MonitorHTTPServer HTTP;
	public int Counter;
	public static Preferences prefs;
	public static final String FILENAME = "rtrconfig.ini";

	public static void main(String[] args) {

		String filename = (args.length > 0) ? args[0] : FILENAME;
		File conf = new File(filename);
		Ini ini;
		if (!conf.exists()) {
			System.out.println("Conf file not found. Created default");
			try {
				conf.createNewFile(); 
				ini = new Ini(conf);
				ini.getConfig().setMultiSection(true);
				ini.getConfig().setMultiOption(true);
				ini.put("RTR", "version", "1.00.00");
				ini.put("RTR", "name", "Retranslator #1 at location");

				ini.put("HTTP", "enabled", 1);
				ini.put("HTTP", "httpport", 8080);
				ini.put("HTTP", "refreshrate", 5);

				ini.put("UDPSERVER", "enabled", 0);
				ini.put("UDPSERVER", "udpport", 60500);

				ini.put("UDPCLIENT", "enabled", 0);
				ini.put("UDPCLIENT", "name", "Name1");
				ini.put("UDPCLIENT", "url", "127.0.0.1");
				ini.put("UDPCLIENT", "port", 60500);

				ini.put("UDPCLIENT", "enabled", 0);
				ini.put("UDPCLIENT", "name", "Name2");
				ini.put("UDPCLIENT", "url", "127.0.0.1");
				ini.put("UDPCLIENT", "port", 60501);

				ini.put("SERIAL", "enabled", 0);
				ini.put("SERIAL", "name", "/dev/ttyUSB0");
				ini.put("SERIAL", "baudrate", 19200);

				ini.put("SERIAL", "enabled", 0);
				ini.put("SERIAL", "name", "/dev/ttyUSB1");
				ini.put("SERIAL", "baudrate", 19200);

				ini.store();
			} catch (InvalidFileFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			ini = new Ini(conf);
			ini.getConfig().setMultiSection(true);
			ini.getConfig().setMultiOption(true);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Default ini file creation ERROR");
			return;
		}

		if (ini.get("UDPSERVER", "enabled", int.class) > 0) {
			UDPServ = new ThreadedUDPServer(ini.get("UDPSERVER", "udpport", int.class));
			UDPServ.receive(new PacketHandler() {
				@Override
				public void process(Packet packet) {
					String data = new String(packet.getData());
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
							}
						}
					}
				}
			});
		}

		if (ini.get("HTTP", "enabled", int.class) > 0)
			HTTP = new MonitorHTTPServer();

		Section sec = ini.get("SERIAL");
		int[] en = sec.getAll("enabled", int[].class);
		String[] nm = sec.getAll("name", String[].class);
		int[] br = sec.getAll("baudrate", int[].class);
		for (int i = 0; i < en.length; ++i) {
			if (en[i] > 0)
				new Thread(new SerialListener(nm[i], br[i]));
		}
		sec = ini.get("UDPCLIENT");
		int[] uen = sec.getAll("enabled", int[].class);
		String[] unm = sec.getAll("name", String[].class);
		String[] url = sec.getAll("url", String[].class);
		int[] uport = sec.getAll("port", int[].class);

		for (int i = 0; i < uen.length; ++i) {

		}

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
