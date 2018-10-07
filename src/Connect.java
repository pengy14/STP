import com.sun.org.apache.xpath.internal.Arg;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Connect extends Thread {


    private void sendPacket(int SYN, int ACK, int seq_num,
                            int ack_num) {
        byte[] header = new byte[Args.header_len];
        byte[] seq = Helper.Int2Byte(seq_num);
        byte[] ack = Helper.Int2Byte(ack_num);
        System.arraycopy(seq, 0, header, 1, 4);
        System.arraycopy(ack, 0, header, 5, 4);
        /*
         *  Set flags in header, 0x80 is decimal value of 1000000,
         *  means SYN is set to 1
         *  0x40 is decimal value of 01000000, means ACK is set to 1.
         */
        if (SYN == 1)
            header[0] = (byte) 0x80;
        if (ACK == 1)
            header[0] = (byte) 0x40;
        System.arraycopy(Helper.Int2Byte(Args.header_len), 0, header, 9, 4);
        Args.log.recordTrans(header, true);
        try {
            DatagramPacket dp = new DatagramPacket(header,
                    header.length,
                    new InetSocketAddress(Args.RECEIVE_HOST_IP, Args.RECEIVE_PORT));
            Args.ds.send(dp);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        int seq_num = 1;
        System.out.println("Sender_start starts.");

        while (!Args.connected) {
            sendPacket(1, 0, seq_num, 0);
            seq_num++;
            System.out.println("SYN sent");
            try {
                /* After sent a SYN packet, this thread will wait for
                 * 2 seconds, then check whether received a SYN ACK
                 */
                Thread.sleep(2000);
                byte[] SYNACK = new byte[1];
                byte[] receiver_seq_byte=new byte[4];
                byte[] buffer = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                Args.ds.receive(dp);
                System.out.println("has received dp from receiver");
                byte[] packet_buffer = dp.getData();
                System.arraycopy(packet_buffer, 0, SYNACK, 0, 1);
                System.arraycopy(packet_buffer, 1, receiver_seq_byte, 0, 4);
                int receive_seq=Helper.Byte2Int(receiver_seq_byte);
                if (packet_buffer[0] == -64) {//SYN=1 && ACk=1
                    System.out.println("third shake");
                    sendPacket(0,1,seq_num,++receive_seq);  //3rd shakehand
                    Args.flagSYNACK=true;
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
             * If received SYN ACK, it will send a ACK packet,
             * otherwise, send SYN again.
             */
            if (!Args.flagSYNACK)
                continue;
            sendPacket(1, 1, seq_num, ++Args.ack_num);
            seq_num++;
            System.out.println("ACK sent.");
            Args.ack_num = 0;
            try {
                Thread.sleep(2000);

                //TODO  connectedflag set 第三次发送实现，确定建立链接
                Args.connected=true;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        Thread transferFile = new TransferFile();
        transferFile.start();

    }

}