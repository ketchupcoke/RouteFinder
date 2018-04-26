package routefinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class RouteFinder {

	// ONLY EDIT THESE FIELDS
	private static boolean toggleHops = false; // False to find the most hops, true to find the longest time.
	private static int threads = 100; // How many threads do you want running at a time?
	private static int hops = 255; // Max number of hops to go through. My poor mac is set to a maximum of 255 :(

	private final static String os = System.getProperty("os.name").toLowerCase();
	private volatile static int maxHops = 0;
	private volatile static double maxPing = 0;

	public static void main(String[] args) {

		// Creates multiple threads to test for long traceroutes
		for (int i = 0; i < threads; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							Random random = new Random();
							String output;
							String ip = random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256)
									+ "." + random.nextInt(256);
							output = doTraceRoute(InetAddress.getByName(ip));
							output = output.trim().replaceAll(" +", " ");
							BufferedReader br = new BufferedReader(new StringReader(output));
							String line = "";
							String lastLine = "";
							int hops = 0;
							output = output.trim().replaceAll(" +", " ");

							while (line != null) {
								if (line.contains("(")) {
									hops++;
								}
								lastLine = line;
								line = br.readLine();
							}

							if (toggleHops) {
								Scanner scan = new Scanner(lastLine);
								scan.useLocale(Locale.ENGLISH);
								double n = 0;
								double total = 0;
								boolean first = false;
								while (scan.hasNext()) {
									if (scan.hasNextDouble()) {
										if (!first) {
											first = true;
										} else {
											double d = scan.nextDouble();
											n++;
											total += d;
										}
									}
									scan.next();
								}
								scan.close();
								if (n > 0) {
									double ping = total / n;
									if (ping > maxPing) {
										maxPing = ping;
										System.out.println("New Ping Record! " + ip + " (" + ping + ")");
										System.out.println(output);
									}
								}
							}

							if (hops > maxHops && !toggleHops) {
								maxHops = hops;
								System.out.println("New Max Found! (" + maxHops + ") at " + ip);
								System.out.println(output);
							}

						}
					} 
					catch (Exception e) {}
				}
			});

			thread.start();
		}

	}

	private static String doTraceRoute(InetAddress address) {
		String traceOutput = "";
		try {
			Process traceRoute;
			traceRoute = Runtime.getRuntime().exec("traceroute -w 1 -m " + hops + " " + address.getHostAddress());
			traceOutput = parseTrace(traceRoute.getInputStream(), address.getHostAddress());
		} 
		catch (Exception e) {}
		return traceOutput;
	}

	private static String parseTrace(InputStream is, String ip) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line = "";
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

		} 
		catch (IOException e) {}

		if (br != null) {
			try {
				br.close();
			} 
			catch (IOException e) {}
		}

		return sb.toString();
	}

}
