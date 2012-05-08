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
package org.cloudfoundry.workers.stocks.integration.client;

import org.cloudfoundry.workers.stocks.StockSymbolLookup;

/**
 *
 * This is an interface that we'll proxy out using Spring Integration's
 * {@link org.springframework.integration.annotation.Gateway gateway}
 * mechanism to show how to do request/reply semantics using messaging. We
 * could also implement the service interface if we wanted. This just
 * shoes that the client contract need not be coupled to the service contract.
 *
 * @author Josh Long (josh.long@springsource.com)
 */
public interface StockClientGateway {

    /**
     * This will invoke the actual Spring Integration messaging code and return the results.
     * From the client's perspective, the call is synchornous. From the provider perspective,
     * however, the call is asynchronous. Code on the client need not
     *
     * @param symbol   the stock symbol lookup result
     * @return the result of the query
     */
    StockSymbolLookup lookup(String symbol);
}
