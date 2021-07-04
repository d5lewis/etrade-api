package com.wolfml.etrade.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public abstract class Client
{

    protected static final Logger log = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    public Client()
    {
    }

    public abstract String getHttpMethod();

    public abstract String getURL();

    public abstract String getURL(final String accountIdkKey);

    public abstract String getQueryParam();
}
