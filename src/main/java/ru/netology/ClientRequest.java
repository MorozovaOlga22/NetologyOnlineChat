package ru.netology;

public class ClientRequest {
    public static final String NEW_MESSAGE_COMMAND = "New message";
    public static final String GET_MESSAGES_COMMAND = "Get messages";
    public static final String END_COMMAND = "End";

    private final String command;
    private final String user;
    private final Long lastReadMessageId;
    private final String messageContent;

    public ClientRequest(String command, String user, Long lastReadMessageId, String messageContent) {
        this.command = command;
        this.user = user;
        this.lastReadMessageId = lastReadMessageId;
        this.messageContent = messageContent;
    }

    public String getCommand() {
        return command;
    }

    public String getUser() {
        return user;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public String getMessageContent() {
        return messageContent;
    }
}