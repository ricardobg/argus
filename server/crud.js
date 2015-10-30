var Enum 	= require('enum');
var EVENTS 	= new Enum(['Invasion', 'Offline', 'IdentPanic', 'Ident', 'DoorChange', 'AlarmChange', 'Snap']);


function house_exists(connection, house_id, callback) {
	connection.query('SELECT * FROM houses WHERE id=?', [house_id], function(err, rows) { 
        if (err) {
			callback(err);
			return;
        }
		if (rows.length == 0) {
			callback(Error("'house_id' " + house_id + " doesn't exist in houses"));
			return;
		}
		callback();
    });
}

function get_sensor_id(connection, house_id, sensor_id, callback) {
	connection.query('SELECT * FROM sensors WHERE house_id=? and sensor_id=?', [house_id, sensor_id], function(err, rows) { 
        if (err) {
			callback(err);
			return;
        }
		if (rows.length == 0) {
			callback(Error("'sensor_id' " + sensor_id + " doesn't exist in sensors"));
			return;
		}
		callback(null, rows[0].id);
    });
}

function is_alarm_on(connection, house_id, callback) {
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
		}
		connection.query('SELECT instant, active  FROM events join alarms_log on alarms_log.event_id=events.id WHERE house_id=? and type=? order by instant DESC', [house_id, EVENTS.AlarmChange.value], 
			function(err, rows) { 
	        if (err) {
				callback(err);
				return;
	        }
			if (rows.length == 0) {
				//alarm not activated by default
				callback(null, false );
				return;
			}
			callback(null, rows[0].active);
	    });
	});
}


function create_event(connection, house_id, type, timestamp, callback) {
	connection.query('INSERT INTO events (instant, type, house_id) VALUES (FROM_UNIXTIME(?),?,?); SELECT LAST_INSERT_ID() as id;',
		[ timestamp, type.value, house_id], function (err, rows) {
		if (err) {
			callback(err);
			return;
		}
		console.log('[create_event] Created event (id=' + rows[1][0].id + ')');
		callback(null, rows[1][0].id)
	});
}

function set_alarm(connection, house_id, active, source, callback) {
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
			}
		create_event(connection, house_id, EVENTS.AlarmChange, Math.floor(Date.now()/1000), function (err, id) {
			if (err) {
				callback(err);
				return;
			}
			connection.query('INSERT INTO alarms_log (event_id, active, device_id) VALUES (?,?,?); SELECT LAST_INSERT_ID() as id;',
				[ id, active, source ], function (err, rows) {
				if (err) {
					callback(err);
					return;
				}
				callback(null, rows[1][0].id);
			});
		});
	});
}

exports.is_alarm_on = is_alarm_on;
exports.set_alarm = set_alarm;

//Function to call when there is sensor timeout
exports.sensor_timeout = function (connection, house_id, initial_time, callback) {	
	connection.query('SELECT instant,type FROM events LEFT JOIN alarms_log on alarms_log.event_id=events.id WHERE house_id=? and type in(?,?) and (active is null or active=0) and instant>=FROM_UNIXTIME(?) order by instant DESC', 
		[house_id, EVENTS.AlarmChange.value, EVENTS.Ident.value, initial_time], function(err, rows) { 
        if (err) {
			callback(err);
			return;
        }
		if (rows.length == 0) {
			//INVASION
			create_event(connection, house_id, EVENTS.Invasion, Math.floor(Date.now()/1000), function (err, id) {
				if (err) {
					callback(err);
					return;
				}
				callback(null, id);
			});
			return;
		}
		callback(null);
    });
}


exports.create_sensor_log = function (connection, house_id, sensor_id, open, timestamp, callback) {
	//verify if house exists
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
		}
		//verify if sensor exists
		get_sensor_id(connection, house_id, sensor_id, function (err, id) {
			if (err) {
				callback(err);
				return;
			}
			//create event
			create_event(connection, house_id, EVENTS.DoorChange, timestamp, function (err, event_id) {
				if (err) {
					callback(err);
					return;
				}
				//create sensor change log
				connection.query('INSERT INTO sensors_change_log (event_id, sensor_id, open) VALUES (?,?,?); SELECT LAST_INSERT_ID() as id;', 
					[ event_id, sensor_id, open ], function (err, rows) {
					if (err) {
						callback(err);
						return;
					}
					var log_id = rows[1][0].id;
					console.log('[create_sensor_log] Created sensors_change_log (id=' + log_id + ')');
					callback(null, log_id);
				});
			});
		});
	});
}