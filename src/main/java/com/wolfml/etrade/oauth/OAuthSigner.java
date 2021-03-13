package com.wolfml.etrade.oauth;

import com.wolfml.etrade.oauth.model.SecurityContext;

import java.security.GeneralSecurityException;

/*
 * Interface used by HmacSha1Signer
 */
public interface OAuthSigner
{
    //Returns oauth signature method, for exmaple HMAC-SHA1
    String getSignatureMethod();

    //compute signature based on given signature method
    String computeSignature(String signatureBaseString, SecurityContext context) throws GeneralSecurityException;
}
