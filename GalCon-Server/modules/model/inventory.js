var mongoose = require('./mongooseConnection').mongoose, 
	db = require('./mongooseConnection').db, 
	_ = require('underscore');

var inventorySchema = mongoose.Schema({
	sku : "String",
	associatedCoins : "Number"
});

var InventoryModel = db.model('Inventory', inventorySchema);

exports.InventoryModel = InventoryModel;

var populateDefaultInventory = function(){
	var p = InventoryModel.withPromise(InventoryModel.create, require('./seed/inventory.json').inventory);
	p.complete();
}

InventoryModel.remove(function(err, doc) {
	if(err) {
		console.error("Could not delete inventory");
	} else {
		populateDefaultInventory();		
	}
});


