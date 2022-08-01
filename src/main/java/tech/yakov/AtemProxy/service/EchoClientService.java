package tech.yakov.AtemProxy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import tech.yakov.AtemProxy.models.Client;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;

@Service
public class EchoClientService {
    private DatagramSocket atemSocket;
    private DatagramSocket javaServer;
    private InetAddress atemAddress;
    private ArrayList<Client> clients;
    private ArrayList<byte[]> bytesHello;

    @Value("${atem.ip}")
    private String atemIp;

    private final byte[] commandHello = new byte[]{
            0x10, 0x14, 0x53, (byte) 0xAB,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x3A, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
    };

    private final byte[] commandInit = new byte[]{
            (byte)0x80, 0x0C, 0x53, (byte)0xAB,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x03, 0x00, 0x00
    };

    private byte[] returnToHello;

    private void initCacheData() {

    }

    public EchoClientService() {
        try{
            javaServer = new DatagramSocket(9910);
            atemSocket = new DatagramSocket();
            atemAddress = InetAddress.getByName(atemIp);
            clients = new ArrayList<>();
            bytesHello = new ArrayList<>();
            returnToHello = null;
        } catch (SocketException | UnknownHostException ignored) { }
    }

    public void startListener(){
        Thread javaServerThread = new Thread(() -> {
            while (true){
                byte[] inputArray = new byte[3000];
                DatagramPacket datagramPacket = new DatagramPacket(inputArray, inputArray.length);
                try {
                    //recive data from clients
                    javaServer.receive(datagramPacket);
                    byte[] outputArray = Arrays.copyOf(inputArray, datagramPacket.getLength());

                    //add new user to clients array
                    Client currentClient = clients.stream().filter(o ->
                            o.getIp().getHostAddress().equals(datagramPacket.getAddress().getHostAddress()) && o.getPort() == datagramPacket.getPort()
                    ).findFirst().orElse(null);

                    if (bytesHello.size() < 32) {
                        //init bytes from atem
                        if (clients.size() == 0) {
                            addNewClient(datagramPacket);
                        }
                        atemSocket.send(new DatagramPacket(outputArray, outputArray.length, atemAddress, 9910));
                    }else {
                        //add new user when init finish
                        if (currentClient == null) addNewClient(datagramPacket);

                        //return message to user from hello
                        if (outputArray == commandHello){
                            System.out.println("hello " + datagramPacket.getPort());
                            sendToUser(returnToHello, new Client(datagramPacket));

                        //return init bytes to new user
                        } else if (outputArray == commandInit){
                            System.out.println("init " + datagramPacket.getPort());
                            for (byte[] messageInit : bytesHello) {
                                sendToUser(messageInit, new Client(datagramPacket));
                            }

                        //user send to atem
                        } else {
                            if (currentClient != null){
                                atemSocket.send(new DatagramPacket(outputArray, outputArray.length, atemAddress, 9910));
                                currentClient.setLastSendTime(new Date().getTime());
                            }

                            //sendToUser(outputArray, new Client(datagramPacket));
                        }
                    }
                } catch (IOException ignored) { }
            }
        });
        javaServerThread.start();

        Thread atemThread = new Thread(() -> {
            while (true) {
                byte[] inputArray = new byte[3000];
                DatagramPacket datagramPacket = new DatagramPacket(inputArray, inputArray.length, atemAddress, 9910);
                try {
                    atemSocket.receive(datagramPacket);
                    byte[] outputArray = Arrays.copyOf(inputArray, datagramPacket.getLength());

                    if (returnToHello == null) {
                        returnToHello = outputArray;
                        sendToUser(returnToHello, new Client(datagramPacket));
                        continue;
                    }

                    if (bytesHello.size() < 32) {
                        if (outputArray.length == 1422) bytesHello = new ArrayList<>();
                        if (outputArray.length > 203) bytesHello.add(outputArray);

                        sendToUser(outputArray, clients.get(0));
                    }else {
                        try {
                            ArrayList<Client> clientToDelete = new ArrayList();
                            for (Client client : clients) {
                                if (client.getLastSendTime() + 2000L < new Date().getTime()){
                                    clientToDelete.add(client);
                                    continue;
                                }

                                DatagramPacket toClient = new DatagramPacket(outputArray, outputArray.length, client.getIp(), client.getPort());
                                javaServer.send(toClient);
                            }

                            for (Client client: clientToDelete) {
                                System.out.println("Client to delete: " + client.getIp() + ":" + client.getPort());
                            }

                        }catch (ConcurrentModificationException ignored) {
                            System.err.println("error");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        atemThread.start();

        /*try {
            DatagramPacket packetHelloToAtem = new DatagramPacket(commandHello, commandHello.length, atemAddress, 9910);
            atemSocket.send(packetHelloToAtem);
            atemSocket.receive(new DatagramPacket(bigRecive, bigRecive.length, atemAddress, 9910));

            DatagramPacket packetHelloAnswerToAtem = new DatagramPacket(commandHelloAnswer, commandHelloAnswer.length, atemAddress, 9910);
            atemSocket.send(packetHelloAnswerToAtem);

            Thread.sleep(1000);
        }catch (IOException e){
            System.err.println("Cannot sent hello message to atem.");
        } catch (InterruptedException e) {
            System.err.println("Cannot start timer delay");
        }

        System.out.println("Hello Array Size: " + bytesHello.size());*/
    }

    private void addNewClient(DatagramPacket datagramPacket){
        System.out.println("New Client " + datagramPacket.getAddress().getHostAddress() + ":" + datagramPacket.getPort());
        clients.add(new Client(datagramPacket));
    }

    private void sendToUser(byte[] array, Client client){
        try {
            DatagramPacket toClient = new DatagramPacket(array, array.length, client.getIp(), client.getPort());
            javaServer.send(toClient);
        }catch (IOException e){
            System.err.println("Cannot send to client.");
        }

    }
}
