
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class AlphavantageService implements StockQuotesService {
  private RestTemplate restTemplate;

  protected AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private String getApiKey() {
    return "69MWIRFA0B9UU3ZM";
  }

  private String buildUrl(String symbol) {
    String apiKey = getApiKey();
    String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
        + symbol + "&outputsize=full&apikey=" + apiKey;
    return url;
  }

  private List<Candle> getAlphavantageCandles(Map<LocalDate, AlphavantageCandle> candles,
      LocalDate startDate, LocalDate endDate) {
    List<Candle> alphavantageCandles = new ArrayList<>();

    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      AlphavantageCandle alphavantageCandle = candles.get(date);

      if (alphavantageCandle != null) {
        alphavantageCandle.setDate(date);
        alphavantageCandles.add(alphavantageCandle);
      }
    }

    return alphavantageCandles;
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
    String url = buildUrl(symbol);
    List<Candle> alphavantageCandles;
    // AlphavantageDailyResponse obj = restTemplate.getForObject(url, AlphavantageDailyResponse.class);
    try {
      String apiResponse = restTemplate.getForObject(url, String.class);
      ObjectMapper objectMapper = getObjectMapper();
      AlphavantageDailyResponse obj = objectMapper.readValue(apiResponse, AlphavantageDailyResponse.class);
      Map<LocalDate, AlphavantageCandle> candles = obj.getCandles();
      alphavantageCandles = getAlphavantageCandles(candles, startDate, endDate);
    } catch (NullPointerException e) {
      throw new StockQuoteServiceException("Alphavantage API returned invalid response", e);
    }
    return alphavantageCandles;
  }

}

// TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
// Implement the StockQuoteService interface as per the contracts. Call Alphavantage service
// to fetch daily adjusted data for last 20 years.
// Refer to documentation here: https://www.alphavantage.co/documentation/
// --
// The implementation of this functions will be doing following tasks:
// 1. Build the appropriate url to communicate with third-party.
// The url should consider startDate and endDate if it is supported by the provider.
// 2. Perform third-party communication with the url prepared in step#1
// 3. Map the response and convert the same to List<Candle>
// 4. If the provider does not support startDate and endDate, then the implementation
// should also filter the dates based on startDate and endDate. Make sure that
// result contains the records for for startDate and endDate after filtering.
// 5. Return a sorted List<Candle> sorted ascending based on Candle#getDate
// IMP: Do remember to write readable and maintainable code, There will be few functions like
// Checking if given date falls within provided date range, etc.
// Make sure that you write Unit tests for all such functions.
// Note:
// 1. Make sure you use {RestTemplate#getForObject(URI, String)} else the test will fail.
// 2. Run the tests using command below and make sure it passes:
// ./gradlew test --tests AlphavantageServiceTest
// CHECKSTYLE:OFF
// CHECKSTYLE:ON
// TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
// 1. Write a method to create appropriate url to call Alphavantage service. The method should
// be using configurations provided in the {@link @application.properties}.
// 2. Use this method in #getStockQuote.

// TODO: CRIO_TASK_MODULE_EXCEPTIONS
// 1. Update the method signature to match the signature change in the interface.
// 2. Start throwing new StockQuoteServiceException when you get some invalid response from
// Alphavantage, or you encounter a runtime exception during Json parsing.
// 3. Make sure that the exception propagates all the way from PortfolioManager, so that the
// external user's of our API are able to explicitly handle this exception upfront.
// CHECKSTYLE:OFF

