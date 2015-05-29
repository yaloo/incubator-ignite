var config = require('./configuration.js');

// Mongoose for mongodb.
var mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    ObjectId = mongoose.Schema.Types.ObjectId,
    passportLocalMongoose = require('passport-local-mongoose');

// Connect to mongoDB database.
mongoose.connect(config.get('mongoDB:url'), {server: {poolSize: 4}});

// Define account model.
var AccountSchema = new Schema({
    username: String
});

AccountSchema.plugin(passportLocalMongoose, { usernameField: 'email' });

exports.Account = mongoose.model('Account', AccountSchema);

// Define space model.
exports.Space =  mongoose.model('Space', new Schema({
    name: String,
    owner: { type: ObjectId, ref: 'Account' },
    usedBy: [{
        permission: { type: String, enum: ['VIEW', 'FULL']},
        account: { type: ObjectId, ref: 'Account' }
    }]
}));

// Define cluster model.
exports.Cluster =  mongoose.model('Cluster', new Schema({
    space: { type: ObjectId, ref: 'Space' },
    name: String,
    discovery: { type: String, enum: ['TcpDiscoveryVmIpFinder', 'TcpDiscoveryMulticastIpFinder'] },
    addresses: [String],
    pubPoolSize: Number,
    sysPoolSize: Number,
    mgmtPoolSize: Number,
    p2pPoolSize: Number
}));

// Define cache model.
exports.Cache =  mongoose.model('Cache', new Schema({
    space: { type: ObjectId, ref: 'Space' },
    name: String,
    mode: { type: String, enum: ['PARTITIONED', 'REPLICATED', 'LOCAL'] },
    backups: Number,
    atomicity: { type: String, enum: ['ATOMIC', 'TRANSACTIONAL'] },
    clusters: [{ type: ObjectId, ref: 'Cluster' }]
}));

exports.upsert = function(model, data, cb){
    if (data._id) {
        var id = data._id;

        delete data._id;

        model.findOneAndUpdate({_id: id}, data, cb);
    }
    else
        model.create(data, cb);
};

exports.mongoose = mongoose;