drop table public.stocks if exists ; 
drop table public.stocks_data if exists ; 

CREATE TABLE PUBLIC.stocks (
                 symbol VARCHAR(10) NOT NULL,
                CONSTRAINT stocks_pk PRIMARY KEY (symbol)
);


CREATE TABLE PUBLIC.stocks_data (
                id serial,
                date_analysed DATE NOT NULL,
                high_price DECIMAL NOT NULL,
                low_price DECIMAL NOT NULL,
                closing_price DECIMAL NOT NULL,
                symbol VARCHAR(10) NOT NULL,
                CONSTRAINT stocks_data_pk PRIMARY KEY (id)
);


ALTER TABLE PUBLIC.stocks_data ADD CONSTRAINT stocks_data_stocks_fk
FOREIGN KEY (symbol)
REFERENCES PUBLIC.stocks (symbol)
ON DELETE NO ACTION
ON UPDATE NO ACTION;


-- seed the table with some sample tickers
INSERT INTO STOCKS(symbol) values('GOOG');
INSERT INTO STOCKS(symbol) values('MSFT');
INSERT INTO STOCKS(symbol) values('ORCL');
INSERT INTO STOCKS(symbol) values('ADBE');
INSERT INTO STOCKS(symbol) values('VMW');
