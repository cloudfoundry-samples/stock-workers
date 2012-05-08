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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This version relies on the following RESTful YQL query:
 *
 * @author Josh Long (josh.long@springsource.com)
 *
 */
public class YahooPipesQuotesApiStockSymbolLookupClient implements StockSymbolLookupClient {

    private RestTemplate restTemplate  ;

    private String url = "http://query.yahooapis.com/v1/public/yql?q={q}&format={format}&env={env}&callback={callback}";

    private Logger logger = Logger.getLogger(YahooPipesQuotesApiStockSymbolLookupClient.class.getName());

    public YahooPipesQuotesApiStockSymbolLookupClient(RestTemplate restTemplate ){
        this.restTemplate = restTemplate;
    }

    private Map<String, String> buildParamsForUrl(String q, String format, String env, String cb) {
        Map<String, String> maps = new HashMap<String, String>();
        maps.put("q", q);
        maps.put("format", format);
        maps.put("env", env);
        maps.put("callback", cb);
        return maps;
    }

    private Map<String, String> buildStockUrl(String symbol) {
        String q = String.format("select * from yahoo.finance.quotes where symbol in (\"%s\")", symbol);
        return buildParamsForUrl(q, "json", "store://datatables.org/alltableswithkeys", "cbfunc");
    }

    @Override
    public StockSymbolLookup lookupSymbol(String symbol) throws Throwable {
        String response = restTemplate.getForObject(url, String.class, buildStockUrl(symbol));
        response = response.substring(response.indexOf("(") + 1);
        response = response.substring(0, response.lastIndexOf(")")).trim();
        logger.info(response);
        JsonNode jsonNode = new ObjectMapper().readTree(response);
        return convertJsonNodeToStockSymbolLookup(jsonNode);
    }

    private StockSymbolLookup convertJsonNodeToStockSymbolLookup(JsonNode jsonNode) {
        JsonNode jsonNode1 =jsonNode.get("query").get("results").get("quote");
        logger.info(ToStringBuilder.reflectionToString(jsonNode1));
        String symbol = jsonNode1.get("symbol").getValueAsText();
        String range = jsonNode1.get("DaysRange").getValueAsText();
        String parts[]=range.split("\\s+-\\s+")  ;
        Double low=Double.parseDouble(parts[0]), high=Double.parseDouble(parts[1]);
        String exchange = jsonNode1.get("StockExchange").getValueAsText();
        Double lastTradedPrice = jsonNode1.get("LastTradePriceOnly").getValueAsDouble();
        return new StockSymbolLookup(null, high - low, symbol,exchange, high,low, lastTradedPrice);
    }



}
