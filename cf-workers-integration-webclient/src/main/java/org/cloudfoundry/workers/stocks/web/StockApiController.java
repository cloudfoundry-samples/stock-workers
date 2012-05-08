package org.cloudfoundry.workers.stocks.web;

import org.cloudfoundry.workers.stocks.StockSymbolLookup;
import org.cloudfoundry.workers.stocks.integration.client.StockClientGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/stocks/")
@Controller
public class StockApiController {

    @Autowired
    private StockClientGateway stockClientGateway ;

    @ResponseBody
    @RequestMapping(value = "/{ticker}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public StockSymbolLookup  customerById(@PathVariable("ticker") String ticker ) {
        return stockClientGateway.lookup(ticker);
    }

}
