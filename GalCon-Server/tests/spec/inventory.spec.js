var inventory = require('../../modules/model/inventory'), 
mongoose = require('mongoose'),
_ = require('underscore');

describe("Testing the use of a store used by the applicaiton to map items in the Play store to in game curreny", function(){
	
	var testSkus = [{sku : "fake_coins_1", associatedCoins : 4},{sku : "fake_coins_2", associatedCoins : 3}];
	
	beforeEach(function(done){
		inventory.InventoryModel.remove().where("sku").in(_.pluck(testSkus, "sku")).exec(function(){
			done();
		});
	});
	
	afterEach(function(done){
		inventory.InventoryModel.remove().where("sku").in(_.pluck(testSkus, "sku")).exec(function(){
			done();
		});
	});
	
	it("Able to retrieve all skus", function(done){
		var p = new mongoose.Promise();
		p.then(function(){
			return inventory.InventoryModel.withPromise(inventory.InventoryModel.create, testSkus);
		}).then(function(){
			return inventory.InventoryModel.find().where('sku').in(_.pluck(testSkus, 'sku')).exec();
		}).then(function(inventoryItems){
			testSkus.forEach(function(product){
				expect(_.any(inventoryItems, function(item){ return item.sku === product.sku;})).toBe(true);
			});
			done();
		}).then(null, function(err){
			console.log(err);
		});
		
		p.complete();
		
	});
	
	
	
});