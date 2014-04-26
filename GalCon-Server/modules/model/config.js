var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var configSchema = mongoose.Schema({
	version : "Number",
	type : "String",
	values : {}
});

configSchema.set('toObject', { getters: true });
configSchema.index({version : -1, type : 1});

var ConfigModel = db.model('Config', configSchema);

exports.findLatestConfig = function(configType) {
	var p = ConfigModel.find({type : configType}).sort({version: -1}).limit(1).exec();
	return p.then(function(configs) {
		if(!configs) {
			return null;
		}
		return configs[0];
	});
	
	return p;
};

exports.ConfigModel = ConfigModel;
