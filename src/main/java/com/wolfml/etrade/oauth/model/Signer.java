package com.wolfml.etrade.oauth.model;

public enum Signer
{
    HMAC_SHA1("HMAC-SHA1");

    Signer(String v)
    {
        value = v;
    }

    private final String value;

    public String getValue()
    {
        return value;
    }

    public static Signer getSigner(String v)
    {
        Signer p = HMAC_SHA1;
        for (Signer s : values())
        {
            if (s.getValue().equals(v))
            {
                p = s;
            }
        }
        return p;
    }
}
