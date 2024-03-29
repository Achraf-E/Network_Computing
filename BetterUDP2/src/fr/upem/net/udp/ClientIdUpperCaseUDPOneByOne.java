package fr.upem.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.*;

public class ClientIdUpperCaseUDPOneByOne {

	private static Logger logger = Logger.getLogger(ClientIdUpperCaseUDPOneByOne.class.getName());
	private static final Charset UTF8 = StandardCharsets.UTF_8;
	private static final int BUFFER_SIZE = 1024;

	private record Response(long id, String message) {
	};

	private final String inFilename;
	private final String outFilename;
	private final long timeout;
	private final InetSocketAddress server;
	private final DatagramChannel dc;
	private final SynchronousQueue<Response> queue = new SynchronousQueue<>();
	private long id = 0;

	public static void usage() {
		System.out.println("Usage : ClientIdUpperCaseUDPOneByOne in-filename out-filename timeout host port ");
	}

	public ClientIdUpperCaseUDPOneByOne(String inFilename, String outFilename, long timeout, InetSocketAddress server)
			throws IOException {
		this.inFilename = Objects.requireNonNull(inFilename);
		this.outFilename = Objects.requireNonNull(outFilename);
		this.timeout = timeout;
		this.server = server;
		this.dc = DatagramChannel.open();
		dc.bind(null);
	}

	private void listenerThreadRun() {
		for(;;) {
			try {
				var buf = ByteBuffer.allocate(BUFFER_SIZE);
				dc.receive(buf);
				buf.flip();
				var current = buf.getLong();
				var message = UTF8.decode(buf).toString();
				buf.clear();
				queue.put(new Response(current, message));
			}catch (AsynchronousCloseException | InterruptedException e) {
				logger.info("Turning OFF");
				return;				
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Listener error");
				return;
			}
		}
	}

	public void launch() throws IOException, InterruptedException {
		try {

			var listenerThread = Thread.ofPlatform().start(this::listenerThreadRun);

			// Read all lines of inFilename opened in UTF-8
			var lines = Files.readAllLines(Path.of(inFilename), UTF8);

			var upperCaseLines = new ArrayList<String>();
			for (var line: lines) {
				var messageBuf = UTF8.encode(line);
				if (Long.BYTES + messageBuf.limit() > BUFFER_SIZE) {
					logger.warning("Message size error");
					return;
				}
				var buf = ByteBuffer.allocate(BUFFER_SIZE);
				buf.putLong(this.id);
				buf.put(messageBuf);

				var waiting = timeout;
				var start = System.currentTimeMillis();
				var elapsed = 0L;
				buf.flip();
				dc.send(buf, server);
				for (;;) {
					var response = queue.poll(waiting, TimeUnit.MILLISECONDS);
					elapsed = System.currentTimeMillis() - start;
					if (response != null && response.id == this.id) {
						logger.info("Adding " + response.message);
						upperCaseLines.add(response.message);
						this.id ++;
						break;
					}
					if (elapsed >= timeout){
						logger.warning("Server don't answer");
						buf.flip();
						dc.send(buf, server);
						start = System.currentTimeMillis();
						waiting = timeout;
					} else {
						waiting = timeout - elapsed;
					}
				}
			}

			listenerThread.interrupt();
			Files.write(Paths.get(outFilename), upperCaseLines, UTF8, CREATE, WRITE, TRUNCATE_EXISTING);
		} finally {
			dc.close();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 5) {
			usage();
			return;
		}

		var inFilename = args[0];
		var outFilename = args[1];
		var timeout = Long.parseLong(args[2]);
		var server = new InetSocketAddress(args[3], Integer.parseInt(args[4]));

		// Create client with the parameters and launch it
		new ClientIdUpperCaseUDPOneByOne(inFilename, outFilename, timeout, server).launch();
	}
}
