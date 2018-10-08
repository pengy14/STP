import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Log {

    // name of log file
    private static String logname = "./src/Sender_log.txt";

    // Constructor of class Log
    public Log() {
        File f = new File(logname);
        if (f.exists())
            f.delete();
        new File(logname);
    }


    // write log for all packets received
    public void recordACK(byte[] ack_packet) {
        byte[] header = new byte[Args.header_len];
        System.arraycopy(ack_packet, 0, header, 0, Args.header_len);
        byte[] ack = new byte[4];
        byte[] seq = new byte[4];
        System.arraycopy(header, 5, ack, 0, 4);
        System.arraycopy(header, 1, seq, 0, 4);
        int ack_num = Helper.Byte2Int(ack);
        int seq_num = Helper.Byte2Int(seq);
        int packet_len = ack_packet.length;
        // set time format
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());

        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(logname, true));
            output.write("Time: " + time + '\n');
//			if (header[0] == -128) 
//				output.write("Event: Received a SYN with seq number "+seq_num+'\n');
            if (header[0] == -64) {
                output.write("Event: Received a SYN ACK with ack number " + ack_num + '\n');
                output.write("Packet Details:\n");
                output.write("Header: SYN = 1  ACK = 1 ack_num: " + ack_num +
                        " seq_num: " + seq_num + " packet length: " + packet_len + "\n\n");
            } else if (header[0] == 64) {
                output.write("Event: Received an ACK with ack number " + ack_num + '\n');
                output.write("Packet Details:\n");
                output.write("Header: SYN = 0  ACK = 1 ack_num: " + ack_num +
                        " seq_num: " + seq_num + " packet length: " + packet_len + "\n\n");
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

    // write log for transmission of data packet
    public void recordTrans(byte[] sent_packet, String type) {
        byte[] header = new byte[Args.header_len];
        System.arraycopy(sent_packet, 0, header, 0, Args.header_len);
        byte[] seq = new byte[4];
        byte[] ack = new byte[4];
        byte[] packet_length = new byte[4];
        System.arraycopy(header, 5, ack, 0, 4);
        System.arraycopy(header, 1, seq, 0, 4);
        System.arraycopy(header, 9, packet_length, 0, 4);
        int seq_num = Helper.Byte2Int(seq);
        int ack_num = Helper.Byte2Int(ack);
        int packet_len = Helper.Byte2Int(packet_length);
        System.out.println("packet_len   " + packet_len);
        byte[] data = new byte[packet_len - Args.header_len];
        System.arraycopy(sent_packet, Args.header_len, data, 0, data.length);
        String content = new String(data);

        // set time format
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());

        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(logname, true));
            output.write("Time: " + time + '\n');
            // transmitted a SYN packet
            switch (type) {
                case "drop":
                    output.write("Event: drop packet with sequence number " + seq_num + "   ");
                    output.write("Packet Details:\n");
                    output.write("Header: SYN = 0  ACK = 0 seq_num: " + seq_num +
                            " packet length: " + packet_len + "   ");
                    output.write("Data:\n" + content + "\n\n");
                    break;
                case "duplicate":
                    output.write("Event: duplicate packet with sequence number " + seq_num + "   ");
                    output.write("Packet Details:\n");
                    output.write("Header: SYN = 0  ACK = 0 seq_num: " + seq_num +
                            " packet length: " + packet_len + "   ");
                    output.write("Data:\n" + content + "\n\n");
                    break;
                case "corrupt":
                    output.write("Event: corrupt packet with sequence number " + seq_num + "   ");
                    output.write("Packet Details:\n");
                    output.write("Header: SYN = 0  ACK = 0 seq_num: " + seq_num +
                            " packet length: " + packet_len + "   ");
                    output.write("Data:\n" + content + "\n\n");
                    break;
                case "delay":
                    output.write("Event: delay packet with sequence number " + seq_num + "   ");
                    output.write("Packet Details:\n");
                    output.write("Header: SYN = 0  ACK = 0 seq_num: " + seq_num +
                            " packet length: " + packet_len + "   ");
                    output.write("Data:\n" + content + "\n\n");
                    break;
                case "reorder":
                    output.write("Event: reorder packet with sequence number " + seq_num + "   ");
                    output.write("Packet Details:\n");
                    output.write("Header: SYN = 0  ACK = 0 seq_num: " + seq_num +
                            " packet length: " + packet_len + "   ");
                    output.write("Data:\n" + content + "\n\n");
                    break;
                default:
                    output.write("Event: send packet with sequence number " + seq_num + "   ");
                    output.write("Packet Details:\n");
                    output.write("Header: SYN = 0  ACK = 0 seq_num: " + seq_num +
                            " packet length: " + packet_len + "   ");
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

    public void recordPLD(byte[] sent_packet, String err) {


    }

    public void recordSendACK() {

    }
}