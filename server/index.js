var express = require('express');
var app = express();

app.get('/', function (req, res) {
  res.send('List of commands:');
});


app.get('/sensor_update', function (req, res) {
	var sensor_id = req.query.sensor_id;
	var open = req.query.open;
	var timestamp = req.query.timestapm;
	console.log("[sensor_update] Init");

	var mysql      = require('mysql');
	var connection = mysql.createConnection({
	  host     : 'argus-adrianodennanni.c9.io',
	  user     : 'adrianodennanni',
	  //password : 'secret',
	  database : 'c9'
	});

	connection.connect();

	connection.

	if (sensor_id == undefined)
		console.log("Missign open argument");
	if (open == undefined)
		console.log("Missign open argument");
	if (timestamp == undefined)
		console.log("Missign open argument");
	res.send('Finish2');	


});


var server = app.listen(8080, function () {
  var host = server.address().address;
  var port = server.address().port;

  console.log('Example app listening at http://%s:%s', host, port);
}); 

