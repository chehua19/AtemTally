package tech.yakov.AtemProxy.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tech.yakov.AtemProxy.models.sessions.ConnectionsSessions;
import tech.yakov.AtemProxy.service.TallyConstellationService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Component
public class SocketTextHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SocketTextHandler.class);
    private ArrayList<ConnectionsSessions> sessions = new ArrayList<>();
    private final TallyConstellationService tallyConstellationService;
    public SocketTextHandler(TallyConstellationService tallyConstellationService){
        this.tallyConstellationService = tallyConstellationService;
    }

    @PostConstruct
    private void init () {
        this.tallyConstellationService.startAtemListener();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        sessions.add(new ConnectionsSessions(new String[]{}, session));
        tallyConstellationService.setSessions(sessions);
        logger.info("New Client connect. Ip: " + session.getUri());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);

        /*String[] stringIds = message.getPayload().split(",");
        session.sendMessage(new TextMessage(tallyConstellationService.getTallyByCamers(stringIds)));
        sessions.add(new ConnectionsSessions(stringIds, session));
        tallyConstellationService.setSessions(sessions);
        logger.info("New Client connect. Ip: " + session.getUri());*/
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.removeIf(item -> item.getSession() == session);
        tallyConstellationService.setSessions(sessions);
        logger.info("Client disconnect. Ip: " + session.getUri());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }
}