var config = require('nconf');

config.file({'file': 'config/default.json'});

module.exports = config