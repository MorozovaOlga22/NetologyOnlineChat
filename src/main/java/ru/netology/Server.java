package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static ru.netology.ClientRequest.*;

public class Server {
    private static final int THREADS_COUNT = 8;
    private static final String LOG_FILE_NAME = "serverLogs" + File.separator + "file.log";

    private static final List<UserMessage> userMessages = loadMessages();
    private static final AtomicLong nextMessageId = calculateNextMessageId();

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {
        final int serverPort = Utils.getServerSettings().getPort();
        final ServerSocket serverSocket = new ServerSocket(serverPort);

        final ExecutorService executorService = Executors.newFixedThreadPool(THREADS_COUNT);
        while (true) {
            try {
                final Socket socket = serverSocket.accept();
                executorService.submit(getServerTask(socket));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static List<UserMessage> loadMessages() {
        final File file = new File(LOG_FILE_NAME);
        final ArrayList<UserMessage> userMessages = new ArrayList<>();
        if (!file.exists()) {
            System.out.println("File with messages not found");
            return userMessages;
        }
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach(messageStr -> userMessages.add(Utils.parseMessageJson(messageStr)));
            return userMessages;
        } catch (IOException e) {
            e.printStackTrace();
            return userMessages;
        }
    }

    private static AtomicLong calculateNextMessageId() {
        final long maxMessageId = userMessages.stream().mapToLong(UserMessage::getId).max().orElse(0L);
        return new AtomicLong(maxMessageId + 1);
    }

    private static Runnable getServerTask(final Socket socket) {
        return () -> {
            processTask(socket);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private static void processTask(Socket socket) {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            while (true) {
                final ClientRequest request = Utils.parseRequestJson(reader.readLine());
                switch (request.getCommand()) {
                    case NEW_MESSAGE_COMMAND: {
                        final UserMessage message = new UserMessage(nextMessageId.getAndIncrement(), request.getUser(), System.currentTimeMillis(), request.getMessageContent());
                        addMessage(message);
                        returnUnreadMessages(request, writer);
                        break;
                    }
                    case GET_MESSAGES_COMMAND: {
                        returnUnreadMessages(request, writer);
                        break;
                    }
                    case END_COMMAND: {
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void addMessage(UserMessage message) {
        userMessages.add(message);
        try (final PrintWriter logWriter = new PrintWriter(new FileOutputStream(LOG_FILE_NAME, true), true)) {
            logWriter.println(Utils.toJson(message));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void returnUnreadMessages(ClientRequest request, PrintWriter writer) {
        final Long lastReadMessageId = request.getLastReadMessageId();
        final List<UserMessage> filteredUserMessages = getUnreadUserMessages(lastReadMessageId);
        writer.println(Utils.toJson(filteredUserMessages));
    }

    private static synchronized List<UserMessage> getUnreadUserMessages(Long lastReadMessageId) {
        return userMessages.stream().filter(userMessage -> userMessage.getId() > lastReadMessageId).collect(Collectors.toList());
    }
}
