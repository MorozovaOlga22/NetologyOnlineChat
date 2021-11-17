package ru.netology;

import java.io.*;
import java.net.Socket;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Client {
    private static final String EXIT_COMMAND = "/exit";
    private static final int TIME_TO_UPDATE_MESSAGES = 5_000;

    private static final String LOG_FILE_NAME = "clientLogs" + File.separator + "file.log";
    private static final String COOKIE_FILE_NAME = "clientLogs" + File.separator + "cookie.log";

    private static final AtomicLong lastReadMessageId = calculateLastReadId();

    public static void main(String[] args) throws IOException, InterruptedException {
        final ServerSettings serverSettings = Utils.getServerSettings();
        final Socket socket = new Socket(serverSettings.getHost(), serverSettings.getPort());

        try (final Scanner scanner = new Scanner(System.in);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            final String login = getLogin(scanner);
            getNewMessages(reader, writer, login);

            final Thread messageSenderThread = getMessageSenderThread(scanner, reader, writer, login);
            messageSenderThread.start();
            final Thread messageGetterThreadThread = getMessageGetterThread(reader, writer, login);
            messageGetterThreadThread.start();
            messageSenderThread.join();
            messageGetterThreadThread.interrupt();
        }
    }

    private static AtomicLong calculateLastReadId() {
        final File file = new File(COOKIE_FILE_NAME);
        if (!file.exists()) {
            return new AtomicLong();
        }
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return new AtomicLong(Long.parseLong(reader.readLine()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AtomicLong();
    }

    private static String getLogin(Scanner scanner) {
        System.out.println("Введите логин");
        return scanner.nextLine();
    }

    private static synchronized void getNewMessages(BufferedReader reader, PrintWriter writer, String login) throws IOException {
        final ClientRequest request = new ClientRequest(ClientRequest.GET_MESSAGES_COMMAND, login, lastReadMessageId.get(), null);
        writer.println(Utils.toJson(request));
        readMessages(reader);
    }

    private static void readMessages(BufferedReader reader) throws IOException {
        final List<UserMessage> userMessages = Utils.parseMessagesJson(reader.readLine());
        final List<UserMessage> newUserMessages = userMessages.stream().filter(userMessage -> userMessage.getId() > lastReadMessageId.get()).collect(Collectors.toList());
        if (!newUserMessages.isEmpty()) {
            newUserMessages.sort(Comparator.comparingLong(UserMessage::getId));
            final long newLastReadMessageId = newUserMessages.get(newUserMessages.size() - 1).getId();
            lastReadMessageId.set(newLastReadMessageId);
            log(newUserMessages);
        }
    }

    private static void log(List<UserMessage> messages) {
        try (final PrintWriter logWriter = new PrintWriter(new FileOutputStream(LOG_FILE_NAME, true), true);
             final PrintWriter cookieWriter = new PrintWriter(new FileOutputStream(COOKIE_FILE_NAME), true)) {
            messages.forEach(logWriter::println);
            cookieWriter.println(lastReadMessageId.get());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Thread getMessageSenderThread(final Scanner scanner, final BufferedReader reader, final PrintWriter writer, final String login) {
        final Runnable task = () -> {
            while (true) {
                System.out.println("Введите сообщение. Чтобы выйти из чата введите '" + EXIT_COMMAND + "'");
                final String message = scanner.nextLine();
                synchronized (Client.class) {
                    if (EXIT_COMMAND.equals(message)) {
                        final ClientRequest request = new ClientRequest(ClientRequest.END_COMMAND, login, null, null);
                        writer.println(Utils.toJson(request));
                        return;
                    } else {
                        final ClientRequest request = new ClientRequest(ClientRequest.NEW_MESSAGE_COMMAND, login, lastReadMessageId.get(), message);
                        writer.println(Utils.toJson(request));
                        try {
                            readMessages(reader);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        return new Thread(task);
    }

    @SuppressWarnings("BusyWait")
    private static Thread getMessageGetterThread(final BufferedReader reader, final PrintWriter writer, final String login) {
        final Runnable task = () -> {
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    getNewMessages(reader, writer, login);

                    Thread.sleep(TIME_TO_UPDATE_MESSAGES);
                } catch (InterruptedException e) {
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        return new Thread(task);
    }
}