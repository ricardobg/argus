var express 		= require('express');
var mysql  			= require('mysql');
var app 			= express();
var cookieParser 	= require('cookie-parser');
var session 		= require('express-session');
var crud 			= require('./crud.js');


app.use(cookieParser());
app.use(session({
    secret: '34SDgsdgspxxxxxxxdfsG', // just a long random string
    resave: false,
    saveUninitialized: true
}));

function connect() {
	var connection = mysql.createConnection({
		host     : 'localhost',
		user     : 'root',
		password : 'root',
		database : 'argus',
		multipleStatements: true
	});

	connection.connect();
	return connection;
}

function disconnect(connection) {
	connection.end();
}


app.get('/', function (req, res) {
 // res.send('List of commands:');
  res.send('Your session: ' + req.sessionID);
});


var SENSOR_TIMEOUT = 5; //in seconds
var RASP_TIMEOUT = 40; //in seconds


app.get('/alarm_switch', function (req, res) {
	var timestamp = Math.floor(Date.now()/1000);
	console.log("[alarm_switch] Processing request at");
	var house_id = req.query.house_id;
	var active = req.query.active;
	//verify if parameters are valid
	if (house_id == undefined) 
		res.status(500).send("error: Missing 'house_id' argument");
	else if (active == undefined) 
		res.status(500).send("error: Missing 'active' argument");
	else {
		var conn = connect();
		crud.set_alarm(conn, house_id, active, null, function (err, id) {
			if (err)
				res.status(500).send('error: ' + err.message);
			else
				res.send('alarm change (' + (active>0 ? 'activated' : 'deactivated') + ') saved (id=' + id + ')');
			disconnect(conn);
		})
	}

})/
app.get('/sensor_update', function (req, res) {
	var timestamp = Math.floor(Date.now()/1000);
	console.log("[sensor_update] Processing request at " + timestamp);
	var house_id = req.query.house_id;
	var sensor_id = req.query.sensor_id;
	var open = req.query.open;
	//verify if parameters are valid
	if (house_id == undefined) 
		res.status(500).send("error: Missing 'house_id' argument");
	else if (sensor_id == undefined) 
		res.status(500).send("error: Missing 'sensor_id' argument");
	else if (open == undefined)
		res.status(500).send("error: Missing 'open' argument");
	else {
		var conn = connect();
		crud.is_alarm_on(conn, house_id, function (err, active) {
			if (!active) {
				res.send('Sensor change not saved because alarm is not active');
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
					res.status(500).send('error: ' + err.message);
				else
					res.send('Sensor change saved (id=' + id + ')');
				disconnect(conn);
			});
		});
	}
});


var server = app.listen(8080, function () {
  var host = server.address().address;
  var port = server.address().port;

  console.log('Started server at port ' + port);
}); 

