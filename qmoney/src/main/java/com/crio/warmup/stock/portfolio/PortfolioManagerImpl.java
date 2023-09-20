
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  
  // Module 5 uses the below constructor
  @Deprecated
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // Module 6 uses the below constructor
  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }


  // TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.

  // The below enclosed code is not required in module 6 (it's used in module 5)
  //-----------------------------------Not used in module 6 (start)--------------------------------
  public List<Candle> getStockQuote(String symbol, LocalDate startDate, LocalDate endDate)
      throws JsonProcessingException {

    String uri = buildUri(symbol, startDate, endDate);
    TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);
    List<Candle> candles = new ArrayList<>();

    if (results != null)
      candles = Arrays.asList(results);

    return candles;
  }

  private String getToken() {
    return "de3846e16619e7c4a23ef0ea6f4ea5cf86dac498";
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = getToken();
    String uri = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate="
        + startDate.toString() + "&endDate=" + endDate.toString() + "&token=" + token;
    return uri;
  }
  //-----------------------------------Not used in module 6 (end)----------------------------------


  private Double getOpeningPriceOnStartDate(List<Candle> candles) {
    Double buyPrice = candles.get(0).getOpen();
    return buyPrice;
  }

  private Double getClosingPriceOnEndDate(List<Candle> candles) {
    Double sellPrice = candles.get(candles.size() - 1).getClose();
    return sellPrice;
  }

  private long calculateNoOfDays(LocalDate startDate, LocalDate endDate) {
    long noOfDays = ChronoUnit.DAYS.between(startDate, endDate);
    return noOfDays;
  }

  private double calculateNoOfYears(long days) {
    return days / 365.0;
  }

  private double calculateTotalReturns(double buyPrice, double sellPrice) {
    double result = (sellPrice - buyPrice) / buyPrice;
    return result;
  }


  private AnnualizedReturn calculateAnnualizedReturnHelper(PortfolioTrade trade, LocalDate endDate,
      Double buyPrice, Double sellPrice) {

    LocalDate startDate = trade.getPurchaseDate();
    long days = calculateNoOfDays(startDate, endDate);
    double years = calculateNoOfYears(days);
    double totalReturns = calculateTotalReturns(buyPrice, sellPrice);
    double annualReturns;

    annualReturns = Math.pow((1 + totalReturns), (1 / years)) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualReturns, totalReturns);
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws StockQuoteServiceException, JsonProcessingException{
    // TODO Auto-generated method stub
    List<AnnualizedReturn> annualReturnsList = new ArrayList<>();


    for (PortfolioTrade trade : portfolioTrades) {
      String symbol = trade.getSymbol();
      LocalDate startDate = trade.getPurchaseDate();
      List<Candle> candleList = stockQuotesService.getStockQuote(symbol, startDate, endDate);
      Double buyPrice = getOpeningPriceOnStartDate(candleList);
      Double sellPrice = getClosingPriceOnEndDate(candleList);
      AnnualizedReturn annualReturns =
          calculateAnnualizedReturnHelper(trade, endDate, buyPrice, sellPrice);
      annualReturnsList.add(annualReturns);
    }

    // Sorting the annualReturnsList according to the annualizedReturns in descending order
    sortAnnualizedReturnsList(annualReturnsList);

    return annualReturnsList;
  }
  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads)
      throws InterruptedException, StockQuoteServiceException {

    ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

    List<Future<AnnualizedReturn>> futures = createAndSubmitPortfolioManagerTasks(executorService, portfolioTrades, endDate);

    List<AnnualizedReturn> annualizedReturns = getAnnualizedReturns(futures);
    
    executorService.shutdown();
    sortAnnualizedReturnsList(annualizedReturns);
    return annualizedReturns;
  }

  private List<Future<AnnualizedReturn>> createAndSubmitPortfolioManagerTasks(ExecutorService executorService, List<PortfolioTrade> portfolioTrades, LocalDate endDate) {
    List<Future<AnnualizedReturn>> futures = new ArrayList<>();

    for(PortfolioTrade portfolioTrade : portfolioTrades) {
      PortfolioManagerTask portfolioManagerTask = new PortfolioManagerTask(portfolioTrade, stockQuotesService, endDate);
      Future<AnnualizedReturn> future = executorService.submit(portfolioManagerTask);
      futures.add(future);
    }

    return futures;
  }

  private List<AnnualizedReturn> getAnnualizedReturns(List<Future<AnnualizedReturn>> futures) throws StockQuoteServiceException, InterruptedException {
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for(Future<AnnualizedReturn> future : futures) {
      AnnualizedReturn annualizedReturn = null;
      try {
        annualizedReturn = future.get();
      } catch (ExecutionException e) {
        throw new StockQuoteServiceException("Failed to get data from service provider");
      }

      if(annualizedReturn != null)
        annualizedReturns.add(annualizedReturn);
    }

    return annualizedReturns;
  }

  private void sortAnnualizedReturnsList(List<AnnualizedReturn> annualizedReturns) {
    Collections.sort(annualizedReturns, new Comparator<AnnualizedReturn>() {

      @Override
      public int compare(AnnualizedReturn a1, AnnualizedReturn a2) {
        // TODO Auto-generated method stub
        if (a1.getAnnualizedReturn() < a2.getAnnualizedReturn())
          return 1;

        return -1;
      }
    });
  }

}


