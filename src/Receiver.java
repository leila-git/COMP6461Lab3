import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Receiver implements Runnable {
    static int pkt_size = 1000;
    int sk2_dst_port;
    int sk3_dst_port;
    int portClient;
    String path;
    int modeClintServer;
    int newCommand;
    Object monitor;

    // Receiver constructor
    public Receiver(int sk2_dst_port, int sk3_dst_port, int portClient, String path, int modeClintServer, Object monitor) {
        this.sk2_dst_port = sk2_dst_port;
        this.sk3_dst_port = sk3_dst_port;
        this.path = path;
        this.portClient = portClient;
        this.modeClintServer = modeClintServer;
        this.monitor = monitor;
    }

    public int getNewCommand() {
        return newCommand;
    }

    public void setNewCommand(int newCommand) {
        this.newCommand = newCommand;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub


        DatagramSocket sk2, sk3;
        System.out.println("Receiver: sk2_dst_port=" + sk2_dst_port + ", " + "sk3_dst_port=" + sk3_dst_port + ".");

        int prevSeqNum = -1; // previous sequence number received in-order
        int nextSeqNum = 0; // next expected sequence number
        boolean isTransferComplete = false; // (flag) if transfer is
        // complete

        // create sockets
        try {
            sk2 = new DatagramSocket(sk2_dst_port); // incoming channel
            sk3 = new DatagramSocket(); // outgoing channel
            System.out.println("Receiver: Listening");
            try {
                byte[] in_data = new byte[pkt_size]; // message data in
                // packet
                DatagramPacket in_pkt = new DatagramPacket(in_data, in_data.length); // incoming
                // packet
                InetAddress dst_addr = InetAddress.getByName("127.0.0.1");

                FileOutputStream fos = null;
                // make directory
                path = ((path.substring(path.length() - 1)).equals("/")) ? path : path + "/"; // append
                // slash
                // if
                // missing
                File filePath = new File(path);
                if (!filePath.exists())
                    filePath.mkdir();

                // listen on sk2_dst_port
                if (modeClintServer == 1) {

                    while (true) {
                        // receive packet
                        newCommand = 0;
                        if (isTransferComplete) {
                            in_data = new byte[pkt_size];
                            in_pkt = new DatagramPacket(in_data, in_data.length);
                            if (fos != null)
                                fos.close();
                            prevSeqNum = -1; // previous sequence number
                            // received in-order
                            nextSeqNum = 0; // next expected sequence number
                            isTransferComplete = false;
                            newCommand = 1;
                            synchronized (monitor) {
                                monitor.notifyAll();
                            }
                        }
                        clearByteArray(in_data);
                        sk2.receive(in_pkt);

                        Packet tmpPacket = Packet.fromBuffer(ByteBuffer.wrap(in_pkt.getData()));
                        byte[] in_data_tmp = purePayload(tmpPacket.getPayload());

                        int seqNum = ByteBuffer.wrap(copyOfRange(in_data_tmp, 0, 4)).getInt();
                        System.out.println("Receiver: Received sequence number: " + seqNum);

                        if (seqNum == nextSeqNum) {

                            if (lastPacket(in_data_tmp) == 0) {
                                byte[] ackPkt = generatePacket(-2); // construct
                                // teardown
                                // packet
                                // (ack
                                // -2)
                                // send 20 acks in case last ack is not
                                // received by Sender (assures Sender
                                // teardown)

                                for (int i = 0; i < 20; i++) {
                                    Packet ackPacket = new Packet(1, seqNum, InetAddress.getByName("localhost"),
                                            portClient, ackPkt);
                                    sk3.send(new DatagramPacket(ackPacket.toBytes(), ackPacket.toBytes().length,
                                            dst_addr, sk3_dst_port));
                                }

                                isTransferComplete = true; // set flag to
                                // true
                                System.out.println("Receiver: All packets received! File Created!");
                                continue; // end listener
                            }
                            // else send ack
                            else {
                                byte[] ackPkt = generatePacket(seqNum);

                                Packet ackPacket = new Packet(1, seqNum, InetAddress.getByName("localhost"),
                                        portClient, ackPkt);
                                Thread.sleep(1);
                                sk3.send(new DatagramPacket(ackPacket.toBytes(), ackPacket.toBytes().length,
                                        dst_addr, sk3_dst_port));
                                System.out.println("Receiver: Sent Ack " + seqNum);
                            }

                            // if first packet of transfer
                            if (seqNum == 0 && prevSeqNum == -1) {
                                int fileNameLength = ByteBuffer.wrap(copyOfRange(in_data_tmp, 4, 8)).getInt(); // 0-8:checksum,
                                // 8-12:seqnum
                                String fileName = new String(copyOfRange(in_data_tmp, 8, 8 + fileNameLength)); // decode
                                // file
                                // name
                                System.out.println(
                                        "Receiver: fileName length: " + fileNameLength + ", fileName:" + fileName);

                                // create file
                                File file = new File(path + fileName);
                                if (!file.exists())
                                    file.createNewFile();

                                // initial file
                                fos = new FileOutputStream(file);
                                fos.write(in_data_tmp, 8 + fileNameLength, in_data_tmp.length - 8 - fileNameLength);
                            } else {
                                fos.write(in_data_tmp, 4, in_data_tmp.length - 4);
                            }

                            nextSeqNum++; // update nextSeqNum
                            prevSeqNum = seqNum; // update prevSeqNum
                        }

                        // if out of order packet received, send duplicate
                        // ack
                        else {
                            byte[] ackPkt = generatePacket(prevSeqNum);

                            Packet ackPacket = new Packet(1, prevSeqNum, InetAddress.getByName("localhost"),
                                    portClient, ackPkt);
                            Thread.sleep(1);
                            sk3.send(new DatagramPacket(ackPacket.toBytes(), ackPacket.toBytes().length, dst_addr,
                                    sk3_dst_port));

                            System.out.println("Receiver: Sent duplicate Ack " + prevSeqNum);
                        }

                    }

                }

                if (modeClintServer == 0) {
                    synchronized (this) {
                        while (!isTransferComplete) {
                            clearByteArray(in_data);
                            sk2.receive(in_pkt);

                            Packet tmpPacket = Packet.fromBuffer(ByteBuffer.wrap(in_pkt.getData()));
                            byte[] in_data_tmp = purePayload(tmpPacket.getPayload());

                            int seqNum = ByteBuffer.wrap(copyOfRange(in_data_tmp, 0, 4)).getInt();
                            System.out.println("Receiver: Received sequence number: " + seqNum);

                            if (seqNum == nextSeqNum) {

                                if (lastPacket(in_data_tmp) == 0) {
                                    byte[] ackPkt = generatePacket(-2); // construct
                                    // teardown
                                    // packet
                                    // (ack
                                    // -2)
                                    // send 20 acks in case last ack is not
                                    // received by Sender (assures Sender
                                    // teardown)

                                    for (int i = 0; i < 20; i++) {
                                        Packet ackPacket = new Packet(1, seqNum, InetAddress.getByName("localhost"),
                                                portClient, ackPkt);

                                        sk3.send(new DatagramPacket(ackPacket.toBytes(), ackPacket.toBytes().length,
                                                dst_addr, sk3_dst_port));
                                    }

                                    isTransferComplete = true; // set flag to
                                    // true
                                    System.out.println("Receiver: All packets received! File Created!");
                                    continue; // end listener
                                }
                                // else send ack
                                else {
                                    byte[] ackPkt = generatePacket(seqNum);

                                    Packet ackPacket = new Packet(1, seqNum, InetAddress.getByName("localhost"),
                                            portClient, ackPkt);

                                    sk3.send(new DatagramPacket(ackPacket.toBytes(), ackPacket.toBytes().length,
                                            dst_addr, sk3_dst_port));
                                    System.out.println("Receiver: Sent Ack " + seqNum);
                                }

                                // if first packet of transfer
                                if (seqNum == 0 && prevSeqNum == -1) {
                                    int fileNameLength = ByteBuffer.wrap(copyOfRange(in_data_tmp, 4, 8)).getInt(); // 0-8:checksum,
                                    // 8-12:seqnum
                                    String fileName = new String(copyOfRange(in_data_tmp, 8, 8 + fileNameLength)); // decode
                                    // file
                                    // name
                                    System.out.println(
                                            "Receiver: fileName length: " + fileNameLength + ", fileName:" + fileName);

                                    // create file
                                    File file = new File(path + fileName);
                                    if (!file.exists())
                                        file.createNewFile();

                                    // init fos
                                    fos = new FileOutputStream(file);
                                    fos.write(in_data_tmp, 8 + fileNameLength, in_data_tmp.length - 8 - fileNameLength);
                                } else {
                                    fos.write(in_data_tmp, 4, in_data_tmp.length - 4);
                                }

                                nextSeqNum++; // update nextSeqNum
                                prevSeqNum = seqNum; // update prevSeqNum
                            }

                            // if out of order packet received, send duplicate
                            // ack
                            else {
                                byte[] ackPkt = generatePacket(prevSeqNum);

                                Packet ackPacket = new Packet(1, prevSeqNum, InetAddress.getByName("localhost"),
                                        portClient, ackPkt);

                                sk3.send(new DatagramPacket(ackPacket.toBytes(), ackPacket.toBytes().length, dst_addr,
                                        sk3_dst_port));

                                System.out.println("Receiver: Sent duplicate Ack " + prevSeqNum);
                            }

                        }
                        if (fos != null)
                            fos.close();
                        synchronized (monitor) {
                            monitor.notify();
                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            } finally {
                sk2.close();
                sk3.close();
                System.out.println("Receiver: sk2 closed!");
                System.out.println("Receiver: sk3 closed!");
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

    }// END constructor

    // generate Ack packet
    public byte[] generatePacket(int ackNum) {
        byte[] ackNumBytes = ByteBuffer.allocate(4).putInt(ackNum).array();
        // construct Ack packet
        ByteBuffer pktBuf = ByteBuffer.allocate(4);
        pktBuf.put(ackNumBytes);
        return pktBuf.array();
    }

    // same as Arrays.copyOfRange in 1.6
    public byte[] copyOfRange(byte[] srcArr, int start, int end) {
        int length = (end > srcArr.length) ? srcArr.length - start : end - start;
        byte[] destArr = new byte[length];
        System.arraycopy(srcArr, start, destArr, 0, length);
        return destArr;
    }

    private void clearByteArray(byte[] byteBuffer) {
        for (int i = 0; i < byteBuffer.length; i++) {
            byteBuffer[i] = 0;
        }
    }

    private int lastPacket(byte[] in_data_tmp) {
        int out = 0;
        for (int i = 4; i < in_data_tmp.length; i++) {
            if (in_data_tmp[i] != 0) {
                out++;
            }
        }
        return out;
    }

    private byte[] purePayload(byte[] in_data_tmp) {
        int maxLen = in_data_tmp.length;
        int i = 0;
        for (i = maxLen - 1; i > -1; i--) {
            if (in_data_tmp[i] != 0) {
                break;
            }
        }
        byte[] newPayload = new byte[i + 1];
        for (int j = 0; j < i + 1; j++) {
            newPayload[j] = in_data_tmp[j];
        }

        return newPayload;
    }


}