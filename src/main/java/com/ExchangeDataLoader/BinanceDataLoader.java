package com.ExchangeDataLoader;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.market.BookTicker;

import java.util.List;

public class BinanceDataLoader {
    static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    static BinanceApiRestClient client = factory.newRestClient();

    public static void main(String[] args) throws Exception {
        List<BookTicker> tickers = client.getBookTickers();
        for (BookTicker ticker: tickers
             ) {
            System.out.println(ticker);
        }
    }
}
