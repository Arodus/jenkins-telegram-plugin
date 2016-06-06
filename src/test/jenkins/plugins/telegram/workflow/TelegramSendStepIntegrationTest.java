package jenkins.plugins.telegram.workflow;

import hudson.model.Result;
import jenkins.plugins.telegram.Messages;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;

/**
 * Created by Sebastian on 06.06.2016.
 */
public class TelegramSendStepIntegrationTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void configRoundTrip() throws Exception{
        TelegramSendStep step1 = new TelegramSendStep("message");
        step1.setChatId("123456789");
        step1.setToken("token");
        step1.setFailOnError(true);

        TelegramSendStep step2 = new StepConfigTester(jenkinsRule).configRoundTrip(step1);
        jenkinsRule.assertEqualDataBoundBeans(step1,step2);
    }

    @Test
    public void test_global_config_override() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "workflow");
        //just define message
        job.setDefinition(new CpsFlowDefinition("telegramSend(message: 'message', token: 'token', chatId: 'chatId');", true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0).get());
        //everything should come from step configuration
        jenkinsRule.assertLogContains(Messages.TelegramSendStepConfig(false, false), run);
    }

    @Test
    public void test_fail_on_error() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "workflow");
        //just define message
        job.setDefinition(new CpsFlowDefinition("telegramSend(message: 'message', token: 'token', chatId: 'chatId', failOnError: true);", true));
        WorkflowRun run = jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        //everything should come from step configuration
        jenkinsRule.assertLogContains(Messages.NotificationFailed(), run);
    }
}
