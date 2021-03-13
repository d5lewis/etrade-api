package com.wolfml.etrade.oauth;

import com.wolfml.etrade.exception.ApiException;
import com.wolfml.etrade.oauth.model.Message;
import com.wolfml.etrade.oauth.model.SecurityContext;

/*
 * Interface used for chaining the oauth related request objects.
 */
public interface Receiver
{
    boolean handleMessage(Message message, SecurityContext context) throws ApiException;

    void handleNext(Receiver nextHandler) throws TokenException;
}
