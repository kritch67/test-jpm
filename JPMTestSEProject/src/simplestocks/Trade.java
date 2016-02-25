package simplestocks;

import java.util.Date;

/**
 * Simple class to model an instrument trade. Since trades are logically immutable, 
 * properties can not be set post-construction. 
 * 
 * 
 */
public class Trade {

	public enum TradeType {BUY,SELL};
	
	private long quantity;
	private Date timestamp;
	private TradeType type;
	private double price;
	private Stock stock;
	
	public Trade(long quantity, TradeType type, double price, 
			Stock stock, Date timestamp) {
		this.quantity = quantity;
		this.timestamp = timestamp;
		this.type = type;
		this.price = price;
		this.stock = stock;
	}
	
	public Trade(long quantity, TradeType type, double price, Stock stock) {
		this(quantity, type, price, stock, new Date());
	}
	
	
	/**
	 * @return the quantity
	 */
	public long getQuantity() {
		return quantity;
	}

	/**
	 * @return the timestamp of this trade.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the type of this trade.
	 */
	public TradeType getType() {
		return type;
	}

	/**
	 * @return the price for this trade.
	 */
	public double getPrice() {
		return price;
	}
	
	/**
	 * @return the stock for this trade.
	 */
	public Stock getStock() {
		return stock;
	}
	
}
