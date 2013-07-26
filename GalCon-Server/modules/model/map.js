var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var populateDefaultMaps = function(){
	var defaultMaps = require('./seed/maps.json');
			
	for(var i = 0 ; i < defaultMaps.maps.length; i++) {
		var map = new MapModel(defaultMaps.maps[i]);
		map.save();
	}
}

var mapSchema = mongoose.Schema({
	key : "Number",
	availableFromLevel : "Number",
	title : "String",
	description : "String",
	width : {
		max : "Number",
		min : "Number"
	},
	gameType : "String"
});

mapSchema.set('toObject', { getters: true });
mapSchema.index({key : 1});
mapSchema.index({availableFromLevel : 1});

var MapModel = db.model('Map', mapSchema);

MapModel.remove(function(err, doc) {
	if(err) {
		console.error("Could not delete maps");
	} else {
		populateDefaultMaps();		
	}
});

exports.findAllMaps = function(callback) {
	MapModel.find({}).exec(function(err, maps) {
		if (err) {
			console.log("Unable to find all maps:" + err);
			callback();
		} else {
			callback(maps);
		}
	});
};


exports.findMapByKey = function(mapKey, callback){
	MapModel.findOne({key : mapKey}).exec(function(err, map){
		if(err){
			console.log("Unable to find a map with the key: " + mapKey);
			callback();
		} else{
			callback(map);
		}
	});
};

exports.MapModel = MapModel;
