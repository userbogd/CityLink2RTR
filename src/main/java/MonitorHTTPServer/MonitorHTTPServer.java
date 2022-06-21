package MonitorHTTPServer;

import com.google.common.base.Charsets;
import com.sun.net.httpserver.*;

import CityLink2RTR.CityLinkRTRMain;
import CityLink2RTR.MainEventBufer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MonitorHTTPServer
  {

    public MonitorHTTPServer(int port)
      {

        HttpServer server;
        try
          {
            server = HttpServer.create();
            server.bind(new InetSocketAddress(8181), 0);
            server.setExecutor(null);
            server.start();
            System.out.format("HTTP server started on port %d\r\n", port);
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
                if (s.equals(res))
                  {

                    CityLinkRTRMain.Stat.setReceivedPacketsUDP(0);
                    CityLinkRTRMain.Stat.setTransmittedPacketsUDP(0);
                    CityLinkRTRMain.Stat.setErrorPacketsUDP(0);
                  }
              }

            String rtrName = CityLinkRTRMain.ini.get("RTR", "name");
            String rtrVer = CityLinkRTRMain.ini.get("RTR", "version");

            builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n");
            builder.append("<head><title>TRS RTR Ver_" + rtrVer + "</title>\r\n");
            builder.append("<meta http-equiv='Refresh' content='10' charset='utf-8'/></head>\r\n");
            builder.append("<body>\r\n");
            builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>\r\n");
            builder.append(
                "<tr><td align='left'><font size = '+2' face='Monospace' ><b>Software UDP retranslator 'CityLink'  Ver_"
                    + rtrVer + "</b></font></td></tr>\r\n");
            builder.append("</table>\r\n");
            builder
                .append("<table align = 'center' width='800' border ='1' cellspacing='2' cellpadding='2'><tr><td>\r\n");
            builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>\r\n");
            builder.append("<tr><font size = '+1' face='Monospace' >\r\n");
            builder.append("Retranslator name: &nbsp; <font color=#006699><b>" + rtrName + "</font></b><br>\r\n");
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

            builder.append("<b>Sources:</b><br>\r\n");
            for (int i = 0; i < CityLinkRTRMain.serialPool.size(); ++i)
              {
                  if(CityLinkRTRMain.serialPool.get(i).isEnabled > 0)
                  {
                    String pName = CityLinkRTRMain.serialPool.get(i).name;
                    String pState = CityLinkRTRMain.serialPool.get(i).State;
                    String pOk = String.valueOf(CityLinkRTRMain.serialPool.get(i).PacketsOk);
                    String pErr = String.valueOf(CityLinkRTRMain.serialPool.get(i).PacketsErrors);

                    builder.append("Serial port <font color=#006699><b>");
                    builder.append(pName);
                    builder.append(" [State:" + pState + "; ");
                    builder.append("Data:" + pOk + "; ");
                    builder.append("Errors:" + pErr + "]</font></color></b><br>\r\n");
                  }
              }

            if (CityLinkRTRMain.ini.get("UDPSERVER", "enabled", int.class) >= 1)
              {
                builder.append("Network protocol UDP: <font color=#006699><b>[Port:"
                    + CityLinkRTRMain.ini.get("UDPSERVER", "udpport", int.class) + ";  Events received:"
                    + CityLinkRTRMain.Stat.getReceivedPacketsUDP() + ";  Errors:"
                    + CityLinkRTRMain.Stat.getErrorPacketsUDP() + "]</b></font>\r\n");
              }

            builder.append("<br><br>");
            builder.append("<b>Destinations:</b><br>\r\n");
            for (int i = 0; i < CityLinkRTRMain.udpPool.size(); ++i)
              {
                if (CityLinkRTRMain.udpPool.get(i).isEnabled > 0)
                  {
                    builder.append(CityLinkRTRMain.udpPool.get(i).name + " <font color=#006699><b>["
                        + CityLinkRTRMain.udpPool.get(i).URL + ":" + CityLinkRTRMain.udpPool.get(i).Port
                        + "]</b></font><br>\r\n");
                  }
              }
            builder.append("Events transmitted: <font color=#006699><b>"
                + CityLinkRTRMain.Stat.getTransmittedPacketsUDP() + "</font></b><br><br>\r\n");
            builder.append(
                "<form method='post'><tr><td>&nbsp<button type='submit' name ='refr_button' value='Refresh'>Refrash</button>\r\n");
            builder.append(
                "&nbsp<button type='submit' name ='reset_btn' value='Reset'>Reset counters</button><td></tr></form>\r\n");

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