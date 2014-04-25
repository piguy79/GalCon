var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var mapSchema = mongoose.Schema({
	key : "Number",
	availableFromXp : "Number",
	title : "String",
	description : "String",
	width : {
		max : "Number",
		min : "Number"
	},
	canHarvest : "Boolean",
	gameType : ["String"],
	version : "Number"
});

mapSchema.set('toObject', { getters: true });
mapSchema.index({key : 1});
mapSchema.index({availableFromXp : 1});

var MapModel = db.model('Map', mapSchema);

exports.findAllMaps = function(version) {
	return MapModel.find({version: {$lte : version}}).exec();
};

exports.findMapByKey = function(mapKey){
	return MapModel.findOne({key : mapKey}).exec();
};

exports.MapModel = MapModel;
