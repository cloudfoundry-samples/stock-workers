package org.cloudfoundry.workers.stocks.web;

import org.cloudfoundry.workers.stocks.integration.client.StockClientGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
public class StockViewController {


    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String customer(HttpServletRequest httpServletRequest, Model model) {
        String ctxPath = httpServletRequest.getContextPath();
        model.addAttribute("context",  ctxPath );
        return "stocks";
    }
}
