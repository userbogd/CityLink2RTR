package MonitorHTTPServer;

import com.google.common.base.Charsets;
import com.sun.net.httpserver.*;

import CityLink2RTR.CityLinkRTRMain;
import CityLink2RTR.MainEventBufer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MonitorHTTPServer
  {
    static
      {
        try (InputStream is = MonitorHTTPServer.class.getClassLoader().getResourceAsStream("logging.properties"))
          {
            LogManager.getLogManager().readConfiguration(is);

          } catch (IOException e)
          {
            e.printStackTrace();
          }
      }
    public static final Logger LOG = Logger.getLogger(MonitorHTTPServer.class.getName());

    public MonitorHTTPServer(int port)
      {

        HttpServer server;
        try
          {
            server = HttpServer.create();
            server.bind(new InetSocketAddress(port), 0);
            server.setExecutor(null);
            server.start();
            LOG.info(String.format("HTTP server started on port %d", port));
            HttpContext context = server.createContext("/", new EchoHandler());
            context.setAuthenticator(new Auth());
          } catch (IOException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        // TODO Auto-generated constructor stub
      }

    static class EchoHandler implements HttpHandler
      {
        @Override
        public void handle(HttpExchange exchange) throws IOException
          {
            StringBuilder builder = new StringBuilder();
            /*
             * System.out.format("Got %s request from %s\r\n", exchange.getRequestMethod(),
             * exchange.getRemoteAddress());
             */

            byte[] buffer = new byte[1024];
            int reqlength = exchange.getRequestBody().read(buffer);

            if (reqlength > 0)
              {
                String s = new String(buffer).substring(0, reqlength);
                String res = new String("reset_btn=Reset");
                String stop = new String ("stop_btn=Stop");
                
                if (s.equals(res))
                  {
                    for (int i = 0; i < CityLinkRTRMain.serialPool.size(); ++i)
                      {
                        CityLinkRTRMain.serialPool.get(i).setPacketsOk(0);
                        CityLinkRTRMain.serialPool.get(i).setPacketsErrors(0);
                      }

                    for (int i = 0; i < CityLinkRTRMain.udpServerPool.size(); ++i)
                      {
                        CityLinkRTRMain.udpServerPool.get(i).setPacketsOk(0);
                        CityLinkRTRMain.udpServerPool.get(i).setPacketsErrors(0);
                      }
                    for (int i = 0; i < CityLinkRTRMain.udpClientPool.size(); ++i)
                      {
                        CityLinkRTRMain.udpClientPool.get(i).setPacketsOk(0);
                        CityLinkRTRMain.udpClientPool.get(i).setPacketsErrors(0);
                      }
                  }

              
                if (s.equals(stop))
                  {
                    CityLinkRTRMain.StopRetranslator();
                    
                  }
              }

            

            String rtrName = CityLinkRTRMain.ini.get("RTR", "username");
            String rtrVer = CityLinkRTRMain.ini.get("RTR", "version");
            String javaVersion = System.getProperty("java.version");

            builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n");
            builder.append("<head><title>TRS RTR Ver_" + rtrVer + "</title>\r\n");
            builder.append("<meta http-equiv='Refresh' content='10' charset='utf-8'/></head>\r\n");
            builder.append("<body>\r\n");
            builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>\r\n");
            builder.append(
                "<tr><td align='left'><font size = '+2' face='Monospace' ><b>Software serial to UDP retranslator 'CityLink'  JAVA edition</b></font></td></tr>\r\n");
            builder.append("</table>\r\n");
            builder
                .append("<table align = 'center' width='800' border ='1' cellspacing='2' cellpadding='2'><tr><td>\r\n");
            builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>\r\n");
            builder.append("<tr><font size = '+1' face='Monospace' >\r\n");
            builder.append("Retranslator name: &nbsp; <font color=#006699><b>" + rtrName + "</font></b><br>\r\n");
            builder.append("Software version: &nbsp; <font color=#006699><b>" + rtrVer + "</font></b><br>\r\n");
            builder.append("JAVA SDK version: &nbsp; <font color=#006699><b>" + javaVersion + "</font></b><br>\r\n");
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date date = new Date();
            builder.append("Current time: <font color=#006699><b>" + formatter.format(date) + "</font></b><br>\r\n");
            builder.append("Started at: <font color=#006699><b>" + formatter.format(CityLinkRTRMain.StartDate)
                + "</font></b><br>\r\n");
            long delta = (date.getTime() - CityLinkRTRMain.StartDate.getTime()) / 1000;
            long d = delta / 86400;
            long h = (delta % 86400) / 3600;
            long m = ((delta % 86400) % 3600) / 60;
            long s = ((delta % 86400) % 3600) % 60;
            String dur = String.format("%dD %d:%02d:%02d", d, h, m, s);
            builder.append("Uptime: <font color=#006699><b>" + dur + "</font></b><br><br>\r\n");

            if (CityLinkRTRMain.ini.get("HTTP", "enabled", int.class) > 0)
              {
                builder.append("HTTP web interface started on port <font color=#006699><b>"
                    + CityLinkRTRMain.ini.get("HTTP", "httpport", int.class) + "</b></font><br><br>\r\n");
              }

            builder.append("<b>INPUT SERIAL:</b><br>\r\n");
            for (int i = 0; i < CityLinkRTRMain.serialPool.size(); ++i)
              {
                if (CityLinkRTRMain.serialPool.get(i).getIsEnabled() > 0)
                  {
                    String pName = CityLinkRTRMain.serialPool.get(i).getName();
                    String pState = CityLinkRTRMain.serialPool.get(i).getState();
                    String pOk = String.valueOf(CityLinkRTRMain.serialPool.get(i).getPacketsOk());
                    String pErr = String.valueOf(CityLinkRTRMain.serialPool.get(i).getPacketsErrors());

                    builder.append("Serial port <font color=#006699><b>");
                    builder.append(pName);
                    if (pState.equals("OK"))
                      builder.append(" [State:<font color=#009900>" + pState + "</font>; ");
                    else
                      builder.append(" [State:<font color=#ff0000>" + pState + "</font>; ");
                    builder.append("Packets OK:<font color=#009900>" + pOk + "</font>; ");
                    builder.append("Errors:<font color=#ff0000>" + pErr + "</font>]</font></color></b><br>\r\n");
                  }
              }

            builder.append("<br>");
            builder.append("<b>INPUT UDP:</b><br>\r\n");
            for (int i = 0; i < CityLinkRTRMain.udpServerPool.size(); ++i)
              {
                if (CityLinkRTRMain.udpServerPool.get(i).getIsEnabled() > 0)
                  {
                    String pName = CityLinkRTRMain.udpServerPool.get(i).getName();
                    String pPort = String.valueOf(CityLinkRTRMain.udpServerPool.get(i).getPort());
                    String pOk = String.valueOf(CityLinkRTRMain.udpServerPool.get(i).getPacketsOk());
                    String pErr = String.valueOf(CityLinkRTRMain.udpServerPool.get(i).getPacketsErrors());
                    builder.append("UDP receiver <font color=#006699><b>");
                    builder.append(pName);
                    builder.append(" [Port:" + pPort + "; ");
                    builder.append("Packets OK:<font color=#009900>" + pOk + "</font>; ");
                    builder.append("Errors:<font color=#ff0000>" + pErr + "</font>]</font></color></b><br>\r\n");
                  }
              }

            builder.append("<br>");
            builder.append("<b>OUTPUT:</b><br>\r\n");
            for (int i = 0; i < CityLinkRTRMain.udpClientPool.size(); ++i)
              {
                if (CityLinkRTRMain.udpClientPool.get(i).getIsEnabled() > 0)
                  {
                    String pName = CityLinkRTRMain.udpClientPool.get(i).getName();
                    String pURL = CityLinkRTRMain.udpClientPool.get(i).getURL();
                    String pPort = String.valueOf(CityLinkRTRMain.udpClientPool.get(i).getPort());
                    String pOk = String.valueOf(CityLinkRTRMain.udpClientPool.get(i).getPacketsOk());
                    String pErr = String.valueOf(CityLinkRTRMain.udpClientPool.get(i).getPacketsErrors());
                    builder.append("UDP sender <font color=#006699><b>");
                    builder.append(pName);
                    builder.append(" [URL:" + pURL + "; ");
                    builder.append("Port:" + pPort + "; ");
                    builder.append("Packets OK:<font color=#009900>" + pOk + "</font>; ");
                    builder.append("Errors:<font color=#ff0000>" + pErr + "</font>]</font></color></b><br>\r\n");
                  }
              }
            builder.append("<br>");
            builder.append(
                "<form method='post'><tr><td>&nbsp<button type='submit' name ='refr_button' value='Refresh'>Refrash</button></td>\r\n");
            builder.append(
                "&nbsp<td><button type='submit' name ='reset_btn' value='Reset'>Reset counters</button></td><td><button type='submit' name ='stop_btn' value='Stop'>Stop</button></td></tr></form>\r\n");

            builder.append("</table>\r\n");
            builder.append("</td></tr></table>\r\n");
            builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>\r\n");
            builder.append("<tr><td align='right'><font face='Monospace'>&copy; 2022 'TRS LLC' </font></td></tr>\r\n");
            builder.append("</table></body></html>\r\n");

            byte[] bytes = (builder.toString()).getBytes(Charsets.UTF_8);

            exchange.getResponseHeaders().set("Content-type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().add("Connection", "close");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
          }
      }

    static class Auth extends Authenticator
      {
        @Override
        public Result authenticate(HttpExchange httpExchange)
          {
            if ("/forbidden".equals(httpExchange.getRequestURI().toString()))
              return new Failure(403);
            else
              return new Success(new HttpPrincipal("c0nst", "realm"));
          }
      }
  }