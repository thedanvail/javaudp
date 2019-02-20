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
            case "png":
                flag = true;
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

    public static byte[] createByteArray(BufferedImage image) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byte[] arr = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return arr;
        } catch (IOException e) {
            e.printStackTrace();
        } return new byte[1];
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
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException {
        int port = 13085;
        String filePath = args[0];
        BufferedImage img = ImageIO.read(new File(filePath));
        String fileExtension = Driver.getFileExtension(args[0]);
        if (!correctFileType(fileExtension)) {
            System.out.println("Incorrect file type. Please double check path and try again.");
            System.exit(0);
        }

        // Assume ip is localhost unless told otherwise
        InetAddress ip = InetAddress.getLocalHost();
        if (args.length > 2) {
            ip = InetAddress.getByName(args[1]);
        }

        byte[] buffer = createByteArray(img);
        // Creating the socket object for carrying the data.
        DatagramSocket ds = new DatagramSocket();

        // Sending the data
        Driver.sendData(buffer, ds, ip, port);

        // Receive data
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        ds.receive(receivePacket);
        ds.close();
    }
}