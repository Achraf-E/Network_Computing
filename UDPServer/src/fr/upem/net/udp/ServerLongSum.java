package fr.upem.net.udp;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.logging.Logger;

public class ServerLongSum {
    private static final Logger logger = Logger.getLogger(ServerIdUpperCaseUDP.class.getName());
    private static final int BUFFER_SIZE = 1024;
    private final HashMap<Long, ClientData> datamap;

    private final DatagramChannel dc;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public ServerLongSum(int port) throws IOException {
        dc = DatagramChannel.open();
        dc.bind(new InetSocketAddress(port));
        datamap = new HashMap<>();
        logger.info("ServerLongSum started on port " + port);
    }

    public void serve() throws IOException {
        try {
            while (!Thread.interrupted()){
                // Prepare Buffer and recieve data
                buffer.clear();
                var sender = (InetSocketAddress) dc.receive(buffer);
                logger.info("Data received from : " + sender.toString());
                buffer.flip();

                //Test the size
                if(buffer.remaining() == 1 + (Long.BYTES * 4)){
                    //Get Infos
                    buffer.get();
                    var sessionID = buffer.getLong();
                    var idPosOper = buffer.getLong();
                    var totalOper = buffer.getLong();
                    var opValue = buffer.getLong();

                    //créate local data
                    var data = datamap.computeIfAbsent(sessionID, key -> new ClientData(totalOper));
                    data.add(opValue, idPosOper);

                    //create ack and send it
                    buffer.clear();
                    buffer.put((byte) 2);
                    buffer.putLong(sessionID);
                    buffer.putLong(idPosOper);
                    buffer.flip();
                    logger.info("Sending message to : " + sender);
                    dc.send(buffer,sender);

                    //Check if we have all operations and send sum
                    if(data.size() == totalOper){
                        buffer.clear();
                        buffer.put((byte) 3);
                        buffer.putLong(sessionID);
                        buffer.putLong(data.getSum());
                        buffer.flip();
                        logger.info("Sending message to : " + sender);
                        dc.send(buffer,sender);
                        datamap.remove(sessionID);
                    }

                }




            }
        }finally {
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
            new ServerLongSum(port).serve();
        } catch (BindException e) {
            logger.severe("Server could not bind on " + port + "\nAnother server is probably running on this port.");
        }
    }
}
