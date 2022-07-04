package UDPConnections;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * A class for handling a multi-threaded instance of a UDP server
 * 
 * @author craig
 * @version 0.1
 */

public class ThreadedUDPServer implements Runnable
  {
    /* Server information */
    private int port;
    private InetAddress BindAdr;
    private DatagramSocket socket;
    private boolean running;

    /* Threads */
    private Thread send, receive, process;

    /* Client relevant */
    public static ArrayList<Connection> CLIENTS = new ArrayList<Connection>();

    static
      {
        try (InputStream is = ThreadedUDPServer.class.getClassLoader().getResourceAsStream("logging.properties"))
          {
            LogManager.getLogManager().readConfiguration(is);

          } catch (IOException e)
          {
            e.printStackTrace();
          }
      }
    public static final Logger LOG = Logger.getLogger(ThreadedUDPServer.class.getName());

    /**
     * Construct a new instance of a multi-threaded udp server
     * 
     * @param port
     */
    public ThreadedUDPServer(int port, String bindip)
      {
        this.port = port;
        try
          {
            this.BindAdr = InetAddress.getByName(bindip) ;
          }
        catch (UnknownHostException e1)
          {
            // TODO Auto-generated catch block
            LOG.log(Level.SEVERE, e1.getMessage(), e1);
          }
        try
          {
            
            this.init();
          } catch (SocketException e)
          {
            e.printStackTrace();
            LOG.log(Level.SEVERE, e.getMessage(), e);
          }
      }

    /**
     * Initialise the server
     * 
     * @throws SocketException
     */
    public void init() throws SocketException
      {
        this.socket = new DatagramSocket(this.port, this.BindAdr);
        
        process = new Thread(this, "server_process");
        process.start();
      }

    /**
     * Get the port that the server is binded to
     * 
     * @return port
     */
    public int getPort()
      {
        return port;
      }

    /**
     * Send a packet to a client
     * 
     * @param packet
     * @param client
     */
    public void send(final Packet packet)
      {
        send = new Thread("send_thread")
          {
            public void run()
              {
                DatagramPacket dgpack = new DatagramPacket(packet.getData(), packet.getData().length, packet.getAddr(),
                    packet.getPort());

                try
                  {
                    socket.send(dgpack);
                  } catch (IOException e)
                  {
                    e.printStackTrace();
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                  }
              }
          };

        send.start();
      }

    /**
     * Send a packet to all connected clients
     * 
     * @param packet
     */
    public void broadcast(byte[] data)
      {
        for (Connection c : CLIENTS)
          {
            send(new Packet(data, c.getAddress(), c.getPort()));
          }
      }

    /**
     * Wait for input... and use a PacketHandler to process the packet
     * 
     * @param handler The packet handler
     */
    public void receive(final PacketHandler handler)
      {
        receive = new Thread("receive_thread")
          {
            public void run()
              {
                while (running)
                  {
                    byte[] buffer = new byte[1300];
                    DatagramPacket dgpacket = new DatagramPacket(buffer, buffer.length);

                    try
                      {

                        socket.receive(dgpacket);
                      } catch (IOException e)
                      {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                      }

                    // handler.process(new Packet(dgpacket.getData(), new Connection(socket,
                    // dgpacket.getAddress(), dgpacket.getPort(), UID.getIdentifier())));
                    handler.process(new Packet(dgpacket.getData(), dgpacket.getAddress(), dgpacket.getPort()));
                  }
              }
          };

        receive.start();
      }

    /**
     * The run method of this runnable thread object.
     */
    
  
    public void run()
      {
        running = true;
        LOG.info(String.format("UDP server started on ip %s port %s", BindAdr.getCanonicalHostName(), port));
      }

  }
