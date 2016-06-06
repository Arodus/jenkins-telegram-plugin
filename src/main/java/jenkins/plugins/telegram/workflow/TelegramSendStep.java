package jenkins.plugins.telegram.workflow;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.Extension;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.telegram.Messages;
import jenkins.plugins.telegram.StandardTelegramService;
import jenkins.plugins.telegram.TelegramNotifier;
import jenkins.plugins.telegram.TelegramService;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

/**
 * Created by Sebastian on 06.06.2016.
 */
public class TelegramSendStep extends AbstractStepImpl {
    @Nonnull
    private final String message;
    private String chatId;
    private boolean failOnError;
    private String token;

    @DataBoundConstructor
    public TelegramSendStep(@Nonnull String message) {
        this.message = message;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }

    public String getChatId() {
        return chatId;
    }

    @DataBoundSetter
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean getFailOnError() {
        return failOnError;
    }

    @DataBoundSetter
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public String getToken() {
        return token;
    }

    @DataBoundSetter
    public void setToken(String token) {
        this.token = token;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {
        public DescriptorImpl() {
            super(TelegramSendStepExecution.class);

        }

        @Override
        public String getFunctionName() {
            return "telegramSend";
        }

        @Override
        public String getDisplayName() {
            return Messages.TelegramSendStepDisplayName();
        }
    }

    public static class TelegramSendStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;

        @Inject
        transient TelegramSendStep step;

        @StepContextParameter
        transient TaskListener listener;

        @Override
        protected Void run() throws Exception {
            Jenkins jenkins;
            try {
                jenkins = Jenkins.getInstance();
            } catch (NullPointerException npe) {
                listener.error(Messages.NotificationFailedWithException(npe));
                return null;
            }

            TelegramNotifier.DescriptorImpl telegramDesc = jenkins.getDescriptorByType(TelegramNotifier.DescriptorImpl.class);

            String chatId = step.chatId != null ? step.chatId : telegramDesc.getChatId();
            String token = step.token != null ? step.token : telegramDesc.getToken();

            listener.getLogger().println(Messages.TelegramSendStepConfig(chatId == null, token == null));
            TelegramService telegramService = getTelegramService(token, chatId);

            boolean publishSuccess = telegramService.publish(step.message);
            if (!publishSuccess && step.failOnError) {
                throw new AbortException(Messages.NotificationFailed());
            } else if (!publishSuccess) {
                listener.error(Messages.NotificationFailed());
            }
            return null;
        }

        //streamline unit testing
        TelegramService getTelegramService(String token, String chatId) {
            return new StandardTelegramService(token, chatId);
        }

    }
}
