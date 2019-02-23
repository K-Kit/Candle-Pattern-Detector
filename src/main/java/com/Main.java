package com;

import com.ExchangeDataLoader.BinanceDataLoader;
import com.binance.api.client.domain.market.Candlestick;

import java.util.List;


// really should add error handling and async asap but lets keep it simple for now
public class Main {

    public static void main(String[] args) {
        /*
        * Sandbox code right now
        * */
        DynamoManager dynamoManager = new DynamoManager();
        BinanceDataLoader binanceDataLoader = new BinanceDataLoader();

        // to keep number of api calls down we'll just fetch ETH pairs
        // i.e. "ADA/ETH" but not "ADA/BTC"
        List<String> marketSymbols = binanceDataLoader.getTickers("BTC");
        String interval = "4h";

        /*
        * populate all pairs candlesticks for interval and store in DB
        * Will likely add functions to fetch from the previous history end if candle table exists for the event of
        * system failure
        */
        marketSymbols.forEach(symbol -> {
            List<Candlestick> candlesticks = binanceDataLoader.getCandles( symbol, interval);
            dynamoManager.writeHistoricalCandles(symbol, interval, candlesticks);
        });




    }
}
