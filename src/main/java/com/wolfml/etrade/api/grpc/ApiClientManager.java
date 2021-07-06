package com.wolfml.etrade.api.grpc;

import com.wolfml.etrade.api.AccountInformationDelegate;
import com.wolfml.etrade.config.OOauthConfig;
import com.wolfml.etrade.config.SandBoxConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.invoke.MethodHandles;

public class ApiClientManager
{
    private static final Logger logger = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    private final AnnotationConfigApplicationContext ctx;
    private final AccountInformationDelegate accountInformationDelegate;

    ApiClientManager(boolean isLive)
    {
        if (isLive)
        {
            ctx = new AnnotationConfigApplicationContext();
            ctx.register(OOauthConfig.class);
            ctx.refresh();
        } else
        {
            ctx = new AnnotationConfigApplicationContext();
            ctx.register(SandBoxConfig.class);
            ctx.refresh();
        }

        accountInformationDelegate = new AccountInformationDelegate(ctx);

        logger.debug(" Context initialized for {}", isLive ? "Live Environment" : " Sandbox Environment");
    }

    public void shutdown()
    {
        ctx.close();
        accountInformationDelegate.shutdown();
    }
}
