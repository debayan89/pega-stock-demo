FROM java:8

COPY ./target/pega-stock-demo-0.0.1-SNAPSHOT.jar ~/Coding/stockapp/

WORKDIR ~/Coding/stockapp/

RUN sh -c 'touch pega-stock-demo-0.0.1-SNAPSHOT.jar'

ENTRYPOINT ["java","-jar","pega-stock-demo-0.0.1-SNAPSHOT.jar"]
