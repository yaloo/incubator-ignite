// mongoose for mongodb
var mongoose = require('mongoose');

// connect to mongoDB database on modulus.io
var uri = 'mongodb://localhost/web-configurator';

mongoose.connect(uri, {server: {poolSize: 4}});

// define model
exports.Cluster =  mongoose.model('Cluster', {
    name : String
});