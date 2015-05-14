var jade = require('jade');
var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res) {
  res.render('index', { title: 'Dashboard' });
});

/* GET cluster page. */
router.get('/cluster', function(req, res) {
    res.render('cluster', { });
});

/* GET cluster edit popup. */
router.get('/cluster/edit', function(req, res) {
    res.render('clusterEdit', { });
});

module.exports = router;
