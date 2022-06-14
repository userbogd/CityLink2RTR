package MonitorHTTPServer;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MonitorHTTPServer {

	public MonitorHTTPServer() {

		HttpServer server;
		try {
			server = HttpServer.create();
			server.bind(new InetSocketAddress(8080), 0);
			server.setExecutor(null);
			server.start();
			System.out.println("HTTP server started");
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

			builder.append("<head><title>TRS RTR Ver_" + "1.00" + "</title>");
			builder.append("<meta http-equiv='Refresh' content='5' charset=utf-8'></head>");
			builder.append("<body><form method='post'>");
			builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>");
			builder.append(
					"<tr><td align='left'><font size = '+2' face='Monospace' ><b>Программный UDP ретранслятор системы 'CityLink'  Ver_"
							+ "1.00" + "</b></font></td></tr>");
			builder.append("</table>");
			builder.append("<table align = 'center' width='800' border ='1' cellspacing='2' cellpadding='2'><tr><td>");
			builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>");
			builder.append("<tr><font size = '+1' face='Monospace' >");
			builder.append(
					"Название ретранслятора: &nbsp; <font color=#006699><b>" + "RTR name" + "</font></b><br>");
			String Date, StDate;
			//DateTimeToString(Date, "dd mmmm yyy hh:mm:ss ", Now());
			//DateTimeToString(StDate, "dd mmmm yyy hh:mm:ss ", Form1 -> StartDateTime);
			//builder.append("Текущее время ретранслятора: <font color=#006699><b>" + Date + "</font></b><br>");
			//builder.append("Ретранслятор запущен: <font color=#006699><b>" + StDate + "</font></b><br>");
			String DeltaTime, str;
			//int days = Now() - StartDateTime;
			//DateTimeToString(DeltaTime, "hh:mm:ss", Now() - StartDateTime);
			//str = IntToStr(days) + " дн. " + DeltaTime;

			builder.append("Время непрерывной работы: <font color=#006699><b>" + "1" + "</font></b><br><br>");
			builder.append("Входные данные на СОМ1: &nbsp; '" + "OK" + "'<br>");
			builder.append("Входные данные на СОМ2: &nbsp; '" + "OK" + "'<br>");
			builder.append("Ошибок на COM1: <font color=#FF0000><b>'" + "0" + "'</font></b><br>");
			builder.append("Ошибок на COM2: <font color=#FF0000><b>'" + "0" + "'</font></b><br>");
			builder.append("Ошибок по  UDP: <font color=#FF0000><b>'" + "0" + "'</font></b><br>");

			if (true)
				builder.append("Принимаем данные на UDP порт: <font color=#006699><b>" + "60500" + "</b></font><br>");
			else
				builder.append("Прием UDP от других ретрансляторов отключен<br>");
			if (true)
				builder.append(
						"Принимаем данные \"ETHERBOX\" на порт: <font color=#006699><b>" + "60500" + "</b></font><br>");
			else
				builder.append("Прием данных от панелей \"ETHERBOX\" отключен<br>");

			builder.append("Принято&nbsp;&nbsp;событий:  <font color=#006699><b>'" + "100" + "'</font></b><br>");
			builder.append("Передано событий: <font color=#006699><b>'" + "100" + "'</font></b><br><br>");

			builder.append("Передаем данные в следующие адреса:<br>");
			for (int i = 0; i < 5; i++)
				if (true)
					builder.append("&nbsp&nbsp&nbsp<font color=#006699><b>" + "Test Name" + ":" + "test_host" + ":"
							+ "22222" + "</font></b><br>");

			builder.append(
					"Интервал передачи UDP данных: &nbsp<font color=#006699><b>" + "1000" + "ms</b></font><br><br>");

			builder.append("</tr>");
			builder.append("<tr><td>&nbsp<button type='submit' name ='refr_button' value='Refresh'>Обновить</button>");
			builder.append(
					"&nbsp<button type='submit' name ='reset_btn' value='Reset'>Сбросить счетчики</button><td></tr>");

			builder.append("</table>");
			builder.append("</td></tr></table>");
			builder.append("<table align = 'center' width='800' border='0' cellspacing='0' cellpadding='1'>");
			builder.append(
					"<tr><td align='right'><font face='Monospace'>&copy; 2015 \"Телеметрические радиосистемы\"</font></td></tr>");
			builder.append("</table></form></body>");

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