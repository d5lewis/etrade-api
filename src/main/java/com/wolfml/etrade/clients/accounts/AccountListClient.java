package com.wolfml.etrade.clients.accounts;

import com.wolfml.etrade.clients.Client;
import com.wolfml.etrade.exception.ApiException;
import com.wolfml.etrade.oauth.AppController;
import com.wolfml.etrade.oauth.model.ApiResource;
import com.wolfml.etrade.oauth.model.ContentType;
import com.wolfml.etrade.oauth.model.Message;
import com.wolfml.etrade.oauth.model.OauthRequired;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * Client provides the account list assoicated with consumerKey.
 * client uses oauth_token & oauth_token_secret to access protected resources that is available via oauth handshake.
 *
 */
public class AccountListClient extends Client {

    @Autowired
    AppController oauthManager;

    @Autowired
    ApiResource apiResource;

    public AccountListClient() {
        super();
    }

    @Override
    public String getQueryParam() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * The HTTP request method used to send the request. Value MUST be uppercase, for example: HEAD, GET , POST, etc
     */
    @Override
    public String getHttpMethod() {
        return "GET";
    }

    @Override
    public String getURL() {
        return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getAcctListUri());
    }

    @Override
    public String getURL(String accountIdkKey) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getAccountList() throws ApiException {

        log.debug(" Calling Accountlist API " + getURL());

        Message message = new Message();
        message.setOauthRequired(OauthRequired.YES);
        message.setHttpMethod(getHttpMethod());
        message.setUrl(getURL());
        message.setContentType(ContentType.APPLICATION_JSON);

        return oauthManager.invoke(message);
    }
}
