package tech.yakov.AtemProxy.models.sessions;

import org.springframework.web.socket.WebSocketSession;

public class ConnectionsSessions {
    private String[] ids;
    private WebSocketSession session;

    public ConnectionsSessions(String[] ids, WebSocketSession session) {
        this.ids = ids;
        this.session = session;
    }

    public String[] getIds() {
        return ids;
    }

    public WebSocketSession getSession() {
        return session;
    }
}
