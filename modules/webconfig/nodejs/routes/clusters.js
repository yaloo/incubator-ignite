var router = require('express').Router();
var db = require('../db');

/**
 * Send spaces and clusters accessed for user account.
 *
 * @param req Request.
 * @param res Response.
 */
function selectAll(req, res) {
    var user_id = req.user._id;

    // Get owned space and all accessed space.
    db.Space.find({$or: [{owner: user_id}, {usedBy: {$elemMatch: {account: user_id}}}]}, function (err, spaces) {
        if (err)
            return res.status(500).send(err);

        var space_ids = spaces.map(function(value, index) {
            return value._id;
        });

        // Get all clusters for spaces.
        db.Cluster.find({space: {$in: space_ids}}, function (err, clusters) {
            if (err)
                return res.status(500).send(err);

            res.json({spaces: spaces, clusters: clusters});
        });
    });
}

/**
 * Get spaces and clusters accessed for user account.
 */
router.get('/', function(req, res) {
    selectAll(req, res);
});

/**
 * Save cluster.
 */
router.post('/save', function(req, res) {
    db.upsert(db.Cluster, req.body, function(err) {
        if (err)
            return res.status(500).send(err);

        selectAll(req, res);
    });
});

/**
 * Remove cluster by ._id.
 */
router.post('/remove', function(req, res) {
    db.Cluster.remove(req.body, function (err) {
        if (err)
            return res.send(err);

        selectAll(req, res);
    })
});

module.exports = router;