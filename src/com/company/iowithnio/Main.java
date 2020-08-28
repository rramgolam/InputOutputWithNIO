package com.company.iowithnio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Main {

    public static void writeLargeBlock() {

        try (FileOutputStream binFile = new FileOutputStream("bigblockdata.dat");
             FileChannel binChannel = binFile.getChannel())
        {
            // Write a large block to buffer and write one time
            ByteBuffer largeBuffer = ByteBuffer.allocate(100);
            byte[] outputBytesMsg = "Hello, again!".getBytes();
            largeBuffer.put(outputBytesMsg);
            largeBuffer.putInt(512);
            largeBuffer.putInt(1024);
            byte[] outputBytesMsg2 = "Bye for now".getBytes();
            largeBuffer.put(outputBytesMsg2);
            largeBuffer.putInt(2048);
            largeBuffer.flip();
            binChannel.write(largeBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    public static void main(String[] args) {

        // Reading Text Files Sequentially with readAllLines
        Path path = FileSystems.getDefault().getPath("data.txt");
        List<String> lines = null;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            System.out.println(line);
        }

        // Write to Text File   - different write options availble with StandardOpenOption
        try {
            Files.write(path,"\nLine 6".getBytes("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Writing Binary Files with NIO
        try (FileOutputStream binFile = new FileOutputStream("data.dat");
             FileChannel binChannel = binFile.getChannel())
        {
            // Writing text files
            byte[] outputBytes = "Hello, World!".getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(outputBytes);   // wrap also resets the buffer to 0

            int numBytes = binChannel.write(buffer);
            System.out.println("Number of bytes written: " + numBytes);

            // now writing numbers with an integer buffer
            ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);
            intBuffer.putInt(128);
            intBuffer.flip();
            numBytes = binChannel.write(intBuffer);
            System.out.println("Number of bytes written: " + numBytes);

                // another
            intBuffer.flip();   // position changed after write
            intBuffer.putInt(256);
            intBuffer.flip();   // position changed after put
            numBytes = binChannel.write(intBuffer);
            System.out.println("Number of bytes written: " + numBytes);


            // Reading the data back (using java.io)
            RandomAccessFile ra = new RandomAccessFile("data.dat", "rwd");
            byte[] b = new byte[outputBytes.length];
            ra.read(b);         // data is read into the buffer
            System.out.println(new String(b));

            long int1 = ra.readInt();
            long int2 = ra.readInt();
            System.out.println("Number 1: " + int1);
            System.out.println("Number 2: " + int2);

            // Reading the data back (using java.nio)
            RandomAccessFile raNio = new RandomAccessFile("data.dat", "rwd");
            FileChannel channel = raNio.getChannel();
            buffer.flip();  // reset buffer - otherwise it'll use current value of outputBytes
            long numBytesRead = channel.read(buffer);   // using previous buffer (to write)
                //System.out.println("outputBytes = " + new String(outputBytes));
            if (buffer.hasArray()) {
                System.out.println("byte buffer = " + new String(buffer.array()));
            }
            // Relative Read
            intBuffer.flip();
            numBytesRead = channel.read(intBuffer); // writes value into buffer
            intBuffer.flip();
            System.out.println(intBuffer.getInt()); // reads from buffer
            intBuffer.flip();
            numBytesRead = channel.read(intBuffer);
            intBuffer.flip();
            System.out.println(intBuffer.getInt());

            // Absolute read    -- Comment out to use Absolute version
//            intBuffer.flip();
//            numBytesRead = channel.read(intBuffer);
//            System.out.println(intBuffer.getInt(0));    // extra flip not needed explicitly specifying the location
//            intBuffer.flip();
//            numBytesRead = channel.read(intBuffer);
//            System.out.println(intBuffer.getInt(0));

        } catch (IOException e) {
            e.printStackTrace();
        }

        writeLargeBlock();

    }
}
