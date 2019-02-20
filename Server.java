import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

class Server {

    public static void main(String args[]) throws Exception {
        boolean listening = true;
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        byte[] carrier;
        ArrayList<byte[]> packets = new ArrayList<>();
        File file = new File(args[0]);
        if (!file.exists()) {
            file.createNewFile();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            System.out.println("Listening.");

            while(listening) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                System.out.println("RECEIVED: " + sentence + "LENGTH: " + receivePacket.getLength());
                out.write(receivePacket.getData(), 0, receivePacket.getLength());
                carrier = receivePacket.getData();
                packets.add(carrier);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String capitalizedSentence = sentence.toUpperCase();
                out.flush();
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
                if(receivePacket.getLength() < 1024)
                    break;
            }
            byte[] finalImage = new byte[(packets.size() * 1024 + 4)];
            out.write(finalImage);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverSocket.close();
    }
}