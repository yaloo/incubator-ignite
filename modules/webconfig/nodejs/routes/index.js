var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Dashboard' });
});

/* GET home page. */
router.get('/cluster', function(req, res, next) {
    res.render('cluster', { });
});

module.exports = router;
