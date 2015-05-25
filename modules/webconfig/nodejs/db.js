var config = require('./configuration.js');

// Mongoose for mongodb.
var mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    ObjectId = mongoose.Schema.Types.ObjectId,
    passportLocalMongoose = require('passport-local-mongoose');

// Connect to mongoDB database on modulus.io.
mongoose.connect(config.get('mongoDB:url'), {server: {poolSize: 4}});

// Define user model.
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
    name : String,
    discovery : { type: String, enum: ['TcpDiscoveryVmIpFinder', 'TcpDiscoveryMulticastIpFinder'] },
    addresses : [String]
}));

// Define cache model.
exports.Cache =  mongoose.model('Cache', new Schema({
    space: { type: ObjectId, ref: 'Space' },
    name: String,
    mode: { type: String, enum: ['PARTITIONED', 'REPLICATED', 'LOCAL'] },
    backups: Number,
    clusters: [{ type: ObjectId, ref: 'Cluster' }]
}));
