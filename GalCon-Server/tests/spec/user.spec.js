var user = require('../../modules/model/user'), 
mongoose = require('mongoose'),
_ = require('underscore'),
apiRunner = require('../fixtures/apiRunner');

describe("Testing interactions with the user model", function(){
	
	var testUser = {
			name : "testing",
			handle : "test",
			createdDate : Date.now(),
			xp : 4,
			wins : 10,
			losses : 6,
			currentGames : [],
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
			currentGames : [],
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
	
	var testOrder = {
			orderId : "1345",
		    packageName : "package",
		    productId : "4",
		    purchaseTime : "12543",
		    purchaseState : "DONE",
		    developerPayload : "",
		    token : "TOK",
		    associatedCoins : 4
		};
	
	
	
	var testUsers = [testUser, testUserWhoHasNotWatchedanAd];
	
	beforeEach(function(done){
		var p = user.UserModel.withPromise(user.UserModel.create, testUsers);
		p.then(function(){
			done();
		});
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
		var p = new mongoose.Promise();
		p.then(function(){
			return user.addCoinsForAnOrder('test',  testOrder);
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
	
	
	
	it("Trying to update with an order which has already been processed", function(done){

		var p = user.addCoinsForAnOrder('testWatchedAd', testOrder);
		p.then(function(updatedUser){
			expect(updatedUser).toBe(null);
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
	
	it("Should delete consumed orders from a user", function(done){

		var p = new mongoose.Promise();
		p.then(function(){
			return user.deleteConsumedOrder('testWatchedAd', testOrder);
		}).then(function(){
			return user.findUserByHandle('testWatchedAd');
		}).then(function(returnedUser){
			expect(returnedUser.consumedOrders.length).toBe(0);
			done();
		}).then(null, function(err){
			console.log(err);
			done();
		});
		p.complete();
	});
	
	
	it("should be able to search for Users", function(done){
		var p = new mongoose.Promise();
		p.then(function(){
			return user.findUserMatchingSearch('t');
		}).then(function(people){
			expect(people.length).toBe(2);
			done();
		}).then(null, function(err){
			console.log(err);
			done();
		});
		
		p.complete();
	});
	
	afterEach(function(done){
		user.UserModel.remove().where('handle').in(_.pluck(testUsers, 'handle')).exec(function(){
			done();
		});
	});
	
});