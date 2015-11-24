var Enum 	= require('enum');
var EVENTS 	= new Enum(['Invasion', 'Offline', 'IdentPanic', 'Ident', 'DoorChange', 'AlarmChange', 'Snap', 'Online']);


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

function get_user_id_and_house_id(connection, user, password, callback) {
	connection.query('SELECT * FROM users WHERE user=? and pass=?', [user, password], function(err, rows) { 
        if (err) {
			callback(err);
			return;
        }
		if (rows.length == 0) {
			callback(Error("user/password invalid"));
			return;
		}
		callback(null, rows[0].id, rows[0].house_id);
    });
}

function get_events(connection, house_id, timestamp, base_url, callback) {
	connection.query('SELECT e.id, UNIX_TIMESTAMP(e.instant) as instant, e.type, i.type as identification_type, i.panic, a.active, s.sensor_id, s.open  \
		FROM events e left join identifications_log i on i.event_id=e.id left join alarms_log a on a.event_id=e.id left join \
		sensors_change_log s on s.event_id=e.id where e.house_id=? and e.instant>=FROM_UNIXTIME(?) ORDER BY e.instant ASC',
		[ house_id, timestamp], function (err, rows) {
		if (err) {
			callback(err);
			return;
		}
		//get last online/offline event before the timestamp
		connection.query('SELECT e.type as type from events e where e.type in (?,?) and e.house_id=? and e.instant<FROM_UNIXTIME(?) ORDER BY e.instant DESC',
		[ EVENTS.Online.value, EVENTS.Offline.value, house_id, timestamp], function (err, online_rows) {
			if (err) {
				callback(err);
				return;
			}
			console.log('[get_events] Loaded ' + rows.length + ' events');

			var events = [];
			var last_online = null;
			if (online_rows.length > 0)
				last_online = online_rows[0].type;
			for (var i = 0; i < rows.length; i++) {
				//Skip repeated online events
				if (rows[i].type == last_online)
					continue;
				else if (rows[i].type == EVENTS.Offline.value || rows[i].type == EVENTS.Online.value) {
					last_online = last_online == EVENTS.Offline.value ? EVENTS.Online.value : EVENTS.Offline.value;
				}
				var event = {};
				event.id = rows[i].id;
				event.timestamp = rows[i].instant;
				event.type = rows[i].type;
				if (event.type == EVENTS.Snap.value) {
					event.snap = {
						link:  base_url + 'get_snap?house_id=' + house_id + '&event_id=' + event.id
					}
				}
				events.push(event);

			}
			callback(null, events);


		});
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
				callback(null, false);
				return;
			}
			callback(null, rows[0].active);
	    });
	});
}

function is_online(connection, house_id, callback) {
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
		}
		connection.query('SELECT type, id FROM events WHERE house_id=? and type in(?,?) order by instant DESC', [house_id, EVENTS.Offline.value, EVENTS.Online.value], 
			function(err, rows) { 
	        if (err) {
				callback(err);
				return;
	        }
			if (rows.length == 0) {
				//alarm not aonline by default
				callback(null, false);
				return;
			}
			callback(null, rows[0].type == EVENTS.Online.value, rows[0].id);
	    });
	});
}

function is_house_in_dangerous(connection, house_id, timestamp, callback) {
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
		}
		connection.query('SELECT instant, type FROM events left join alarms_log on alarms_log.event_id=events.id WHERE (active=0 or active is null) and house_id=? and instant>=FROM_UNIXTIME(?) and type in (?,?,?,?) order by instant ASC', 
			[house_id, timestamp, EVENTS.AlarmChange.value, EVENTS.Ident.value, EVENTS.IdentPanic.value, EVENTS.Invasion.value], 
			function(err, rows) { 
	        if (err) {
				callback(err);
				return;
	        }
			if (rows.length == 0) {
				//Strange behvavior!
				console.log("[WARNING] Unexpected behavior. Can't know if house is in dangerous or not. Assuming it's in dangerous");
				callback(null, true);
				return;
			}
			if (rows[0].type == EVENTS.Invasion.value || ows[0].type == EVENTS.IdentPanic.value) {
				callback(null, true);
				return;
			}
			callback(null, false);
	    });
	});
}

function is_house_waiting_identification(connection, house_id, since, callback) {
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
		}
		if (since < 0)
			since = 0;
		connection.query('SELECT instant, type FROM events left join sensors_change_log on sensors_change_log.event_id=events.id left join alarms_log on alarms_log.event_id=events.id WHERE (open=1 or open is null) and (active=0 or active is null) and house_id=? and instant>=FROM_UNIXTIME(?) and type in (?,?,?,?,?) order by instant DESC', 
			[house_id, since, EVENTS.DoorChange.value, EVENTS.AlarmChange.value, EVENTS.Ident.value, EVENTS.IdentPanic.value, EVENTS.Invasion.value], 
			function(err, rows) { 
	        if (err) {
				callback(err);
				return;
	        }
			if (rows.length == 0) {
				//Not waiting
				callback(null, false);
				return;
			}
			var door_changes = 0;
			for (var i = 0; i < rows.length; i++) {
				if (rows[i].type == EVENTS.DoorChange.value) 
					door_changes++;
				else
					door_changes--;
			}
			callback(null, door_changes > 0);
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


function save_snap(connection, house_id, timestamp, image, callback) {
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
		}
		create_event(connection, house_id, EVENTS.Snap, timestamp, function (err, id) {
			if (err) {
				callback(err);
				return;
			}
			connection.query('INSERT INTO pictures_log (event_id, picture) VALUES (?,?); SELECT LAST_INSERT_ID() as id;',
				[ id, image ], function (err, rows) {
				if (err) {
					callback(err);
					return;
				}
				callback(null, rows[1][0].id);
			});
		});
	});
}

function get_snap(connection, event_id, callback) {
	connection.query('SELECT * FROM pictures_log WHERE event_id=?', [event_id], function(err, rows) { 
        if (err) {
			callback(err);
			return;
        }
		if (rows.length == 0) {
			callback(Error("snap with id=" + event_id + " not found"));
			return;
		}
		callback(null, rows[0].picture);
    });
}


exports.is_online = is_online;
exports.is_house_waiting_identification = is_house_waiting_identification;
exports.is_house_in_dangerous = is_house_in_dangerous;
exports.get_snap = get_snap;
exports.save_snap = save_snap;
exports.is_alarm_on = is_alarm_on;
exports.set_alarm = set_alarm;
exports.get_user_id_and_house_id = get_user_id_and_house_id;
exports.get_events = get_events;

//Function to call when there is sensor timeout
exports.sensor_timeout = function (connection, house_id, initial_time, callback) {	
	connection.query('SELECT instant,type FROM events LEFT JOIN alarms_log on alarms_log.event_id=events.id WHERE house_id=? and type in(?,?,?) and (active is null or active=0) and instant>=FROM_UNIXTIME(?) order by instant DESC', 
		[house_id, EVENTS.AlarmChange.value, EVENTS.Ident.value, EVENTS.IdentPanic.value, initial_time], function(err, rows) { 
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

exports.create_identification_log = function (connection, house_id, type, panic, timestamp, callback) {
	//verify if house exists
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
		}
		//verify if sensor exists
		if (err) {
			callback(err);
			return;
		}
		//create event
		create_event(connection, house_id,  panic ? EVENTS.IdentPanic : EVENTS.Ident, timestamp, function (err, event_id) {
			if (err) {
				callback(err);
				return;
			}
			//create identifications change log
			connection.query('INSERT INTO identifications_log (event_id, type, panic) VALUES (?,?,?); SELECT LAST_INSERT_ID() as id;', 
				[ event_id, type, panic ], function (err, rows) {
				if (err) {
					callback(err);
					return;
				}
				var log_id = rows[1][0].id;
				console.log('[create_identification_log] Created create_identification_log (id=' + log_id + ')');
				callback(null, log_id);
			});
		});
	});
} 


exports.create_online_event = function (connection, house_id, online, timestamp, callback) {
	//verify if house exists
	house_exists(connection, house_id, function (err) {
		if (err) {
			callback(err);
			return;
		}
		//create event
		create_event(connection, house_id,  online ? EVENTS.Online : EVENTS.Offline, timestamp, function (err, event_id) {
			if (err) {
				callback(err);
				return;
			}
			callback(null, event_id);
		});
	});
} 