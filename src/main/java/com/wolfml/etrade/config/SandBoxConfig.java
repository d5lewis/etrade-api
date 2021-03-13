package com.wolfml.etrade.config;

import com.wolfml.etrade.clients.accounts.AccountListClient;
import com.wolfml.etrade.clients.accounts.BalanceClient;
import com.wolfml.etrade.clients.accounts.PortfolioClient;
import com.wolfml.etrade.clients.market.QuotesClient;
import com.wolfml.etrade.clients.order.OrderClient;
import com.wolfml.etrade.clients.order.OrderPreview;
import com.wolfml.etrade.oauth.model.ApiResource;
import com.wolfml.etrade.oauth.model.Resource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SandBoxConfig extends OOauthConfig
{

    @Override
    public ApiResource apiResource()
    {
        ApiResource apiResource = super.apiResource();
        apiResource.setApiBaseUrl(sandboxBaseUrl);
        return apiResource;
    }

    @Override
    public Resource oauthResource()
    {
        Resource resourceDetails = super.oauthResource();
        resourceDetails.setSharedSecret(sandboxSecretKey);
        resourceDetails.setConsumerKey(sandboxConsumerKey);
        return resourceDetails;
    }

    @Override
    public AccountListClient accountListClient()
    {
        return super.accountListClient();
    }

    @Override
    public BalanceClient balanceClient()
    {
        return super.balanceClient();
    }

    @Override
    public PortfolioClient portfolioClient()
    {
        return super.portfolioClient();
    }

    @Override
    public QuotesClient quotesClient()
    {
        return super.quotesClient();
    }

    @Override
    public OrderClient orderClient()
    {
        return super.orderClient();
    }

    @Override
    public OrderPreview orderPreview()
    {
        return super.orderPreview();
    }
}
