import com.sun.org.apache.xpath.internal.Arg;
import com.sun.org.apache.xpath.internal.operations.String;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class TransferFile extends Thread {
    private int port;
    private String receiveFilePath;

    public TransferFile() {
        this.port = Args.RECEIVE_PORT;
        this.receiveFilePath = receiveFilePath;
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
            byte[] readBuf = new byte[1024];
            byte[] toSendSegment = new byte[segmentSize + 13];
            toSendSegment[0] = (byte) 0x00;
            int seq = 0;
            receiveACKPacket = new DatagramPacket(ACKBuf, ACKBuf.length);
            while ((lenthPerRead = inputStream.read(readBuf)) != -1) {
                //TODO PLD
                while (Args.LastByteAcked != readBuf.length) {//
                    if (Args.baseEnd - Args.LastByteSent >0) {//avaliable space to send

                        //initial header
                        System.arraycopy(Helper.Int2Byte(seq), 0, toSendSegment, 1, 4);
                        System.arraycopy(Helper.Int2Byte(0), 0, toSendSegment, 5, 4);
                        System.arraycopy(Helper.Int2Byte(lenthPerRead + 13), 0, toSendSegment, 9, 4);
                        //initial header

                        //copy data
                        System.arraycopy(readBuf, 0, toSendSegment, 13, toSendSegment.length - 13);

                        PLD pld = new PLD();
                        if (pld.isDrop()) {//TODO  PLD detail
                            Args.log.recordTrans(toSendSegment, "drop");
                        } else if (pld.isDuplicate()) {
                            Args.log.recordTrans(toSendSegment, "duplicate");
                        } else if (pld.isCorrupt()) {
                            Args.log.recordTrans(toSendSegment, "corrupt");
                        } else if (pld.isDelay()) {
                            Args.log.recordTrans(toSendSegment, "delay");
                        } else if (pld.isOrder()) {
                            Args.log.recordTrans(toSendSegment, "reorder");
                        } else {//normal
                            toSendPacket = new DatagramPacket(toSendSegment, lenthPerRead + 13, new InetSocketAddress(Args.RECEIVE_HOST_IP, Args.RECEIVE_PORT));

                            Args.log.recordTrans(toSendSegment, "normal");
                            Args.ds.send(toSendPacket);
                            Args.LastByteSent+=segmentSize;
                            byte[] bytes = new byte[4096];
                            receiveACKPacket = new DatagramPacket(bytes, bytes.length);
                            Args.ds.receive(receiveACKPacket);
                            byte[] header =receiveACKPacket.getData();
                            byte[] ackNum=new byte[4];
                            System.arraycopy(header,5,ackNum,0,4);
                            int ack = Helper.Byte2Int(ackNum);
                            Args.LastByteAcked = ack+1;

                            Args.sentNotAcked.put(seq,toSendSegment);//buffer sent not acked
                            seq++;
                        }


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
    public void timeOut(byte[] readBuf) throws Exception {
        for(int i = Args.LastByteAcked;i <Args.LastByteSent;i++){
            
            System.out.println("向服务器重新发送的数据:" + i);

            DatagramPacket datagramPacket = new DatagramPacket(data, data.length,new InetSocketAddress(Args.RECEIVE_HOST_IP, Args.RECEIVE_PORT));
            Args.ds.send(datagramPacket);
        }
    }


}
