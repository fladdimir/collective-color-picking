package org.hwp;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolationException;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hwp.ColorBoard.PositionColor;
import org.hwp.color.Color;
import org.jboss.logging.Logger;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.common.annotation.Blocking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@ServerEndpoint("/colorboardws")
@ApplicationScoped
@RequiredArgsConstructor
@Blocking
public class ColorBoardWebsocket {

    final ColorBoard board;
    final ObjectMapper jsonMapper;
    final Logger logger;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);

        board.addNewPositionColor(session.getId());
        broadcastState();
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());

        board.remove(session.getId());
        broadcastState();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        onClose(session);
    }

    @OnMessage
    public void onMessage(Session session, String message) {

        Color color;
        try {
            color = jsonMapper.readValue(message, Color.class);
        } catch (JsonProcessingException jsonException) {
            handleJsonException(session, jsonException);
            return;
        }
        try {
            board.updateColor(session.getId(), color);
        } catch (ConstraintViolationException e) {
            logger.infof("constraint violation during board color update: %s", e);
            trySendErrorDetails(e, session);
            return;
        }
        broadcastState();
    }

    private void trySendErrorDetails(Throwable error, Session session) {
        trySendObject(new ErrorMessage(error.toString()), session, "sending error message");
    }

    private void broadcastState() {
        var positionColors = board.getAllPositionsAndColors();
        sessions.values().forEach(s -> trySendState(positionColors, s));
    }

    private void trySendState(Collection<PositionColor> positionColors, Session session) {
        trySendObject(new IndividualPositionColorDto(session.getId(), positionColors), session, "broadcasting state");
    }

    private void trySendObject(Object object, Session session, String errorContext) {
        String data;
        try {
            data = jsonMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            handleJsonException(session, e);
            return;
        }
        session.getAsyncRemote().sendText(data, result -> {
            if (result.getException() != null) {
                logger.warnf("exception when %s: %s", errorContext, result.getException());
                tryClose(session, result.getException().getMessage());
            }
        });
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @RegisterForReflection
    static class IndividualPositionColorDto {
        private String id;
        private Collection<PositionColor> positionColors;
    }

    @Data
    @RegisterForReflection
    @NoArgsConstructor
    @AllArgsConstructor
    static class ErrorMessage {
        private String error;
    }

    private void handleJsonException(Session session, JsonProcessingException jsonException) {
        logger.warnf("closing connection due to json-processing exception: %s", jsonException);
        String closeMsg = jsonException.getMessage();
        tryClose(session, closeMsg);
    }

    private void tryClose(Session session, String closeMsg) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, closeMsg));
        } catch (IOException ioe) {
            logger.error(ioe);
        }
    }

}
