package com.wolfml.etrade.api.terminal;

import java.util.Scanner;

public class KeyIn
{

    private static final Scanner scanner = new Scanner(System.in);

    public static int getKeyInInteger() throws RuntimeException
    {
        String input = scanner.nextLine();
        int choice;
        if (input.equalsIgnoreCase("x"))
        {
            choice = 'x';
        } else if (input.isEmpty())
        {
            TerminalClientManager.out.println("Invalid input, please enter valid number");
            choice = getKeyInInteger();
        } else
        {
            try
            {
                choice = Integer.parseInt(input);
            } catch (Exception e)
            {
                TerminalClientManager.out.println("Invalid input, please enter valid number");
                choice = getKeyInInteger();
            }
        }

        return choice;
    }

    public static double getKeyInDouble() throws RuntimeException
    {
        String input = scanner.nextLine();
        double value;

        try
        {
            value = Double.parseDouble(input);
        } catch (Exception e)
        {
            TerminalClientManager.out.println("Invalid input, please enter valid number");
            value = getKeyInDouble();
        }

        return value;
    }

    public static String getKeyInString() throws RuntimeException
    {
        return scanner.nextLine();
    }
}
