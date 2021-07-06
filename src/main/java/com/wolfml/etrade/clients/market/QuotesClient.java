package com.wolfml.etrade.clients.market;

import com.wolfml.etrade.clients.Client;
import com.wolfml.etrade.exception.ApiException;
import com.wolfml.etrade.oauth.AppController;
import com.wolfml.etrade.oauth.model.ApiResource;
import com.wolfml.etrade.oauth.model.ContentType;
import com.wolfml.etrade.oauth.model.Message;
import com.wolfml.etrade.oauth.model.OauthRequired;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuotesClient extends Client
{
    private static final Logger logger = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    @Autowired
    AppController oauthManager;

    @Autowired
    ApiResource apiResource;

    public QuotesClient()
    {
    }

    @Override
    public String getHttpMethod()
    {
        return "GET";
    }

    @Override
    public String getURL(String symbol)
    {
        return String.format("%s%s", getURL(), symbol);
    }

    @Override
    public String getQueryParam()
    {
        return null;
    }

    @Override
    public String getURL()
    {
        return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getQuoteUri());
    }

    /**
     * Client will provide REALTIME quotes only in case of client holding the valid access token/secret(ie, if the user accessed protected resource) and should have
     * accepted the market data agreement on website.  If the user  has not authorized the client, this client will return DELAYED quotes.
     */
    public String getQuotes(String symbol) throws ApiException
    {

        Message message = new Message();
        //delayed quotes without oauth handshake
        if (oauthManager.getContext().isIntialized())
        {
            message.setOauthRequired(OauthRequired.YES);
        } else
        {
            message.setOauthRequired(OauthRequired.NO);
        }
        message.setHttpMethod(getHttpMethod());
        message.setUrl(getURL(symbol));
        message.setContentType(ContentType.APPLICATION_JSON);

        return oauthManager.invoke(message);
    }

    /**
     * For stocks:
     * {{Date Time", "Symbol", "Security Type", "Last Price", "Today's Change $", "Today's Change %",
     * "Open", "Previous Close", "Bid (Size)", "Ask (Size)", "Day's Range", "Volume"}}
     *
     * For Mutual Funds:
     * {{"Net Asset Value", "Today's Change $", "Today's Change %", "Public Offer Price", "Previous Close"}}
     *
     * @param response
     * @return
     * @throws ParseException
     */
    public List<Map<String, String>> parseResponse(String response) throws ParseException
    {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
        List<Map<String, String>> quoteDataList = new ArrayList<>();

        logger.debug(" JSONObject : {}", jsonObject);

        JSONObject quoteResponse = (JSONObject) jsonObject.get("QuoteResponse");
        JSONArray quoteData = (JSONArray) quoteResponse.get("QuoteData");

        if (quoteData != null)
        {
            for (Object quoteDatum : quoteData)
            {
                Map<String, String> quoteMap = new HashMap<>();
                JSONObject innerObj = (JSONObject) quoteDatum;
                if (innerObj != null && innerObj.get("dateTime") != null)
                {
                    String dateTime = (String) (innerObj.get("dateTime"));
                    quoteMap.put("Date Time", dateTime);
                }

                assert innerObj != null;
                JSONObject product = (JSONObject) innerObj.get("Product");
                if (product != null && product.get("symbol") != null)
                {
                    String symbolValue = (product.get("symbol")).toString();
                    quoteMap.put("Symbol", symbolValue);
                }

                if (product != null && product.get("securityType") != null)
                {
                    String securityType = (product.get("securityType")).toString();
                    quoteMap.put("Security Type", securityType);
                }

                JSONObject all = (JSONObject) innerObj.get("All");
                if (all != null && all.get("lastTrade") != null)
                {
                    String lastTrade = (all.get("lastTrade")).toString();
                    quoteMap.put("Last Price", lastTrade);
                }

                if (all != null && all.get("changeClose") != null && all.get("changeClosePercentage") != null)
                {
                    String changeClose = all.get("changeClose").toString();
                    String changeClosePercentage = (all.get("changeClosePercentage")).toString();
                    quoteMap.put("Today's Change $", changeClose);
                    quoteMap.put("Today's Change %", changeClosePercentage);
                }

                if (all != null && all.get("open") != null)
                {
                    String open = (all.get("open")).toString();
                    quoteMap.put("Open", open);
                }

                if (all != null && all.get("previousClose") != null)
                {
                    String previousClose = all.get("previousClose").toString();
                    quoteMap.put("Previous Close", previousClose);
                }

                if (all != null && all.get("bid") != null && all.get("bidSize") != null)
                {
                    String bid = all.get("bid").toString();
                    String bidSize = all.get("bidSize").toString();
                    quoteMap.put("Bid (Size)", bid + "x" + bidSize);
                }

                if (all != null && all.get("ask") != null && all.get("askSize") != null)
                {
                    String ask = all.get("ask").toString();
                    String askSize = all.get("askSize").toString();
                    quoteMap.put("Ask (Size)", ask + "x" + askSize);
                }

                if (all != null && all.get("low") != null && all.get("high") != null)
                {
                    String low = all.get("low").toString();
                    String high = all.get("high").toString();
                    quoteMap.put("Day's Range", low + "-" + high);
                }

                if (all != null && all.get("totalVolume") != null)
                {
                    String totalVolume = all.get("totalVolume").toString();
                    quoteMap.put("Volume", totalVolume);
                }

                JSONObject mutualFund = (JSONObject) innerObj.get("MutualFund");
                if (mutualFund != null && mutualFund.get("netAssetValue") != null)
                {
                    String netAssetValue = mutualFund.get("netAssetValue").toString();
                    quoteMap.put("Net Asset Value", netAssetValue);
                }

                if (mutualFund != null && mutualFund.get("changeClose") != null
                        && mutualFund.get("changeClosePercentage") != null)
                {
                    String changeClose = mutualFund.get("changeClose").toString();
                    String changeClosePercentage = mutualFund.get("changeClosePercentage").toString();
                    quoteMap.put("Today's Change $", changeClose);
                    quoteMap.put("Today's Change %", changeClosePercentage);
                }

                if (mutualFund != null && mutualFund.get("publicOfferPrice") != null)
                {
                    String publicOfferPrice = mutualFund.get("publicOfferPrice").toString();
                    quoteMap.put("Public Offer Price", publicOfferPrice);
                }

                if (mutualFund != null && mutualFund.get("previousClose") != null)
                {
                    String previousClose = mutualFund.get("previousClose").toString();
                    quoteMap.put("Previous Close", previousClose);
                }

                quoteDataList.add(quoteMap);
            }
        } else
        {
            logger.error("Error : Invalid stock symbol.");
        }

        return quoteDataList;
    }
}
