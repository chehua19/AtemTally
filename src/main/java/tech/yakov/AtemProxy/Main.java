package tech.yakov.AtemProxy;

import tech.yakov.AtemProxy.service.EchoClient;

public class Main {
    public static void main(String[] args) {
        EchoClient echoClient = new EchoClient();
        echoClient.startListener();
    }
}
