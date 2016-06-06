package jenkins.plugins.telegram;


import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Sebastian on 27.04.2016.
 */
public class StandardTelegramService implements TelegramService {
    private static final String  TELEGRAM_API_URL=  "https://api.telegram.org/bot%s/sendMessage";
    private static final Logger logger = Logger.getLogger(StandardTelegramService.class.getName());
    private String token;
    private String chatId;

    public StandardTelegramService(String token, String chatId) {
        super();

        this.token = token;
        this.chatId = chatId;
    }
    @Override
    public boolean publish(String message) {
        return publish(message,"");
    }
    @Override
    public boolean publish(String message, String color) {
        Boolean result = true;
        HttpClient client = getHttpClient();
        PostMethod post = new PostMethod(String.format(TELEGRAM_API_URL,token));
        post.getParams().setContentCharset("UTF-8");
        //logger.log(Level.INFO,"Telegram post url: " + String.format(TELEGRAM_API_URL,token));
        post.setRequestBody(new NameValuePair[]{
                new NameValuePair("chat_id",chatId),
                new NameValuePair("parse_mode","HTML"),
                new NameValuePair("disable_web_page_preview","true"),
                new NameValuePair("text",message)
        });

        try {
            int responseCode = client.executeMethod(post);
            String response = post.getResponseBodyAsString();
            if(responseCode != HttpStatus.SC_OK) {
                logger.log(Level.WARNING, "Telegram post may have failed. Response: " + response);
                result = false;
            }
            else {
                logger.info("Posting succeeded");
            }
        } catch (IOException e) {
            logger.severe("Error while sending notification: " + message);
            e.printStackTrace();
            result = false;
        }
        return result;
    }
    protected HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            ProxyConfiguration proxy = instance.proxy;
            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
                String username = proxy.getUserName();
                String password = proxy.getPassword();
                // Consider it to be passed if username specified. Sufficient?
                if (username != null && !"".equals(username.trim())) {
                    logger.info("Using proxy authentication (user=" + username + ")");
                    // http://hc.apache.org/httpclient-3.x/authentication.html#Proxy_Authentication
                    // and
                    // http://svn.apache.org/viewvc/httpcomponents/oac.hc3x/trunk/src/examples/BasicAuthenticationExample.java?view=markup
                    client.getState().setProxyCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(username, password));
                }
            }
        }
        return client;
    }


}
