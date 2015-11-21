use argus;

CREATE TABLE pictures_log
(
     event_id BIGINT NOT NULL PRIMARY KEY,
     picture BLOB NOT NULL,   
     FOREIGN KEY (event_id) REFERENCES events(id)
);