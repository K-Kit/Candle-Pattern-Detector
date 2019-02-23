package com;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;


/*
* This class will include helper functions for DynamoDB
* Should make interface maybe? keeping it simple for now
* Also look into async client if extra time.
* */
public class DynamoManager {
    AmazonDynamoDBClient client;
    DynamoDB dynamoDB;

    public DynamoManager(){
        /*
         * Having trouble setting up my environmental variables, using deprecated method for now
         * will revisit this later if I have time
         * credentials class will not be included on github for obvious reasons
         * */
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build();
        dynamoDB = new DynamoDB(client);
    }

    public Item createCandleItem(Candlestick candlestick) {
        return new Item().withPrimaryKey("timestamp", candlestick.getOpenTime())
                .withDouble("open", Double.parseDouble((candlestick.getOpen())))
                .withDouble("high", Double.parseDouble((candlestick.getHigh())))
                .withDouble("low", Double.parseDouble((candlestick.getLow())))
                .withDouble("close", Double.parseDouble((candlestick.getClose())))
                .withDouble("volume", Double.parseDouble((candlestick.getVolume())));
    }

    /*
    * Think theres some way to not have to do 2 seperate funcs with generics maybe... will visit later
    * Static type is nice but definitely a bit of a learning curve
    * */
    public Item createCandleEventItem(CandlestickEvent candlestick) {
        return new Item().withPrimaryKey("timestamp", candlestick.getOpenTime())
                .withDouble("open", Double.parseDouble((candlestick.getOpen())))
                .withDouble("high", Double.parseDouble((candlestick.getHigh())))
                .withDouble("low", Double.parseDouble((candlestick.getLow())))
                .withDouble("close", Double.parseDouble((candlestick.getClose())))
                .withDouble("volume", Double.parseDouble((candlestick.getVolume())));
    }




    /*
     * naming convention is SYMBOL_INTERVAL
     * symbol being the "pair" ex. ADA/BTC
     * interval AKA time period ex. "5m", "15m"
     * resulting in something like ADABTC_5m
     * for this example I will likely only actually use 1 timeframe however I'm organizing it like this to allow
     * multi-timeperiod support in the future
    * */
    public String getCandleTableName(String symbol, String interval){
        return String.format("%s_%s", symbol, interval);
    }


    /* Just found this in the docs, will check it out later and likely refactor, too tired right now
    * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.html
    * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.BatchWriteExample.html
    * probably make a que of candlesticks as they come in from the websockets and batch add every 5sec
    * */
    public void writeCandle(CandlestickEvent candlestick){
        /*
        * Insert a single candle into its table
        */
        String symbol, interval;
        Item item = createCandleEventItem(candlestick);
        /*
        * it seems inefficient to be caching these variables, if I were writing python I'd map a list of touples
        * or something into the db functions, may revisit this later don't want to waste time fixing something
        * that works right now.  Ultimately doesn't matter on such a small scale but still curious what the "proper"
        * way of doing this would be
        */

        // probably move this to its own function at some point
        String tableName = getCandleTableName(candlestick.getSymbol().toUpperCase(), candlestick.getIntervalId());

        Table table = dynamoDB.getTable(tableName);


        // if the table doesn't exist create the table and put the item
        try {
            table.putItem(item);
        }
        catch (ResourceNotFoundException e) {
            createCandleTable(tableName);
            table.putItem(item);
        }
    }

    /*
    * Batch add entire candle history to avoid having to write 500 candles 1 at a time for 100+ pairs
    * still slow but much improved
    * */
    public void writeHistoricalCandles(String symbol, String interval, List<Candlestick> candlesticks){
        String tableName = getCandleTableName(symbol, interval);
        createCandleTable(tableName);
        try {
            do {
                TableWriteItems tableWriteItems = new TableWriteItems(tableName);

                // max batch write items is 25 so we pop the candlesticks and pass in 24 at a time
                for (int i=0; i <= 25; i++){
                    if (candlesticks.isEmpty()) break;
                    tableWriteItems.addItemToPut(createCandleItem(candlesticks.remove(0)));
                }
                BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(tableWriteItems);

                do {

                    // Check for unprocessed keys which could happen if you exceed
                    // provisioned throughput
                    Map<String, List<WriteRequest>> unprocessedItems = outcome.getUnprocessedItems();

                    if (outcome.getUnprocessedItems().size() == 0) {
                        System.out.println("No unprocessed items found");
                    }
                    else {
                        System.out.println("Retrieving the unprocessed items");
                        outcome = dynamoDB.batchWriteItemUnprocessed(unprocessedItems);
                    }

                } while (outcome.getUnprocessedItems().size() > 0);
            }
            while(!candlesticks.isEmpty());
        }
            catch (Exception e) {
            System.err.println("Failed to retrieve items: ");
            e.printStackTrace(System.err);
        }

}

    public void createCandleTable(String tableName){
        /*
        * Timestamp is index, other values will be Open High Low Close Volume
        * TODO add error handling
        * */
        try {
            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(new KeySchemaElement("timestamp", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("timestamp", ScalarAttributeType.N)),
                    new ProvisionedThroughput(10L, 10L));
            try {
                table.waitForActive();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Success.  Table status: " + table.getDescription().getTableStatus());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    public static void main(String[] args){
        String s = String.format("%s_%s", "ADA/BTC", "5m");
        System.out.println(s);
    }


}
