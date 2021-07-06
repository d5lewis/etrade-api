package com.wolfml.etrade.clients.order;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.wolfml.etrade.api.terminal.TerminalClientManager.lineSeparator;
import static com.wolfml.etrade.api.terminal.TerminalClientManager.out;

/*
 * Client fetches the order list for selected accountIdKey available with account list.
 * client uses oauth_token & oauth_token_secret to access protected resources that is available via oauth handshake.
 */
public class OrderClient extends Client
{
    private static final Logger logger = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    @Autowired
    AppController oauthManager;

    @Autowired
    ApiResource apiResource;

    public OrderClient()
    {
    }

    Map<String, String> apiProperties;

    /*
     * The HTTP request method used to send the request. Value MUST be uppercase, for example: HEAD, GET , POST, etc
     */
    @Override
    public String getHttpMethod()
    {
        return "GET";
    }

    @Override
    public String getQueryParam()
    {
        return null;
    }

    @Override
    public String getURL(String accountIdkKey)
    {
        return String.format("%s%s%s", getURL(), accountIdkKey, "/orders");
    }

    @Override
    public String getURL()
    {
        return String.format("%s%s", apiResource.getApiBaseUrl(), apiResource.getOrderListUri());
    }

    public void setApiProperties(Map<String, String> apiProperties)
    {
        this.apiProperties = apiProperties;
    }

    public String getOrders(final String accountIdKey) throws ApiException
    {

        log.debug(" Calling OrderList API " + getURL(accountIdKey));

        Message message = new Message();
        message.setOauthRequired(OauthRequired.YES);
        message.setHttpMethod(getHttpMethod());
        message.setUrl(getURL(accountIdKey));
        message.setContentType(ContentType.APPLICATION_JSON);

        return oauthManager.invoke(message);
    }

    /**
     * Gets all of the order data per instrument returning a list with data in the following format:
     *
     *{{"Date", "OrderId", "Order Type", "Action", "Qty", "Symbol", "Price Type", "Term", "Price", "Executed", "Status"}}
     *
     * @param response
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> parseResponse(final String response) throws Exception
    {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
        JSONObject orderResponse = (JSONObject) jsonObject.get("OrdersResponse");
        JSONArray orderData;
        List<Map<String, String>> instrumentList = new ArrayList<>();

        if (jsonObject.get("OrdersResponse") != null)
        {
            orderData = (JSONArray) orderResponse.get("Order");
            logger.debug("Date  OrderId  Type  Action  Qty  Symbol  Type  Term  Price  Executed  Status");

            for (Object orderDatum : orderData)
            {
                JSONObject order = (JSONObject) orderDatum;
                JSONArray orderDetailArr = (JSONArray) order.get("OrderDetail");
                JSONObject orderDetail = (JSONObject) orderDetailArr.get(0);
                JSONArray orderInstArr = (JSONArray) orderDetail.get("Instrument");

                for (Object o : orderInstArr)
                {
                    HashMap<String, String> instrumentMap = new HashMap<>();

                    JSONObject instrument = (JSONObject) o;
                    JSONObject product = (JSONObject) instrument.get("Product");

                    instrumentMap.put("Date", OrderUtil.convertLongToDate((Long) orderDetail.get("placedTime")));
                    instrumentMap.put("OrderId", order.get("orderId").toString());
                    instrumentMap.put("Order Type", order.get("orderType").toString());
                    instrumentMap.put("Action", instrument.get("orderAction").toString());
                    instrumentMap.put("Qty", instrument.get("orderedQuantity").toString());
                    instrumentMap.put("Symbol", product.get("symbol").toString());
                    instrumentMap.put("Price Type", PriceType.getPriceType(String.valueOf(orderDetail.get("priceType"))).toString());
                    instrumentMap.put("Term", OrderUtil.getTerm((OrderTerm.getOrderTerm(String.valueOf(orderDetail.get("orderTerm"))))));
                    instrumentMap.put("Price", OrderUtil.getPrice(PriceType.getPriceType(String.valueOf(orderDetail.get("priceType"))), orderDetail));

                    if (instrument.containsKey("averageExecutionPrice"))
                    {
                        instrumentMap.put("Executed", String.valueOf(instrument.get("averageExecutionPrice")));
                    } else
                    {
                        instrumentMap.put("Executed", "-");
                    }

                    instrumentMap.put("Status", orderDetail.get("status").toString());
                    instrumentList.add(instrumentMap);
                }
            }
        }

        return instrumentList;
    }
}
