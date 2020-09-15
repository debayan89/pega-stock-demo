package com.pegasystems.project.model;


import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static AtomicInteger ID_GENERATOR = new AtomicInteger();
	//@Id
	//@GeneratedValue(strategy = GenerationType.AUTO)	
	
	@ApiModelProperty( hidden=true)
    private int orderId;
	@ApiModelProperty(required = true)
	private String user_id;
	@ApiModelProperty(required = true)
	private String stock_symbol;
	@ApiModelProperty(required = true, allowableValues = "buy,sell")
	private String order_type;
	@ApiModelProperty(required = true)
	private String units;
	@ApiModelProperty(required = true)
	private String price;
	@ApiModelProperty(hidden=true)
	private Date order_time;
	@ApiModelProperty(hidden=true)
	private String updateEligible;

	/**
	 * 
	 */
	public Order() {
		
	}
	
	/**
	 * @param user_id
	 * @param stock_symbol
	 * @param order_type
	 * @param units
	 * @param price
	 */
	public Order(String user_id, String stock_symbol, String order_type, String units, String price) {
		super();
		this.user_id = user_id;
		this.stock_symbol = stock_symbol;
		this.order_type = order_type;
		this.units = units;
		this.price = price;
	}
	
	/**
	 * @return the orderId
	 */
	
	public int getOrderId() {
		return orderId;
	}
	/**
	 * @param orderId the orderId to set
	 */
	
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	/**
	 * @return the user_id
	 */
	public String getUser_id() {
		return user_id;
	}
	/**
	 * @param user_id the user_id to set
	 */
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	/**
	 * @return the stock_symbol
	 */
	public String getStock_symbol() {
		return stock_symbol;
	}
	/**
	 * @param stock_symbol the stock_symbol to set
	 */
	public void setStock_symbol(String stock_symbol) {
		this.stock_symbol = stock_symbol;
	}
	/**
	 * @return the order_type
	 */
	public String getOrder_type() {
		return order_type;
	}
	/**
	 * @param order_type the order_type to set
	 */
	public void setOrder_type(String order_type) {
		this.order_type = order_type;
	}
	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}
	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	/**
	 * @return the price
	 */
	public String getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public void setPrice(String price) {
		this.price = price;
	}
	/**
	 * @return the order_time
	 */
	
	public Date getOrder_time() {
		return order_time;
	}
	/**
	 * @param order_time the order_time to set
	 */
	
	public void setOrder_time(Date order_time) {
		this.order_time = order_time;
	}
	
	
	/**
	 * @return the updateEligible
	 */
	public String getUpdateEligible() {
		return updateEligible;
	}
	/**
	 * @param updateEligible the updateEligible to set
	 */
	public void setUpdateEligible(String updateEligible) {
		this.updateEligible = updateEligible;
	}
	
	
	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", user_id=" + user_id + ", stock_symbol=" + stock_symbol + ", order_type="
				+ order_type + ", units=" + units + ", price=" + price + ", order_time=" + order_time + "]";
	}
	
    
	
}
