package CityLink2RTR;

import java.net.SocketException;

import data.Connection;
import data.Packet;
import data.PacketHandler;
import data.UID;
import java.util.Arrays;

import CityLink2RTR.ThreadedUDPClient;


public class CityLinkRTRMain {
	static ThreadedUDPServer UDPServ;
	
	public static void main(String[] args) {
		UDPServ = new ThreadedUDPServer(60500);
		UDPServ.receive(new PacketHandler() {
			@Override
			public void process(Packet packet) {
				String data = new String(packet.getData());
			    if(data.length() >=13)
			    {
			        byte[] byteArray;
			        byte[] b;
			        // convert the string to a byte array
			        // using platform's default charset
			        byteArray = data.getBytes();
			        b = new byte[13];
			        for (int k=0; k<1000; k+=13)
			        {
			        	for(int kk=0;kk<13;++kk)
			        		b[kk] = byteArray[k+kk];
			        
			        if(b[0] != 0)
			        	System.out.println(bytesToHex(b));
			        }
			    }
			}
		});
		
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
