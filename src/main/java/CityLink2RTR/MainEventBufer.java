package CityLink2RTR;

import java.util.LinkedList;
import java.util.Queue;

public class MainEventBufer
  {
    private static Queue<CityLinkEventPacket> mainDataBuffer;
    public MainEventBufer()
      {
        mainDataBuffer = new LinkedList<>();
      }

    public static synchronized int PutEvent(CityLinkEventPacket pkt)
    {
      mainDataBuffer.offer(pkt);
      return 0;
    }
    
    public static synchronized  CityLinkEventPacket PollEvent()
    {
      return mainDataBuffer.poll();
    }
    
    public static int getSize()
    {
      return mainDataBuffer.size();
    }
    
  }
