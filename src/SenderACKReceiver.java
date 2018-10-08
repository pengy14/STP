import java.io.IOException;
import java.net.DatagramPacket;

public class SenderACKReceiver extends Thread {


    @Override
    public void run(){
        byte[] buffer = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        while(true){
            try {
                Args.ds.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
