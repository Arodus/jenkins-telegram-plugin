package jenkins.plugins.telegram;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TelegramNotifierTest extends TestCase {

    private TelegramNotifierStub.DescriptorImplStub descriptor;
    private TelegramServiceStub telegramServiceStub;
    private boolean response;
    private FormValidation.Kind expectedResult;

    @Before
    @Override
    public void setUp() {
        descriptor = new TelegramNotifierStub.DescriptorImplStub();
    }

    public TelegramNotifierTest(TelegramServiceStub telegramServiceStub, boolean response, FormValidation.Kind expectedResult) {
        this.telegramServiceStub = telegramServiceStub;
        this.response = response;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection businessTypeKeys() {
        return Arrays.asList(new Object[][]{
                {new TelegramServiceStub(), true, FormValidation.Kind.OK},
                {new TelegramServiceStub(), false, FormValidation.Kind.ERROR},
                {null, false, FormValidation.Kind.ERROR}
        });
    }

    @Test
    public void testDoTestConnection() {
        if (telegramServiceStub != null) {
            telegramServiceStub.setResponse(response);
        }
        descriptor.setTelegramService(telegramServiceStub);
        try {
            FormValidation result = descriptor.doTestConnection("authToken", "room", "buildServerUrl");
            assertEquals(result.kind, expectedResult);
        } catch (Descriptor.FormException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public static class TelegramServiceStub implements TelegramService {

        private boolean response;

        public boolean publish(String message) {
            return response;
        }

        public boolean publish(String message, String color) {
            return response;
        }

        public void setResponse(boolean response) {
            this.response = response;
        }
    }
}
