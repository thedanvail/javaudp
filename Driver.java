import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.imageio.ImageIO;

public class Driver {

    public static DatagramPacket makePacket(byte[] myByteArray, InetAddress myIP) {
        DatagramPacket packet = new DatagramPacket(myByteArray, myByteArray.length, myIP, 1234);
        return packet;
    }

    public static void main(String args[]) throws IOException {
        // Creating the socket object for carrying the data.
        DatagramSocket ds = new DatagramSocket();

        // The image being passed in.
        BufferedImage img = ImageIO.read(new File(args[0]));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", outputStream);
        outputStream.flush();

        // the byte array that holds the image data.
        byte[] buffer = outputStream.toByteArray();
        byte[] buff = new byte[(buffer.length / 12) + 1];
        byte[] padded = new byte[buffer.length % (buffer.length / 12)];
        int counter = 0;
        int startOffsetValue = 0;

        InetAddress ip = InetAddress.getLocalHost();

        int packetCounter = 1;
        // Sending the data
        for (int i = 0; i < buffer.length; i++) {
            startOffsetValue = i;
            if (packetCounter == 13) {
                for (int j = 0; j < padded.length - 1; j++) {
                    padded[counter] = buffer[i];
                    i++;
                    counter++;

                }
                ds.send(makePacket(padded, ip));
                System.out.println(
                        " Packet Number: " + packetCounter + "\t SO: " + startOffsetValue + "    \tEO: " + (i - 1));

            } else {
                buff[counter] = buffer[i];
                counter++;
                startOffsetValue = i - (buffer.length / 12);

            }

            if (i % (buffer.length / 12) == 0 && i != 0) {
                // Creating the datagramPacket for sending the data.
                ds.send(makePacket(buff, ip));
                buff = new byte[(buffer.length / 12) + 1];
                counter = 0;

                System.out.println(
                        " Packet Number: " + packetCounter + "\t SO: " + startOffsetValue + "     \tEO: " + (i - 1));
                packetCounter++;
            }

        }
    }
}