import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
class Server {

    public static void main(String args[]) throws Exception {
        boolean listening = true;
        DatagramSocket serverSocket = new DatagramSocket(13085);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        byte[] carrier;
        ByteArrayOutputStream packets = new ByteArrayOutputStream();
        File file = new File("C:/Users/danie/Desktop/test.jpg");
        try {
            FileOutputStream out = new FileOutputStream(file);
            System.out.println("Listening.");

            while(listening) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, 0, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                //System.out.println("RECEIVED: " + sentence + "LENGTH: " + receivePacket.getLength());
                out.write(receivePacket.getData(), 0, receivePacket.getLength());
                carrier = receivePacket.getData();
                packets.write(carrier);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String capitalizedSentence = sentence.toUpperCase();
                out.flush();
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(packets.toByteArray()));
        ImageIO.write(image, "jpg", new File("C:/Users/danie/Desktop/test.jpg"));
        serverSocket.close();
    }
}