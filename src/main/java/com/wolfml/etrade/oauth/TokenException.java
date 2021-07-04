package com.wolfml.etrade.oauth;

public class TokenException extends RuntimeException
{
    String message;

    public TokenException(Exception e, String message)
    {
        super(e);
        this.message = message;
    }

    @Override
    public String getMessage()
    {
        return message;
    }
}
