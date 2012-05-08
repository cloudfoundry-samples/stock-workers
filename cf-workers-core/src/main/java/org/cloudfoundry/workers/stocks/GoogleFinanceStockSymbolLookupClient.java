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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

/**
 * Client API to the Google Finance stock symbol JSON feed.
 *
 *
 * @author Josh Long (josh.long@springsource.com)
 *
 */
public class GoogleFinanceStockSymbolLookupClient  implements StockSymbolLookupClient {

    private String urlTemplateForSymbol = "http://www.google.com/finance/info?infotype=infoquoteall&q={symbol}";

    private RestTemplate restTemplate;


    public GoogleFinanceStockSymbolLookupClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GoogleFinanceStockSymbolLookupClient(String urlTemplateForSymbol, RestTemplate restTemplate) {
        this.urlTemplateForSymbol = urlTemplateForSymbol;
        this.restTemplate = restTemplate;
    }

    public StockSymbolLookup lookupSymbol(String symbol) throws Throwable {
        String response = restTemplate.getForObject(this.urlTemplateForSymbol, String.class, symbol);
        logger.info("response:" + response);
        response = response.substring(response.indexOf("{"));
        response = response.substring(0, response.lastIndexOf("}") + 1).trim();
        JsonNode node = new ObjectMapper().readTree(response);
        return convertJsonNodeInToSymbolLookup(node);
    }

    private StockSymbolLookup convertJsonNodeInToSymbolLookup(JsonNode jsonNode) throws Throwable {
        Number id = jsonNode.get("id").getValueAsLong();
        Double changeWhileOpen = jsonNode.get("c").getValueAsDouble();
        String ticker = jsonNode.get("t").getValueAsText();
        String exchange = jsonNode.get("e").getValueAsText();
        Double highPrice = jsonNode.get("hi").getValueAsDouble();
        Double lowPrice = jsonNode.get("l").getValueAsDouble();
        Double lastValueWhileOpen = jsonNode.get("l").getValueAsDouble();
        StockSymbolLookup lookup = new StockSymbolLookup(id, changeWhileOpen, ticker, exchange, highPrice, lowPrice, lastValueWhileOpen);
        logger.info("service: retrieved stock information: "+ ToStringBuilder.reflectionToString(lookup));
        return lookup;
    }
    private Logger logger = Logger .getLogger(getClass().getName());
}
