var mongoose = require('mongoose')
, mongoUrl = process.env.MONGOLAB_URI || 'mongodb://localhost:27017/galcon'
, db = mongoose.connect(mongoUrl);

var withPromise = function(func) {
	var p = new mongoose.Promise();
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

mongoose.Model.withPromise = withPromise;
mongoose.Document.prototype.withPromise = withPromise;

exports.mongoose = mongoose;
exports.db = db;