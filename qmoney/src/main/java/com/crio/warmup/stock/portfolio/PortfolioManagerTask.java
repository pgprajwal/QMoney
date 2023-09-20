package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.quotes.StockQuotesService;

public class PortfolioManagerTask implements Callable<AnnualizedReturn> {

    private PortfolioTrade portfolioTrade;
    private StockQuotesService stockQuotesService;
    private LocalDate endDate;

    public PortfolioManagerTask(PortfolioTrade portfolioTrade, StockQuotesService stockQuotesService, LocalDate endDate) {
        this.stockQuotesService = stockQuotesService;
        this.portfolioTrade = portfolioTrade;
        this.endDate = endDate;

    }

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
    
    
    private AnnualizedReturn calculateAnnualizedReturnHelper(PortfolioTrade trade, LocalDate endDate, Double buyPrice, Double sellPrice) {
        LocalDate startDate = trade.getPurchaseDate();
        long days = calculateNoOfDays(startDate, endDate);
        double years = calculateNoOfYears(days);
        double totalReturns = calculateTotalReturns(buyPrice, sellPrice);
        double annualReturns;
    
        annualReturns = Math.pow((1 + totalReturns), (1 / years)) - 1;
    
        return new AnnualizedReturn(trade.getSymbol(), annualReturns, totalReturns);
      }

    @Override
    public AnnualizedReturn call() throws Exception {
        String symbol = portfolioTrade.getSymbol();
        LocalDate startDate = portfolioTrade.getPurchaseDate();
        List<Candle> candleList = stockQuotesService.getStockQuote(symbol, startDate, endDate);
        Double buyPrice = getOpeningPriceOnStartDate(candleList);
        Double sellPrice = getClosingPriceOnEndDate(candleList);

        AnnualizedReturn annualizedReturn = calculateAnnualizedReturnHelper(portfolioTrade, endDate, buyPrice, sellPrice);

        return annualizedReturn;
    }
    
}
