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
 * Base contract. Let's be honest: you probably don't need this interface. However, I developed the initial revision of this
 * code while on a plane, and didn't have a connection to test everything with,
 * so being able to mock it out was pretty handy...
 *
 * @author Josh Long (josh.long@springsource.com)
 */
public  interface StockSymbolLookupClient {

    /**
     * given the ticker symbol, lookup the information about the stock
     *
     * @param symbol the symbol to lookup
     * @return a {@link StockSymbolLookup} containing information about the ticker including its closing price
     * @throws Throwable
     */
    StockSymbolLookup lookupSymbol (String symbol) throws Throwable;
}
