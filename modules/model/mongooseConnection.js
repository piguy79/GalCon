var mongoose = require('mongoose')
, mongoUrl = process.env.MONGO_URL || 'mongodb://localhost:27017/galcon'
, db = mongoose.connect(mongoUrl);

exports.mongoose = mongoose;
exports.db = db;