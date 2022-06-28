package UDPConnections;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * A class for handling a multi-threaded instance of a UDP client
 * 
 * @author craig
 *
 */
public class ThreadedUDPClient implements Runnable
  {
    static
      {
        try (InputStream is = ThreadedUDPClient.class.getClassLoader().getResourceAsStream("logging.properties"))
          {
            LogManager.getLogManager().readConfiguration(is);

          }
        catch (IOException e)
          {
            e.printStackTrace();
          }
      }
    public static final Logger LOG = Logger.getLogger(ThreadedUDPClient.class.getName());

    private Connection connection;
    private boolean running;

    private DatagramSocket socket;
    private Thread process, send, receive;

    public ThreadedUDPClient(String addr, int port)
      {
        try
          {
            socket = new DatagramSocket();
            connection = new Connection(socket, InetAddress.getByName(addr), port, 0);
            this.init();
          }
        catch (SocketException | UnknownHostException e)
          {
            LOG.log(Level.SEVERE, e.getMessage(), e);
          }
      }

    /**
     * Initialise the client
     */
    private void init()
      {
        process = new Thread(this, "server_process");
        process.start();
      }

    /**
     * Send some data
     * 
     * @param the data
     */
    public void send(final byte[] data)
      {
        send = new Thread("Sending Thread")
          {
            public void run()
              {
                connection.send(data);
              }
          };

        send.start();
      }

    
   
    /**
     * Receive data on the given server connection
     */
   /*
    public void receive(final PacketHandler handler)
      {
        receive = new Thread("receive_thread")
          {
            public void run()
              {
                while (running)
                  {
                    byte[] buffer = new byte[1024];
                    DatagramPacket dgpacket = new DatagramPacket(buffer, buffer.length);

                    try
                      {
                        socket.receive(dgpacket);
                      }
                    catch (IOException e)
                      {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                      }

                    handler.process(new Packet(dgpacket.getData(), dgpacket.getAddress(), dgpacket.getPort()));
                  }
              }
          };

        receive.start();
      }

*/
    /**
     * Close the current connection for this client
     */
    public void close()
      {
        LOG.info(String.format("UDP client stopped with address %s", connection.getAddress()));
        connection.close();
        running = false;
      }

    @Override
    public void run()
      {
        running = true;
        LOG.info(String.format("UDP client started with address %s", connection.getAddress()));
      }

  }
