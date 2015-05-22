var express = require('express');
var router = express.Router();

var db = require('../db');

function selectAll(res) {
    db.Cluster.find(function(err, clusters) {
        // if there is an error retrieving, send the error. nothing after res.send(err) will execute
        if (err)
            res.send(err);
        else
            res.json(clusters); // return all clusters in JSON format
    });
}

router.get('/', function(req, res) {
    selectAll(res);
});

router.post('/save', function(req, res) {
    if (req.body._id) {
        var clusterId = req.body._id;

        delete req.body._id;

        db.Cluster.findByIdAndUpdate(clusterId, req.body, null, function(err) {
            if (err)
                res.send(err);
            else
                selectAll(res);
        });
    }
    else {
        var cluster = new db.Cluster(req.body);

        cluster.save(function (err) {
            if (err)
                res.send(err);
            else
                selectAll(res);
        });
    }
});

router.post('/remove', function(req, res) {
    db.Cluster.remove(req.body, function (err) {
        if (err)
            res.send(err);
        else
            selectAll(res);
    })
});

module.exports = router;