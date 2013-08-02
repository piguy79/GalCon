var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var populateDefaultConfig = function(){
	var defaultConfig = require('./seed/config.json');
			
	for(var i = 0 ; i < defaultConfig.config.length; i++) {
		var config = new ConfigModel(defaultConfig.config[i]);
		config.save();
	}
}

var configSchema = mongoose.Schema({
	version : "Number",
	values : [ {
		key : "String",
		value : "String"
	}]
});

configSchema.set('toObject', { getters: true });
configSchema.index({version : 1});

var ConfigModel = db.model('Config', configSchema);

ConfigModel.remove(function(err, doc) {
	if(err) {
		console.error("Could not delete existing config");
	} else {
		populateDefaultConfig();		
	}
});

exports.findLatestConfig = function(callback) {
	ConfigModel.findOne({version : {$max : version}}).exec(function(err, config) {
		if (err) {
			console.log("Unable to find latest config:" + err);
			callback();
		} else {
			callback(config);
		}
	});
};


exports.findConfigByVersion = function(configVersion, callback){
	ConfigModel.findOne({version : configVersion}).exec(function(err, config){
		if(err){
			console.log("Unable to find config with the key: " + configVersion);
			callback();
		} else{
			callback(config);
		}
	});
};

exports.ConfigModel = ConfigModel;
