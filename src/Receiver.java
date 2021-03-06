import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

class Receiver extends Thread {

    private FileOutputStream fos;
    // path of log file
    private String logFilePath = "./Receiver_log.txt";
    //log file
    private File logFile;
    // length of mtp header
    private final static int header_len = 13;
    // port number of receiver
    private int port;
    // path of file which data will be written to
    private String receiveFilePath = "./Receiver.txt";
    //receive file
    private File receiveFile;


    public Receiver(int port, String receiveFilePath) {
        this.port = port;
        this.receiveFilePath = receiveFilePath;
    }

    public Receiver() {
    }


    /*
     * write data to file
     */
    public void WriteFile(byte[] data) {
        try {
            String content=new String(data);
//            System.out.println("write content  "+content+'\n');
            fos.write(data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /*
     * write log for packet sent
     */
    public void recordToSend(byte[] data, int SYN, int ACK,
                         int seq_num, int ack_num, int packet_len) {
        // set time format
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());
        String content = null;
        if (data != null)
            content = new String(data);
        System.out.println("content    " + content);
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(logFile, true));
            output.write("Time: " + time + '\n');
            if (SYN == 1) {
                System.out.println("come in SYN 1 not write");
                output.write("Event: Received a SYN with seq number " + seq_num + '\n');
                output.write("Packet Details:\n");
                output.write("Header: SYN = 1  ACK = 0 seq_num: " + seq_num +
                        " ack_num: " + ack_num + " packet length: " + packet_len + "\n\n");
            } else if (ACK == 1) {
                output.write("Event: Received an ACK with ack number " + ack_num + '\n');
                output.write("Packet Details:\n");
                output.write("Header: SYN = 0  ACK = 1 ack_num: " + ack_num +
                        " seq_num: " + seq_num + " packet length: " + packet_len + "\n\n");
            } else {
                output.write("Event: Received an DATA packet with seq number " + seq_num + '\n');
                output.write("Packet Details:\n");
                output.write("Header: SYN = 0  ACK = 0 seq_num: " + seq_num +
                        " ack_num: " + ack_num + " packet length: " + packet_len + '\n');
                output.write("Data:\n" + content + "\n\n");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * write log for packet received
     */
    public void recordReceived(int SYN, int ACK, int seq_num, int ack_num,
                          int packet_len) {

        // set time format
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());

        BufferedWriter output = null;

        try {
            output = new BufferedWriter(new FileWriter(logFilePath, true));
            output.write("Time: " + time + '\n');
            if (SYN == 1 && ACK == 0) {
                output.write("Event:Connection request" + '\n');
                output.write("Packet Details:\n");
                output.write("Header: SYN = 1  ACK = 0 seq_num: " + seq_num +
                        " packet length: " + packet_len + "\n\n");

            } else if (SYN == 0 && ACK == 1) {
                output.write("Event: Transmitted an ACK packet with ack number " + ack_num + '\n');
                output.write("Packet Details:\n");
                output.write("Header: SYN = 0  ACK = 1 seq_num: " + seq_num +
                        " packet length: " + packet_len + "\n\n");
            } else if (SYN == 1 && ACK == 1) {
                output.write("Event: Transmitted an ACK packet with ack number " + ack_num + '\n');
                output.write("Packet Details:\n");
                output.write("Header: SYN = 1  ACK = 1 seq_num: " + seq_num +
                        " packet length: " + packet_len + "\n\n");
            }else {//SYN=0  ACK=0  data packet
                output.write("Event: Received an data packet with ack number " + ack_num + "   ");
                output.write("Packet Details:\n");
                output.write("Header: SYN = 0  ACK = 0 seq_num: " + seq_num +
                        " packet length: " + packet_len + "\n\n");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        receiveFile = new File(receiveFilePath);

        try {
            fos = new FileOutputStream(new File(receiveFilePath), true);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        logFile = new File(logFilePath);
        try {
            if (logFile.createNewFile()) System.out.println("craete successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }

        DatagramSocket ds = null;

        try {
            ds = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("Fail to create DatagramSocket in receiver!");
        }

        byte[] buffer = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        System.out.println("Reveiver starts.");
        SocketAddress dest_addr = null;
        int senderport = 0;
        HashMap<Integer, byte[]> list = new HashMap<Integer, byte[]>();
        int LastWriteByte = 0;
        int tosendSeq = 0;
        while (true) {
            try {
                ds.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
             * read packet length from packet header, and then take the packet
             * out from the buffer with length packet_len, get other information
             * like ack number, seq number from header.
             */
            dest_addr = dp.getSocketAddress();
            System.out.println("dest_addr   " + dest_addr);
            byte[] buf = dp.getData();
            byte[] header = new byte[header_len];
            System.arraycopy(buf, 0, header, 0, header_len);
            byte[] ack = new byte[4];
            byte[] seq = new byte[4];
            byte[] pl = new byte[4];
            System.arraycopy(header, 1, seq, 0, 4);
            System.arraycopy(header, 5, ack, 0, 4);
            System.arraycopy(header, 9, pl, 0, 4);
            int ack_num = Helper.Byte2Int(ack);
            int seq_num = Helper.Byte2Int(seq);
            int packet_len = Helper.Byte2Int(pl);
            byte[] data = new byte[packet_len - header_len];
            System.out.println("received data length   "+data.length);
            System.arraycopy(buf, header_len, data, 0, data.length);
            System.out.println("received data :"+new String(data));
            /*
             *  header[0] == -128 means SYN flag is 1,
             *  header[0] == 64 means ACK flag is 1
             */
            if (header[0] == -128) {//SYN flag is 1,connection request
                byte[] header1 = new byte[header_len];
                if (header[0] == -128) {
                    recordReceived(1, 0, seq_num, ack_num, packet_len);
                    // set both  SYN and ACK to 1
                    header1[0] = (byte) 0xc0;
                    System.out.println("receive a SYN");
                } else {
                    recordToSend(data, 0, 1, seq_num, ack_num, packet_len);
                    // set ACK to 1
                    header1[0] = (byte) 0x40;
                    System.out.println("received an ACK");
                }
                int tosendAck = seq_num + 1;
                System.out.println("tosendAck  " + tosendAck);
                System.arraycopy(Helper.Int2Byte(tosendAck), 0, header1, 5, 4);
                System.arraycopy(Helper.Int2Byte(tosendSeq), 0, header1, 1, 4);
                byte[] packet = new byte[header1.length];
                int length = header.length;
                System.arraycopy(Helper.Int2Byte(length), 0, header1, 9, 4);
                System.arraycopy(header1, 0, packet, 0, header1.length);
                DatagramPacket dp1;
                try {
                    // send ack back
                    dp1 = new DatagramPacket(packet,
                            packet.length,
                            dest_addr);
                    // write log
                    recordToSend(null,1, 1, tosendSeq, tosendAck, packet.length);
                    System.out.println("has sent dp1");
                    ds.send(dp1);
                } catch (SocketException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            // this means it is a FIN packet
            else if (header[0] == 32) {
                try {
                    // send FIN packet back to sender to tell him can close socket
                    DatagramPacket out_dp = new DatagramPacket(header, header.length, dest_addr);
                    ds.send(out_dp);
                    System.out.println("End transfer, socket close");
                    ds.close();
                    break;
                } catch (SocketException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // all flags in header are 0, means this is a data packet
            else if (header[0] == 0) {
                System.out.println("receive a datapacket");
                recordReceived( 0, 0, seq_num, ack_num, packet_len);
                list.put(seq_num, data);
                System.out.println("Received a data packet with seq num " + seq_num);
                WriteFile(data);
                System.out.println(new String(data)+"  " +
                        " dataaaaaaaaa  ");

                // set ACK flag to 1

                byte[] toSendAckHeader=new byte[13];
                int tosend_ack_num = seq_num+data.length;
                byte[] headerAckNum=Helper.Int2Byte(tosend_ack_num);
                byte[] headerSequenceNum=Helper.Int2Byte(seq_num);
                toSendAckHeader[0]=  (byte) 0x40;
                System.arraycopy(headerAckNum,0,toSendAckHeader,1,4);
                System.arraycopy(headerSequenceNum,0,toSendAckHeader,5,4);
                System.arraycopy(Helper.Int2Byte(13),0,toSendAckHeader,9,4);
                DatagramPacket toSendPacket=new DatagramPacket(toSendAckHeader,toSendAckHeader.length,dest_addr);
                try {
                    ds.send(toSendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*
                 * Find out the correct acknowledgment number, since this is
                 * cumulative ack, so the ack number will be last byte which is
                 * in right order.
                 */
//                while (true) {
//                    if (!list.containsKey(tosend_ack_num))
//                        break;
//                    int add = list.get(tosend_ack_num).length;
//                    tosend_ack_num += add;
//                }
//
//                byte[] tosend_ack = Helper.Int2Byte(tosend_ack_num);
//                System.arraycopy(Helper.Int2Byte(tosendSeq), 0, tosend, 1, 4);
//                System.arraycopy(tosend_ack, 0, tosend, 5, tosend_ack.length);
//                System.arraycopy(Helper.Int2Byte(tosend.length), 0, tosend, 9, 4);
//                try {
//                    DatagramPacket out_dp = new DatagramPacket(tosend, tosend.length, dest_addr);
//                    recordReceived(0, 1, tosendSeq, tosend_ack_num, tosend.length);
//                    ds.send(out_dp);
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                /*
//                 * Write all ordered data in receive window to file
//                 */
//                while (true) {
//                    if (!list.containsKey(LastWriteByte))
//                        break;
//                    byte[] towrite = list.get(LastWriteByte);
//                    WriteFile(towrite);
//                    int add = towrite.length;
//                    list.remove(LastWriteByte);
//                    LastWriteByte += add;
//                }
            }
            tosendSeq++;
        }
    }
}
