package edu.eci.arsw.blueprints.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/** Logs de conexión/suscripción STOMP para observabilidad. */
@Component
public class WebSocketEventsLogger {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventsLogger.class);

    @EventListener
    public void onConnected(SessionConnectedEvent e) {
        log.info("STOMP conectado: sesión {}", StompHeaderAccessor.wrap(e.getMessage()).getSessionId());
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent e) {
        StompHeaderAccessor h = StompHeaderAccessor.wrap(e.getMessage());
        log.info("STOMP suscripción: sesión {} -> {}", h.getSessionId(), h.getDestination());
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent e) {
        log.info("STOMP desconectado: sesión {} ({})", e.getSessionId(), e.getCloseStatus());
    }
}
