import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.imageio.ImageIO;

public class Driver {

    public static boolean correctFileType(String extension){
        boolean flag = false;
        switch (extension) {
            case "jpg":
                flag = true;
        }
        return flag;
    }

    public static String getFileExtension(String fullName) {
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    public static void sendData(byte[] buffer, DatagramSocket ds, InetAddress ip, int port ) {

        int startOffsetValue, packetCounter = 1;
        int counter = 0;
        byte[] buff = new byte[(buffer.length / 12) + 1];
        byte[] padded = new byte[buffer.length % (buffer.length / 12)];

        try {
            for (int i = 0; i < buffer.length; i++) {
                startOffsetValue = i;
                if (packetCounter > 12) {
                    for (int j = 0; j < padded.length - 1; j++) {
                        padded[counter] = buffer[i];
                        i++;
                        counter++;
                    }

                    DatagramPacket buffPacket = new DatagramPacket(padded, padded.length, ip, port);
                    ds.send(buffPacket);
                    System.out.println(
                            " Packet Number: " + packetCounter + "\t SO: " + startOffsetValue + "    \tEO: " + (i - 1));
                } else {
                    buff[counter] = buffer[i];
                    counter++;
                    startOffsetValue = i - (buffer.length / 12);
                }
                if (i % (buffer.length / 12) == 0 && i != 0) {

                    // Creating the datagramPacket for sending the data.
                    DatagramPacket bufferPacket = new DatagramPacket(buff, buff.length, ip, port);
                    ds.send(bufferPacket);
                    buff = new byte[(buffer.length / 12) + 1];
                    counter = 0;
                    System.out.println(
                            " Packet Number: " + packetCounter + "\t SO: " + startOffsetValue + "     \tEO: " + (i - 1));
                    packetCounter++;
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException {
        int port = 9876;
        // Creating the socket object for carrying the data.
        DatagramSocket ds = new DatagramSocket();
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        String filePath = args[0];

        // The image being passed in.
        BufferedImage img = ImageIO.read(new File(filePath));
        String fileExtension = Driver.getFileExtension(args[0]);
        if (!correctFileType(fileExtension)) {
            System.out.println("Incorrect file type. Please double check path and try again.");
            System.exit(0);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(img, fileExtension, outputStream);
        outputStream.flush();

        // the byte array that holds the image data.
        byte[] buffer = outputStream.toByteArray();
        outputStream.close();

        // Assume ip is localhost unless told otherwise
        InetAddress ip = InetAddress.getLocalHost();
        if (args.length > 2) {
            ip = InetAddress.getByName(args[1]);
        }

        // Sending the data
        Driver.sendData(buffer, ds, ip, port);

        // Receive data
        ds.receive(receivePacket);
        ds.close();
    }
}