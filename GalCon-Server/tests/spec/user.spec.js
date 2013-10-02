var user = require('../../modules/model/user'), 
mongoose = require('mongoose'),
_ = require('underscore');

describe("Testing interactions with the user model", function(){
	
	var testUser = {
			Purchaname : "testing",
			handle : "test",
			createdDate : Date.now(),
			xp : 4,
			wins : 10,
			losses : 6,
			currentGames : ["12345"],
			consumedOrders : [],
			coins : 0,
			usedCoins : 14567,
			watchedAd : true,
			rankInfo : {
				level : 1,
				startFrom : 0,
				endAt : 20
			}
	};
	
	var testUserWhoHasNotWatchedanAd = {
			name : "testingWatchAd",
			handle : "testWatchedAd",
			createdDate : Date.now(),
			xp : 4,
			wins : 10,
			losses : 6,
			currentGames : ["12345"],
			consumedOrders : [
								{
									orderId : "1345",
								    packageName : "package",
								    productId : "4",
								    purchaseTime : "12543",
								    purchaseState : "DONE",
								    developerPayload : "",
								    token : "TOK"
								}
			                 ],
			coins : 0,
			usedCoins : 1000,
			watchedAd : false,
			rankInfo : {
				level : 1,
				startFrom : 0,
				endAt : 20
			}
	};
	
	beforeEach(function(done){
		var p = user.UserModel.withPromise(user.UserModel.create, [testUser, testUserWhoHasNotWatchedanAd]);
		p.then(function(){
			done();
		});
		p.complete();
	});
	
	it("Add coins to a test user", function(done){
		var p = new mongoose.Promise();
		p.then(function(){
			return user.addCoins(4, 'test', 14567);
		}).then(function(person){
			expect(person.coins).toBe(4);
			expect(person.usedCoins).toBe(-1);
			expect(person.watchedAd).toBe(false);
			done();
		}).then(null, function(err){
			console.log(err);
			done();
		});
		
		p.complete();
	});
	
	it("Add coins to a test user using an order object", function(done){
		var testOrder = {
				orderId : "1345",
			    packageName : "package",
			    productId : "4",
			    purchaseTime : "12543",
			    purchaseState : "DONE",
			    developerPayload : "",
			    token : "TOK"
			};
		var p = new mongoose.Promise();
		p.then(function(){
			return user.addCoinsForAnOrder(4, 'test', 14567, testOrder);
		}).then(function(person){
			expect(person.coins).toBe(4);
			expect(person.usedCoins).toBe(-1);
			expect(person.watchedAd).toBe(false);
			expect(person.consumedOrders.length).toBe(1);
			expect(_.filter(person.consumedOrders, function(order){return order.orderId === "1345"}).length).toBe(1);
			done();
		}).then(null, function(err){
			console.log(err);
			done();
		});
		
		p.complete();
	});
	
	it("Add coins with an invalid usedCoins count.", function(done){
		var p = new mongoose.Promise();
		p.then(function(){
			return user.addCoins(4, 'test', 8767);
		}).then(function(person){
			expect(person).toBe(null);
			done();
		}).then(null, function(err){
			console.log(err);
			done();
		});
		p.complete();
	});
	
	
	it("Trying to update with an order which has already been processed", function(done){
		var testOrder = {
				orderId : "1345",
			    packageName : "package",
			    productId : "4",
			    purchaseTime : "12543",
			    purchaseState : "DONE",
			    developerPayload : "",
			    token : "TOK"
			};
		var p = user.addCoinsForAnOrder(1, 'testWatchedAd', 1000, testOrder);
		p.then(function(updatedUser){
			expect(updatedUser.consumedOrders.length).toBe(1);
			done();
		}).then(null, function(err){
			console.log(err);
			done();
		});
	});
	
	it("Time should be reduced for watching an AD", function(done){
		var p = new mongoose.Promise();
		p.then(function(){
			return user.reduceTimeForWatchingAd('testWatchedAd', 1000, 100, 0.5);
		}).then(function(updatedUser){
			expect(updatedUser.usedCoins).toBe(950);
			expect(updatedUser.watchedAd).toBe(true);
			done();
		}).then(null, function(err){
			console.log(err);
			done();
		});
		p.complete();
	});
	
	it("You should not be able to reduce time with stale data", function(done){
		var p = new mongoose.Promise();
		p.then(function(){
			return user.reduceTimeForWatchingAd('testWatchedAd', 999, 100, 0.5);
		}).then(function(updatedUser){
			expect(updatedUser).toBe(null);
			done();
		}).then(null, function(err){
			console.log(err);
			done();
		});
		p.complete();
	});
	
	afterEach(function(done){
		user.UserModel.remove().where('handle').in(['test', 'testWatchedAd']).exec(function(){
			done();
		});
	});
	
});