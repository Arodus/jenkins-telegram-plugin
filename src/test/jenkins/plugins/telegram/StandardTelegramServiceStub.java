package jenkins.plugins.telegram;


public class StandardTelegramServiceStub extends StandardTelegramService {

    private HttpClientStub httpClientStub;

    public StandardTelegramServiceStub(String token, String roomId) {
        super(token, roomId);
    }

    @Override
    public HttpClientStub getHttpClient() {
        return httpClientStub;
    }

    public void setHttpClient(HttpClientStub httpClientStub) {
        this.httpClientStub = httpClientStub;
    }
}
