package com.wolfml.etrade.terminal;

import java.util.Scanner;

public class KeyIn
{

    private static final Scanner scanner = new Scanner(System.in);

    public static int getKeyInInteger() throws RuntimeException
    {
        String input = scanner.nextLine();
        int choice = 0;
        if (input.equalsIgnoreCase("x"))
        {
            choice = 'x';
        } else if (input == null || input.length() == 0)
        {
            ETClientApp.out.println("Invalid input, please enter valid number");
            choice = getKeyInInteger();
        } else
        {
            try
            {
                choice = Integer.valueOf(input);
            } catch (Exception e)
            {
                ETClientApp.out.println("Invalid input, please enter valid number");
                choice = getKeyInInteger();
            }
        }
        return choice;
    }

    public static double getKeyInDouble() throws RuntimeException
    {
        String input = scanner.nextLine();
        double value = 0;
        try
        {
            value = Double.valueOf(input);
        } catch (Exception e)
        {
            ETClientApp.out.println("Invalid input, please enter valid number");
            value = getKeyInDouble();
        }
        return value;
    }

    public static String getKeyInString() throws RuntimeException
    {
        String input = scanner.nextLine();

        return input;
    }

    public static void close()
    {
        scanner.close();
    }
}
