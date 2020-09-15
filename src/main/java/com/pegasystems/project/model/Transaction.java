package com.pegasystems.project.model;


import java.io.Serializable;
import java.security.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Transaction")
public class Transaction implements Serializable {
	
	private static AtomicInteger ID_GENERATOR = new AtomicInteger();
	@Id
	//@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
	
	private int buy_order_id;
	private int sell_order_id;
	private String stock_symbol;
	private String order_type;
	private String units;
	private String price;
	private Date transactionTime;
	
	
	
	/**
	 * 
	 */
	public Transaction() {
		
	}



	/**
	 * @param id
	 * @param buy_order_id
	 * @param sell_order_id
	 * @param stock_symbol
	 * @param order_type
	 * @param units
	 * @param price
	 */
	public Transaction(int buy_order_id, int sell_order_id, String stock_symbol, String order_type,
			String units, String price) {
		super();
		this.id = ID_GENERATOR.getAndIncrement();
		this.buy_order_id = buy_order_id;
		this.sell_order_id = sell_order_id;
		this.stock_symbol = stock_symbol;
		this.order_type = order_type;
		this.units = units;
		this.price = price;
		this.transactionTime = new Date(System.currentTimeMillis());
	}



	@Override
	public String toString() {
		return "Transaction [id=" + id + ", buy_order_id=" + buy_order_id + ", sell_order_id=" + sell_order_id
				+ ", stock_symbol=" + stock_symbol + ", order_type=" + order_type + ", units=" + units + ", price="
				+ price + ", transactionTime=" + transactionTime + "]";
	}
    
	
}
