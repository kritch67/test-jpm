package simplestocks;

import java.util.Date;

import simplestocks.Trade.TradeType;

/**
 * Simple class to model a stock. Pricing information is not modeled here.  
 * 
 */
public class Stock {

	
	/**
	 * Provides stock type identification and type specific methods 
	 * to calculate dividend yield and P/E ratio. 
	 *
	 */
	public enum StockType {
		COMMON {
			@Override
			public double calcDivYield(double price, Stock calcForStock) {
				return calcForStock.lastDividend / price;
			}
			
			@Override
			public double calcPERatio(double price, Stock calcForStock) {
				return calcForStock.lastDividend != 0 ? price / calcForStock.lastDividend : 0;
			}
		},
		PREFERRED {
			@Override
			public double calcDivYield(double price, Stock calcForStock) {
				return ((calcForStock.dividendRate/100) *  calcForStock.parValue) /price;
			}
			
			@Override
			public double calcPERatio(double price, Stock calcForStock) {
				//calc dividend first then ratio.
				double curDividend = ((calcForStock.dividendRate/100) *  calcForStock.parValue);
				return curDividend !=0 ?  price / curDividend : 0;
			}
			
		};
		
		
		/**
		 * Calculates the dividend yield according to stock type. 
		 * 
		 * Preferred Stock yield: (fixed rate X par value)/price
		 * 
		 * Common Stock yield: last rate/price
		 * 
		 * NOTE: Assumes we don't care too much about floating point precision. i.e. 
		 * use primitive fp maths and accept some errors in precision. 
		 * 
		 * @param price The current stock price
		 * @param calcForStock The stock to calculate the yield for
		 * @return The calculated yield.
		 */
		public abstract double calcDivYield(double price, Stock calcForStock);
		
		/**
		 * Calculates the P/E ratio according to stock type. 
		 * 
		 * Calculation used for Common Stock: price / last dividend.
		 * Calculation used for Preferred Stock: price / calculated dividend.
		 * 
		 * NOTE: Assumes we don't care too much about floating point precision. i.e. 
		 * use primitive fp maths and accept some errors in precision. 
		 * 
		 * NOTE: Assumes we want the current dividend (i.e. based on current price) rather 
		 * than the last dividend for preferred stock. 
		 * 
		 * @param price The current stock price
		 * @param calcForStock The stock to calculate the yield for
		 * @return The calculated ratio.
		 */
		public abstract double calcPERatio(double price, Stock calcForStock);
	
	};
	
	private String symbol;
	private StockType type;
	private int parValue;
	
	//Dividend penny value 
	private long lastDividend; 
	
	//Dividend rate if fixed return.
	private double dividendRate; 

	public Stock (String symbol, StockType type, int parValue, 
			long lastDividend, double dividendRate) {
		this.symbol = symbol;
		this.type = type;
		this.parValue = parValue;
		this.lastDividend = lastDividend;
		this.dividendRate = dividendRate;
	}
	
	public Stock (String symbol, StockType type, int parValue, long lastDividend) {
		this(symbol, type, parValue, lastDividend,-1);
	}
	
	/**
	 * Create a trade for this stock.
	 * 
	 * @param price The price per unit 
	 * @param quantity The number of units traded
	 * @param timestamp The time of trade
	 * @param type The type of trade.
	 * @return A trade object representing the trade details.
	 */
	public Trade trade(double price, long quantity, Date timestamp, TradeType type) {
		return new Trade(quantity, type, price, this, timestamp);
	}
	
	/**
	 * Create a trade for this stock with timestamp "now".
	 * 
	 * @param price The price per unit 
	 * @param quantity The number of units traded
	 * @param timestamp The time of trade
	 * @param type The type of trade.
	 * @return A trade object representing the trade details.
	 */
	public Trade trade(double price, long quantity, TradeType type) {
		return new Trade(quantity, type, price, this);
	}
	
	/**
	 * Calculates and returns the stock PE Ratio. 
	 * 
	 * P/E ratio calculation is based on stock type. 
	 * 
	 * @param price the current market price
	 * @return the calculated pe ratio
	 */
	public double getPERatio(double price) {
		return type.calcPERatio(price, this);
	}
	
	/**
	 * Calculates and returns the dividend yield. 
	 * 
	 * Yield calculation is based on stock type. 
	 * 
	 * @param price the current market price
	 * @return the calculated yield
	 */
	public double getDividendYield(double price) {
		return type.calcDivYield(price, this);
	}
	
	/**
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * @return the type
	 */
	public StockType getType() {
		return type;
	}

	/**
	 * @return the parValue
	 */
	public int getParValue() {
		return parValue;
	}

	/**
	 * @return the latest dividend
	 */
	public long getLastDividend() {
		return lastDividend;
	}
	
	/**
	 * @return the current fixed dividend rate or -1 if N/A
	 */
	public double getDividendRate() {
		return dividendRate;
	}
	
}
