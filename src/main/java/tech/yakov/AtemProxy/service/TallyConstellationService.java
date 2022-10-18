package tech.yakov.AtemProxy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import tech.yakov.AtemProxy.models.ConnectionsSessions;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class TallyConstellationService {
    private ArrayList<ConnectionsSessions> sessions;
    private static final Logger logger = LoggerFactory.getLogger(TallyConstellationService.class);
    private DatagramSocket atemSocket;
    private InetAddress atemAddress;
    private byte[] tallysArray;

    @Value("${atem.ip}")
    private String hostName;
    private byte[] commandHello = new byte[]{
            0x10, 0x14, 0x53, (byte)0xAB,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x3A, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
    };

    private byte[] commandHelloAnswer = new byte[]{
            (byte)0x80, 0x0C, 0x53, (byte)0xAB,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x03, 0x00, 0x00
    };

    public TallyConstellationService(){
        try{
            atemSocket = new DatagramSocket();
        }catch (SocketException ignored) { }

    }

    public void startAtemListener() {
        try {
            atemAddress = InetAddress.getByName(hostName);
            atemSocket.send(new DatagramPacket(commandHello, commandHello.length, atemAddress, 9910));
            logger.info("Send hello to Atem");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread atemThread = new Thread(() -> {
            while (true) {
                try {
                    byte[] inputArray = new byte[3000];
                    DatagramPacket datagramPacket = new DatagramPacket(inputArray, inputArray.length, atemAddress, 9910);
                    atemSocket.receive(datagramPacket);
                    byte[] outputArray = Arrays.copyOf(inputArray, datagramPacket.getLength());
                    byte[] sessionId = new byte[]{outputArray[2], outputArray[3]};
                    byte[] remotePacketId = new byte[]{outputArray[10], outputArray[11]};

                    if (outputArray[0] == 0x10 && outputArray[1] == 0x14) {
                        atemSocket.send(new DatagramPacket(commandHelloAnswer, commandHelloAnswer.length, atemAddress, 9910));
                    }

                    //ping - pong
                    if (outputArray[0] == 40 && outputArray[1] == 12 && outputArray.length == 12){
                        byte[] ack = new byte[]{(byte) 0x80, 0x0C, sessionId[0], sessionId[1], remotePacketId[0], remotePacketId[1], 0, 0, 0, (byte) 0xb3, 0, 0};
                        atemSocket.send(new DatagramPacket(ack, ack.length, atemAddress, 9910));
                    }else {
                        parseTally(Arrays.copyOfRange(outputArray, 12, outputArray.length));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        atemThread.start();
    }

    private void parseTally(byte[] inputArr){
        if (inputArr.length == 0) return;
        int dataLength = inputArr[0] + (inputArr[1] & 0xFF);
        String dataName = new String(Arrays.copyOfRange(inputArr, 4, 8));

        if (dataLength < 4) return;
        if (dataName.equals("TlIn")){
            byte[] tallyData = Arrays.copyOfRange(inputArr, 8, dataLength);

            if (tallysArray == null) {
                tallysArray = new byte[tallyData[1]];
            }

            for (int i = 0; i < tallysArray.length; i++) {
                if (tallysArray[i] != tallyData[i + 2]) {
                    switch (tallyData[i + 2]) {
                        case 0:
                            logger.info("Cam" + i + " now is empty");
                            break;
                        case 1:
                            logger.info("Cam" + i + " now is PGM");
                            break;
                        case 2:
                            logger.info("Cam" + i + " now is PRV");
                            break;
                        case 3:
                            logger.info("Cam" + i + " now are PGM and PRV");
                            break;
                        default:
                            break;
                    }
                    if (sessions != null){
                        for (ConnectionsSessions connectionsSessions: sessions) {
                            boolean contains = false;
                            for (String id : connectionsSessions.getIds()){
                                if (id.equals(String.valueOf(i+1))){
                                    contains = true;
                                    break;
                                }
                            }
                            if (contains){
                                tallysArray = Arrays.copyOfRange(tallyData, 2, tallyData[1]+2);
                                try {
                                    connectionsSessions.getSession().sendMessage(new TextMessage(getTallyByCamers(connectionsSessions.getIds())));
                                } catch (IOException | IllegalStateException e){
                                    logger.info("Cannot send update to " +  connectionsSessions.getSession().getUri());
                                }
                            }
                        }
                    }
                }
            }

        }

        if (inputArr.length > dataLength){
            parseTally(Arrays.copyOfRange(inputArr, dataLength, inputArr.length));
        }
    }

    public String getTallyByCamera(int cameraId){
        return String.valueOf(tallysArray[cameraId-1]);
    }

    public String getTallyByCamers(String[] camersId){
        byte[] tallys = new byte[camersId.length];
        for (int i = 0; i < camersId.length; i++) {
            tallys[i] = tallysArray[Integer.parseInt(camersId[i])-1];
        }

        int max = 0;
        for (byte tally : tallys) {
            if (tally > 0 && max != 1) max = tally;
        }
        return String.valueOf(max);
    }

    public void setSessions(ArrayList<ConnectionsSessions> sessions){
        this.sessions = sessions;
    }
}