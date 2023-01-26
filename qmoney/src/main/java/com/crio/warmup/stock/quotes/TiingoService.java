
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private String getToken() {
    return "de3846e16619e7c4a23ef0ea6f4ea5cf86dac498";
  }

  private String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = getToken();
    String uri = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate="
        + startDate.toString() + "&endDate=" + endDate.toString() + "&token=" + token;
    return uri;
  }

  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate startDate, LocalDate endDate)
      throws JsonProcessingException, StockQuoteServiceException {
    // TODO Auto-generated method stub
    String uri = buildUri(symbol, startDate, endDate);
    List<Candle> candles = new ArrayList<>();

    try {
      String apiResponse = restTemplate.getForObject(uri, String.class);
      ObjectMapper objectMapper = getObjectMapper();
      TiingoCandle[] results = objectMapper.readValue(apiResponse, TiingoCandle[].class);


      if (results != null)
        candles = Arrays.asList(results);
    } catch (NullPointerException e) {
      throw new StockQuoteServiceException("Error occured when requesting response from Tiingo API", e);
    }

    return candles;
  }

}

// TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
// Implement getStockQuote method below that was also declared in the interface.

// Note:
// 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
// 2. Run the tests using command below and make sure it passes.
// ./gradlew test --tests TiingoServiceTest


// CHECKSTYLE:OFF

// TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
// Write a method to create appropriate url to call the Tiingo API.



// TODO: CRIO_TASK_MODULE_EXCEPTIONS
// 1. Update the method signature to match the signature change in the interface.
// Start throwing new StockQuoteServiceException when you get some invalid response from
// Tiingo, or if Tiingo returns empty results for whatever reason, or you encounter
// a runtime exception during Json parsing.
// 2. Make sure that the exception propagates all the way from
// PortfolioManager#calculateAnnualisedReturns so that the external user's of our API
// are able to explicitly handle this exception upfront.

// CHECKSTYLE:OFF
