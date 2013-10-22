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
	
	var testOrder = {
			orderId : "1345",
		    packageName : "package",
		    productId : "4",
		    purchaseTime : "12543",
		    purchaseState : "DONE",
		    developerPayload : "",
		    token : "TOK"
		};
	
	
	
	var testUsers = [testUser];
	
	beforeEach(function(done){
		var p = user.UserModel.withPromise(user.UserModel.create, testUsers);
		p.then(function(){
			done();
		});
	});	
	
	it("Should add coins for multiple orders", function(done){
		var p = new mongoose.Promise();
		p.then(function(){
			return user.findUserByHandle('test');
		}).then(function(ourUser){
			console.log(ourUser);
			return apiRunner.addCoinsForAnOrder('test', 2, [testOrder]);
		}).then(function(foundUser){
			expect(foundUser.consumedOrders.length).toBe(1);
			done();
		}, function(err){
			expect(true).toBe(false);
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