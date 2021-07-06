package com.wolfml.etrade.api.terminal;

import com.wolfml.etrade.api.AccountInformationDelegate;
import com.wolfml.etrade.clients.order.OrderPreview;
import com.wolfml.etrade.config.OOauthConfig;
import com.wolfml.etrade.config.SandBoxConfig;
import com.wolfml.etrade.exception.ApiException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;

public class TerminalClientManager
{
    private static final Logger logger = LoggerFactory.getLogger((MethodHandles.lookup().lookupClass()));
    public static final String lineSeparator = System.lineSeparator();

    private AnnotationConfigApplicationContext ctx = null;
    private AccountInformationDelegate accountInformationDelegate;

    private boolean isLive = false;
    public final static PrintStream out = System.out;
    private CommandLine line = null;
    private final Options options = new Options();
    private final HelpFormatter formatter = new HelpFormatter();
    private final Options menuItems = new Options();
    private final Options subMenuItems = new Options();
    private final Options keyMenuItems = new Options();
    private final Options orderActionMenu = new Options();
    private final Options orderPriceTypeMenu = new Options();
    private final Options durationTypeMenu = new Options();
    private final Options orderMenu = new Options();

    public TerminalClientManager(String[] args)
    {
        initMenuItems();
        initSubMenuItems();
        initOrderActionMenu();
        initOrderPriceType();
        initDurationMenu();
        initOrderMenu();
        initKeyMenuItems();

        try
        {
            CommandLineParser parser = new DefaultParser();
            line = parser.parse(menuItems, args);
        } catch (ParseException e)
        {
            logger.error("Error parsing args {}", args, e);
        }
    }

    public void init(boolean flag)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Current Thread :{}, Id : {}", Thread.currentThread().getName(), Thread.currentThread().getId());
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

        accountInformationDelegate = new AccountInformationDelegate(ctx);

        logger.debug(" Context initialized for {}", isLive ? "Live Environment" : " Sandbox Environment");
    }

    public static void main(String[] args)
    {
        TerminalClientManager obj = new TerminalClientManager(args);

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
            logger.error("Main menu : NumberFormatException ");
        } catch (IOException e)
        {
            logger.error("Main menu : System failure ");
        }
    }

    public void keyMenu(TerminalClientManager obj) throws NumberFormatException, IOException
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

    public void mainMenu(TerminalClientManager obj) throws NumberFormatException, IOException
    {
        String symbol;
        printMainMenu();
        int choice = KeyIn.getKeyInInteger();

        switch (choice)
        {

            case 1:
                out.println(" Input selected for main menu : " + choice);
                out.printf("\n%20s %25s %25s %25s %25s\n%n", "Number", "AccountId", "AccountIdKey", "AccountDesc", "InstitutionType");

                JSONArray array = accountInformationDelegate.getAccountList();
                for (int i = 0; i < array.size(); i++)
                {
                    JSONObject innerObj = (JSONObject) array.get(i);
                    out.printf("%20s %25s %25s %25s %25s\n%n", i + 1, innerObj.get("accountId"), innerObj.get("accountIdKey"), innerObj.get("accountDesc"), innerObj.get("institutionType"));
                }

                obj.subMenu(obj);
                break;
            case 2:
                out.println(" Input selected for main menu : " + choice);
                out.print("Please enter Stock Symbol: ");
                symbol = KeyIn.getKeyInString();
                out.println(lineSeparator + "\t\t Orders for selected account index: " + lineSeparator + lineSeparator);
                accountInformationDelegate.getQuotes(symbol);
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

    public void subMenu(TerminalClientManager obj) throws NumberFormatException, IOException
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
                    out.printf("%s\t\t\tBalances for %s %s%s%n", lineSeparator, acctKey, lineSeparator, lineSeparator);
                    Map<String, Long> balances = accountInformationDelegate.getBalance(acctKey);

                    if (balances.get("accountBalance") != null)
                    {
                        out.println("\t\tCash purchasing power:   $" + balances.get("accountBalance"));
                    }

                    if (balances.get("totalAccountValue") != null)
                    {
                        out.println("\t\tLive Account Value:      $" + balances.get("totalAccountValue"));
                    }

                    if (balances.get("marginBuyingPower") != null)
                    {
                        out.println("\t\tMargin Buying Power:     $" + balances.get("marginBuyingPower"));
                    }

                    if (balances.get("cashBuyingPower") != null)
                    {
                        out.println("\t\tCash Buying Power:       $" + balances.get("cashBuyingPower"));
                    }

                    break;
                case 2:
                    out.print("Please select an account index for which you want to get Portfolio: ");
                    acctKey = KeyIn.getKeyInString();
                    accountInformationDelegate.getPortfolio(acctKey);
                    break;
                case 3:
                    printMenu(orderMenu);

                    int orderChoice = KeyIn.getKeyInInteger();

                    switch (orderChoice)
                    {
                        case 1:
                            out.print("Please select an account index for which you want to get Orders: ");
                            acctKey = KeyIn.getKeyInString();
                            out.print(accountInformationDelegate.getOrders(acctKey));
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
        Map<String, String> inputs = client.getOrderDataMap();
        String accountIdKey;

        logger.info("Please select an account index for which you want to preview Order: ");
        String acctKeyIndx = KeyIn.getKeyInString();

        try
        {
            accountIdKey = accountInformationDelegate.getAccountIdKeyForIndex(acctKeyIndx);
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

    private boolean hasOption(String key)
    {
        boolean isPresent = Boolean.FALSE;
        if (line.hasOption(key))
        {
            isPresent = Boolean.TRUE;
        }
        return isPresent;
    }

    private void printHelp()
    {
        formatter.printHelp("\nUsage: java", options);
    }

    private void initMenuItems()
    {
        Option acctList = new Option("1", "Account List");
        Option quotes = new Option("2", "Market Quotes");
        Option exitApp = new Option("3", "Go Back");
        menuItems.addOption(acctList);
        menuItems.addOption(quotes);
        menuItems.addOption(exitApp);
    }

    private void initSubMenuItems()
    {
        Option balance = new Option("1", "Get Balance");
        Option portfolios = new Option("2", "Get Portfolios");
        Option order = new Option("3", "Order");
        Option exitApp = new Option("4", "Go Back");
        subMenuItems.addOption(balance);
        subMenuItems.addOption(portfolios);
        subMenuItems.addOption(order);
        subMenuItems.addOption(exitApp);
    }

    private void initKeyMenuItems()
    {
        Option sandbox = new Option("1", "Sandbox");
        Option live = new Option("2", "Live");
        Option exitapp = new Option("x", "Exit");
        keyMenuItems.addOption(sandbox);
        keyMenuItems.addOption(live);
        keyMenuItems.addOption(exitapp);
    }

    private void printKeyMenu()
    {

        formatter.printHelp("Please select an option: ", keyMenuItems);
    }

    private void printMainMenu()
    {

        formatter.printHelp("Please select an option", menuItems);
    }

    private void printSubMenu()
    {

        formatter.printHelp("Please select an option", subMenuItems);
    }

    private void printKeyMenu(Options menu, String msg)
    {

        formatter.printHelp(msg, menu);
    }

    private void printMenu(Options menu)
    {
        formatter.printHelp("Please select an option", menu);
    }

    private void initOrderActionMenu()
    {
        Option buy = new Option("1", "Buy");
        Option sell = new Option("2", "Sell");
        Option sellShort = new Option("3", "Sell Short");

        orderActionMenu.addOption(buy);
        orderActionMenu.addOption(sell);
        orderActionMenu.addOption(sellShort);
    }

    private void initOrderPriceType()
    {
        Option market = new Option("1", "Market");
        Option limit = new Option("2", "Limit");
        orderPriceTypeMenu.addOption(market);
        orderPriceTypeMenu.addOption(limit);
    }

    private void initDurationMenu()
    {
        Option goodForDay = new Option("1", "Good for Day");
        Option immdiateOrCacel = new Option("2", "Immediate or Cancel");
        Option fillOrKill = new Option("3", "Fill or Kill");

        durationTypeMenu.addOption(goodForDay);
        durationTypeMenu.addOption(immdiateOrCacel);
        durationTypeMenu.addOption(fillOrKill);
    }

    private void initOrderMenu()
    {
        Option viewOrder = new Option("1", "View Order");
        Option previewOrder = new Option("2", "Preview Order");
        Option prevMenu = new Option("3", "Go Back");
        orderMenu.addOption(viewOrder);
        orderMenu.addOption(previewOrder);
        orderMenu.addOption(prevMenu);
    }

    private int isValidMenuItem(final String msg, Options options)
    {
        int choice = KeyIn.getKeyInInteger();

        while (!options.hasOption(String.valueOf(choice)))
        {
            printKeyMenu(options, msg);
            choice = KeyIn.getKeyInInteger();
        }
        return choice;
    }
}
