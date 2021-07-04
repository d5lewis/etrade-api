# E*TRADE API Java Application

The example application is built to run as a microservice to manage an E*Trade account 
from a trading platform.  It also includes a command line feature to test and validate
algorithm trading in a sandbox and/or live environment.

## Table of Contents

* [Requirements](#requirements)
* [Setup](#setup)
* [Running Code](#running-code)
* [Features](#features)
* [Roadmap](#roadmap)

## Requirements
 - Java 11+
 - Gradle 7.0.1 or later (for Java 16 support)
 - An [E*TRADE](https://us.etrade.com) account.
 - E*TRADE consumer key and consumer secret.
	
 ## Setup
This microservice utilizes the Gradle plugin [nu.studer.credentials](https://plugins.gradle.org/plugin/nu.studer.credentials)
to maintain encrypted credentials for use in the live and sandbox environment of the Etrade API. A Gradle task called 
addCredentials must be executed to set your Etrade credentials.

 - Run `./gradlew addCredentials --key consumerKey --value 'your_consumer_key'`
 - Run `./gradlew addCredentials --key consumerSecret --value 'your_secret_key'`
 - Run `./gradlew addCredentials --key sandboxConsumerKey --value 'sandbox_consumer_key'`
 - Run `./gradlew addCredentials --key sandboxConsumerSecret --value 'sandbox_secret_key'`

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
   
## Roadmap
 - April 2021 release of terminal testing and validation application
 - May 2021 release of gRPC based API microservice 

## Documentation
 - [Developer Guide](https://developer.etrade.com/home)
