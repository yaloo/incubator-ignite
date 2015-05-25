var jade = require('jade');
var express = require('express');
var router = express.Router();

/* GET login page. */
router.get('/login', function(req, res) {
    res.render('login');
});

/* GET register page. */
router.get('/register', function(req, res) {
    res.render('register');
});

/* GET home page. */
router.get('/', function(req, res) {
    res.render('index', { user: req.user, messages: req.flash('info') });
});

/* GET clusters page. */
router.get('/clusters', function(req, res) {
    res.render('clusters', { user: req.user });
});

/* GET cluster edit popup. */
router.get('/cluster/edit', function(req, res) {
    res.render('clusterEdit', {});
});

/* GET caches page. */
router.get('/caches', function(req, res) {
    res.render('caches', { });
});

/* GET persistence page. */
router.get('/persistence', function(req, res) {
    res.render('persistence', { });
});

/* GET sql page. */
router.get('/sql', function(req, res) {
    res.render('sql', { });
});

/* GET clients page. */
router.get('/clients', function(req, res) {
    res.render('clients', { });
});

// GET dropdown-menu template.
router.get('/select', function(req, res) {
    res.render('templates/select', { });
});

module.exports = router;
