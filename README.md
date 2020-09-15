# Stock Application 

I have created a basic Stock Application which follows the FIFO Price Time Matching algorithm.
This application is bare bones without using any Message Brokers. I have tried to use Core Java to show what happens under the hood and the logic behind it.

To run this application do the following steps:
 1. Download code and go to the root directory of the app and run **mvn clean install**
 2. run **docker build -t stock-app .**
 3. run **docker-compose up**  
 4. Once running open in browser: *localhost:8082/h2-console/* to check the DB creation.
 5. Then open *localhost:8082/swagger-ui.html*
  
  Sample Input for adding order:
   
  {
    "order_type": "buy",
    "price": "21",
    "stock_symbol": "PEGA",
    "units": "20",
    "user_id": "Debayan1"
  }
  
  
  
**NOTE:**
To test the order update first go to Get Sell or Get Buy order list. Then use the same similar Input order format as above. Currently the Only fields editable is Units and Price. Once updated the mathcing engine will again run again to update orders.
  
  
 The Current Functionality will just let you post buy and sell orders, and you will onl see tables in H2 getting updated when the matching engile matches and order. All orders will stay in the session Redis cache. Only a valid transion gets stored in DB.
 
 Development environment used:
 Windows 10, Eclipse, Java 8, Maven, Git, Redis for Session, H2 DB for Transction records, Docker

 
