package com.pegasystems.project.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.pegasystems.project.model.Order;
import com.pegasystems.project.model.Transaction;
import com.pegasystems.project.repository.TransactionRepository;

/**
 * Enter description of class here.
 * 
 * @author PAULD26
 *
 */
@Service
@Qualifier("StockInterfaceImpl")
public class StockInterfaceImpl {

	// All Sell Orders reside here
	HashMap<String, ArrayList<Order>> sellMap = new HashMap<String, ArrayList<Order>>(); // Key is Stock Ticker

	// All Buy Orders Reside here
	HashMap<String, ArrayList<Order>> buyMap = new HashMap<String, ArrayList<Order>>();// Key is Stock Ticker

	private ArrayList<Order> buyOrderList;

	private ArrayList<Order> sellOrderList;

	@Autowired
	private TransactionRepository transactionRepository;
	private static AtomicInteger ID_GENERATOR = new AtomicInteger();

	public void saveProduct(HttpSession session, Order incomingOrder) {

		incomingOrder.setOrder_time(new Date(System.currentTimeMillis()));
		incomingOrder.setOrderId(ID_GENERATOR.getAndIncrement());
		incomingOrder.setUpdateEligible("true");

		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<Order>> buyMapSession = (HashMap<String, ArrayList<Order>>) session
				.getAttribute("Buy_Map");
		if (buyMapSession != null) {
			buyMap = buyMapSession;
		}
		/*
		 * ArrayList<Order> buyOrderListSession = (ArrayList<Order>)
		 * session.getAttribute("Buy_Order_List"); if(buyOrderListSession!=null) {
		 * buyOrderList = buyOrderListSession; }
		 */

		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<Order>> sellMapSession = (HashMap<String, ArrayList<Order>>) session
				.getAttribute("Sell_Map");
		if (sellMapSession != null) {
			sellMap = sellMapSession;
		}

		try {

			// ----------------------------------------------------------When Order Type is BUY-----------------------------------------------------
			if (incomingOrder.getOrder_type().equals("buy")) {

				// If incoming order is of type buy we will check the price in sellMap and then
				// put partial order in buyMap
				boolean addToBuyOrderList = true;

				if (!sellMap.isEmpty()) {

					if (sellMap.containsKey(incomingOrder.getStock_symbol())) {

						if (sellMap.get(incomingOrder.getStock_symbol()) != null
								&& !sellMap.get(incomingOrder.getStock_symbol()).isEmpty()) {
							// for(Order order:sellMap.get(incomingOrder.getStock_symbol())) {
							for (Iterator<Order> itr = sellMap.get(incomingOrder.getStock_symbol()).iterator(); itr
									.hasNext();) {
								Order order = itr.next();

								if (Double.parseDouble(order.getPrice()) <= Double
										.parseDouble(incomingOrder.getPrice())) {

									// Fully Filled Buy Order
									if (Integer.parseInt(order.getUnits()) == Integer
											.parseInt(incomingOrder.getUnits())) {
										addToBuyOrderList = false;

										// Create a transaction Object with status Fully filled

										Transaction transaction = new Transaction(incomingOrder.getOrderId(),
												order.getOrderId(), incomingOrder.getStock_symbol(),
												incomingOrder.getOrder_type(), incomingOrder.getUnits(),
												order.getPrice());
										///////////////////////////////// Save Transaction in
										///////////////////////////////// DB////////////////////////////
										transactionRepository.save(transaction);

										// sellMap.get(incomingOrder.getStock_symbol()).remove(order);
										itr.remove();
										break;
										// To-Do

										// Partial Fill Buy Order
									} else if (Integer.parseInt(order.getUnits()) < Integer
											.parseInt(incomingOrder.getUnits())) {

										Transaction transaction = new Transaction(incomingOrder.getOrderId(),
												order.getOrderId(), incomingOrder.getStock_symbol(),
												incomingOrder.getOrder_type(), incomingOrder.getUnits(),
												order.getPrice());

										transactionRepository.save(transaction);

										Integer remaining = null;
										remaining = Integer.parseInt(incomingOrder.getUnits())
												- Integer.parseInt(order.getUnits());

										incomingOrder.setUnits(remaining.toString());
										incomingOrder.setUpdateEligible("false");
										// sellMap.get(incomingOrder.getStock_symbol()).remove(order);
										itr.remove();

									} // Fully Filled Buy Order
									else if (Integer.parseInt(order.getUnits()) > Integer
											.parseInt(incomingOrder.getUnits())) {

										// Create Transaction Object (fully Filled Buy Order)
										addToBuyOrderList = false;
										Integer remaining = null;
										remaining = Integer.parseInt(order.getUnits())
												- Integer.parseInt(incomingOrder.getUnits());
										order.setUnits(remaining.toString());

										Transaction transaction = new Transaction(incomingOrder.getOrderId(),
												order.getOrderId(), incomingOrder.getStock_symbol(),
												incomingOrder.getOrder_type(), incomingOrder.getUnits(),
												order.getPrice());

										// Do a DB Transaction
										transactionRepository.save(transaction);
										break;
									}

								}
								if (sellMap.get(incomingOrder.getStock_symbol()).isEmpty()) {
									break;
								}
							}
						}
					}

				}

				if (addToBuyOrderList) {
					buyOrderList = new ArrayList<Order>();

					if (!buyMap.isEmpty() && buyMap.containsKey(incomingOrder.getStock_symbol())) {
						buyOrderList = buyMap.get(incomingOrder.getStock_symbol());
					}

					buyOrderList.add(incomingOrder);

					// Sorting by Price First then by Time thus implementing Price-Time Priority
					// (FIFO) matching algorithm
					Collections.sort(buyOrderList,
							Comparator.comparing(Order::getPrice).reversed().thenComparing(Order::getOrder_time));

					if (buyMap.containsKey(incomingOrder.getStock_symbol())) {
						buyMap.replace(incomingOrder.getStock_symbol(), buyOrderList);
					} else {
						buyMap.put(incomingOrder.getStock_symbol(), buyOrderList);
					}
					session.setAttribute("Buy_Map", buyMap);

				}

				session.setAttribute("Sell_Map", sellMap);
				// ----------------------------------------------------------When Order Type is SELL-----------------------------------------------------

			} else if (incomingOrder.getOrder_type().equals("sell")) {

				// If incoming order is of type buy we will check the price in sellMap and then
				// put partial order in buyMap
				boolean addToSellOrderList = true;

				if (!buyMap.isEmpty()) {

					if (buyMap.containsKey(incomingOrder.getStock_symbol())) {

						if (buyMap.get(incomingOrder.getStock_symbol()) != null
								&& !buyMap.get(incomingOrder.getStock_symbol()).isEmpty()) {

							for (Iterator<Order> itr = buyMap.get(incomingOrder.getStock_symbol()).iterator(); itr
									.hasNext();) {
								Order order = itr.next();
								// for(Order order:buyMap.get(incomingOrder.getStock_symbol())) {

								if (Double.parseDouble(order.getPrice()) >= Double
										.parseDouble(incomingOrder.getPrice())) {

									// Fully Filled Sell order
									if (Integer.parseInt(order.getUnits()) == Integer
											.parseInt(incomingOrder.getUnits())) {
										addToSellOrderList = false;
										itr.remove();
										// buyMap.get(incomingOrder.getStock_symbol()).remove(order);

										// Create a transaction Object with status Fully filled
										Transaction transaction = new Transaction(incomingOrder.getOrderId(),
												order.getOrderId(), incomingOrder.getStock_symbol(),
												incomingOrder.getOrder_type(), incomingOrder.getUnits(),
												order.getPrice());
										transactionRepository.save(transaction);
										break;

										// Partial Fill Sell Order
									} else if (Integer.parseInt(order.getUnits()) < Integer
											.parseInt(incomingOrder.getUnits())) {

										Transaction transaction = new Transaction(incomingOrder.getOrderId(),
												order.getOrderId(), incomingOrder.getStock_symbol(),
												incomingOrder.getOrder_type(), order.getUnits(),
												incomingOrder.getPrice());
										transactionRepository.save(transaction);
										Integer remaining = null;
										remaining = Integer.parseInt(incomingOrder.getUnits())
												- Integer.parseInt(order.getUnits());

										incomingOrder.setUnits(remaining.toString());
										incomingOrder.setUpdateEligible("false");
										itr.remove();
										// buyMap.get(incomingOrder.getStock_symbol()).remove(order);

									} // Fully Filled Sell order
									else if (Integer.parseInt(order.getUnits()) > Integer
											.parseInt(incomingOrder.getUnits())) {

										// Create Transaction Object (fully Filled Buy Order)
										addToSellOrderList = false;
										Integer remaining = null;
										remaining = Integer.parseInt(order.getUnits())
												- Integer.parseInt(incomingOrder.getUnits());
										order.setUnits(remaining.toString());

										Transaction transaction = new Transaction(incomingOrder.getOrderId(),
												order.getOrderId(), incomingOrder.getStock_symbol(),
												incomingOrder.getOrder_type(), incomingOrder.getUnits(),
												incomingOrder.getPrice());
										transactionRepository.save(transaction);
										break;

									}

								}

								if (buyMap.get(incomingOrder.getStock_symbol()).isEmpty()) {
									break;
								}
							}
						}
					}

				}

				if (addToSellOrderList) {
					sellOrderList = new ArrayList<Order>();

					if (!sellMap.isEmpty() && sellMap.containsKey(incomingOrder.getStock_symbol())) {
						sellOrderList = sellMap.get(incomingOrder.getStock_symbol());
					}
					sellOrderList.add(incomingOrder);

					// Sorting by Price First then by Time thus implementing Price-Time Priority
					// (FIFO) matching algorithm
					Collections.sort(sellOrderList,
							Comparator.comparing(Order::getPrice).thenComparing(Order::getOrder_time));

					if (sellMap.containsKey(incomingOrder.getStock_symbol())) {
						sellMap.replace(incomingOrder.getStock_symbol(), sellOrderList);
					} else {
						sellMap.put(incomingOrder.getStock_symbol(), sellOrderList);
					}
					session.setAttribute("Sell_Map", sellMap);
				}

				session.setAttribute("Buy_Map", buyMap);

			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * Enter method description here.
	 * 
	 * @param session
	 * @return
	 */
	public HashMap<String, ArrayList<Order>> getBuyOrders(HttpSession session) {
		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<Order>> buyMapSession = (HashMap<String, ArrayList<Order>>) session
				.getAttribute("Buy_Map");
		if (buyMapSession != null) {
			return buyMapSession;
		}

		return null;
	}

	/**
	 * Enter method description here.
	 * 
	 * @param session
	 * @return
	 */
	public HashMap<String, ArrayList<Order>> getSellOrders(HttpSession session) {
		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<Order>> sellMapSession = (HashMap<String, ArrayList<Order>>) session
				.getAttribute("Sell_Map");
		if (sellMapSession != null) {
			return sellMapSession;
		}

		return null;
	}

	/**
	 * Enter method description here.
	 * 
	 * @param session
	 * @param OrderId
	 * @param OrderType
	 * @return
	 */
	public String deleteOrder(HttpSession session, int orderId, String orderType, String stockSymbol) {

		if (orderType.equals("buy")) {
			@SuppressWarnings("unchecked")
			HashMap<String, ArrayList<Order>> buyMapSession = (HashMap<String, ArrayList<Order>>) session
					.getAttribute("Buy_Map");
			if (buyMapSession != null) {

				if (buyMapSession.containsKey(stockSymbol)) {

					for (Iterator<Order> itr = buyMap.get(stockSymbol).iterator(); itr.hasNext();) {
						Order order = itr.next();
						if (order.getOrderId() == orderId && order.getUpdateEligible().equals("true")) {
							itr.remove();
						}

					}

					session.setAttribute("Buy_Map", buyMap);

					return "success";
				}
			}

		}

		if (orderType.equals("sell")) {
			@SuppressWarnings("unchecked")
			HashMap<String, ArrayList<Order>> sellMapSession = (HashMap<String, ArrayList<Order>>) session
					.getAttribute("Sell_Map");
			if (sellMapSession != null) {

				if (sellMapSession.containsKey(stockSymbol)) {

					for (Iterator<Order> itr = sellMap.get(stockSymbol).iterator(); itr.hasNext();) {
						Order order = itr.next();
						if (order.getOrderId() == orderId && order.getUpdateEligible().equals("true")) {
							itr.remove();
						}

					}

					session.setAttribute("Sell_Map", sellMap);

					return "success";
				}
			}

		}
		return "unsuccessful";
	}

	/**
	 * Enter method description here.
	 * 
	 * @param session
	 * @param incomingOrder
	 * @param orderId
	 */
	public void updateProduct(HttpSession session, Order incomingOrder, int orderId) {

		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<Order>> buyMapSession = (HashMap<String, ArrayList<Order>>) session
				.getAttribute("Buy_Map");
		if (buyMapSession != null) {
			buyMap = buyMapSession;
		}
		/*
		 * ArrayList<Order> buyOrderListSession = (ArrayList<Order>)
		 * session.getAttribute("Buy_Order_List"); if(buyOrderListSession!=null) {
		 * buyOrderList = buyOrderListSession; }
		 */

		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<Order>> sellMapSession = (HashMap<String, ArrayList<Order>>) session
				.getAttribute("Sell_Map");
		if (sellMapSession != null) {
			sellMap = sellMapSession;
		}

		try {

 // ----------------------------------------------------------When Order Type is BUY-----------------------------------------------------
			if (incomingOrder.getOrder_type().equals("buy")) {
				
				boolean addToBuyOrderList = true;

				//First update the BUY_MAP then run the matching engine again
				
				if (!buyMap.isEmpty()) {

					if (buyMap.containsKey(incomingOrder.getStock_symbol())) {

						if (buyMap.get(incomingOrder.getStock_symbol()) != null
								&& !buyMap.get(incomingOrder.getStock_symbol()).isEmpty()) {

							for (Iterator<Order> itr = buyMap.get(incomingOrder.getStock_symbol()).iterator(); itr
									.hasNext();) {
								Order order = itr.next();

								if (order.getOrderId() == orderId && order.getUpdateEligible().equals("true")) {
									addToBuyOrderList = false;

									order.setOrder_time(new Date(System.currentTimeMillis()));
									
									order.setPrice(incomingOrder.getPrice());
									
									order.setUnits(incomingOrder.getUnits());
									session.setAttribute("Buy_Map", buyMap);
									break;
								}
							}
						}
					}
				}

				
				//Matching Engine
				

				if (!sellMap.isEmpty()) {

					if (sellMap.containsKey(incomingOrder.getStock_symbol())) {

						if (sellMap.get(incomingOrder.getStock_symbol()) != null
								&& !sellMap.get(incomingOrder.getStock_symbol()).isEmpty()) {
							// for(Order order:sellMap.get(incomingOrder.getStock_symbol())) {
							for (Iterator<Order> itr = sellMap.get(incomingOrder.getStock_symbol()).iterator(); itr
									.hasNext();) {
								Order order = itr.next();

								if (order.getOrderId() == orderId && order.getUpdateEligible().equals("true")) {
									
									/*
									 * order.setOrder_time(new Date(System.currentTimeMillis()));
									 * 
									 * order.setPrice(incomingOrder.getPrice());
									 * 
									 * order.setUnits(incomingOrder.getUnits());
									 */
									
									
									if (Double.parseDouble(order.getPrice()) <= Double
											.parseDouble(incomingOrder.getPrice())) {

										// Fully Filled Buy Order
										if (Integer.parseInt(order.getUnits()) == Integer
												.parseInt(incomingOrder.getUnits())) {
											addToBuyOrderList = false;

											// Create a transaction Object with status Fully filled

											Transaction transaction = new Transaction(incomingOrder.getOrderId(),
													order.getOrderId(), incomingOrder.getStock_symbol(),
													incomingOrder.getOrder_type(), incomingOrder.getUnits(),
													order.getPrice());
											///////////////////////////////// Save Transaction in
											///////////////////////////////// DB////////////////////////////
											transactionRepository.save(transaction);

											// sellMap.get(incomingOrder.getStock_symbol()).remove(order);
											itr.remove();
											break;
											// To-Do

											// Partial Fill Buy Order
										} else if (Integer.parseInt(order.getUnits()) < Integer
												.parseInt(incomingOrder.getUnits())) {

											Transaction transaction = new Transaction(incomingOrder.getOrderId(),
													order.getOrderId(), incomingOrder.getStock_symbol(),
													incomingOrder.getOrder_type(), incomingOrder.getUnits(),
													order.getPrice());

											transactionRepository.save(transaction);

											Integer remaining = null;
											remaining = Integer.parseInt(incomingOrder.getUnits())
													- Integer.parseInt(order.getUnits());

											incomingOrder.setUnits(remaining.toString());
											incomingOrder.setUpdateEligible("false");
											// sellMap.get(incomingOrder.getStock_symbol()).remove(order);
											itr.remove();

										} // Fully Filled Buy Order
										else if (Integer.parseInt(order.getUnits()) > Integer
												.parseInt(incomingOrder.getUnits())) {

											// Create Transaction Object (fully Filled Buy Order)
											addToBuyOrderList = false;
											Integer remaining = null;
											remaining = Integer.parseInt(order.getUnits())
													- Integer.parseInt(incomingOrder.getUnits());
											order.setUnits(remaining.toString());

											Transaction transaction = new Transaction(incomingOrder.getOrderId(),
													order.getOrderId(), incomingOrder.getStock_symbol(),
													incomingOrder.getOrder_type(), incomingOrder.getUnits(),
													order.getPrice());

											// Do a DB Transaction
											transactionRepository.save(transaction);
											break;
										}

									}
									if (sellMap.get(incomingOrder.getStock_symbol()).isEmpty()) {
										break;
									}

								}

							}
						}
					}

				}

				if (addToBuyOrderList) {
					buyOrderList = new ArrayList<Order>();

					if (!buyMap.isEmpty() && buyMap.containsKey(incomingOrder.getStock_symbol())) {
						buyOrderList = buyMap.get(incomingOrder.getStock_symbol());
					}

					buyOrderList.add(incomingOrder);

					// Sorting by Price First then by Time thus implementing Price-Time Priority
					// (FIFO) matching algorithm
					Collections.sort(buyOrderList,
							Comparator.comparing(Order::getPrice).reversed().thenComparing(Order::getOrder_time));

					if (buyMap.containsKey(incomingOrder.getStock_symbol())) {
						buyMap.replace(incomingOrder.getStock_symbol(), buyOrderList);
					} else {
						buyMap.put(incomingOrder.getStock_symbol(), buyOrderList);
					}
					session.setAttribute("Buy_Map", buyMap);

				}

				session.setAttribute("Sell_Map", sellMap);
				
				
	// ----------------------------------------------------------When Order Type is SELL-----------------------------------------------------

			} else if (incomingOrder.getOrder_type().equals("sell")) {
				
				boolean addToSellOrderList = true;

				//First update the BUY_MAP then run the matching engine again
				
				if (!sellMap.isEmpty()) {

					if (sellMap.containsKey(incomingOrder.getStock_symbol())) {

						if (sellMap.get(incomingOrder.getStock_symbol()) != null
								&& !sellMap.get(incomingOrder.getStock_symbol()).isEmpty()) {

							for (Iterator<Order> itr = sellMap.get(incomingOrder.getStock_symbol()).iterator(); itr
									.hasNext();) {
								Order order = itr.next();

								if (order.getOrderId() == orderId && order.getUpdateEligible().equals("true")) {
									
									addToSellOrderList = false;

									order.setOrder_time(new Date(System.currentTimeMillis()));
									
									order.setPrice(incomingOrder.getPrice());
									
									order.setUnits(incomingOrder.getUnits());
									session.setAttribute("Sell_Map", sellMap);
									break;
								}
							}
						}
					}
				}
				
				//Matching Engine
				

				if (!buyMap.isEmpty()) {

					if (buyMap.containsKey(incomingOrder.getStock_symbol())) {

						if (buyMap.get(incomingOrder.getStock_symbol()) != null
								&& !buyMap.get(incomingOrder.getStock_symbol()).isEmpty()) {

							for (Iterator<Order> itr = buyMap.get(incomingOrder.getStock_symbol()).iterator(); itr
									.hasNext();) {
								Order order = itr.next();

								if (order.getOrderId() == orderId && order.getUpdateEligible().equals("true")) {

									order.setOrder_time(new Date(System.currentTimeMillis()));
									
									order.setPrice(incomingOrder.getPrice());
									
									order.setUnits(incomingOrder.getUnits());

									if (Double.parseDouble(order.getPrice()) >= Double
											.parseDouble(incomingOrder.getPrice())) {

										// Fully Filled Sell order
										if (Integer.parseInt(order.getUnits()) == Integer
												.parseInt(incomingOrder.getUnits())) {
											addToSellOrderList = false;
											itr.remove();
											// buyMap.get(incomingOrder.getStock_symbol()).remove(order);

											// Create a transaction Object with status Fully filled
											Transaction transaction = new Transaction(incomingOrder.getOrderId(),
													order.getOrderId(), incomingOrder.getStock_symbol(),
													incomingOrder.getOrder_type(), incomingOrder.getUnits(),
													order.getPrice());
											transactionRepository.save(transaction);
											break;

											// Partial Fill Sell Order
										} else if (Integer.parseInt(order.getUnits()) < Integer
												.parseInt(incomingOrder.getUnits())) {

											Transaction transaction = new Transaction(incomingOrder.getOrderId(),
													order.getOrderId(), incomingOrder.getStock_symbol(),
													incomingOrder.getOrder_type(), order.getUnits(),
													incomingOrder.getPrice());
											transactionRepository.save(transaction);
											Integer remaining = null;
											remaining = Integer.parseInt(incomingOrder.getUnits())
													- Integer.parseInt(order.getUnits());

											incomingOrder.setUnits(remaining.toString());
											incomingOrder.setUpdateEligible("false");
											itr.remove();
											// buyMap.get(incomingOrder.getStock_symbol()).remove(order);

										} // Fully Filled Sell order
										else if (Integer.parseInt(order.getUnits()) > Integer
												.parseInt(incomingOrder.getUnits())) {

											// Create Transaction Object (fully Filled Buy Order)
											addToSellOrderList = false;
											Integer remaining = null;
											remaining = Integer.parseInt(order.getUnits())
													- Integer.parseInt(incomingOrder.getUnits());
											order.setUnits(remaining.toString());

											Transaction transaction = new Transaction(incomingOrder.getOrderId(),
													order.getOrderId(), incomingOrder.getStock_symbol(),
													incomingOrder.getOrder_type(), incomingOrder.getUnits(),
													incomingOrder.getPrice());
											transactionRepository.save(transaction);
											break;

										}

									}

									if (buyMap.get(incomingOrder.getStock_symbol()).isEmpty()) {
										break;
									}

								}
							}
						}
					}

				}

				if (addToSellOrderList) {
					sellOrderList = new ArrayList<Order>();

					if (!sellMap.isEmpty() && sellMap.containsKey(incomingOrder.getStock_symbol())) {
						sellOrderList = sellMap.get(incomingOrder.getStock_symbol());
					}
					sellOrderList.add(incomingOrder);

					// Sorting by Price First then by Time thus implementing Price-Time Priority
					// (FIFO) matching algorithm
					Collections.sort(sellOrderList,
							Comparator.comparing(Order::getPrice).thenComparing(Order::getOrder_time));

					if (sellMap.containsKey(incomingOrder.getStock_symbol())) {
						sellMap.replace(incomingOrder.getStock_symbol(), sellOrderList);
					} else {
						sellMap.put(incomingOrder.getStock_symbol(), sellOrderList);
					}
					session.setAttribute("Sell_Map", sellMap);
				}

				session.setAttribute("Buy_Map", buyMap);

			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}
}
