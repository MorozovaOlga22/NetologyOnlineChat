package ru.netology;

public class UserMessage {
    private final long id;
    private final String author;
    private final long dispatchTime;
    private final String content;

    public UserMessage(long id, String author, long dispatchTime, String content) {
        this.id = id;
        this.author = author;
        this.dispatchTime = dispatchTime;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public long getDispatchTime() {
        return dispatchTime;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Сообщение от пользователя '" + author + "' (" + Utils.formatDate(dispatchTime) + ")\n"
                + content + "\n";
    }
}