package com.pegasystems.project.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pegasystems.project.model.Order;
import com.pegasystems.project.service.StockInterfaceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Enter description of class here.
 * 
 * @author PAULD26
 *
 */

@RestController
@RequestMapping("/stock")
@Api(value="StockTrading", description="Operations pertaining to limit order stock trading")
public class StockController {
	
	@Autowired
	public StockInterfaceImpl stockInterfaceImpl;

    @ApiOperation(value = "Add an order Buy or Sell")
    @ApiResponses
    (value={@ApiResponse( code = 200, message = "- 0: Success", response = Order.class)})
	@RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
    public Order createTransaction(@ApiIgnore HttpSession session, @RequestBody Order incomingOrder ) {
    	stockInterfaceImpl.saveProduct(session, incomingOrder);
		return incomingOrder;
	}
	
    
    @ApiOperation(value = "See all buy Orders in queue")
    @ApiResponses
    (value={@ApiResponse( code = 200, message = "- 0: Success", response = Order.class)})
	@RequestMapping(value = "/buyOrders", method = RequestMethod.GET, produces = "application/json")
    public HashMap<String, ArrayList<Order>>  getBuyOrders(@ApiIgnore HttpSession session ) {
    	return	stockInterfaceImpl.getBuyOrders(session);
		
	}
	
    @ApiOperation(value = "See all sell Orders in queue")
    @ApiResponses
    (value={@ApiResponse( code = 200, message = "- 0: Success", response = Order.class)})
	@RequestMapping(value = "/sellOrders", method = RequestMethod.GET, produces = "application/json")
    public HashMap<String, ArrayList<Order>>  getSellOrders(@ApiIgnore HttpSession session ) {
    	return	stockInterfaceImpl.getSellOrders(session);
		
	}
    
    @ApiOperation(value = "Update an unfulfilled order. You can only update Unit and Price")    
    @ApiImplicitParam(name = "orderId", value = "Order Id to be Deleted", required = true, dataType = "int",  paramType = "query")    
    @ApiResponses
    (value={@ApiResponse( code = 200, message = "- 0: Success", response = Order.class)})
	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
    public Order updateOrder(@ApiIgnore HttpSession session, @RequestBody Order incomingOrder , @ApiParam("Order Id to Update") @RequestParam(value = "orderId", required = true) int orderId ) {
    	stockInterfaceImpl.updateProduct(session, incomingOrder, orderId);
		return incomingOrder;
	}
    
    @ApiOperation(value = "Delete any Unfulfilled Orders in queue")    
	@RequestMapping(value = "/delete", method = RequestMethod.PUT, produces = "application/json")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "orderId", value = "Order Id to be Deleted", required = true, dataType = "int",  paramType = "query"),
        @ApiImplicitParam(name = "orderType", allowableValues = "buy,sell", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "stockSymbol", value = "Stock Symbol To Be Deleted", required = true, dataType = "string", paramType = "query")
      })
    @ApiResponses
    (value={@ApiResponse( code = 200, message = "- 0: Success")})
	public String deteteOrderbyId(@ApiIgnore HttpSession session,
			@ApiParam("Id of the order to delete") @RequestParam(value = "orderId", required = true) int orderId,
			@ApiParam("Type of the order to delete") @RequestParam(value = "orderType", required = true) String orderType,
			@ApiParam("Stock Ticker") @RequestParam(value = "stockSymbol", required = true) String stockSymbol) {

		return stockInterfaceImpl.deleteOrder(session, orderId, orderType, stockSymbol);

	}
}
