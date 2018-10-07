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
            byte[] readBuf = new byte[1024];
            byte[] toSendBuf=new byte[1024+13];
            toSendBuf[0]= (byte) 0x00;
            int seq=0;

            //TODO header要填充
            receiveACKPacket = new DatagramPacket(ACKBuf, ACKBuf.length);
            while ((lenthPerRead = inputStream.read(readBuf)) != -1) {
                System.out.println("everytime read "+readBuf);
                System.arraycopy(Helper.Int2Byte(seq),0,toSendBuf,1,4);
                System.arraycopy(Helper.Int2Byte(0),0,toSendBuf,5,4);
                System.arraycopy(Helper.Int2Byte(lenthPerRead+13),0,toSendBuf,9,4);
                System.arraycopy(readBuf,0,toSendBuf,13,lenthPerRead);
                toSendPacket = new DatagramPacket(toSendBuf, lenthPerRead, new InetSocketAddress(Args.RECEIVE_HOST_IP, Args.RECEIVE_PORT));
                //TODO  PLD and log and send
                Args.log.recordTrans(toSendBuf,true);
                Args.ds.send(toSendPacket);
                seq++;
            }

            toSendBuf[0]=(byte) 32;
            System.arraycopy(Helper.Int2Byte(seq),0,toSendBuf,1,4);
            System.arraycopy(Helper.Int2Byte(0),0,toSendBuf,5,4);
            System.arraycopy(Helper.Int2Byte(13),0,toSendBuf,9,4);
            Args.ds.send(new DatagramPacket(toSendBuf,13,new InetSocketAddress(Args.RECEIVE_HOST_IP,Args.RECEIVE_PORT)));
            Args.ds.close();
            System.out.println("client has closed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
