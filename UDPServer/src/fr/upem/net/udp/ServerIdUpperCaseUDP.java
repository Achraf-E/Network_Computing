package fr.upem.net.udp;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ServerIdUpperCaseUDP {

    private static final Logger logger = Logger.getLogger(ServerIdUpperCaseUDP.class.getName());
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final int BUFFER_SIZE = 1024;

    private final DatagramChannel dc;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public ServerIdUpperCaseUDP(int port) throws IOException {
        dc = DatagramChannel.open();
        dc.bind(new InetSocketAddress(port));
        logger.info("ServerBetterUpperCaseUDP started on port " + port);
    }

    public void serve() throws IOException {
        try {
            while (!Thread.interrupted()) {
                // Prepare Buffer and recieve message
                buffer.clear();
                var sender =(InetSocketAddress) dc.receive(buffer);
                logger.info("Message recieved from : " +  sender.toString());
                buffer.flip();

                // Get Infos
                var id= buffer.getLong();
                var msg= UTF8.decode(buffer).toString();
                var upperCaseMsg = msg.toUpperCase();

                //Put infos in the buffer (cleared)
                buffer.clear();
                buffer.putLong(id);
                buffer.put(UTF8.encode(upperCaseMsg));

                // Send response
                buffer.flip();
                logger.info("Sending message to : " + sender.toString());
                dc.send(buffer,sender);
            }
        } finally {
            dc.close();
        }
    }

    public static void usage() {
        System.out.println("Usage : ServerIdUpperCaseUDP port");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            usage();
            return;
        }

        var port = Integer.parseInt(args[0]);

        if (!(port >= 1024) & port <= 65535) {
            logger.severe("The port number must be between 1024 and 65535");
            return;
        }

        try {
            new ServerIdUpperCaseUDP(port).serve();
        } catch (BindException e) {
            logger.severe("Server could not bind on " + port + "\nAnother server is probably running on this port.");
            return;
        }
    }
}