package com.wolfml.etrade.oauth;

import com.wolfml.etrade.exception.ApiException;
import com.wolfml.etrade.oauth.model.Message;
import com.wolfml.etrade.oauth.model.OAuthToken;
import com.wolfml.etrade.oauth.model.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.invoke.MethodHandles;

/*
 * Consumer Obtains a Access Token. Consumer sends an HTTP request to the service provider’s access token URL. 
 * The request MUST be signed and contains the following parameters:
 *  oauth_token : Request Token
	oauth_consumer_key : consumerKey
	oauth_nonce : A nonce is a random string, uniquely generated for each request
	oauth_signature : The signature string generated after signing Requests.
	oauth_signature_method : The signature method the Consumer used to sign the request.
	oauth_version: 1.0
 */
public class AccessTokenService implements Receiver
{

    private static final Logger log = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    Receiver nextReceiver;

    CustomRestTemplate customRestTemplate;

    @Override
    public boolean handleMessage(Message message, SecurityContext context) throws ApiException
    {

        log.debug("AccessTokenService..." + context.size());
        log.debug("AccessTokenService..." + context.getResouces().getAccessTokenUrl());

        try
        {

            message.setUrl(context.getResouces().getAccessTokenUrl());
            message.setHttpMethod(context.getResouces().getAccessTokenHttpMethod());
            OAuth1Template params = new OAuth1Template(context, message);
            //params.computeSignature(context.getResouces().getAccessTokenHttpMethod(), context.getResouces().getAccessTokenUrl());
            params.computeOauthSignature(context.getResouces().getRequestTokenHttpMethod(), context.getResouces().getRequestTokenUrl());

            //message.getHeaderMap().putAll(params.getHeaderMap());
            message.setOauthHeader(params.getAuthorizationHeader());
        } catch (Exception e)
        {
            throw new ApiException(500, "503", e.getMessage());
        }
        ResponseEntity<LinkedMultiValueMap> response = customRestTemplate.execute(message);

        MultiValueMap<String, String> body = response.getBody();

        OAuthToken oauthToken = new OAuthToken(body.getFirst("oauth_token"), body.getFirst("oauth_token_secret"));

        log.debug(" Access Token :" + oauthToken.getOauth_token());
        log.debug(" Access Token Secret :" + oauthToken.getOauth_token_secret());

        context.put("TOKEN", oauthToken);

        context.setIntialized(true);

        if (nextReceiver != null)
        {
            nextReceiver.handleMessage(message, context);
        }

        return true;
    }

    @Override
    public void handleNext(Receiver nextHandler) throws TokenException
    {
        nextReceiver = nextReceiver;
    }

    public void setCustomRestTemplate(CustomRestTemplate customRestTemplate)
    {
        this.customRestTemplate = customRestTemplate;
    }
}
