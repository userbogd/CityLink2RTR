package CityLink2RTR;

public class Statistics
  {
    private long ReceivedPacketsUDP;
    private long TransmittedPacketsUDP;

    private long ErrorPacketsUDP;
    public Statistics()
      {
        ReceivedPacketsUDP = 0;
        ErrorPacketsUDP = 0;
      }
    
    public synchronized void IncrReceivedPacketsUDP(int i)
      {
        ReceivedPacketsUDP += i;
      }
    
    public synchronized void IncrTransmittedPacketsUDP(int i)
      {
        TransmittedPacketsUDP += i;
      }
    
    public synchronized void IncrErrorPacketsUDP(int i)
      {
        TransmittedPacketsUDP += i;
      }
    
    public synchronized long getReceivedPacketsUDP()
      {
        return ReceivedPacketsUDP;
      }
    public synchronized void setReceivedPacketsUDP(long receivedPacketsUDP)
      {
        ReceivedPacketsUDP = receivedPacketsUDP;
      }
    public synchronized long getErrorPacketsUDP()
      {
        return ErrorPacketsUDP;
      }
    public synchronized void setErrorPacketsUDP(long errorPacketsUDP)
      {
        ErrorPacketsUDP = errorPacketsUDP;
      }
    public synchronized long getTransmittedPacketsUDP()
      {
        return TransmittedPacketsUDP;
      }

    public synchronized void setTransmittedPacketsUDP(long transmittedPacketsUDP)
      {
        TransmittedPacketsUDP = transmittedPacketsUDP;
      }
    
    
  }
