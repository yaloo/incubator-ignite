var db = require('../db');

var express = require('express');
var router = express.Router();

router.get('/cluster', function(req, res) {
    db.Cluster.find(function(err, clusters) {
        // if there is an error retrieving, send the error. nothing after res.send(err) will execute
        if (err)
            res.send(err);

        res.json(clusters); // return all clusters in JSON format
    });
});

router.post('/cluster/save', function(req, res) {
    db.Cluster.create({
        name: req.body.name
    }, function (err, todo) {
        if (err)
            res.send(err);
    });
});

//router.get('rest/cluster', function(req, res) {
//    res.render('cluster', { });
//});

module.exports = router;