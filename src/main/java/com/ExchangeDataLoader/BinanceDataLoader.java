package com.ExchangeDataLoader;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BinanceDataLoader {
    public Map<String, CandlestickInterval> stringCandlestickMap = new HashMap<String, CandlestickInterval>();
    BinanceApiClientFactory factory;
    BinanceApiRestClient client;

    public BinanceDataLoader(){
        /*
        * Mapping strings to candlestick intervals for easier interface
        * */
        stringCandlestickMap.put("5m", CandlestickInterval.FIVE_MINUTES);
        stringCandlestickMap.put("15m", CandlestickInterval.FIFTEEN_MINUTES);
        stringCandlestickMap.put("30m", CandlestickInterval.HALF_HOURLY);
        stringCandlestickMap.put("1h", CandlestickInterval.HOURLY);
        stringCandlestickMap.put("4h", CandlestickInterval.FOUR_HOURLY);
        /*
        * Instantiate exchange client for data fetching
        * may add API keys to constructor if there's rate limiting issues
        * */
        factory = BinanceApiClientFactory.newInstance();
        client = factory.newRestClient();
    }


    /*
    * TODO learn how to java
    * Currently sandboxing some code to adjust to java
    * just using print statements in main() to figure out what I'm working with
    */
    public static void main(String[] args) throws Exception {

    }

    public List<String> getTickers(String market){
        List<BookTicker> tickers = client.getBookTickers();
        List<String> symbols = tickers.stream().filter(s->s.getSymbol().endsWith(market)).map(bookTicker -> {
            return bookTicker.getSymbol();
        }).collect(Collectors.toList());
        return symbols;
    }

    public List<Candlestick> getCandles(String symbol, String interval){
        /*
        * Get candle interval from map and fetch candles, values supported in this example:
        * "5m", "15m", "30m", "1h", "4h"
        */
//        return client.getCandlestickBars(symbol, stringCandlestickMap.get(interval));
        // since we're only working with patterns we dont need as many candles, didn't think of this at first...
        // so we're fetching 25 candles
        return client.getCandlestickBars(symbol, stringCandlestickMap.get(interval),25, null, null);
    }
}
