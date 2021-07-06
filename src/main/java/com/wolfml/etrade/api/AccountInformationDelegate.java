package com.wolfml.etrade.api;

import com.wolfml.etrade.clients.accounts.AccountListClient;
import com.wolfml.etrade.clients.accounts.BalanceClient;
import com.wolfml.etrade.clients.accounts.PortfolioClient;
import com.wolfml.etrade.clients.market.QuotesClient;
import com.wolfml.etrade.clients.order.OrderClient;
import com.wolfml.etrade.exception.ApiException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountInformationDelegate
{
    private static final Logger logger = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    private final AnnotationConfigApplicationContext ctx;
    private final Map<String, String> acctListMap = new HashMap<>();

    public AccountInformationDelegate(AnnotationConfigApplicationContext context)
    {
        ctx = context;
    }

    public void shutdown()
    {
    }

    public String getAccountIdKeyForIndex(String acctIndex) throws ApiException
    {
        String accountIdKey = "";
        try
        {
            accountIdKey = acctListMap.get(acctIndex);
            if (accountIdKey == null)
            {
                logger.error("Error: Invalid account index selected.");
            }
        } catch (Exception e)
        {
            logger.error(" getAccountIdKeyForIndex ", e);
        }
        if (accountIdKey == null)
        {
            throw new ApiException(0, "0", "Invalid selection for accountId index");
        }
        return accountIdKey;
    }

    /**
     * Returns a JSON array with all the available accounts listed in the format: ["Number", "AccountId",
     * "AccountIdKey", "AccountDesc", "InstitutionType"]
     *
     * @return JSON array with 0 or more JSON objects containing the five data points
     */
    public JSONArray getAccountList()
    {
        AccountListClient client = ctx.getBean(AccountListClient.class);
        JSONArray accountsArr = new JSONArray();

        try
        {
            logger.debug("Number  AccountId  AccountIdKey  AccountDesc  InstitutionType");
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(client.getAccountList());
            JSONObject acctLstResponse = (JSONObject) jsonObject.get("AccountListResponse");
            JSONObject accounts = (JSONObject) acctLstResponse.get("Accounts");
            accountsArr = (JSONArray) accounts.get("Account");
            for (int i = 0; i < accountsArr.size(); i++)
            {
                JSONObject innerObj = (JSONObject) accountsArr.get(i);
                String acctIdKey = (String) innerObj.get("accountIdKey");
                String acctStatus = (String) innerObj.get("accountStatus");
                if (acctStatus != null && !acctStatus.equals("CLOSED"))
                {
                    acctListMap.put(String.valueOf(i + 1), acctIdKey);
                    logger.debug("{} {} {} {} {}", i + 1, innerObj.get("accountId"), acctIdKey, innerObj.get("accountDesc"), innerObj.get("institutionType"));
                }
            }
        } catch (ApiException e)
        {
            logApiException(e);
        } catch (Exception e)
        {
            logger.error("Exception getting account list: {}", e.getMessage(), e);
        }

        return accountsArr;
    }

    /**
     * Gets the account balance associated with the indexed account.
     *
     * @param acctIndex 1 indexed value associated with the account to retrieve the value from.
     * @return map of associated account balances with the potential for containing the keys "accountBalance",
     * "totalAccountValue", "marginBuyingPower", and "cashBuyingPower", or less if any of those balances were not available.
     */
    public Map<String, Long> getBalance(String acctIndex)
    {
        BalanceClient client = ctx.getBean(BalanceClient.class);
        String response;
        String accountIdKey;
        Map<String, Long> balances = new HashMap<>();

        try
        {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e)
        {
            logApiException(e);
            return balances;
        }

        try
        {
            response = client.getBalance(accountIdKey);

            logger.debug("Response to get balance request {}", response);

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);

            logger.debug("JSON response to get balance request {}", jsonObject);

            JSONObject balanceResponse = (JSONObject) jsonObject.get("BalanceResponse");
            JSONObject computedRec = (JSONObject) balanceResponse.get("Computed");
            JSONObject realTimeVal = (JSONObject) computedRec.get("RealTimeValues");

            if (computedRec.get("accountBalance") != null)
            {
                long accountBalance = Long.parseLong((String) computedRec.get("accountBalance"));
                logger.debug("Cash purchasing power: ${}", accountBalance);
                balances.put("accountBalance", accountBalance);
            }

            if (realTimeVal.get("totalAccountValue") != null)
            {
                long totalAccountValue = Long.parseLong((String) realTimeVal.get("totalAccountValue"));
                logger.debug("Live Account Value: ${}", totalAccountValue);
                balances.put("totalAccountValue", totalAccountValue);
            }

            if (computedRec.get("marginBuyingPower") != null)
            {
                long marginBuyingPower = Long.parseLong((String) computedRec.get("marginBuyingPower"));
                logger.debug("Margin Buying Power: ${}", marginBuyingPower);
                balances.put("marginBuyingPower", marginBuyingPower);
            }

            if (computedRec.get("cashBuyingPower") != null)
            {
                long cashBuyingPower = Long.parseLong((String) computedRec.get("cashBuyingPower"));
                logger.debug("Cash Buying Power: ${}", cashBuyingPower);
                balances.put("cashBuyingPower", cashBuyingPower);
            }
        } catch (ApiException e)
        {
            logApiException(e);
        } catch (Exception e)
        {
            logger.error("Exception getting balance {}", e.getMessage(), e);
        }

        return balances;
    }

    /**
     * {{"Symbol", "Quantity", "Type", "LastPrice", "PricePaid", "TotalGain", "Value"}}
     *
     * @param acctIndex associated with teh account to pull the portfolio from.
     */
    public List<Map<String, String>> getPortfolio(String acctIndex)
    {
        PortfolioClient client = ctx.getBean(PortfolioClient.class);
        String response;
        String accountIdKey;
        List<Map<String, String>> portfolioList = new ArrayList<>();

        try
        {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e)
        {
            logApiException(e);
            return portfolioList;
        }

        try
        {
            response = client.getPortfolio(accountIdKey);
            logger.debug(" Response String : {}", response);
            portfolioList.addAll(client.parseResponse(response));
        } catch (ApiException e)
        {
            logApiException(e);
        } catch (Exception e)
        {
            logger.error("Exception getting portfolio {}", e.getMessage(), e);
        }

        return portfolioList;
    }

    public List<Map<String, String>> getQuotes(String symbol)
    {
        QuotesClient client = ctx.getBean(QuotesClient.class);
        String response = null;
        List<Map<String, String>> quoteList = new ArrayList<>();

        try
        {
            response = client.getQuotes(symbol);
            logger.debug(" Response String : {}", response);
        } catch (ApiException e)
        {
            logApiException(e);
        } catch (Exception e)
        {
            logger.error("Error parsing JSON return", e);
        }

        try
        {
            quoteList.addAll(client.parseResponse(response));
        } catch (Exception e)
        {
            logger.error("Error parsing JSON return", e);
        }

        return quoteList;
    }

    public List<Map<String, String>> getOrders(final String acctIndex)
    {
        OrderClient client = ctx.getBean(OrderClient.class);
        String response;
        String accountIdKey;
        List<Map<String, String>> orderList = new ArrayList<>();

        try
        {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e)
        {
            logApiException(e);
            return orderList;
        }

        try
        {
            response = client.getOrders(accountIdKey);
            logger.debug(" Get Order response : {}", response);

            if (response != null)
            {
                logger.debug("Orders for selected account index : {}", acctIndex);
                orderList.addAll(client.parseResponse(response));
            } else
            {
                logger.debug("No records for account {}", accountIdKey);
            }
        } catch (ApiException e)
        {
            logApiException(e);
        } catch (Exception e)
        {
            logger.error("Exception getting balance {}", e.getMessage(), e);
        }

        return orderList;
    }

    private static void logApiException(ApiException e)
    {
        logger.error("HttpStatus: {}", e.getHttpStatus());
        logger.error("Message: {}", e.getMessage());
        logger.error("Error Code: {}", e.getCode());
    }
}
