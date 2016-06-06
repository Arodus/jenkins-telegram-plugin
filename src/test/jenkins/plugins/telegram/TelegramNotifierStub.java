package jenkins.plugins.telegram;

public class TelegramNotifierStub extends TelegramNotifier {

    public TelegramNotifierStub(String authToken, String chatId, String buildServerUrl,
                             String sendAs, boolean startNotification, boolean notifyAborted, boolean notifyFailure,
                             boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable, boolean notifyBackToNormal,
                             boolean notifyRepeatedFailure, boolean includeTestSummary,boolean includeFailedTests, CommitInfoChoice commitInfoChoice,
                             boolean includeCustomMessage, String customMessage) {
        super(authToken, chatId, buildServerUrl, sendAs, startNotification, notifyAborted, notifyFailure,
                notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
                includeTestSummary,includeFailedTests, commitInfoChoice, includeCustomMessage, customMessage);
    }

    public static class DescriptorImplStub extends TelegramNotifier.DescriptorImpl {

        private TelegramService telegramService;

        @Override
        public synchronized void load() {
        }

        @Override
        TelegramService getTelegramService(final String authToken, final String chatId) {
            return telegramService;
        }

        public void setTelegramService(TelegramService telegramService) {
            this.telegramService = telegramService;
        }
    }
}
