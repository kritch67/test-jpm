package simplestocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import simplestocks.Stock.StockType;
import simplestocks.Trade.TradeType;


/**
 * 
 * Assumptions and limitations:
 * 
 * 1) No effort has been made with regards thread safety.
 * 2) doubles are used throughout for clarity where floating point results are a possibility. Precision is not considered.
 * 3) No stock will have both a common and preferred variant with the same symbolic name. 
 * 4) Price information is not stored in the Stock object itself but rather is taken from 
 * the latest trade. 
 * 
 *
 */
public class StockExchange {

	private String exchangeName;
	
	// Store of all trades with stock symbolic name as the key. This won't work if multiple 
	// variants of the stock can have the same name. 
	private Map <String, List<Trade>> tradeStore;
	
	///Store the available stocks
	private Map <String, Stock> stockStore; 
	
	///Store the latest trade to use for pricing information 
	private Map <String, Trade> priceStore;
	
	public StockExchange(String name) {
		this.exchangeName = name;
		stockStore = new HashMap<String, Stock>();
		tradeStore = new HashMap<String, List<Trade>>();
		priceStore = new HashMap<String, Trade>();
	}
	
	/**
	 * Calculate the Volume Weighted Stock Price based on trades in past 15 minutes
	 * 
	 * @param symName The stock name to calculate for
	 * @return The Volume Weighted Stock Price
	 */
	public double getVolWeightedPrice(String symName) {
		return getVolWeightedPrice(symName, 15);
	}
	
	/**
	 * Calculate the Volume Weighted Stock Price based on trades over the 
	 * specified interval
	 * 
	 * @param symName The stock name to calculate for
	 * @param interval The interval in minutes
	 * @return The Volume Weighted Stock Price
	 */
	public double getVolWeightedPrice(String symName, int interval) {
		
		//interval is in minutes so convert to millis 
		Date curTime = new Date();
		long timeBound = curTime.getTime() - TimeUnit.MINUTES.toMillis(interval);
		
		double totalTradeValue = 0.0;
		long totalSharesTraded = 0;

		for(Trade t:tradeStore.get(symName)) {
			if(t.getTimestamp().getTime() >= timeBound) {
				totalTradeValue = totalTradeValue + (t.getPrice() * t.getQuantity());
				totalSharesTraded = totalSharesTraded + t.getQuantity();
			}
		}
		
		// now if we have some trades in the interval 
		// calc the volume weighted price		
		if(totalSharesTraded>0) {
			return totalTradeValue / totalSharesTraded;	
		}
		return 0;
	}

	/**
	 * Calculates the exchange All Share Index using the geometric mean 
	 * of prices for all stocks.
	 * 
	 * Assumes there have been at least one trade for each stock as the price used
	 * is the that of the last trade.
	 * 
	 * @return the calculated All Share Index
	 */
	public double getAllShareIndex() {
		
		//Get the latest trades for each stocks pricing information
		double totalPrice = 1.0;
		for(Trade t:priceStore.values()) {
			totalPrice = totalPrice * t.getPrice();
		}
		
		// now if we have some trades in the interval calc the geometric mean.
		if(!priceStore.values().isEmpty()) {
			return Math.pow(totalPrice, (1.0 / priceStore.values().size()));	
		}
		return 0;
	}
	

	/**
	 * Record a buy trade, with "now" timestamp, quantity of shares, and trade price
	 * 
	 * @param symName The stock symbol to trade
	 * @param price The trade price
	 * @param quantity The quantity of shares to trade.
	 * 
	 * @return A Trade object representing this trade.
	 */
	public Trade buyStock(String symName, double price, long quantity) {
		return addTradeToStore(symName, price, quantity, TradeType.BUY);
	}
	
	/**
	 * Record a buy trade, with a timestamp, quantity of shares, and trade price
	 * 
	 * @param symName The stock symbol to trade.
	 * @param price The trade price.
	 * @param quantity The quantity of shares to trade.
	 * @param timestamp The time of trade.
	 * 
	 * @return A Trade object representing this trade.
	 */
	public Trade buyStock(String symName, double price, long quantity, Date timestamp) {
		return addTradeToStore(symName, price, quantity, TradeType.BUY, timestamp);
	}
	
	
	private Trade addTradeToStore(String symName, double price, 
			long quantity, TradeType type) {
		return addTradeToStore(symName, price,quantity,type, new Date());
	}
	
	private Trade addTradeToStore(String symName, double price, 
			long quantity, TradeType type, Date timestamp) {
		
		Stock toTrade = stockStore.get(symName);
		Trade toReturn = toTrade.trade(price, quantity, timestamp, type);
		List<Trade> stockTrades = tradeStore.get(symName);
		if(stockTrades==null) {
			stockTrades = new ArrayList<Trade>();
			tradeStore.put(symName, stockTrades);
		}
		stockTrades.add(toReturn);
		
		//Now update the price information
		Trade curPrice = priceStore.get(symName);
		if(curPrice==null) {
			priceStore.put(symName, toReturn);
		} else {
			//check this trade is newer than the current price
			if(curPrice.getTimestamp().getTime()<curPrice.getTimestamp().getTime()) {
				priceStore.put(symName, toReturn);
			}
		}
		
		return toReturn;
	}
	
	/**
	 * Record a sell trade, with a "now" timestamp, quantity of shares, and trade price
	 * 
	 * @param symName The stock symbol to trade.
	 * @param price The trade price.
	 * @param quantity The quantity of shares to trade.
	 * 
	 * @return A Trade object representing this trade.
	 */
	public Trade sellStock(String symName, double price, long quantity){
		return addTradeToStore(symName, price, quantity, TradeType.SELL);
	}
	
	/**
	 * Record a sell trade, with a timestamp, quantity of shares, and trade price
	 * 
	 * @param symName The stock symbol to trade.
	 * @param price The trade price.
	 * @param quantity The quantity of shares to trade.
	 * @param timestamp The time of trade.
	 * 
	 * @return A Trade object representing this trade.
	 */
	public Trade sellStock(String symName, double price, long quantity, Date timestamp){
		return addTradeToStore(symName, price, quantity, TradeType.SELL, timestamp);
	}
	
	/**
	 * Given a market price as input, calculates the dividend yield for a stock
	 * 
	 * @param symName The stock symbol
	 * @param price The price to calculate yield. 
	 * 
	 * @return The calculated yield.
	 */
	public double getStockDividendYield(String symName, double price){
		Stock calcFor = stockStore.get(symName);
		return calcFor.getDividendYield(price);
	}
	
	/**
	 * Given a market price as input, calculates the P/E Ratio for a stock
	 * 
	 * @param symName The stock symbol
	 * @param price The price to calculate P/E Ratio. 
	 * 
	 * @return The calculated P/E Ratio.
	 */
	public double getStockPERatio(String symName, double price){
		Stock calcFor = stockStore.get(symName);
		return calcFor.getPERatio(price);
	}
	
	/**
	 * Get the Stocks listed on this exchange
	 * 
	 * @return An unmodifiable Collection containing all listed Stocks
	 */
	public Collection<Stock> getExchangeListedStocks() {
		return Collections.unmodifiableCollection(stockStore.values());
	}
	
	/**
	 * Get the Stock Exchange name.
	 * 
	 * @return The exchange name.
	 */
	public String getExchangeName() {
		return exchangeName;
	}
	
	public void loadStocks() {
		
		stockStore.put("TEA", new Stock("TEA", StockType.COMMON, 100, 0));
		stockStore.put("POP", new Stock("POP", StockType.COMMON, 100, 8));
		stockStore.put("ALE", new Stock("ALE", StockType.COMMON, 60, 23));
		stockStore.put("GIN", new Stock("GIN", StockType.PREFERRED, 100, 8, 2));
		stockStore.put("JOE", new Stock("JOE", StockType.COMMON, 250, 13));
	}
	
}
