var mongoose = require('./mongooseConnection').mongoose
,db = require('./mongooseConnection').db
,ObjectId = require('mongoose').Types.ObjectId;

var gameQueueSchema = mongoose.Schema({
	requester : {type: mongoose.Schema.ObjectId, ref: 'User'},
	invitee : "String",
	game : {type: mongoose.Schema.ObjectId, ref: 'Game'},
	creaatedtime : "Date"
});

gameQueueSchema.set('toObject', { getters: true });
gameQueueSchema.index({requester : 1});
gameQueueSchema.index({invitee : 1});

var GameQueueModel = db.model('GameQueue', gameQueueSchema);
exports.GameQueueModel = GameQueueModel;

exports.findByInvitee = function(handle){
	return GameQueueModel.find({invitee : handle}).populate('requester game').exec();
}


