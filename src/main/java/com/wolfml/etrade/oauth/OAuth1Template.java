package com.wolfml.etrade.oauth;

import com.wolfml.etrade.oauth.model.Message;
import com.wolfml.etrade.oauth.model.OAuthToken;
import com.wolfml.etrade.oauth.model.OauthRequired;
import com.wolfml.etrade.oauth.model.SecurityContext;
import com.wolfml.etrade.oauth.model.Signer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * The class used for generating the oauth signature and related params for oauth and api request
 */
public class OAuth1Template
{

    private static final Logger log = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    private static final SecureRandom secureRand = new SecureRandom();

    private OAuthSigner oauthsigner = null;

    private String callback = "oob";

    /**
     * Required nonce value. Should be computed}.
     */
    private String oauth_nonce;

    /**
     * Realm.
     */
    private String realm = "";

    /**
     * Signature. Required but normally computed.
     */
    private String signature;

    /**
     * Name of the signature method used by the client to sign the request. Required, but normally
     * computed using
     */
    private String signatureMethod;

    /**
     * Required timestamp value. Should be computed	}.
     */
    private String timestamp;

    private final SecurityContext context;

    private final Message message;

    private final BitSet unreserved = null;

    private static final BitSet WWW_FORM_URL_SAFE = new BitSet(256);

    public OAuth1Template(SecurityContext context, Message message)
    {
        this.message = message;
        this.context = context;
		oauthsigner = getSigner();
    }

    public void setOauthsigner(OAuthSigner oauthsigner)
    {
        this.oauthsigner = oauthsigner;
    }

    public String getCallback()
    {
        return callback;
    }

    public void setCallback(String callback)
    {
        this.callback = callback;
    }

    public String getOauth_nonce()
    {
        return oauth_nonce;
    }

    public void setOauth_nonce(String oauth_nonce)
    {
        this.oauth_nonce = oauth_nonce;
    }

    public String getRealm()
    {
        return realm;
    }

    public void setRealm(String realm)
    {
        this.realm = realm;
    }

    public String getSignature()
    {
        return signature;
    }

    public void setSignature(String signature)
    {
        this.signature = signature;
    }

    public String getSignatureMethod()
    {
        return signatureMethod;
    }

    public void setSignatureMethod(String signatureMethod)
    {
        this.signatureMethod = signatureMethod;
    }

    /*
     * The timestamp value MUST be a positive integer and MUST be equal or greater than the timestamp used in previous requests
     */
    public String computeNonce()
    {
        long generatedNo = secureRand.nextLong();
        return (oauth_nonce = new String(Base64.encodeBase64((String.valueOf(generatedNo)).getBytes())));
    }

    public String computeTimestamp()
    {
        return (timestamp = Long.toString(System.currentTimeMillis() / 1000));
    }

    /**
     * Mehtod to generate the oauth signature string.  oauth_signature, string made up of several HTTP request elements in a single string.
     *
     * @param method      - Http Method
     * @param uri         - Url for api request
     * @param queryString - Query String used in request
     * @throws UnsupportedEncodingException
     * @throws GeneralSecurityException
     */
    public void computeOauthSignature(String method, String uri, String querySring) throws UnsupportedEncodingException, GeneralSecurityException
    {
        if (message.getOauthRequired() == OauthRequired.YES)
        {

			oauth_nonce = computeNonce();

			signatureMethod = getSigner().getSignatureMethod();

			timestamp = computeTimestamp();

            Map<String, String[]> requestMap = new TreeMap<String, String[]>(String.CASE_INSENSITIVE_ORDER);

            requestMap.put("oauth_consumer_key", new String[]{context.getResouces().getConsumerKey()});
            requestMap.put("oauth_timestamp", new String[]{timestamp});
            requestMap.put("oauth_nonce", new String[]{oauth_nonce});
            requestMap.put("oauth_signature_method", new String[]{signatureMethod});

            OAuthToken oauthtoken = context.getToken();

            if (oauthtoken != null)
            {
                requestMap.put("oauth_token", new String[]{oauthtoken.getOauth_token()});
                log.debug(" verifier code :" + message.getVerifierCode());
                if (StringUtils.isNotBlank(message.getVerifierCode()))
                {
                    requestMap.put("oauth_verifier", new String[]{encode(message.getVerifierCode())});
                }
            } else
            {
                requestMap.put("oauth_callback", new String[]{getCallback()});
                requestMap.put("oauth_version", new String[]{"1.0"});
            }

            method = method.toUpperCase();

            log.debug("The Query String  " + querySring);

            if (StringUtils.isNotBlank(querySring))
            {

                Map<String, String[]> qParamMap = getQueryStringMap(querySring);

                if (!qParamMap.isEmpty())
                {

                    requestMap.putAll(qParamMap);
                }
            }

            String encodedParams = encode(getNormalizedParams(requestMap));

            log.debug("Normalized string after encoding " + encodedParams);

            String encodedUri = encode(message.getUrl());

            String baseString = method + "&" + encodedUri + "&" + encodedParams;

            log.debug("signature base string " + baseString);

			signature = oauthsigner.computeSignature(baseString, context);

            log.debug(" signature " + signature);
        } else
        {
            log.debug("Nothing to do here");
        }
    }

    /**
     * @param method - Http Method
     * @param uri    - Url for api request
     * @throws UnsupportedEncodingException
     * @throws GeneralSecurityException
     */
    public void computeOauthSignature(String method, String url) throws UnsupportedEncodingException, GeneralSecurityException
    {
        computeOauthSignature(method, url, "");
    }

    /*
     * HTTP GET parameters added to the URLs in the query part (as defined by [RFC3986] section 3).
     */
    private Map<String, String[]> getQueryStringMap(String queryString)
    {
        Map<String, String[]> queryParamMap = new HashMap<String, String[]>();
        if (queryString != null && queryString.length() > 0)
        {
            for (String keyValue : queryString.split("&"))
            {
                String[] p = keyValue.split("=");
                queryParamMap.put(p[0], new String[]{p[1]});
            }
        }
        return queryParamMap;
    }

    /**
     * The request parameters are collected, sorted and concatenated into a normalized string
     * Parameters in the OAuth HTTP Authorization header excluding the realm parameter.
     * HTTP GET parameters added to the URLs in the query part (as defined by [RFC3986] section 3).
     *
     * @param paramMap - Request parameter for normalization process.
     * @return String - Normalized string
     */
    private String getNormalizedParams(Map<String, String[]> paramMap)
    {
        StringBuilder combinedParams = new StringBuilder();
        TreeMap<String, String[]> sortedMap = new TreeMap<String, String[]>();
        for (String key : paramMap.keySet())
        {
            //the value can be null, in which case we do not want any associated array here
            String[] encodedValues = (paramMap.get(key) != null) ? new String[paramMap.get(key).length] : new String[0];
            //encode keys, and sort the array
            for (int i = 0; i < encodedValues.length; i++)
            {
                encodedValues[i] = encode(paramMap.get(key)[i]);
            }
            Arrays.sort(encodedValues);
            sortedMap.put(encode(key), encodedValues);
        }
        //now we generate a string for the map in key=value&key1=value1 format by concatenating the values
        StringBuilder normalizedRequest = new StringBuilder();
        for (String key : sortedMap.keySet())
        {
            //we need to handle the case if the value is null
            if (sortedMap.get(key) == null || sortedMap.get(key).length == 0)
            {
                normalizedRequest.append(key + "=&");
            }
            for (String value : sortedMap.get(key))
            {
                //this for loop will not execute if the value is null or empty
                normalizedRequest.append(key + "=" + value + "&");
            }
        }
        return normalizedRequest.substring(0, normalizedRequest.length() - 1);
    }

    /**
     * The OAuth header is a part of the signed request, it contains the oauth_signature and oauth_signature_method parameters and their values.
     *
     * @return String - oauth header map for  oaut/api request
     */
    public MultiValueMap<String, String> getHeaderMap()
    {

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<String, String>();

        if (context.isIntialized() || OauthRequired.YES == message.getOauthRequired())
        {

            requestMap.add("oauth_consumer_key", context.getResouces().getConsumerKey());
            requestMap.add("oauth_timestamp", timestamp);
            requestMap.add("oauth_nonce", oauth_nonce);
            requestMap.add("oauth_signature_method", context.getResouces().getSignatureMethod().getValue());
            requestMap.add("oauth_signature", signature);
            if (context != null && context.getToken() != null)
            {
                OAuthToken oAuthToken = context.getToken();
                requestMap.add("oauth_token", /*encode(*/oAuthToken.getOauth_token()/*)*/);
                if (StringUtils.isNotBlank(message.getVerifierCode()))
                {
                    requestMap.add("oauth_verifier", /*encode(*/message.getVerifierCode()/*)*/);
                }
            } else
            {
                requestMap.add("oauth_callback", getCallback());
                requestMap.add("oauth_version", "1.0");
            }
            if (StringUtils.isNotBlank(message.getQueryString()))
            {

                for (String keyValue : message.getQueryString().split("&"))
                {
                    String[] p = keyValue.split("=");
                    requestMap.add(p[0], p[1]);
                }
            }
        } else
        {
            //in case of quotes api call, delayed quotes will be returned
            requestMap.add("consumerKey", context.getResouces().getConsumerKey());
        }
        return requestMap;
    }

    /**
     * It is a single string and separated generally by a comma and named Authorization.
     *
     * @return String - oauth header string for  oaut/api request
     */
    public String getAuthorizationHeader()
    {
        MultiValueMap<String, String> requestMap = getHeaderMap();
        StringBuilder buf = new StringBuilder();
        log.debug(" getAuthorizationHeader : " + context.isIntialized() + " isOauthRequired " + message.getOauthRequired());
        if (context.isIntialized() || OauthRequired.YES == message.getOauthRequired())
        {

            buf.append("OAuth ");
            //append(buf,"realm", "");z\

            for (Map.Entry<String, List<String>> e : requestMap.entrySet())
            {
                append(buf, encode(e.getKey()), encode(e.getValue().get(0)));
            }
            return buf.substring(0, buf.length() - 1);
        } else
        {
            message.setQueryString("consumerKey=" + context.getResouces().getConsumerKey());
        }

        return "";
    }

    /**
     * @param buf   - Buffer to  append the key=value seperated by coma
     * @param key   - Key name to append to buffer
     * @param value - value to append to buffer
     *              Format : OAuth realm="",oauth_signature="y%2Fkoia4FeOmALwM9Zl94pAKxjiw%3D",oauth_nonce="MTE2OTUzMzA0NDM4MDg2NzQwMw%3D%3D",
     *              oauth_signature_method="HMAC-SHA1",oauth_consumer_key="xxxxxxxxxxxxxxxxxxx",
     *              oauth_token="yyyyyyyyyyyyyyyyyyyyyyyyyyyy",oauth_timestamp="1557366622"
     */
    private void append(StringBuilder buf, final String key, final String value)
    {
        buf.append(key).append("=\"").append(value).append("\",");
    }

    /**
     * The timestamp value MUST be a positive integer and MUST be equal or greater than the timestamp used in previous requests.
     *
     * @return String  -   Returns timestamp used in generating the signature.
     */
    public String getTimestamp()
    {
        return timestamp;
    }

    /**
     * @param value - used for encoding
     * @return String - Returns the encoded string
     */
    public static String encode(String value)
    {
        if (value == null)
        {
            return "";
        }
		return new String(URLCodec.encodeUrl(WWW_FORM_URL_SAFE, value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.US_ASCII);
	}

    public static String decode(String value)
    {
        if (value == null)
        {
            return "";
        }

        try
        {
            return new String(URLCodec.decodeUrl(value.getBytes(StandardCharsets.US_ASCII)),
					StandardCharsets.UTF_8);
        } catch (DecoderException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
     * unreserved - Characters in the unreserved character set MUST NOT be encoded
     * unreserved = ALPHA, DIGIT, '-', '.', '_', '~'
     */
    static
    {
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++)
        {
            WWW_FORM_URL_SAFE.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++)
        {
            WWW_FORM_URL_SAFE.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++)
        {
            WWW_FORM_URL_SAFE.set(i);
        }
        // special chars
        WWW_FORM_URL_SAFE.set('-');
        WWW_FORM_URL_SAFE.set('_');
        WWW_FORM_URL_SAFE.set('.');
        WWW_FORM_URL_SAFE.set('~');
    }

    /**
     * @return OAuthSigner - Returns HMAC-SHA1 implementation based on the property defined under oauth property file
     */
    private OAuthSigner getSigner()
    {
        OAuthSigner signer = null;
		if (context.getResouces().getSignatureMethod() == Signer.HMAC_SHA1)
		{
			signer = new HmacSha1Signer();
		}
        return signer;
    }
}
