package com;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.binance.api.client.domain.market.Candlestick;

import java.util.Arrays;
import java.util.List;


/*
* This class will include helper functions for DynamoDB
* Should make interface maybe? keeping it simple for now
* Also look into async client if extra time.
* */
public class DynamoManager {
    AmazonDynamoDBClient client;
    DynamoDB ddb;

    public DynamoManager(){
        /*
         * Having trouble setting up my environmental variables, using deprecated method for now
         * will revisit this later if I have time
         * credentials class will not be included on github for obvious reasons
         * */
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build();
        ddb = new DynamoDB(client);

    }


    /* Just found this in the docs, will check it out later and likely refactor, too tired right now
    * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.html
    * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.BatchWriteExample.html
    * */
    public void insertCandle(String symbol, String interval, Candlestick candlestick){
        /*
        * Insert a single candle into its table
        * naming convention is SYMBOL_INTERVAL
        * symbol being the "pair" ex. ADA/BTC
        * interval AKA time period ex. "5m", "15m"
        * resulting in something like ADABTC_5m
        * for this example I will likely only actually use 1 timeframe however I'm organizing it like this to allow
        * multi-timeperiod support in the future
        */

        /*
        * it seems inefficient to be caching these variables, if I were writing python I'd map a list of touples
        * or something into the db functions, may revisit this later don't want to waste time fixing something
        * that works right now.  Ultimately doesn't matter on such a small scale but still curious what the "proper"
        * way of doing this would be
        */
        Double open, high, low, close, volume;
        long timestamp;

        // probably move this to its own function at some point
        String tableName = String.format("%s_%s", symbol, interval);

        Table table = ddb.getTable(tableName);

        timestamp = candlestick.getOpenTime();
        open = Double.parseDouble((candlestick.getOpen()));
        high = Double.parseDouble((candlestick.getHigh()));
        low = Double.parseDouble((candlestick.getLow()));
        close = Double.parseDouble((candlestick.getClose()));
        volume = Double.parseDouble((candlestick.getVolume()));

        Item item = new Item().withPrimaryKey("timestamp", timestamp)
                .withDouble("open", open)
                .withDouble("high", high)
                .withDouble("low", low)
                .withDouble("close", close)
                .withDouble("volume", volume);

        // if the table doesn't exist create the table and put the item
        try {
            table.putItem(item);
        }
        catch (ResourceNotFoundException e) {
            createCandleTable(tableName);
            table.putItem(item);
        }
    }

    public void createCandleTable(String tableName){
        /*
        * Timestamp is index, other values will be Open High Low Close Volume
        * TODO add error handling
        * */
        Table table = ddb.createTable(tableName,
                Arrays.asList(new KeySchemaElement("timestamp", KeyType.HASH)), // Sort key
                Arrays.asList(new AttributeDefinition("timestamp", ScalarAttributeType.N)),
                new ProvisionedThroughput(10L, 10L));
        try {
            table.waitForActive();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());
    }



    public static void main(String[] args){
        String s = String.format("%s_%s", "ADA/BTC", "5m");
        System.out.println(s);
    }


}
