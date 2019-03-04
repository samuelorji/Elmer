CREATE KEYSPACE IF NOT EXISTS elmer WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE elmer;

DROP TABLE IF EXISTS food_order_request;
CREATE TABLE food_order_request (
  user_id int,
  insertion_day int,
  insertion_time timestamp,
  transaction_id varchar,
  food_name varchar,
  quantity int,
  callback_url varchar,
  PRIMARY KEY (user_id, insertion_day, insertion_time, transaction_id)
) WITH CLUSTERING ORDER BY (insertion_day DESC, insertion_time DESC);

CREATE INDEX food_order_request_name ON food_order_request(food_name);

DROP TABLE IF EXISTS food_order_status;
CREATE TABLE food_order_status (
  transaction_id varchar,
  status varchar,
  description varchar,
  PRIMARY KEY (transaction_id)
);
