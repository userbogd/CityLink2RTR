package CityLink2RTR;

import org.checkerframework.checker.signedness.qual.Unsigned;

import com.google.common.primitives.UnsignedBytes;

public class Helpers
  {

    public Helpers()
      {
        // TODO Auto-generated constructor stub
      }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes)
      {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
          {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
          }
        return new String(hexChars);
      }

    public static void SignedBytesToUnsignedInt(byte in[], int out[])
      {
        for (int i = 0; i < in.length; ++i)
          out[i] = Byte.toUnsignedInt(in[i]);
      }

    public static int CheckCityLinkEventError(int[] e)
      {
        if (e.length != 13)
          return -100; // wrong data format
        int crc = (e[0] + e[1] + e[2] + e[3] + e[4] + e[5] + e[6] + e[7] + e[8]) & 0xff;
        if (e[9] != crc)
          return -1;
        if (e[0] != 0x34)
          return -2;
        if (e[10] != 0x0D)
          return -3;
        return 0;
      }

  }
