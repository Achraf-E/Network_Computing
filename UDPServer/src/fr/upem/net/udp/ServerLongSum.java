package fr.upem.net.udp;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.logging.Logger;

public class ServerLongSum {
    private static final Logger logger = Logger.getLogger(ServerLongSum.class.getName());
    private static final int BUFFER_SIZE = 1024;
    private final DatagramChannel dc;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private final HashMap<Long,ClientData> sessionData = new HashMap<>();


    public ServerLongSum(int port) throws IOException {
        dc = DatagramChannel.open();
        dc.bind(new InetSocketAddress(port));
        logger.info("ServerBetterUpperCaseUDP started on port " + port);
    }

    public void serve() throws IOException {
        try{
            while (!Thread.interrupted()){
                buffer.clear();
                var sender = dc.receive(buffer);
                logger.info("packet recieved from : " + sender);
                buffer.flip();

                var op = buffer.get();
                var sessionId = buffer.getLong();
                var idPosOp = buffer.getLong();
                var totalOp = buffer.getLong();
                var opValue = buffer.getLong();

                sessionData.computeIfAbsent(sessionId, key -> new ClientData(totalOp));
                sessionData.get(sessionId).add(opValue, idPosOp);

                buffer.clear();
                buffer.put((byte) 2);
                buffer.putLong(sessionId);
                buffer.putLong(idPosOp);
                buffer.flip();
                dc.send(buffer, sender);
                logger.info("ACK send to : " + sender);

                if(sessionData.get(sessionId).checkAllOp()){
                    buffer.clear();
                    buffer.put((byte) 3);
                    buffer.putLong(sessionId);
                    buffer.putLong(sessionData.get(sessionId).getSum());
                    buffer.flip();
                    dc.send(buffer, sender);
                    logger.info("Sum send to : " + sender);
                    sessionData.remove(sessionId);
                }

            }
        }finally{
            dc.close();
        }
    }

    public static void usage() {
        System.out.println("Usage : ServerLongSum port");
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
            new ServerLongSum(port).serve();
        } catch (BindException e) {
            logger.severe("Server could not bind on " + port + "\nAnother server is probably running on this port.");
            return;
        }
    }

}