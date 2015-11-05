create database argus;
use argus;

CREATE TABLE houses (
	id BIGINT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL
);

CREATE TABLE sensors (
	id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	house_id BIGINT NOT NULL,
    sensor_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    FOREIGN KEY (house_id) REFERENCES houses(id),
    UNIQUE (house_id, sensor_id)
);

CREATE TABLE users (
	id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	house_id BIGINT NOT NULL,
    user TEXT NOT NULL,
    pass TEXT NOT NULL,
    FOREIGN KEY (house_id) REFERENCES houses(id)
);

CREATE TABLE devices (
	id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    device_id TEXT(50) NOT NULL,
    house_id BIGINT NOT NULL,
    last_sent_info TIMESTAMP NOT NULL,
    name TEXT NOT NULL,
    FOREIGN KEY (house_id) REFERENCES houses(id)
);

CREATE TABLE events (
	id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    instant timestamp NOT NULL,
    type INT NOT NULL,
    house_id BIGINT NOT NULL,
    FOREIGN KEY (house_id) REFERENCES houses(id)
);

CREATE TABLE sensors_change_log (
	 event_id BIGINT NOT NULL PRIMARY KEY,
	 sensor_id BIGINT NOT NULL,
     open BOOL NOT NULL,
     FOREIGN KEY (sensor_id) REFERENCES sensors(id),
     FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE identifications_log (
	 event_id BIGINT NOT NULL PRIMARY KEY,
	 type INT NOT NULL,
     panic BOOL NOT NULL,
     FOREIGN KEY (event_id) REFERENCES events(id)
);
    
CREATE TABLE alarms_log (
	event_id BIGINT NOT NULL PRIMARY KEY,
    active BOOL NOT NULL,
    device_id BIGINT,
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);