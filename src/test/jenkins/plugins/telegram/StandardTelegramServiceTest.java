package jenkins.plugins.telegram;

import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StandardTelegramServiceTest {

    /**
     * Publish should generally not rethrow exceptions, or it will cause a build job to fail at end.
     */

    /**
     * Use a valid host, but an invalid team domain
     */


    /**
     * Use a valid team domain, but a bad token
     */
    @Test
    public void invalidTokenShouldFail() {
        StandardTelegramService service = new StandardTelegramService("", "chatId");
        service.publish("message");
    }

    @Test
    public void publishToASingleRoomSendsASingleMessage() {
        StandardTelegramServiceStub service = new StandardTelegramServiceStub("token", "chatId");
        HttpClientStub httpClientStub = new HttpClientStub();
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertEquals(1, service.getHttpClient().getNumberOfCallsToExecuteMethod());
    }


    @Test
    public void successfulPublishToASingleRoomReturnsTrue() {
        StandardTelegramServiceStub service = new StandardTelegramServiceStub("token", "chatId");
        HttpClientStub httpClientStub = new HttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }



    @Test
    public void failedPublishToASingleRoomReturnsFalse() {
        StandardTelegramServiceStub service = new StandardTelegramServiceStub("token", "chatId");
        HttpClientStub httpClientStub = new HttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_NOT_FOUND);
        service.setHttpClient(httpClientStub);
        assertFalse(service.publish("message"));
    }



    @Test
    public void publishToEmptyRoomReturnsTrue() {
        StandardTelegramServiceStub service = new StandardTelegramServiceStub("token", "");
        HttpClientStub httpClientStub = new HttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }
}
