/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cloudfoundry.workers.stocks;


/**
 * A simple entity to wrap the lookup result data.
 *
 * @author Josh Long (josh.long@springsource.com)
 *
 */
public class StockSymbolLookup {

    private Number id;
    private Double changeWhileOpen;
    private String ticker;
    private String exchange;
    private Double highPrice;
    private Double lowPrice;
    private Double lastValueWhileOpen;

    public StockSymbolLookup(){}
    public StockSymbolLookup(Number id, Double changeWhileOpen, String ticker, String exchange, Double highPrice, Double lowPrice, Double lastValueWhileOpen) {
        this.id = id;
        this.changeWhileOpen = changeWhileOpen;
        this.ticker = ticker;
        this.exchange = exchange;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.lastValueWhileOpen = lastValueWhileOpen;
    }

    public Number getId() {
        return id;
    }

    public void setId(Number id) {
        this.id = id;
    }

    public Double getChangeWhileOpen() {
        return changeWhileOpen;
    }

    public void setChangeWhileOpen(Double changeWhileOpen) {
        this.changeWhileOpen = changeWhileOpen;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public Double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(Double highPrice) {
        this.highPrice = highPrice;
    }

    public Double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(Double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Double getLastValueWhileOpen() {
        return lastValueWhileOpen;
    }

    public void setLastValueWhileOpen(Double lastValueWhileOpen) {
        this.lastValueWhileOpen = lastValueWhileOpen;
    }

    @Override
    public String toString() {
        return (String.format("ticker: %s, exchange: %s, highPrice: %s, lowPrice: %s, changeWhileOpen: %s, ID: %s, last value while open: %s",
                ticker, exchange, highPrice, lowPrice, changeWhileOpen, id, lastValueWhileOpen));
    }
}
