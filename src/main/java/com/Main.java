package com;

import com.ExchangeDataLoader.BinanceDataLoader;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.*;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import java.io.Closeable;
import java.time.LocalTime;
import java.util.List;
import java.util.Queue;


// really should add error handling and async asap but lets keep it simple for now
public class Main {

    public static void main(String[] args) {
        /*
        * Sandbox code right now
        * */
        DynamoManager dynamoManager = new DynamoManager();
        BinanceDataLoader binanceDataLoader = new BinanceDataLoader();
        BinanceApiWebSocketClient webSocketClient = BinanceApiClientFactory.newInstance().newWebSocketClient();

        // to keep number of api calls down we'll just fetch ETH pairs
        // i.e. "ADA/USDC" but not "ADA/BTC"
        // using USDC because smallest amount of pairs, keep it cheap
        List<String> marketSymbols = binanceDataLoader.getTickers("USDC");
        String interval = "4h";

        /*
        * populate all pairs candlesticks for interval and store in DB
        * Will likely add functions to fetch from the previous history end if candle table exists for the event of
        * system failure
        * currently this is going to repeat if the db is already populated, will optimize later
        */
        String s = marketSymbols.toString().toLowerCase();
        String socketListString = s.substring(1, s.length()-1);
        marketSymbols.forEach(symbol -> {
            List<Candlestick> candlesticks = binanceDataLoader.getCandles( symbol, interval);
            dynamoManager.writeHistoricalCandles(symbol, interval, candlesticks);
        });
        webSocketClient.onCandlestickEvent(socketListString, binanceDataLoader.stringCandlestickMap.get(interval), response -> {
            dynamoManager.writeCandle(response);
        });

    }
}
