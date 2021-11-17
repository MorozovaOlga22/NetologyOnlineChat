package ru.netology;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static final String SETTINGS_FILE_NAME = "settings.txt";

    private static final Gson gson = createGson();
    private static final Type messageListType = new TypeToken<List<UserMessage>>() {
    }.getType();

    private static final DateTimeFormatter dateFormmater = DateTimeFormatter.ofPattern("dd.yyyy.MM HH:mm", Locale.ENGLISH);

    private static Gson createGson() {
        final GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }

    public static ServerSettings getServerSettings() throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE_NAME))) {
            return gson.fromJson(reader.readLine(), ServerSettings.class);
        }
    }

    public static ClientRequest parseRequestJson(String request) {
        return gson.fromJson(request, ClientRequest.class);
    }

    public static UserMessage parseMessageJson(String request) {
        return gson.fromJson(request, UserMessage.class);
    }

    public static List<UserMessage> parseMessagesJson(String request) {
        return gson.fromJson(request, messageListType);
    }

    public static String toJson(ClientRequest request) {
        return gson.toJson(request);
    }

    public static String toJson(UserMessage userMessage) {
        return gson.toJson(userMessage);
    }

    public static String toJson(List<UserMessage> userMessages) {
        return gson.toJson(userMessages);
    }

    public static String formatDate(long epochMilli) {
        final LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
        return date.format(dateFormmater);
    }
}
