package com.wolfml.etrade.oauth;

import com.wolfml.etrade.exception.ApiException;
import com.wolfml.etrade.oauth.model.Message;
import com.wolfml.etrade.oauth.model.OAuthToken;
import com.wolfml.etrade.oauth.model.SecurityContext;
import com.wolfml.etrade.terminal.KeyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.lang.invoke.MethodHandles;
import java.net.URI;

/*
 * Send the user to authorize url with oauth token. On success, client prompts the user for verifier token available at authorization page.
 */
public class AuthorizationService implements Receiver
{

    private static final Logger log = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    private Receiver nextReceiver;

    @Override
    public boolean handleMessage(Message message, SecurityContext context) throws ApiException
    {
        log.debug(" AuthorizationService .. ");
        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
        {
            try
            {
                if (context.getToken() != null)
                {

                    OAuthToken token = context.getToken();

                    String url = String.format("%s?key=%s&token=%s", context.getResouces().getAuthorizeUrl(), context.getResouces().getConsumerKey(), token.getOauth_token());

                    Desktop.getDesktop().browse(new URI(url));

                    System.out.print("Enter Verifier Code : ");

                    String code = KeyIn.getKeyInString();

                    log.debug("set code on to params " + code);

                    message.setVerifierCode(code);

                    if (nextReceiver != null)
                    {
                        nextReceiver.handleMessage(message, context);
                    } else
                    {
                        log.error(" AuthorizationService : nextReceiver is null");
                    }
                } else
                {
                    return false;
                }
            } catch (Exception e)
            {
                log.error("Error opening authorization service", e);
                throw new ApiException(500, "502", e.getMessage());
            }
        } else
        {
            log.error(" Launching default browser is not supported on current platform ");
        }

        return false;
    }

    @Override
    public void handleNext(Receiver nextHandler) throws TokenException
    {
        nextReceiver = nextHandler;
    }
}
