package com.wolfml.etrade.terminal;

import com.wolfml.etrade.clients.accounts.AccountListClient;
import com.wolfml.etrade.clients.accounts.BalanceClient;
import com.wolfml.etrade.clients.accounts.PortfolioClient;
import com.wolfml.etrade.clients.market.QuotesClient;
import com.wolfml.etrade.clients.order.OrderClient;
import com.wolfml.etrade.clients.order.OrderPreview;
import com.wolfml.etrade.config.OOauthConfig;
import com.wolfml.etrade.config.SandBoxConfig;
import com.wolfml.etrade.exception.ApiException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ETClientApp extends AppCommandLine
{
    private static final Logger logger = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));

    AnnotationConfigApplicationContext ctx = null;

    Map<String, String> acctListMap = new HashMap<>();

    boolean isLive = false;

    public final static String lineSeparator = System.lineSeparator();

    public final static PrintStream out = System.out;

    public ETClientApp(String[] args)
    {
        super(args);
    }

    public void init(boolean flag)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Current Thread :{}, Id : {}", Thread.currentThread().getName(), Thread.currentThread().getId());
            }

            if (ctx != null)
            {
                ctx.close();
            }

            if (flag)
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
        } catch (Exception e)
        {
            out.println(" Sorry we are not able to initiate oauth request at this time..");
            logger.error("Oauth Initialization failed ", e);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug(" Context initialized for {}", isLive ? "Live Environment" : " Sandbox Environment");
        }
    }

    public static void main(String[] args)
    {
        ETClientApp obj = new ETClientApp(args);

        if (obj.hasOption("help"))
        {
            obj.printHelp();
            return;
        }

        try
        {
            obj.keyMenu(obj);
        } catch (NumberFormatException e)
        {
            System.out.println(" Main menu : NumberFormatException ");
        } catch (IOException e)
        {
            System.out.println(" Main menu : System failure ");
        }
    }

    public void keyMenu(ETClientApp obj) throws NumberFormatException, IOException
    {

        printKeyMenu();
        int choice = KeyIn.getKeyInInteger();

        switch (choice)
        {

            case 1:
                logger.debug(" Initializing sandbox application context..");
                isLive = false;
                init(false);
                mainMenu(this);
                break;
            case 2:
                isLive = true;
                init(true);
                logger.debug(" Initializing Live application context..");
                mainMenu(this);
                break;
            case 'x':
                out.println("Goodbye");
                System.exit(0);
                break;
            default:
                out.println("Invalid Option :");
                out.println("Goodbye");
                System.exit(0);
                break;
        }

        obj.mainMenu(obj);
    }

    public void mainMenu(ETClientApp obj) throws NumberFormatException, IOException
    {

        String symbol;

        printMainMenu();

        int choice = KeyIn.getKeyInInteger();

        switch (choice)
        {

            case 1:
                out.println(" Input selected for main menu : " + choice);

                getAccountList();
                obj.subMenu(obj);
                break;
            case 2:
                out.println(" Input selected for main menu : " + choice);

                out.print("Please enter Stock Symbol: ");
                symbol = KeyIn.getKeyInString();
                getQuotes(symbol);
                break;
            case 3:
                out.println("Back to previous menu");
                keyMenu(this);
                break;
            default:
                out.println("Invalid Option :");
                out.println("Goodbye");
                System.exit(0);
                break;
        }
    }

    public void subMenu(ETClientApp obj) throws NumberFormatException, IOException
    {

        int choice;
        String acctKey;

        do
        {

            printSubMenu();

            choice = KeyIn.getKeyInInteger();

            out.println(" Input selected for submenu : " + choice);
            switch (choice)
            {

                case 1:
                    out.print("Please select an account index for which you want to get balances: ");
                    acctKey = KeyIn.getKeyInString();
                    getBalance(acctKey);
                    break;
                case 2:
                    out.print("Please select an account index for which you want to get Portfolio: ");
                    acctKey = KeyIn.getKeyInString();
                    getPortfolio(acctKey);
                    break;
                case 3:
                    printMenu(orderMenu);

                    int orderChoice = KeyIn.getKeyInInteger();

                    switch (orderChoice)
                    {
                        case 1:
                            out.print("Please select an account index for which you want to get Orders: ");
                            acctKey = KeyIn.getKeyInString();
                            getOrders(acctKey);
                            break;
                        case 2:
                            previewOrder();
                            break;
                        case 3:
                            out.println("Back to previous menu");
                            subMenu(this);
                            break;
                        default:
                            printMenu(orderMenu);
                            break;
                    }
                    break;
                case 4:
                    //choice = 'x';
                    out.println("Going to main menu");
                    obj.mainMenu(obj);
                    break;
                default:
                    printSubMenu();
                    break;
            }
        } while (choice != 4);
    }

    public void previewOrder()
    {

        OrderPreview client = ctx.getBean(OrderPreview.class);

        //client.setSessionData(sessionData);

        Map<String, String> inputs = client.getOrderDataMap();

        String accountIdKey;

        out.print("Please select an account index for which you want to preview Order: ");
        String acctKeyIndx = KeyIn.getKeyInString();
        try
        {
            accountIdKey = getAccountIdKeyForIndex(acctKeyIndx);
        } catch (ApiException e)
        {
            return;
        }
        out.print(" Enter Symbol : ");

        String symbol = KeyIn.getKeyInString();
        inputs.put("SYMBOL", symbol);

        /* Shows Order Action Menu */
        printMenu(orderActionMenu);
        /* Accept OrderAction choice*/
        int choice = isValidMenuItem("Please select valid index for Order Action", orderActionMenu);

        /* Fills data to service*/
        client.fillOrderActionMenu(choice, inputs);

        out.print(" Enter Quantity : ");
        int qty = KeyIn.getKeyInInteger();
        inputs.put("QUANTITY", String.valueOf(qty));

        /* Shows Order PriceType  Menu */
        printMenu(orderPriceTypeMenu);

        /* Accept PriceType choice */
        choice = isValidMenuItem("Please select valid index for price type", orderPriceTypeMenu);

        /* Fills data to service*/
        client.fillOrderPriceMenu(choice, inputs);

        if (choice == 2)
        {
            out.print(" Enter limit price : ");
            double limitPirce = KeyIn.getKeyInDouble();
            inputs.put("LIMIT_PRICE", String.valueOf(limitPirce));
        }

        /* Shows Order Duration  Menu */
        printMenu(durationTypeMenu);

        choice = isValidMenuItem("Please select valid index for Duration type", durationTypeMenu);

        client.fillDurationMenu(choice, inputs);

        client.previewOrder(accountIdKey, inputs);
    }

    public void getAccountList()
    {

        AccountListClient client = ctx.getBean(AccountListClient.class);
        String response;
        try
        {
            response = client.getAccountList();
            out.printf("\n%20s %25s %25s %25s %25s\n%n", "Number", "AccountId", "AccountIdKey", "AccountDesc", "InstitutionType");

            try
            {

                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
                JSONObject acctLstResponse = (JSONObject) jsonObject.get("AccountListResponse");
                JSONObject accounts = (JSONObject) acctLstResponse.get("Accounts");
                JSONArray acctsArr = (JSONArray) accounts.get("Account");
                Iterator itr = acctsArr.iterator();
                long count = 1;
                while (itr.hasNext())
                {
                    JSONObject innerObj = (JSONObject) itr.next();
                    String acctIdKey = (String) innerObj.get("accountIdKey");
                    String acctStatus = (String) innerObj.get("accountStatus");
                    if (acctStatus != null && !acctStatus.equals("CLOSED"))
                    {
                        acctListMap.put(String.valueOf(count), acctIdKey);
                        out.printf("%20s %25s %25s %25s %25s\n%n", count, innerObj.get("accountId"), acctIdKey, innerObj.get("accountDesc"), innerObj.get("institutionType"));
                        count++;
                    }
                }
            } catch (Exception e)
            {
                logger.error(" Exception on get accountlist : {}", e.getMessage(), e);
            }
        } catch (ApiException e)
        {
            out.println();
            out.printf("HttpStatus: %20s%n", e.getHttpStatus());
            out.printf("Message: %23s%n", e.getMessage());
            out.printf("Error Code: %20s%n", e.getCode());
            out.println();
            out.println();
        } catch (Exception e)
        {
            logger.error(" getAccountList : GenericException ", e);
        }
    }

    public void getBalance(String acctIndex)
    {
        BalanceClient client = ctx.getBean(BalanceClient.class);
        String response = "";
        String accountIdKey;
        try
        {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e)
        {
            return;
        }

        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(" Response String : {}", response);
            }

            response = client.getBalance(accountIdKey);

            if (logger.isDebugEnabled())
            {
                logger.debug(" Response String : {}", response);
            }

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);

            if (logger.isDebugEnabled())
            {
                logger.debug(" JSONObject : {}", jsonObject);
            }

            JSONObject balanceResponse = (JSONObject) jsonObject.get("BalanceResponse");
            String accountId = (String) balanceResponse.get("accountId");
            out.printf("%s\t\t\tBalances for %s %s%s%n", lineSeparator, accountId, lineSeparator, lineSeparator);

            JSONObject computedRec = (JSONObject) balanceResponse.get("Computed");
            JSONObject realTimeVal = (JSONObject) computedRec.get("RealTimeValues");
            if (computedRec.get("accountBalance") != null)
            {
                if (Double.class.isAssignableFrom(computedRec.get("accountBalance").getClass()))
                {
                    Double accountBalance = (Double) computedRec.get("accountBalance");
                    out.println("\t\tCash purchasing power:   $" + accountBalance);
                } else if (Long.class.isAssignableFrom(computedRec.get("accountBalance").getClass()))
                {
                    Long accountBalance = (Long) computedRec.get("accountBalance");
                    out.println("\t\tCash purchasing power:   $" + accountBalance);
                }
            }
            if (realTimeVal.get("totalAccountValue") != null)
            {
                if (Double.class.isAssignableFrom(realTimeVal.get("totalAccountValue").getClass()))
                {
                    Double totalAccountValue = (Double) realTimeVal.get("totalAccountValue");
                    out.println("\t\tLive Account Value:      $" + totalAccountValue);
                } else if (Long.class.isAssignableFrom(realTimeVal.get("totalAccountValue").getClass()))
                {
                    Long totalAccountValue = (Long) realTimeVal.get("totalAccountValue");
                    out.println("\t\tLive Account Value:      $" + totalAccountValue);
                }
            }
            if (computedRec.get("marginBuyingPower") != null)
            {
                if (Double.class.isAssignableFrom(computedRec.get("marginBuyingPower").getClass()))
                {
                    Double marginBuyingPower = (Double) computedRec.get("marginBuyingPower");
                    out.println("\t\tMargin Buying Power:     $" + marginBuyingPower);
                } else if (Long.class.isAssignableFrom(computedRec.get("marginBuyingPower").getClass()))
                {
                    Long totalAccountValue = (Long) computedRec.get("marginBuyingPower");
                    out.println("\t\tMargin Buying Power:     $" + totalAccountValue);
                }
            }
            if (computedRec.get("cashBuyingPower") != null)
            {
                if (Double.class.isAssignableFrom(computedRec.get("cashBuyingPower").getClass()))
                {
                    Double cashBuyingPower = (Double) computedRec.get("cashBuyingPower");
                    out.println("\t\tCash Buying Power:       $" + cashBuyingPower);
                } else if (Long.class.isAssignableFrom(computedRec.get("cashBuyingPower").getClass()))
                {
                    Long cashBuyingPower = (Long) computedRec.get("cashBuyingPower");
                    out.println("\t\tCash Buying Power:       $" + cashBuyingPower);
                }
            }
            System.out.println("\n");
        } catch (ApiException e)
        {
            out.println();
            out.printf("HttpStatus: %20s%n", e.getHttpStatus());
            out.printf("Message: %23s%n", e.getMessage());
            out.printf("Error Code: %20s%n", e.getCode());
            out.println();
            out.println();
        } catch (Exception e)
        {
            logger.error(" getBalance : GenericException ", e);
        }
    }

    public void getPortfolio(String acctIndex)
    {
        PortfolioClient client = ctx.getBean(PortfolioClient.class);
        String response;
        String accountIdKey;
        try
        {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e)
        {
            return;
        }
        try
        {
            response = client.getPortfolio(accountIdKey);

            if (logger.isDebugEnabled())
            {
                logger.debug(" Response String : {}", response);
            }

            JSONParser jsonParser = new JSONParser();

            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
            //out.println(" JSONObject : " + jsonObject);
            out.println("*************************************");
            JSONObject portfolioResponse = (JSONObject) jsonObject.get("PortfolioResponse");
            //out.println(" portfolioResponse : " + portfolioResponse);
            out.println("*************************************");
            JSONArray accountPortfolioArr = (JSONArray) portfolioResponse.get("AccountPortfolio");
            Object[] responseData = new Object[8];
            Iterator acctItr = accountPortfolioArr.iterator();

            StringBuilder sbuf = new StringBuilder();
            Formatter fmt = new Formatter(sbuf);

            StringBuilder acctIdBuf = new StringBuilder();

            while (acctItr.hasNext())
            {
                JSONObject acctObj = (JSONObject) acctItr.next();

                String accountId = (String) acctObj.get("accountId");

                acctIdBuf.append(lineSeparator).append("\t\t Portfolios for ").append(accountId).append(lineSeparator).append(lineSeparator);

                JSONArray positionArr = (JSONArray) acctObj.get("Position");

                for (Object o : positionArr)
                {
                    StringBuilder formatString = new StringBuilder();
                    JSONObject innerObj = (JSONObject) o;

                    JSONObject prdObj = (JSONObject) innerObj.get("Product");
                    responseData[0] = prdObj.get("symbol");
                    formatString.append("%25s");

                    responseData[1] = innerObj.get("quantity");
                    formatString.append(" %25s");

                    responseData[2] = prdObj.get("securityType");
                    formatString.append(" %25s");

                    JSONObject quickObj = (JSONObject) innerObj.get("Quick");

                    if (Double.class.isAssignableFrom(quickObj.get("lastTrade").getClass()))
                    {
                        responseData[3] = quickObj.get("lastTrade");
                        formatString.append(" %25f");
                    } else
                    {
                        responseData[3] = quickObj.get("lastTrade");
                        formatString.append(" %25d");
                    }

                    if (Double.class.isAssignableFrom(innerObj.get("pricePaid").getClass()))
                    {
                        responseData[4] = innerObj.get("pricePaid");
                        formatString.append(" %25f");
                    } else
                    {
                        responseData[4] = innerObj.get("pricePaid");
                        formatString.append(" %25d");
                    }
                    if (Double.class.isAssignableFrom(innerObj.get("totalGain").getClass()))
                    {
                        responseData[5] = innerObj.get("totalGain");
                        formatString.append(" %25f");
                    } else
                    {
                        responseData[5] = innerObj.get("totalGain");
                        formatString.append(" %25d");
                    }
                    if (Double.class.isAssignableFrom(innerObj.get("marketValue").getClass()))
                    {
                        responseData[6] = innerObj.get("marketValue");
                        formatString.append(" %25f").append(lineSeparator);
                    } else
                    {
                        responseData[6] = innerObj.get("marketValue");
                        formatString.append(" %25d").append(lineSeparator);
                    }

                    fmt.format(formatString.toString(), responseData[0], responseData[1], responseData[2], responseData[3], responseData[4], responseData[5], responseData[6]);
                }
            }
            out.println(acctIdBuf);

            String titleFormat = "%25s %25s %25s %25s %25s %25s %25s" + System.lineSeparator();

            out.printf(titleFormat, "Symbol", "Quantity", "Type", "LastPrice", "PricePaid", "TotalGain", "Value");
            out.println(sbuf);
            out.println();
            out.println();
        } catch (ApiException e)
        {
            out.println();
            out.printf("HttpStatus: %20s%n", e.getHttpStatus());
            out.printf("Message: %23s%n", e.getMessage());
            out.printf("Error Code: %20s%n", e.getCode());
            out.println();
            out.println();
        } catch (Exception e)
        {
            logger.error(" getPortfolio ", e);
            out.println();

            out.printf("Message: %23s%n", e.getMessage());
            out.println();
            out.println();
        }
    }

    public void getQuotes(String symbol)
    {
        DecimalFormat format = new DecimalFormat("#.00");
        QuotesClient client = ctx.getBean(QuotesClient.class);
        String response;

        try
        {
            response = client.getQuotes(symbol);

            if (logger.isDebugEnabled())
            {
                logger.debug(" Response String : {}", response);
            }

            try
            {

                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(response);

                if (logger.isDebugEnabled())
                {
                    logger.debug(" JSONObject : {}", jsonObject);
                }

                JSONObject quoteResponse = (JSONObject) jsonObject.get("QuoteResponse");
                JSONArray quoteData = (JSONArray) quoteResponse.get("QuoteData");

                if (quoteData != null)
                {
                    for (Object quoteDatum : quoteData)
                    {
                        out.println();
                        JSONObject innerObj = (JSONObject) quoteDatum;
                        if (innerObj != null && innerObj.get("dateTime") != null)
                        {
                            String dateTime = (String) (innerObj.get("dateTime"));
                            out.println("Date Time: " + dateTime);
                        }

                        assert innerObj != null;
                        JSONObject product = (JSONObject) innerObj.get("Product");
                        if (product != null && product.get("symbol") != null)
                        {
                            String symbolValue = (product.get("symbol")).toString();
                            out.println("Symbol: " + symbolValue);
                        }

                        if (product != null && product.get("securityType") != null)
                        {
                            String securityType = (product.get("securityType")).toString();
                            out.println("Security Type: " + securityType);
                        }

                        JSONObject all = (JSONObject) innerObj.get("All");
                        if (all != null && all.get("lastTrade") != null)
                        {
                            String lastTrade = (all.get("lastTrade")).toString();
                            out.println("Last Price: " + lastTrade);
                        }

                        if (all != null && all.get("changeClose") != null && all.get("changeClosePercentage") != null)
                        {
                            String changeClose = format.format(all.get("changeClose"));
                            String changeClosePercentage = (all.get("changeClosePercentage")).toString();
                            out.println("Today's Change: " + changeClose + " (" + changeClosePercentage + "%)");
                        }

                        if (all != null && all.get("open") != null)
                        {
                            String open = (all.get("open")).toString();
                            out.println("Open: " + open);
                        }

                        if (all != null && all.get("previousClose") != null)
                        {
                            String previousClose = format.format(all.get("previousClose"));
                            out.println("Previous Close: " + previousClose);
                        }

                        if (all != null && all.get("bid") != null && all.get("bidSize") != null)
                        {
                            String bid = format.format(all.get("bid"));
                            String bidSize = (all.get("bidSize")).toString();
                            out.println("Bid (Size): " + bid + "x" + bidSize);
                        }

                        if (all != null && all.get("ask") != null && all.get("askSize") != null)
                        {
                            String ask = format.format(all.get("ask"));
                            String askSize = (all.get("askSize")).toString();
                            out.println("Ask (Size): " + ask + "x" + askSize);
                        }

                        if (all != null && all.get("low") != null && all.get("high") != null)
                        {
                            String low = format.format(all.get("low"));
                            String high = (all.get("high")).toString();
                            out.println("Day's Range: " + low + "-" + high);
                        }

                        if (all != null && all.get("totalVolume") != null)
                        {
                            String totalVolume = all.get("totalVolume").toString();
                            out.println("Volume: " + totalVolume);
                        }

                        JSONObject mutualFund = (JSONObject) innerObj.get("MutualFund");
                        if (mutualFund != null && mutualFund.get("netAssetValue") != null)
                        {
                            String netAssetValue = (mutualFund.get("netAssetValue")).toString();
                            out.println("Net Asset Value: " + netAssetValue);
                        }

                        if (mutualFund != null && mutualFund.get("changeClose") != null
                                && mutualFund.get("changeClosePercentage") != null)
                        {
                            String changeClose = format.format(mutualFund.get("changeClose"));
                            String changeClosePercentage = (mutualFund.get("changeClosePercentage")).toString();
                            out.println("Today's Change: " + changeClose + " (" + changeClosePercentage + "%)");
                        }

                        if (mutualFund != null && mutualFund.get("publicOfferPrice") != null)
                        {
                            String publicOfferPrice = (mutualFund.get("publicOfferPrice")).toString();
                            out.println("Public Offer Price: " + publicOfferPrice);
                        }

                        if (mutualFund != null && mutualFund.get("previousClose") != null)
                        {
                            String previousClose = (mutualFund.get("previousClose")).toString();
                            out.println("Previous Close: " + previousClose);
                        }
                    }
                    out.println();
                } else
                {
                    logger.error(" Error : Invalid stock symbol.");
                    out.println("Error : Invalid Stock Symbol.\n");
                }
            } catch (Exception e)
            {
                logger.error("Error parsing JSON return", e);
            }
        } catch (ApiException e)
        {
            out.println();
            out.printf("HttpStatus: %20s%n", e.getHttpStatus());
            out.printf("Message: %23s%n", e.getMessage());
            out.printf("Error Code: %20s%n", e.getCode());
            out.println();
            out.println();
        } catch (Exception e)
        {
            logger.error("Error parsing JSON return", e);
        }
    }

    public void getOrders(final String acctIndex)
    {
        OrderClient client = ctx.getBean(OrderClient.class);

        String response;
        String accountIdKey;
        try
        {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e)
        {
            return;
        }
        try
        {
            response = client.getOrders(accountIdKey);

            if (logger.isDebugEnabled())
            {
                logger.debug(" Get Order response : {}", response);
            }

            if (response != null)
            {

                out.println(lineSeparator + "\t\t Orders for selected account index : " + acctIndex + lineSeparator + lineSeparator);

                client.parseResponse(response);
            } else
            {
                out.println("No records...");
            }
        } catch (ApiException e)
        {
            out.println();
            out.printf("HttpStatus: %20s%n", e.getHttpStatus());
            out.printf("Message: %23s%n", e.getMessage());
            out.printf("Error Code: %20s%n", e.getCode());
            out.println();
            out.println();
        } catch (Exception e)
        {
            logger.error(" getBalance : GenericException ", e);
            out.println();

            out.printf("Message: %23s%n", e.getMessage());
            out.println();
            out.println();
        }
    }

    private String getAccountIdKeyForIndex(String acctIndex) throws ApiException
    {
        String accountIdKey = "";
        try
        {
            accountIdKey = acctListMap.get(acctIndex);
            if (accountIdKey == null)
            {
                out.println(" Error : !!! Invalid account index selected !!! ");
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
}
