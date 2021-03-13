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
 *
 * Client fetches the portfoli details for selected accountIdKey available with account list.
 * client uses oauth_token & oauth_token_secret to access protected resources that is available via oauth handshake.
 */
public class PortfolioClient extends Client
{

    @Autowired
    AppController oauthManager;

    @Autowired
    ApiResource apiResource;

    public PortfolioClient()
    {
    }

    @Override
    public String getHttpMethod()
    {
        return "GET";
    }

    @Override
    public String getURL(String accountIdkKey)
    {
        return String.format("%s%s%s", getURL(), accountIdkKey, "/portfolio");
    }

    @Override
    public String getQueryParam()
    {
        return null;
    }

    @Override
    public String getURL()
    {
        return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getPortfolioUri());
    }

    public String getPortfolio(final String accountIdKey) throws ApiException
    {

        log.debug(" Calling Portfolio API " + getURL(accountIdKey));

        Message message = new Message();
        message.setOauthRequired(OauthRequired.YES);
        message.setHttpMethod(getHttpMethod());
        message.setUrl(getURL(accountIdKey));
        message.setContentType(ContentType.APPLICATION_JSON);

        return oauthManager.invoke(message);
    }
}
