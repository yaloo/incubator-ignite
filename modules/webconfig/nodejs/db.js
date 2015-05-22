var config = require('./configuration.js');

// mongoose for mongodb
var mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    ObjectId = mongoose.Types.ObjectId,
    passportLocalMongoose = require('passport-local-mongoose');

// connect to mongoDB database on modulus.io
mongoose.connect(config.get('mongoDB:url'), {server: {poolSize: 4}});

// define user model.
var AccountSchema = new Schema({
    email: String
});

AccountSchema.plugin(passportLocalMongoose);

exports.Account = mongoose.model('Account', AccountSchema);

// define cache model.
exports.Cache =  mongoose.model('Cache', new Schema({
    name: String,
    mode: { type: String, enum: ['PARTITIONED', 'REPLICATED', 'LOCAL'] },
    backups: Number
}));

// define cluster model.
exports.Cluster =  mongoose.model('Cluster', new Schema({
    name : String,
    caches : [String],
    discovery : { type: String, enum: ['TcpDiscoveryVmIpFinder', 'TcpDiscoveryMulticastIpFinder'] },
    addresses : [String]
}));
