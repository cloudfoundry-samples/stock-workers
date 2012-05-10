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

import org.springframework.util.StringUtils;

import java.util.Random;

/**
 * a mock implementation of the {@link StockSymbolLookup}. Handy for offline testing.
 *
 * @author Josh Long (josh.long@springsource.com)
 */
public class MockStockSymbolLookupClient implements StockSymbolLookupClient {

    private Double randomDouble() {
        return new Random().nextDouble();
    }

    private Long randomLong() {
        return new Random().nextLong();
    }

    private StockSymbolLookup fabricateForSymbol(String ticker, String exchange) {
        return new StockSymbolLookup(randomLong(), randomDouble(), ticker,
                StringUtils.hasText(exchange) ? exchange : "NYSE", randomDouble(), randomDouble(), randomDouble());
    }

    @Override
    public StockSymbolLookup lookupSymbol(String symbol)  throws Throwable {
        return fabricateForSymbol(symbol, null);
    }
}
