import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Timer;

public class Args {

    public static int header_len = 13;
    public static String RECEIVE_HOST_IP = "127.0.0.1";
    public static int RECEIVE_PORT = 6666;
    public static int CLIENT_PORT = 2000;
    public static String filename = "./src/";
    public static int MWS = 400;
    public static int MSS = 50;
    public static int gamma;
    public static double pDrop = 0.5;
    public static double pDuplicate = 0.5;
    public static double pCorrupt = 0.5;
    public static double pOrder = 0.5;
    public static int maxOrder = 3;//between 1~6
    public static double pDelay = 0.5;
    public static int maxDelay = 100;
    public static long seed;
    public static int seq_num = 0;
    public static int ack_num = 0;
    public static int local_port;
    public static DatagramSocket ds;
    public static int LastByteAcked = 0;
    public static int LastByteSent = 0;
    public static int base = 0;
    public static int baseEnd = base + MWS;
    public static Timer timer = new Timer();
//    public static int timeout=
//    public static
    // HashMap used to simulate send window
    public static HashMap<Integer, byte[]> window = new HashMap<Integer, byte[]>();

    public static HashMap<Integer, byte[]> sentNotAcked = new HashMap<Integer, byte[]>();

    public static int file_length;
    // the instance of class Log
    public static Log log;
    // flag of whether connection is established
    public static boolean connected = false;
    // flag of whether SYN ACK is received
    public static boolean flagSYNACK = false;

    public void closeSocket() {
        ds.close();
    }
}
