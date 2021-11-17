package ru.netology;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static ru.netology.ClientRequest.GET_MESSAGES_COMMAND;

public class UtilsTest {

    @Test
    public void parseRequestJson() {
        final String request = "{\"command\":\"Get messages\",\"user\":\"user\",\"lastReadMessageId\":19}";

        final ClientRequest actualClientRequest = Utils.parseRequestJson(request);

        assertNotNull(actualClientRequest);
        assertEquals(GET_MESSAGES_COMMAND, actualClientRequest.getCommand());
        assertEquals("user", actualClientRequest.getUser());
        assertEquals("19", String.valueOf(actualClientRequest.getLastReadMessageId()));
        assertNull(actualClientRequest.getMessageContent());
    }

    @Test
    public void parseMessageJson() {
        final String request = "{\"id\":21,\"author\":\"user\",\"dispatchTime\":1637190612755,\"content\":\"New message\"}";

        final UserMessage actualMessage = Utils.parseMessageJson(request);

        assertNotNull(actualMessage);
        assertEquals(21, actualMessage.getId());
        assertEquals("user", actualMessage.getAuthor());
        assertEquals(1637190612755L, actualMessage.getDispatchTime());
        assertEquals("New message", actualMessage.getContent());
    }

    @Test
    public void parseMessagesJson() {
        final String request = "[{\"id\":21,\"author\":\"user\",\"dispatchTime\":1637190612755,\"content\":\"New message\"}]";

        final List<UserMessage> actualUserMessages = Utils.parseMessagesJson(request);

        assertNotNull(actualUserMessages);
        assertEquals(1, actualUserMessages.size());

        final UserMessage actualMessage = actualUserMessages.get(0);
        assertNotNull(actualMessage);
        assertEquals(21, actualMessage.getId());
        assertEquals("user", actualMessage.getAuthor());
        assertEquals(1637190612755L, actualMessage.getDispatchTime());
        assertEquals("New message", actualMessage.getContent());
    }

    @Test
    public void toJson_ClientRequest() {
        final String expectedRequestStr = "{\"command\":\"Get messages\",\"user\":\"user\",\"lastReadMessageId\":19}";
        final ClientRequest clientRequest = new ClientRequest(GET_MESSAGES_COMMAND, "user", 19L, null);

        final String actualRequestStr = Utils.toJson(clientRequest);

        assertEquals(expectedRequestStr, actualRequestStr);
    }

    @Test
    public void toJson_UserMessage() {
        final String expectedRequestStr = "{\"id\":21,\"author\":\"user\",\"dispatchTime\":1637190612755,\"content\":\"New message\"}";
        final UserMessage message = new UserMessage(21, "user", 1637190612755L, "New message");

        final String actualRequestStr = Utils.toJson(message);

        assertEquals(expectedRequestStr, actualRequestStr);
    }

    @Test
    public void testToJson1() {
        final String expectedRequestStr = "[{\"id\":21,\"author\":\"user\",\"dispatchTime\":1637190612755,\"content\":\"New message\"}]";
        final UserMessage message = new UserMessage(21, "user", 1637190612755L, "New message");
        final List<UserMessage> userMessages = Collections.singletonList(message);

        final String actualRequestStr = Utils.toJson(userMessages);

        assertEquals(expectedRequestStr, actualRequestStr);
    }

    @Test
    public void formatDate() {
        final long epochMilli = 1637190612755L;
        final String expectedFormattedDate = "18.2021.11 02:10";

        final String actualFormattedDate = Utils.formatDate(epochMilli);

        assertEquals(expectedFormattedDate, actualFormattedDate);
    }
}