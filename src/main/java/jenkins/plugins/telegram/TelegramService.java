package jenkins.plugins.telegram;


public interface TelegramService {
    boolean publish(String message);

    boolean publish(String message, String color);
}
