var mongoose = require('./mongooseConnection').mongoose, 
	db = require('./mongooseConnection').db, 
	_ = require('underscore');

var inventorySchema = mongoose.Schema({
	sku : "String",
	associatedCoins : "Number"
});

var InventoryModel = db.model('Inventory', inventorySchema);

exports.InventoryModel = InventoryModel;