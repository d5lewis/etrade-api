package com.wolfml.etrade.clients.accounts;

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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *
 * Client fetches the portfoli details for selected accountIdKey available with account list.
 * client uses oauth_token & oauth_token_secret to access protected resources that is available via oauth handshake.
 */
public class PortfolioClient extends Client
{

    @Autowired
    AppController oauthManager;

    @Autowired
    ApiResource apiResource;

    public PortfolioClient()
    {
    }

    @Override
    public String getHttpMethod()
    {
        return "GET";
    }

    @Override
    public String getURL(String accountIdkKey)
    {
        return String.format("%s%s%s", getURL(), accountIdkKey, "/portfolio");
    }

    @Override
    public String getQueryParam()
    {
        return null;
    }

    @Override
    public String getURL()
    {
        return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getPortfolioUri());
    }

    public String getPortfolio(final String accountIdKey) throws ApiException
    {

        log.debug(" Calling Portfolio API " + getURL(accountIdKey));

        Message message = new Message();
        message.setOauthRequired(OauthRequired.YES);
        message.setHttpMethod(getHttpMethod());
        message.setUrl(getURL(accountIdKey));
        message.setContentType(ContentType.APPLICATION_JSON);

        return oauthManager.invoke(message);
    }

    /**
     * {{"Symbol", "Quantity", "Type", "Last Price", "Price Paid", "Total Gain", "Value"}}
     * @param response
     * @return
     * @throws ParseException
     */
    public List<Map<String, String>> parseResponse(String response) throws ParseException
    {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
        JSONObject portfolioResponse = (JSONObject) jsonObject.get("PortfolioResponse");
        JSONArray accountPortfolioArr = (JSONArray) portfolioResponse.get("AccountPortfolio");
        List<Map<String, String>> responseData = new ArrayList<>();

        for (Object value : accountPortfolioArr)
        {
            JSONObject acctObj = (JSONObject) value;
            JSONArray positionArr = (JSONArray) acctObj.get("Position");

            for (Object o : positionArr)
            {
                JSONObject innerObj = (JSONObject) o;
                JSONObject prdObj = (JSONObject) innerObj.get("Product");
                Map<String, String> dataMap = new HashMap<>();

                dataMap.put("Symbol", prdObj.get("symbol").toString());
                dataMap.put("Quantity", innerObj.get("quantity").toString());
                dataMap.put("Type", prdObj.get("securityType").toString());

                JSONObject quickObj = (JSONObject) innerObj.get("Quick");
                dataMap.put("Last Price", quickObj.get("lastTrade").toString());

                dataMap.put("Price Paid", innerObj.get("pricePaid").toString());
                dataMap.put("Total Gain", innerObj.get("totalGain").toString());
                dataMap.put("Value", innerObj.get("marketValue").toString());

                responseData.add(dataMap);
            }
        }

        return responseData;
    }
}
