var settings = require('./settings.js');

// mongoose for mongodb
var mongoose = require('mongoose');

// connect to mongoDB database on modulus.io
mongoose.connect(settings.url, {server: {poolSize: 4}});

// define user model.
exports.User = mongoose.model('User', new mongoose.Schema({
    username: String,
    password: String,
    email: String
}));

// define cache model.
exports.Cache =  mongoose.model('Cache', new mongoose.Schema({
    name: String,
    mode: { type: String, enum: ['PARTITIONED', 'REPLICATED', 'LOCAL'] },
    backups: Number
}));

// define cluster model.
exports.Cluster =  mongoose.model('Cluster', new mongoose.Schema({
    name : String,
    caches : [String],
    discovery : { type: String, enum: ['TcpDiscoveryVmIpFinder', 'TcpDiscoveryMulticastIpFinder'] },
    addresses : [String]
}));