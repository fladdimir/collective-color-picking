package org.hwp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hwp.position.Position.posOf;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hwp.ColorBoardWebsocket.IndividualPositionColorDto;
import org.hwp.color.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@QuarkusTest
@QuarkusTestResource(QuarkusPostgresLocalOrTestContainerTestResource.class)
class ColorBoardWebsocketTest {

    private static final int RECEIVE_TIMEOUT_S = 5;

    private static final LinkedBlockingDeque<String> CLIENT_RECEIVED_MESSAGES_1 = new LinkedBlockingDeque<>();
    private static final LinkedBlockingDeque<String> CLIENT_RECEIVED_MESSAGES_2 = new LinkedBlockingDeque<>();

    ObjectMapper jsonMapper = new ObjectMapper();

    @TestHTTPResource("/colorboardws")
    URI uri;

    @BeforeEach
    void beforeEach() {
        CLIENT_RECEIVED_MESSAGES_1.clear();
        CLIENT_RECEIVED_MESSAGES_2.clear();
    }

    @Test
    void test_2Clients() throws IOException, DeploymentException, InterruptedException {
        var wsContainer = ContainerProvider.getWebSocketContainer();

        try (Session session1 = wsContainer.connectToServer(new Client(CLIENT_RECEIVED_MESSAGES_1), uri)) {

            // onopen1 -> broadcast
            var c1received1 = CLIENT_RECEIVED_MESSAGES_1.poll(RECEIVE_TIMEOUT_S, TimeUnit.SECONDS);
            var c1receivedObj1 = jsonMapper.readValue(c1received1, IndividualPositionColorDto.class);
            assertThat(c1receivedObj1.getId()).isNotBlank();
            assertThat(c1receivedObj1.getPositionColors()).hasSize(1);
            var c1receivedPosCol1 = c1receivedObj1.getPositionColors().iterator().next();
            assertThat(c1receivedPosCol1.getId()).isEqualTo(c1receivedObj1.getId());
            assertThat(c1receivedPosCol1.getPosition()).isEqualTo(posOf(0, 0));

            try (Session session2 = wsContainer.connectToServer(new Client(CLIENT_RECEIVED_MESSAGES_2), uri)) {

                // onopen2 -> broadcast
                // -> 1
                var c1received2 = CLIENT_RECEIVED_MESSAGES_1.poll(RECEIVE_TIMEOUT_S, TimeUnit.SECONDS);
                var c1receivedObj2 = jsonMapper.readValue(c1received2, IndividualPositionColorDto.class);
                assertThat(c1receivedObj2.getId()).isNotBlank();
                assertThat(c1receivedObj2.getPositionColors()).hasSize(2);
                var c1receivedPosCol2 = c1receivedObj2.getPositionColors().stream()
                        .filter(pc -> pc.getId().equals(c1receivedObj2.getId())).findAny().orElseThrow();
                assertThat(c1receivedPosCol2.getPosition()).isEqualTo(posOf(0, 0)); // not changed
                assertThat(c1receivedPosCol2.getColor()).isEqualTo(c1receivedPosCol1.getColor()); // not changed

                // -> 2
                var c2received1 = CLIENT_RECEIVED_MESSAGES_2.poll(RECEIVE_TIMEOUT_S, TimeUnit.SECONDS);
                var c2receivedObj1 = jsonMapper.readValue(c2received1, IndividualPositionColorDto.class);
                assertThat(c2receivedObj1.getId()).isNotBlank();
                assertThat(c2receivedObj1.getPositionColors()).hasSize(2);
                var c2receivedPosCol1 = c2receivedObj1.getPositionColors().stream()
                        .filter(pc -> pc.getId().equals(c2receivedObj1.getId())).findAny().orElseThrow();
                assertThat(c2receivedPosCol1.getPosition()).isEqualTo(posOf(0, 1));
                assertThat(c2receivedPosCol1.getColor()).isEqualTo(c1receivedPosCol2.getColor());

                // onmessage1 -> broadcast
                var color11 = c1receivedPosCol1.getColor();
                var r = color11.getR() == 255 ? 0 : color11.getR() + 1; // change color
                var g = color11.getG();
                var b = color11.getB();
                var c1firstSentUpdate = new Color(r, g, b);
                session1.getBasicRemote().sendText(jsonMapper.writeValueAsString(c1firstSentUpdate));

                // -> 1
                var c1received3 = CLIENT_RECEIVED_MESSAGES_1.poll(RECEIVE_TIMEOUT_S, TimeUnit.SECONDS);
                var c1receivedObj3 = jsonMapper.readValue(c1received3, IndividualPositionColorDto.class);
                assertThat(c1receivedObj3.getId()).isNotBlank();
                assertThat(c1receivedObj3.getPositionColors()).hasSize(2);
                var c1receivedPosCol3 = c1receivedObj3.getPositionColors().stream()
                        .filter(pc -> pc.getId().equals(c1receivedObj3.getId())).findAny().orElseThrow();
                assertThat(c1receivedPosCol3.getPosition()).isEqualTo(posOf(0, 0)); // not changed
                assertThat(c1receivedPosCol3.getColor()).isEqualTo(c1firstSentUpdate); // changed

                // -> 2
                var c2received2 = CLIENT_RECEIVED_MESSAGES_2.poll(RECEIVE_TIMEOUT_S, TimeUnit.SECONDS);
                var c2receivedObj2 = jsonMapper.readValue(c2received2, IndividualPositionColorDto.class);
                assertThat(c2receivedObj2.getId()).isNotBlank();
                assertThat(c2receivedObj2.getPositionColors()).hasSize(2);
                var c2receivedPosCol2 = c2receivedObj2.getPositionColors().stream()
                        .filter(pc -> pc.getId().equals(c2receivedObj2.getId())).findAny().orElseThrow();
                assertThat(c2receivedPosCol2.getPosition()).isEqualTo(posOf(0, 1)); // not changed
                assertThat(c2receivedPosCol2.getColor()).isEqualTo(c2receivedPosCol1.getColor()); // not changed
                var c2receivedPosCol2_c1 = c2receivedObj2.getPositionColors().stream()
                        .filter(pc -> pc.getId().equals(c1receivedObj3.getId())).findAny().orElseThrow();
                assertThat(c2receivedPosCol2_c1.getPosition()).isEqualTo(posOf(0, 0)); // c1, not changed
                assertThat(c2receivedPosCol2_c1.getColor()).isEqualTo(c1firstSentUpdate); // change received
            }
        }
    }

    @ClientEndpoint
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Client extends Endpoint {
        private LinkedBlockingDeque<String> receivedMessages;

        @OnMessage
        void message(String msg) {
            receivedMessages.add(msg);
        }

        @Override
        public void onOpen(Session session, EndpointConfig config) {
        }
    }

}
