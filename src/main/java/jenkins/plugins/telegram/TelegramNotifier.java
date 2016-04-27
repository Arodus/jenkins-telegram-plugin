package jenkins.plugins.telegram;



import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.listeners.ItemListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TelegramNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(TelegramNotifier.class.getName());


    private String authToken;
    private String buildServerUrl;
    private String chatId;
    private String sendAs;
    private boolean startNotification;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;
    private boolean includeTestSummary;
    private CommitInfoChoice commitInfoChoice;
    private boolean includeCustomMessage;
    private String customMessage;

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    public String getChatId() {
        return chatId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getBuildServerUrl() {
        if(buildServerUrl == null || buildServerUrl.equals("")) {
            JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
            return jenkinsConfig.getUrl();
        }
        else {
            return buildServerUrl;
        }
    }

    public String getSendAs() {
        return sendAs;
    }

    public boolean getStartNotification() {
        return startNotification;
    }

    public boolean getNotifySuccess() {
        return notifySuccess;
    }

    public CommitInfoChoice getCommitInfoChoice() {
        return commitInfoChoice;
    }

    public boolean getNotifyAborted() {
        return notifyAborted;
    }

    public boolean getNotifyFailure() {
        return notifyFailure;
    }

    public boolean getNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public boolean getNotifyUnstable() {
        return notifyUnstable;
    }

    public boolean getNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    public boolean includeTestSummary() {
        return includeTestSummary;
    }

    public boolean getNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    public boolean includeCustomMessage() {
        return includeCustomMessage;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    @DataBoundConstructor
    public TelegramNotifier(final String authToken, final String chatId, final String buildServerUrl,
                         final String sendAs, final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
                         final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyBackToNormal,
                         final boolean notifyRepeatedFailure, final boolean includeTestSummary, CommitInfoChoice commitInfoChoice,
                         boolean includeCustomMessage, String customMessage) {
        super();
        this.authToken = authToken;
        this.buildServerUrl = buildServerUrl;
        this.chatId = chatId;
        this.sendAs = sendAs;
        this.startNotification = startNotification;
        this.notifyAborted = notifyAborted;
        this.notifyFailure = notifyFailure;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifySuccess = notifySuccess;
        this.notifyUnstable = notifyUnstable;
        this.notifyBackToNormal = notifyBackToNormal;
        this.notifyRepeatedFailure = notifyRepeatedFailure;
        this.includeTestSummary = includeTestSummary;
        this.commitInfoChoice = commitInfoChoice;
        this.includeCustomMessage = includeCustomMessage;
        this.customMessage = customMessage;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public TelegramService newTelegramService(AbstractBuild r, BuildListener listener) {


        String authToken = this.authToken;
        if (StringUtils.isEmpty(authToken)) {
            authToken = getDescriptor().getToken();
        }
        String chatId = this.chatId;
        if (StringUtils.isEmpty(chatId)) {
            chatId = getDescriptor().getChatId();
        }

        EnvVars env = null;
        try {
            env = r.getEnvironment(listener);
        } catch (Exception e) {
            listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
            env = new EnvVars();
        }
        authToken = env.expand(authToken);
        chatId = env.expand(chatId);

        return new StandardTelegramService(authToken, chatId);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        if (startNotification) {
            Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
            for (Publisher publisher : map.values()) {
                if (publisher instanceof TelegramNotifier) {
                    logger.info("Invoking Started...");
                    new ActiveNotifier((TelegramNotifier) publisher, listener).started(build);
                }
            }
        }
        return super.prebuild(build, listener);
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {


        private String token;
        private String chatId;
        private String buildServerUrl;
        private String sendAs;

        public static final CommitInfoChoice[] COMMIT_INFO_CHOICES = CommitInfoChoice.values();

        public DescriptorImpl() {
            load();
        }


        public String getToken() {
            return token;
        }

        public String getChatId() {
            return chatId;
        }

        public String getBuildServerUrl() {
            if(buildServerUrl == null || buildServerUrl.equals("")) {
                JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
                return jenkinsConfig.getUrl();
            }
            else {
                return buildServerUrl;
            }
        }

        public String getSendAs() {
            return sendAs;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public TelegramNotifier newInstance(StaplerRequest sr, JSONObject json) {
            String token = sr.getParameter("telegramToken");
            String chatId = sr.getParameter("telegramChatId");
            boolean startNotification = "true".equals(sr.getParameter("telegramStartNotification"));
            boolean notifySuccess = "true".equals(sr.getParameter("telegramNotifySuccess"));
            boolean notifyAborted = "true".equals(sr.getParameter("telegramNotifyAborted"));
            boolean notifyNotBuilt = "true".equals(sr.getParameter("telegramNotifyNotBuilt"));
            boolean notifyUnstable = "true".equals(sr.getParameter("telegramNotifyUnstable"));
            boolean notifyFailure = "true".equals(sr.getParameter("telegramNotifyFailure"));
            boolean notifyBackToNormal = "true".equals(sr.getParameter("telegramNotifyBackToNormal"));
            boolean notifyRepeatedFailure = "true".equals(sr.getParameter("telegramNotifyRepeatedFailure"));
            boolean includeTestSummary = "true".equals(sr.getParameter("includeTestSummary"));
            CommitInfoChoice commitInfoChoice = CommitInfoChoice.forDisplayName(sr.getParameter("telegramCommitInfoChoice"));
            boolean includeCustomMessage = "on".equals(sr.getParameter("includeCustomMessage"));
            String customMessage = sr.getParameter("customMessage");
            return new TelegramNotifier(token, chatId, buildServerUrl, sendAs, startNotification, notifyAborted,
                    notifyFailure, notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
                    includeTestSummary, commitInfoChoice, includeCustomMessage, customMessage);
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
            token = sr.getParameter("telegramToken");
            chatId = sr.getParameter("telegramChatId");
            buildServerUrl = sr.getParameter("telegramBuildServerUrl");
            sendAs = sr.getParameter("telegramSendAs");
            if(buildServerUrl == null  || buildServerUrl.equals("")) {
                JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
                buildServerUrl = jenkinsConfig.getUrl();
            }
            if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
                buildServerUrl = buildServerUrl + "/";
            }
            save();
            return super.configure(sr, formData);
        }

        TelegramService getTelegramService(final String authToken, final String chatId) {
            return new StandardTelegramService(authToken, chatId);
        }

        @Override
        public String getDisplayName() {
            return "Telegram Notifications";
        }

        public FormValidation doTestConnection(@QueryParameter("telegramToken") final String authToken,
                                               @QueryParameter("telegramChatId") final String chatId,
                                               @QueryParameter("telegramBuildServerUrl") final String buildServerUrl) throws FormException {
            try {


                String targetToken = authToken;
                if (StringUtils.isEmpty(targetToken)) {
                    targetToken = this.token;
                }
                String targetChatId = chatId;
                if (StringUtils.isEmpty(targetChatId)) {
                    targetChatId = this.chatId;
                }
                String targetBuildServerUrl = buildServerUrl;
                if (StringUtils.isEmpty(targetBuildServerUrl)) {
                    targetBuildServerUrl = this.buildServerUrl;
                }
                TelegramService testTelegramService = getTelegramService(targetToken, targetChatId);
                String message = "Telegram/Jenkins plugin: you're all set on " + targetBuildServerUrl;
                boolean success = testTelegramService.publish(message, "good");
                return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }
    }

    @Deprecated
    public static class TelegramJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {

        private String token;
        private String chatId;
        private boolean startNotification;
        private boolean notifySuccess;
        private boolean notifyAborted;
        private boolean notifyNotBuilt;
        private boolean notifyUnstable;
        private boolean notifyFailure;
        private boolean notifyBackToNormal;
        private boolean notifyRepeatedFailure;
        private boolean includeTestSummary;
        private boolean showCommitList;
        private boolean includeCustomMessage;
        private String customMessage;

        @DataBoundConstructor
        public TelegramJobProperty(String token,
                                String chatId,
                                boolean startNotification,
                                boolean notifyAborted,
                                boolean notifyFailure,
                                boolean notifyNotBuilt,
                                boolean notifySuccess,
                                boolean notifyUnstable,
                                boolean notifyBackToNormal,
                                boolean notifyRepeatedFailure,
                                boolean includeTestSummary,
                                boolean showCommitList,
                                boolean includeCustomMessage,
                                String customMessage) {
            this.token = token;
            this.chatId = chatId;
            this.startNotification = startNotification;
            this.notifyAborted = notifyAborted;
            this.notifyFailure = notifyFailure;
            this.notifyNotBuilt = notifyNotBuilt;
            this.notifySuccess = notifySuccess;
            this.notifyUnstable = notifyUnstable;
            this.notifyBackToNormal = notifyBackToNormal;
            this.notifyRepeatedFailure = notifyRepeatedFailure;
            this.includeTestSummary = includeTestSummary;
            this.showCommitList = showCommitList;
            this.includeCustomMessage = includeCustomMessage;
            this.customMessage = customMessage;
        }



        @Exported
        public String getToken() {
            return token;
        }

        @Exported
        public String getChatId() {
            return chatId;
        }

        @Exported
        public boolean getStartNotification() {
            return startNotification;
        }

        @Exported
        public boolean getNotifySuccess() {
            return notifySuccess;
        }

        @Exported
        public boolean getShowCommitList() {
            return showCommitList;
        }

        @Override
        public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
            return super.prebuild(build, listener);
        }

        @Exported
        public boolean getNotifyAborted() {
            return notifyAborted;
        }

        @Exported
        public boolean getNotifyFailure() {
            return notifyFailure;
        }

        @Exported
        public boolean getNotifyNotBuilt() {
            return notifyNotBuilt;
        }

        @Exported
        public boolean getNotifyUnstable() {
            return notifyUnstable;
        }

        @Exported
        public boolean getNotifyBackToNormal() {
            return notifyBackToNormal;
        }

        @Exported
        public boolean includeTestSummary() {
            return includeTestSummary;
        }

        @Exported
        public boolean getNotifyRepeatedFailure() {
            return notifyRepeatedFailure;
        }

        @Exported
        public boolean includeCustomMessage() {
            return includeCustomMessage;
        }

        @Exported
        public String getCustomMessage() {
            return customMessage;
        }

    }

    @Extension public static final class Migrator extends ItemListener {
        @SuppressWarnings("deprecation")
        @Override
        public void onLoaded() {
            logger.info("Starting Settings Migration Process");
            Jenkins instance = Jenkins.getInstance();
            if(instance == null){
                logger.severe("Could not retrieve Jenkins instance");
                return;
            }
            for (AbstractProject<?, ?> p : instance.getAllItems(AbstractProject.class)) {
                logger.info("processing Job: " + p.getName());

                final TelegramJobProperty telegramJobProperty = p.getProperty(TelegramJobProperty.class);

                if (telegramJobProperty == null) {
                    logger.info(String
                            .format("Configuration is already up to date for \"%s\", skipping migration",
                                    p.getName()));
                    continue;
                }

                TelegramNotifier telegramNotifier = p.getPublishersList().get(TelegramNotifier.class);

                if (telegramNotifier == null) {
                    logger.info(String
                            .format("Configuration does not have a notifier for \"%s\", not migrating settings",
                                    p.getName()));
                } else {

                    //map settings

                    if (StringUtils.isBlank(telegramNotifier.authToken)) {
                        telegramNotifier.authToken = telegramJobProperty.getToken();
                    }
                    if (StringUtils.isBlank(telegramNotifier.chatId)) {
                        telegramNotifier.chatId = telegramJobProperty.getChatId();
                    }

                    telegramNotifier.startNotification = telegramJobProperty.getStartNotification();

                    telegramNotifier.notifyAborted = telegramJobProperty.getNotifyAborted();
                    telegramNotifier.notifyFailure = telegramJobProperty.getNotifyFailure();
                    telegramNotifier.notifyNotBuilt = telegramJobProperty.getNotifyNotBuilt();
                    telegramNotifier.notifySuccess = telegramJobProperty.getNotifySuccess();
                    telegramNotifier.notifyUnstable = telegramJobProperty.getNotifyUnstable();
                    telegramNotifier.notifyBackToNormal = telegramJobProperty.getNotifyBackToNormal();
                    telegramNotifier.notifyRepeatedFailure = telegramJobProperty.getNotifyRepeatedFailure();

                    telegramNotifier.includeTestSummary = telegramJobProperty.includeTestSummary();
                    telegramNotifier.commitInfoChoice = telegramJobProperty.getShowCommitList() ? CommitInfoChoice.AUTHORS_AND_TITLES : CommitInfoChoice.NONE;
                    telegramNotifier.includeCustomMessage = telegramJobProperty.includeCustomMessage();
                    telegramNotifier.customMessage = telegramJobProperty.getCustomMessage();
                }

                try {
                    //property section is not used anymore - remove
                    p.removeProperty(TelegramJobProperty.class);
                    p.save();
                    logger.info("Configuration updated successfully");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }
}
