public class Reveiver_start {

    public static void main(String args[]) {
        int port = Integer.parseInt(args[0]);
        String filename = args[1];
        /*
         *  start receiver to listen at port number given
         *  by standard input
         */
        Thread receiver = new Receiver(port, filename);
        receiver.start();
    }
}
