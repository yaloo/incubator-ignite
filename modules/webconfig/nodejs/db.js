// mongoose for mongodb
var mongoose = require('mongoose');

// connect to mongoDB database on modulus.io
var uri = 'mongodb://localhost/web-configurator';

mongoose.connect(uri, {server: {poolSize: 4}});

// define model
var cacheSchema = new mongoose.Schema({
    name: String,
    mode: { type: String, enum: ['PARTITIONED', 'REPLICATED', 'LOCAL'] },
    backups: Number
});

exports.Cache =  mongoose.model('Cache', cacheSchema);

var clusterSchema = new mongoose.Schema({
    name : String,
    caches : [String],
    discovery : { type: String, enum: ['VM', 'Multicast'] },
    addresses : [String]
});

exports.Cluster =  mongoose.model('Cluster', clusterSchema);