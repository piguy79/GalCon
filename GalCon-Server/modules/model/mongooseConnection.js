var mongoose = require('mongoose')
, mongoUrl = process.env.MONGOLAB_URI || 'mongodb://localhost:27017/galcon'
, db = mongoose.connect(mongoUrl);

exports.mongoose = mongoose;
exports.db = db;