# E*TRADE API Java Example Appication 

The example application provides an environment for testing and validating the sandbox and live API.

## Table of Contents

* [Requirements](#requirements)
* [Setup](#setup)
* [Running Code](#running-code)

## Requirements
 - Java 11
 - Gradle 6.6.1 or later
 - An [E*TRADE](https://us.etrade.com) account.
 - E*TRADE consumer key and consumer secret.
	
 ## Setup
 - Update oauth keys in the oauth.properties file available with source.

## Building the Code
- Run `./gradlew clean build`

## Running Code
 - Run `./gradlew run`

## Features
 - Sandbox
   * Account List
   * Balance
   * Portfolio
   * Order List
   * Order Preview
   * Quote
 - Live
   * Account List
   * Balance
   * Portfolio
   * Order List
   * Order Preview
   * Quote

## Documentation
 - [Developer Guide](https://developer.etrade.com/home)
