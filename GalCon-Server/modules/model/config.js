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
	type : "String",
	values : {}
});

configSchema.set('toObject', { getters: true });
configSchema.index({version : -1, type : 1});

var ConfigModel = db.model('Config', configSchema);

ConfigModel.remove(function(err, doc) {
	if(err) {
		console.error("Could not delete existing config");
	} else {
		populateDefaultConfig();		
	}
});

exports.findLatestConfig = function(configType) {
	var p = ConfigModel.find({type : configType}).sort({version: -1}).limit(1).exec();
	return p.then(function(configs) {
		if(!configs) {
			return null;
		}
		return configs[0];
	});
};

exports.ConfigModel = ConfigModel;
