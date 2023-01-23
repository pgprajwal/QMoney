
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Implement the method to return new instance of PortfolioManager.
  // Remember, pass along the RestTemplate argument that is provided to the new instance.

  @Deprecated
  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {

    StockQuoteServiceFactory stockQuoteServiceFactory = StockQuoteServiceFactory.INSTANCE;

    StockQuotesService stockQuotesService =
        stockQuoteServiceFactory.getService("Tiingo", restTemplate);

    PortfolioManager portfolioManager = new PortfolioManagerImpl(stockQuotesService);

    return portfolioManager;
  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Implement the method to return new instance of PortfolioManager.
  // Steps:
  // 1. Create appropriate instance of StoockQuoteService using StockQuoteServiceFactory and then
  // use the same instance of StockQuoteService to create the instance of PortfolioManager.
  // 2. Mark the earlier constructor of PortfolioManager as @Deprecated.
  // 3. Make sure all of the tests pass by using the gradle command below:
  // ./gradlew test --tests PortfolioManagerFactory

  // Module 6 uses the below method and not the above one
  public static PortfolioManager getPortfolioManager(String provider, RestTemplate restTemplate) {
    StockQuoteServiceFactory stockQuoteServiceFactory = StockQuoteServiceFactory.INSTANCE;
    StockQuotesService stockQuotesService = stockQuoteServiceFactory.getService(provider, restTemplate);
    PortfolioManager portfolioManager = new PortfolioManagerImpl(stockQuotesService);

    return portfolioManager;
  }

}
