package tech.yakov.AtemProxy;

import tech.yakov.AtemProxy.service.EchoClientService;

public class Main {
    public static void main(String[] args) {
        EchoClientService echoClient = new EchoClientService();
        echoClient.startListener();
    }
}
