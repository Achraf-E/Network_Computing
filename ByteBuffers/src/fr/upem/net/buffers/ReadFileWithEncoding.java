package fr.upem.net.buffers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ReadFileWithEncoding {

	private static void usage() {
		System.out.println("Usage: ReadFileWithEncoding charset filename");
	}

	private static String stringFromFile(Charset cs, Path path) throws IOException {
		var fc = FileChannel.open(path, StandardOpenOption.READ);
		var buffer = ByteBuffer.allocate(Math.toIntExact(fc.size()));
		while(buffer.hasRemaining()) {
			if(fc.read(buffer) == -1) {
				throw new IllegalStateException("Can't read more of file");
			}
		}
		buffer.flip();
		return cs.decode(buffer).toString();

	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			usage();
			return;
		}
		var cs = Charset.forName(args[0]);
		var path = Path.of(args[1]);
		System.out.print(stringFromFile(cs, path));
	}
}
