package tech.yakov.AtemProxy.service;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import tech.yakov.AtemProxy.models.sessions.ConnectionsSessions;
import tech.yakov.AtemProxy.models.atem.Atem;
import tech.yakov.AtemProxy.models.atem.Signal;

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
    private Atem atem;
    @Value("${atem.ip}")
    private String hostName;
    private final byte[] commandHello = new byte[]{
            0x10, 0x14, 0x53, (byte)0xAB,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x3A, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
    };

    private final byte[] commandHelloAnswer = new byte[]{
            (byte)0x80, 0x0C, 0x53, (byte)0xAB,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x03, 0x00, 0x00
    };

    public TallyConstellationService(){
        try {
            atemSocket = new DatagramSocket();
        } catch (SocketException ignored) { }

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
                        atemSocket.send(new DatagramPacket(commandHelloAnswer, commandHelloAnswer.length, atemAddress, 9910)); // send hello data
                    }

                    if (outputArray[0] == 40 && outputArray[1] == 12 && outputArray.length == 12){
                        byte[] pong = new byte[]{(byte) 0x80, 0x0C, sessionId[0], sessionId[1], remotePacketId[0], remotePacketId[1], 0, 0, 0, (byte) 0xb3, 0, 0}; // pong array
                        atemSocket.send(new DatagramPacket(pong, pong.length, atemAddress, 9910)); // sender pong data to atem
                    }

                    parseDataFromAtem(Arrays.copyOfRange(outputArray, 12, outputArray.length)); // parse data from atem

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        atemThread.start();
    }

    private void parseDataFromAtem(byte[] inputArr){
        if (inputArr.length == 0) return;
        int dataLength = (inputArr[0] << 8) + (inputArr[1] & 0xFF);
        String dataName = new String(Arrays.copyOfRange(inputArr, 4, 8));

        if (dataLength < 4) return;
        byte[] inputBuffer = Arrays.copyOfRange(inputArr, 8, dataLength);

        switch (dataName) {
            case "_pin":
                atem = new Atem(arrayToString(inputBuffer));
                break;
            case "InPr":
                int signalId = (inputBuffer[0] << 8) + inputBuffer[1];
                String signalName = arrayToString(Arrays.copyOfRange(inputBuffer, 2,21));
                String signalLabel = arrayToString(Arrays.copyOfRange(inputBuffer, 22,26));

                //filter output signals and Black
                if (signalId > 100 || signalId == 0) break;
                if (!atem.findSignal(signalId)){
                    atem.putNewSignal(signalId, new Signal(signalId, signalName, signalLabel));
                    logger.info(signalName + " init.");
                }
                break;

            case "TlIn":
                byte[] tallyData = Arrays.copyOfRange(inputBuffer, 2, dataLength);

                for (int i = 1; i < atem.getNumOfInputs(); i++) {
                    Signal currentSignal = atem.getSignalById(i);
                    if (currentSignal.getTallyState().getId() != tallyData[i-1]) {
                        Signal.TallyState newTallyState = Signal.TallyState.getTallyState(tallyData[i-1]);
                        atem.getSignalById(i).setTallyState(newTallyState);
                        logger.info(currentSignal.getName() + " now is " + newTallyState.name());

                        if (sessions != null) {
                            for (ConnectionsSessions connectionsSessions : sessions) {
                                try {
                                    String jsonMessage = new Gson().toJson(atem.getSignalById(i));
                                    connectionsSessions.getSession().sendMessage(new TextMessage(jsonMessage));
                                } catch (IOException | IllegalStateException e){
                                    logger.error("Cannot send update to " +  connectionsSessions.getSession().getUri());
                                }
                            }
                        }
                    }
                }

                break;
            default:
                break;
        }

        if (inputArr.length > dataLength){
            parseDataFromAtem(Arrays.copyOfRange(inputArr, dataLength, inputArr.length));
        }
    }

    private String arrayToString(byte[] arr){
        StringBuilder sb = new StringBuilder();
        for (byte ch: arr) {
            if (ch == 0) break;
            sb.append((char) ch);
        }

        return sb.toString();
    }

    public void setSessions(ArrayList<ConnectionsSessions> sessions){
        this.sessions = sessions;
    }

    public Atem getAtem(){
        return atem;
    }
}