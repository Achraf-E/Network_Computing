package fr.upem.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientUpperCaseUDPTimeout {
	public static final int BUFFER_SIZE = 1024;
	private static final Logger logger = Logger.getLogger(ClientUpperCaseUDPTimeout.class.getName());
	private static BlockingQueue<String> queue = new LinkedBlockingQueue<>();
	private static String msg;
	
	private static void usage() {
		System.out.println("Usage : NetcatUDP host port charset");
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 3) {
			usage();
			return;
		}

		var server = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		var cs = Charset.forName(args[2]);
		var bufferSend = ByteBuffer.allocate(BUFFER_SIZE);
		var buffer = ByteBuffer.allocate(BUFFER_SIZE);
		DatagramChannel dc = DatagramChannel.open();
		try (var scanner = new Scanner(System.in);) {
			Thread.ofPlatform().start(() -> {
				SocketAddress sender;
				for (;;) {
					try {
						sender = dc.receive(buffer);
						buffer.flip();
						queue.put(cs.decode(buffer).toString());
						queue.put(sender.toString());
					} catch (IOException e) {
						logger.log(Level.SEVERE, "Recieve error ", e);
					} catch (InterruptedException e) {
						logger.log(Level.SEVERE, "Insertion on the queue interrupted", e);
					}
					buffer.clear();
				}
			});
			while (scanner.hasNextLine()) {
				var line = scanner.nextLine();
				bufferSend.put(cs.encode(line));
				bufferSend.flip();
				dc.send(bufferSend, server);
				msg = queue.poll(3000, TimeUnit.MILLISECONDS);
				var sender = queue.poll(3000, TimeUnit.MILLISECONDS);
				if(msg == null) {
					logger.warning("No answer from the client");
				}
				else {
					logger.info("Recieved : " + msg + "| size : " + msg.length() + " | from : " +  sender);
				}
				msg = null;
				bufferSend.clear();
			}
		}
		dc.close();
	}
}

//Le proxy reçois et renvoie vers le client la chaine, mais la drop le packet qui va etre renvoyé vers l'utilisateur
//Par la suite le recieve etant un appel bloquant, l'application ne repond plus.