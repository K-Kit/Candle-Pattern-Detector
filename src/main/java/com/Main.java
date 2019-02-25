package com;

import com.ExchangeDataLoader.BinanceDataLoader;
import com.ExchangeDataLoader.DepthCache;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.market.Candlestick;

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
        BinanceDataLoader binanceDataLoader = new BinanceDataLoader();


        List<String> marketSymbols = binanceDataLoader.getTickers("ETHBTC");

//        String s = marketSymbols.toString().toLowerCase();
//        String socketListString = s.substring(1, s.length()-1);
//        String interval = "4h";

        /*
        * populate all pairs candlesticks for interval and store in DB
        * Will likely add functions to fetch from the previous history end if candle table exists for the event of
        * system failure
        * currently this is going to repeat if the db is already populated, will optimize later
        */
//        marketSymbols.forEach(symbol -> {
//            List<Candlestick> candlesticks = binanceDataLoader.getCandles( symbol, interval);
//            dynamoManager.writeHistoricalCandles(symbol, interval, candlesticks);
//        });
//        webSocketClient.onCandlestickEvent(socketListString, binanceDataLoader.stringCandlestickMap.get(interval), response -> {
//            dynamoManager.writeCandle(response);
//        });

//        webSocketClient.onDepthEvent("ethbtc", (DepthEvent response) -> {
//            System.out.println(response.getAsks());
//        });

        DepthCache depthCache = new DepthCache("ETHBTC");

    }
}
