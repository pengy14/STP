import java.net.DatagramSocket;
import java.net.SocketException;

public class Sender_start {
    public static void main(String args[]) {
        Args.RECEIVE_HOST_IP = args[0];
        Args.RECEIVE_PORT = Integer.parseInt(args[1]);
        Args.filename += args[2];
        Args.MWS = Integer.parseInt(args[3]);
        Args.MSS = Integer.parseInt(args[4]);
        Args.gamma = Integer.parseInt(args[5]);
        Args.pDrop = Double.parseDouble(args[6]);
        Args.pDuplicate = Double.parseDouble(args[7]);
        Args.pCorrupt = Double.parseDouble(args[8]);
        Args.pOrder = Double.parseDouble(args[9]);
        if (Integer.parseInt(args[10]) > 0 && Integer.parseInt(args[10]) < 7) {
            Args.maxOrder = Integer.parseInt(args[10]);
        } else {
            System.out.println("maxOrder is invalid,we use default 3");
        }
        Args.pDelay = Double.parseDouble(args[11]);
        Args.maxDelay = Integer.parseInt(args[12]);
        Args.seed = Long.parseLong(args[13]);

        Args.log = new Log();


        try {
            Args.ds = new DatagramSocket();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Args.local_port = Args.ds.getLocalPort();
        System.out.println("local port number is "+Args.local_port);

//        Receiver receiver=new Receiver();
//        receiver.run();

        Thread connect=new Connect();
        connect.run();
//        Args.ds.close();
    }
}
