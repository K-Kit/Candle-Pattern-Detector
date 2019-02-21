package com.ExchangeDataLoader;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.market.BookTicker;

import java.util.List;
import java.util.stream.Collectors;

public class BinanceDataLoader {
    static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    static BinanceApiRestClient client = factory.newRestClient();

    /*
    * TODO learn how to java
    * Currently sandboxing some code to adjust to java
    * just using print statements in main() to figure out what I'm working with
    */
    public static void main(String[] args) throws Exception {
        List<BookTicker> tickers = client.getBookTickers();
        for (BookTicker ticker: tickers
             ) {
            System.out.println(ticker.getSymbol());
        }
        List<String> l;
        /*
        * Definitely didn't have arrow functions last time I wrote Java, Sweet!
        *
        * */
        l = tickers.stream().filter(s->s.getSymbol().endsWith("ETH")).map(bookTicker -> {
            return bookTicker.getSymbol();
        }).collect(Collectors.toList());
        System.out.println(l);
    }
}
