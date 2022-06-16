package MonitorHTTPServer;

import com.sun.net.httpserver.*;

import CityLink2RTR.CityLinkRTRMain;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MonitorHTTPServer {

	public MonitorHTTPServer(int port) {

		HttpServer server;
		try {
			server = HttpServer.create();
			server.bind(new InetSocketAddress(port), 0);
			server.setExecutor(null);
			server.start();
			System.out.format("HTTP server started on port %d\r\n", port);
			HttpContext context = server.createContext("/", new EchoHandler());
			context.setAuthenticator(new Auth());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated constructor stub
	}

	static class EchoHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			StringBuilder builder = new StringBuilder();
			System.out.format("Got %s request from %s\r\n", exchange.getRequestMethod(), exchange.getRemoteAddress());

			byte[] buffer = new byte[1024];
			if (exchange.getRequestBody().read(buffer) > 0) {
				String s = new String(buffer);
				System.out.print(s);
			}

			String rtrName = CityLinkRTRMain.ini.get("RTR", "name");
			String rtrVer = CityLinkRTRMain.ini.get("RTR", "version");

			builder.append("<head><title>TRS RTR Ver_" + rtrVer + "</title>");
			builder.append("<meta http-equiv='Refresh' content='5' charset=utf-8'></head>");
			builder.append("<body>");
			builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>");
			builder.append(
					"<tr><td align='left'><font size = '+2' face='Monospace' ><b>����������� UDP ������������ ������� 'CityLink'  Ver_"
							+ rtrVer + "</b></font></td></tr>");
			builder.append("</table>");
			builder.append("<table align = 'center' width='800' border ='1' cellspacing='2' cellpadding='2'><tr><td>");
			builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>");
			builder.append("<tr><font size = '+1' face='Monospace' >");
			builder.append("�������� �������������: &nbsp; <font color=#006699><b>" + rtrName + "</font></b><br>");
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Date date = new Date();
			builder.append("������� ����� �������������: <font color=#006699><b>" + formatter.format(date)
					+ "</font></b><br>");
			builder.append("������������ �������: <font color=#006699><b>" + formatter.format(CityLinkRTRMain.StartDate)
					+ "</font></b><br>");
			long delta = (date.getTime() - CityLinkRTRMain.StartDate.getTime()) / 1000;
			long d = delta / 86400;
			long h = (delta % 86400) / 3600;
			long m = ((delta % 86400) % 3600) / 60;
			long s = ((delta % 86400) % 3600) % 60;
			String dur = String.format("%d� %d:%02d:%02d", d, h, m, s);
			builder.append("����� ����������� ������: <font color=#006699><b>" + dur + "</font></b><br><br>");
			
			builder.append("������� ������ �� ���1: &nbsp; '" + "OK" + "'<br>");
			builder.append("������� ������ �� ���2: &nbsp; '" + "OK" + "'<br>");
			builder.append("������ �� COM1: <font color=#FF0000><b>'" + "0" + "'</font></b><br>");
			builder.append("������ �� COM2: <font color=#FF0000><b>'" + "0" + "'</font></b><br>");
			
			builder.append("������ ��  UDP: <font color=#FF0000><b>'" + "0" + "'</font></b><br>");

			if (true)
				builder.append("��������� ������ �� UDP ����: <font color=#006699><b>" + "60500" + "</b></font><br>");
			else
				builder.append("����� UDP �� ������ �������������� ��������<br>");
			if (true)
				builder.append(
						"��������� ������ \"ETHERBOX\" �� ����: <font color=#006699><b>" + "60500" + "</b></font><br>");
			else
				builder.append("����� ������ �� ������� \"ETHERBOX\" ��������<br>");

			builder.append("�������&nbsp;&nbsp;�������:  <font color=#006699><b>'" + "100" + "'</font></b><br>");
			builder.append("�������� �������: <font color=#006699><b>'" + "100" + "'</font></b><br><br>");

			builder.append("�������� ������ � ��������� ������:<br>");
			for (int i = 0; i < 5; i++)
				if (true)
					builder.append("&nbsp&nbsp&nbsp<font color=#006699><b>" + "Test Name" + ":" + "test_host" + ":"
							+ "22222" + "</font></b><br>");

			builder.append(
					"�������� �������� UDP ������: &nbsp<font color=#006699><b>" + "1000" + "ms</b></font><br><br>");

			builder.append("</tr>");
			builder.append(
					"<form method='post'><tr><td>&nbsp<button type='submit' name ='refr_button' value='Refresh'>��������</button>");
			builder.append(
					"&nbsp<button type='submit' name ='reset_btn' value='Reset'>�������� ��������</button><td></tr></form>");

			builder.append("</table>");
			builder.append("</td></tr></table>");
			builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>");
			builder.append(
					"<tr><td align='right'><font face='Monospace'>&copy; 2015 \"��������������� ������������\"</font></td></tr>");
			builder.append("</table></body>");

			byte[] bytes = builder.toString().getBytes();
			exchange.sendResponseHeaders(200, bytes.length);

			OutputStream os = exchange.getResponseBody();
			os.write(bytes);
			os.close();
		}
	}

	static class Auth extends Authenticator {
		@Override
		public Result authenticate(HttpExchange httpExchange) {
			if ("/forbidden".equals(httpExchange.getRequestURI().toString()))
				return new Failure(403);
			else
				return new Success(new HttpPrincipal("c0nst", "realm"));
		}
	}
}