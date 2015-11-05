INSERT INTO houses (id, name) values (1, 'Família Kenzo');
INSERT INTO houses (id, name) values (2, 'Família Fabrice');
INSERT INTO houses (id, name) values (3, 'Família Brócoli');

INSERT INTO sensors (house_id, sensor_id, description) VALUES (1, 1, 'Porta Principal');
INSERT INTO sensors (house_id, sensor_id, description) VALUES (1, 2, 'Janela térreo');
INSERT INTO sensors (house_id, sensor_id, description) VALUES (1, 3, 'Janela primeiro andar');
INSERT INTO sensors (house_id, sensor_id, description) VALUES (1, 4, 'Porta dos fundos');

INSERT INTO sensors (house_id, sensor_id, description) VALUES (2, 1, 'Porta Principal');
INSERT INTO sensors (house_id, sensor_id, description) VALUES (2, 2, 'Porta dos fundos');

INSERT INTO sensors (house_id, sensor_id, description) VALUES (3, 1, 'Porta Principal');

INSERT INTO users (house_id, user, pass) VALUES (1, 'kenzo', 'kenzao');
INSERT INTO users (house_id, user, pass) VALUES (2, 'franca', 'brasils2');
INSERT INTO users (house_id, user, pass) VALUES (3, 'ricardo', 'ricardo');