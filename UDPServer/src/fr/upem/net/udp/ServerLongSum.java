package fr.upem.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ServerLongSum {
    private static final Logger logger = Logger.getLogger(ServerIdUpperCaseUDP.class.getName());
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final int BUFFER_SIZE = 1024;

    private final DatagramChannel dc;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public ServerLongSum(int port) throws IOException {
        dc = DatagramChannel.open();
        dc.bind(new InetSocketAddress(port));
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
                    var session = buffer.get();
                    var sessionID = buffer.getLong();
                    var idPosOper = buffer.getLong();
                    var totalOper = buffer.getLong();
                    var opValue = buffer.getLong();
                    buffer.clear();

                }




            }
        }finally {

        }
    }
}
