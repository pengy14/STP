
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class TransferFile extends Thread {
    private int port;
    private String receiveFilePath;
    private Timer timer;
    private Model model;
    byte[] readBuf = new byte[1024];
    int base;
    int baseEnd;

    public TransferFile() {
        this.port = Args.RECEIVE_PORT;
        this.receiveFilePath = receiveFilePath;
        model = new Model();
        timer = new Timer(this, model);

        model.setTime(0);
        timer.start();
    }

    @Override
    public void run() {

        DatagramPacket toSendPacket = null;
        DatagramPacket receiveACKPacket = null;
        int lenthPerRead;
        byte[] ACKBuf = new byte[20];

        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(Args.filename)));
            int segmentSize = Args.MSS;

            byte[] toSendSegment = new byte[segmentSize + 13];
            toSendSegment[0] = (byte) 0x00;
            int seq = 0;

            while ((lenthPerRead = inputStream.read(readBuf)) != -1) {
                base = 0;
                baseEnd = Args.MWS;
                //TODO PLD
                while (true) {

                    sendData(readBuf, toSendSegment, segmentSize, seq, toSendPacket, lenthPerRead);


                    receiveACKPacket = new DatagramPacket(ACKBuf, ACKBuf.length);
                    Args.ds.receive(receiveACKPacket);
                    byte[] header = receiveACKPacket.getData();
                    byte[] ackNum = new byte[4];
                    System.arraycopy(header, 5, ackNum, 0, 4);
                    int ack = Helper.Byte2Int(ackNum);
                    Args.LastByteAcked = ack - 1;
                    seq = ack;
                    if (Args.LastByteSent == Args.LastByteAcked) {
                        model.setTime(0);
                        baseEnd += segmentSize;
                    } else {
                        model.setTime(3);
                    }
                }
            }

            toSendSegment[0] = (byte) 32;
            System.arraycopy(Helper.Int2Byte(seq), 0, toSendSegment, 1, 4);
            System.arraycopy(Helper.Int2Byte(0), 0, toSendSegment, 5, 4);
            System.arraycopy(Helper.Int2Byte(13), 0, toSendSegment, 9, 4);
            Args.ds.send(new DatagramPacket(toSendSegment, 13, new InetSocketAddress(Args.RECEIVE_HOST_IP, Args.RECEIVE_PORT)));
            Args.ds.close();
            System.out.println("client has closed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendData(byte[] readBuf, byte[] toSendSegment, int segmentSize, int seq, DatagramPacket toSendPacket, int lenthPerRead) {
        while (Args.LastByteAcked != readBuf.length) {//
            if (baseEnd - Args.LastByteSent > 0) {//avaliable space to send
                //initial header
                System.arraycopy(Helper.Int2Byte(seq), 0, toSendSegment, 1, 4);
                System.arraycopy(Helper.Int2Byte(0), 0, toSendSegment, 5, 4);
                System.arraycopy(Helper.Int2Byte(segmentSize + 13), 0, toSendSegment, 9, 4);
                //initial header

                //copy data
                if (baseEnd - Args.LastByteSent >= segmentSize) {
                    System.arraycopy(readBuf, Args.LastByteSent + 1, toSendSegment, 13, toSendSegment.length - 13);
                } else {
                    System.arraycopy(readBuf, Args.LastByteSent + 1, toSendSegment, 13, baseEnd - Args.LastByteSent);
                }

                PLD pld = new PLD();
                if (pld.isDrop()) {//TODO  PLD detail
                    Args.log.recordTrans(toSendSegment, "drop");
                    model.setTime(0);
                    continue;
                } else if (pld.isDuplicate()) {
                    Args.log.recordTrans(toSendSegment, "duplicate");
                } else if (pld.isCorrupt()) {
                    Args.log.recordTrans(toSendSegment, "corrupt");
                } else if (pld.isDelay()) {
                    Args.log.recordTrans(toSendSegment, "delay");
                } else if (pld.isOrder()) {
                    Args.log.recordTrans(toSendSegment, "reorder");
                } else {//normal
                    toSendPacket = new DatagramPacket(toSendSegment, segmentSize + 13, new InetSocketAddress(Args.RECEIVE_HOST_IP, Args.RECEIVE_PORT));
                    Args.log.recordTrans(toSendSegment, "normal");
                    try {
                        Args.ds.send(toSendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (Args.LastByteSent == (Args.LastByteAcked + segmentSize)) {

                        model.setTime(3);
                    }
                    if (baseEnd - Args.LastByteSent >= segmentSize) {
                        Args.LastByteSent += segmentSize;
                    } else {
                        Args.LastByteSent = baseEnd;
                    }
                }


            }
        }

    }

    public void timeOut() throws Exception {
        byte[] toSendSegment = new byte[Args.MSS + 13];
        toSendSegment[0] = (byte) 0x00;

        System.arraycopy(Helper.Int2Byte(Args.LastByteAcked), 0, toSendSegment, 1, 4);//sequence
        System.arraycopy(Helper.Int2Byte(0), 0, toSendSegment, 5, 4);//ack
        System.arraycopy(Helper.Int2Byte(Args.MSS + 13), 0, toSendSegment, 9, 4);//packet_lenth

        System.arraycopy(this.readBuf, Args.LastByteAcked, toSendSegment, 13, Args.MSS);
        DatagramPacket datagramPacket = new DatagramPacket(toSendSegment, toSendSegment.length, new InetSocketAddress(Args.RECEIVE_HOST_IP, Args.RECEIVE_PORT));
        Args.ds.send(datagramPacket);

    }


}
