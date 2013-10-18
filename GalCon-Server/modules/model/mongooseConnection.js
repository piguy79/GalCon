var mongoose = require('mongoose')
, mongoUrl = process.env.MONGOLAB_URI || 'mongodb://localhost:27017/galcon'
, db = mongoose.connect(mongoUrl);

mongoose.Model.withPromise = function(func){
	var prom = new mongoose.Promise();
	var array = Array.prototype.slice.call(arguments, 1);
	array.push(function(err, result){
		if(err){
			prom.reject(err);
		}else{
			prom.complete(result);
		}
	});
	func.apply(this, array);
	return prom;
}

mongoose.Document.prototype.withPromise = function(func) {
	var p = new mongoose.Promise();
	p.complete();
	var array = Array.prototype.slice.call(arguments, 1);
	array.push(function(err, result) {
		if(err) {
			p.reject(err);
		} else {
			p.complete(result);
		}
	});
	func.apply(this, array);
	return p;
}

exports.mongoose = mongoose;
exports.db = db;