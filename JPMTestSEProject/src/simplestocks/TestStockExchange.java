package simplestocks;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestStockExchange {

	public static void dumpExampleExchangeInfo(StockExchange exchange) {
		StringBuffer sb = new StringBuffer("Exchange Name: ");
		sb.append(exchange.getExchangeName());
		sb.append("\n");
		sb.append("All Share Index value: ");
		sb.append(exchange.getAllShareIndex());
		
		//Loop over trade store and calc 
		sb.append("\nVolume Weighted Stock Price for last 15 minutes:\n");
		for (Stock s:exchange.getExchangeListedStocks()) {
			sb.append(s.getSymbol());
			sb.append(": ");
			sb.append(exchange.getVolWeightedPrice(s.getSymbol()));
			sb.append("\n");
		}
		
		sb.append("\nVolume Weighted Stock Price for last 30 minutes:\n");
		for (Stock s:exchange.getExchangeListedStocks()) {
			sb.append(s.getSymbol());
			sb.append(": ");
			sb.append(exchange.getVolWeightedPrice(s.getSymbol(), 30));
			sb.append("\n");
		}
		
		//Sample yields of all stocks for price of 10 
		sb.append("\nDividend yield for all stocks using 10 as the price:\n");
		for (Stock s:exchange.getExchangeListedStocks()) {
			sb.append(s.getSymbol());
			sb.append(": ");
			sb.append(s.getDividendYield(10));
			sb.append("\n");
		}
		
		//Sample yields of all stocks for price of 10 
		sb.append("\nP/E Ratio for all stocks using 10 as the price:\n");
		for (Stock s:exchange.getExchangeListedStocks()) {
			sb.append(s.getSymbol());
			sb.append(": ");
			sb.append(exchange.getStockPERatio(s.getSymbol(), 10));
			sb.append("\n");
		}
		
		System.out.println(sb);
	}
	
	public static void main (String[] args) {
	
		StockExchange se = new StockExchange("Global Beverage Corporation Exchange");
		
		//Load the sample stock data
		se.loadStocks();
		
		//Perform some trades to ensure pricing info is available.
		//values used for easy verification of calculations
		se.buyStock("ALE", 10, 100);
		se.buyStock("TEA", 20, 1000);
		se.buyStock("POP", 200, 10000);
		se.buyStock("GIN", 200000, 1000000);
		se.buyStock("JOE", 2000000, 10000000);
		
		//perform some trades to check vol weight calcuations for "ALE" stock.
		se.buyStock("ALE", 10, 1000);
		se.sellStock("ALE", 11, 1000);
		se.buyStock("ALE", 11, 100);

		//add another trade outside the 15 min window to check calculation bound 
		//correctly for VWSP
		long oldTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(16);
		se.buyStock("ALE", 12, 10000, new Date(oldTime));
		
		//perform and dump the various calculations to std::out 
		dumpExampleExchangeInfo(se);
		
	}
	
}
