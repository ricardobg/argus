var express 		= require('express');
var mysql  			= require('mysql');
var app 			= express();
var cookieParser 	= require('cookie-parser');
var session 		= require('express-session');
var crud 			= require('./crud.js');
//Load bd config
var db_config 		= require('./db_config.json');

//Set multiple statements
db_config.multipleStatements = true;

app.use(cookieParser());


var bodyParser = require('body-parser')
app.use( bodyParser.json({limit: '50mb'}) );       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
	limit: '50mb',
  	extended: true
})); 

app.use(session({
    secret: '34SDgsdgspxxxxxxxdfsG', // just a long random string
    resave: false,
    saveUninitialized: true
}));

function connect() {
	var connection = mysql.createConnection(db_config);
	connection.connect();
	return connection;
}

function disconnect(connection) {
	connection.end();
}

function identified_user(req, callback) {
	if (req.cookies.user_id !== undefined && req.cookies.house_id !== undefined)
		callback();
	else
		callback(Error('user not identified. Please login'));
}


app.get('/', function (req, res) {
 // res.send('List of commands:');
  res.send('Your session: ' + req.sessionID);
});


var SENSOR_TIMEOUT = 5; //in seconds
var RASP_TIMEOUT = 40; //in seconds


app.get('/alarm_switch', function (req, res) {
	var timestamp = Math.floor(Date.now()/1000);
	console.log("[alarm_switch] Processing request...");
	var house_id = req.query.house_id;
	var active = req.query.active;
	//verify if parameters are valid
	if (house_id == undefined) 
		res.status(500).json({ "error": "Missing 'house_id' argument" });
	else if (active == undefined) 
		res.status(500).json({ "error": "Missing 'active' argument" });
	else {
		var conn = connect();
		crud.set_alarm(conn, house_id, active, null, function (err, id) {
			if (err)
				res.status(500).json({"error": err.message});
			else
				res.json({"result": "'alarm change (' + (active>0 ? 'activated' : 'deactivated') + ') saved (id=' + id + ')'"});
			disconnect(conn);
		})
	}
});

app.get('/sensor_update', function (req, res) {
	var timestamp = Math.floor(Date.now()/1000);
	console.log("[sensor_update] Processing request at " + timestamp);
	var house_id = req.query.house_id;
	var sensor_id = req.query.sensor_id;
	var open = req.query.open;
	//verify if parameters are valid
	if (house_id == undefined) 
		res.status(500).json({ "error": "Missing 'house_id' argument" });
	else if (sensor_id == undefined) 
		res.status(500).json({ "error": "Missing 'sensor_id' argument" });
	else if (open == undefined)
		res.status(500).json({ "error": "Missing 'open' argument" });
	else {
		var conn = connect();
		crud.is_alarm_on(conn, house_id, function (err, active) {
			if (!active) {
				res.json({"warning": "'Sensor change not saved because alarm is not active'", "result": "no event created"});
				disconnect(conn);
				return;
			}
			setTimeout(function () {
				console.log("[sensor_update] timeout (" + SENSOR_TIMEOUT + "s)");
				var connection = connect();
				crud.sensor_timeout(connection, house_id, timestamp, function (err, id) {
					if (err)
						console.log('[sensor_timeout] ' + err.message);
					else if (id)
						console.log('[sensor_timeout] Created INVASION event (id=' + id + ')');
					else
						console.log('[sensor_timeout] Nothing to be done!');
					disconnect(connection);
				});
			}, SENSOR_TIMEOUT * 1000);

			//Save entry
			crud.create_sensor_log(conn, house_id, sensor_id, open, timestamp, function (err, id) {
				if (err)
					res.status(500).json({"error": err.message});
				else
					res.json({"result": "Sensor change saved (id=" + id + ")"});
				disconnect(conn);
			});
		});
	}
});


app.post('/send_snap', function (req, res) {
	var timestamp = Math.floor(Date.now()/1000);
	console.log("[send_snap] Processing request at " + timestamp);
	var house_id = req.body.house_id;
	var image = req.body.image;
	//verify if parameters are valid
	if (house_id == undefined) 
		res.status(500).json({ "error": "Missing 'house_id' argument" });
	else if (image == undefined) 
		res.status(500).json({ "error": "Missing 'image' argument" });
	else {
		var image_buf = new Buffer(image, 'base64');
		setTimeout(function () {
				console.log("[send_snap] timeout (" + SENSOR_TIMEOUT + "s)");
				var connection = connect();
				crud.is_house_in_dangerous(connection, house_id, timestamp, function (err, danger) {
					if (err) {
						console.log('[send_snap] ' + err.message);
						disconnect(connection);
					}
					else if (danger) {
						//Save snap
						crud.save_snap(connection, house_id, Math.floor(Date.now()/1000), image_buf, function (err, event_id) {
							console.log('[send_snap] Created SNAP event (id=' + event_id + ')');
							disconnect(connection);
						});
					}
					else {
						console.log('[send_snap] Snap won\'t be saved');
						disconnect(connection);
					}
					
				});
		}, SENSOR_TIMEOUT * 1000 + 10);
		res.status(500).json({"result": "snap received"});
	}
});

app.get('/get_snap', function (req, res) {
	console.log("[get_snap] Processing request...");
	var house_id = req.query.house_id;
	var event_id = req.query.event_id;
	//verify if parameters are valid
	if (house_id == undefined) 
		res.status(500).json({ "error": "Missing 'house_id' argument" });
	else if (event_id == undefined) 
		res.status(500).json({ "error": "Missing 'event_id' argument" });
	else {
		var conn = connect();
		crud.get_snap(conn, event_id, function (err, image) {
			if (err)
				res.status(500).json({"error": err.message});
			else {
				res.header('Content-Type', 'image/jpeg');
				res.end(image);
			}
			disconnect(conn);
		})
	}
});


app.get('/login', function (req, res) {
	console.log("[login] Processing request");
	var user = req.query.user;
	var password = req.query.password;
	
	//verify if parameters are valid
	if (user == undefined) 
		res.status(500).json({ "error": "Missing 'user' argument" });
	else if (password == undefined) 
		res.status(500).json({ "error": "Missing 'password' argument" });
	else {
		var conn = connect();
		crud.get_user_id_and_house_id(conn, user, password, function (err, user_id, house_id) {
			if (err) {
				res.status(500).json({"error": err.message });
				console.log('[login] error ' + err.message);
				req.cookies.user_id = undefined;
				req.cookies.house_id = undefined;
				disconnect(conn);
				return;
			}
			console.log('[login] User ' + user + " logged!");
			res.cookie('user_id', user_id);
			res.cookie('house_id', house_id);
			res.json({"result": "user logged"});
			disconnect(conn);
		});
	}
});

app.get('/update_info', function (req, res) {
	console.log("[update_info] Processing request");
	var timestamp = req.query.timestamp; 
	if (timestamp == undefined)
		timestamp = -1;
	identified_user(req, function (err) {
		if (err) {
			res.status(500).send({"error": err.message});
			return;
		}
		var conn = connect();
		crud.get_events(conn, req.cookies.house_id, parseInt(timestamp)+1, req.protocol + '://' + req.get('host') + '/', function (err, events) {
			if (err) {
				res.status(500).send({"error": err.message});
				disconnect(conn);
				return;
			}
			crud.is_alarm_on(conn, req.cookies.house_id, function (err, active) {
				if (err) {
					res.status(500).send({"error": err.message});
					disconnect(conn);
					return;
				}	
				disconnect(conn);
				res.json({ 
					alarm_status: active,
					events: events 
				});		
			});
		});
	});
});

var server = app.listen(8080, function () {
  var host = server.address().address;
  var port = server.address().port;

  console.log('Started server at port ' + port);
}); 

