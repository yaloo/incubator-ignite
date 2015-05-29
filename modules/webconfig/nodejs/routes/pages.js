var router = require('express').Router();

// GET dropdown-menu template.
router.get('/select', function(req, res) {
    res.render('templates/select', { });
});

/* GET login page. */
router.get('/login', function(req, res) {
    res.render('login');
});

/* GET page for discovery advanced settings. */
router.get('/discovery', function(req, res) {
    res.render('discovery');
});

/* GET register page. */
router.get('/register', function(req, res) {
    res.render('register');
});

/* GET home page. */
router.get('/', function(req, res) {
    if (req.isAuthenticated())
        res.redirect('/clusters');
    else
        res.render('index', { user: req.user });
});

/* GET clusters page. */
router.get('/clusters', function(req, res) {
    res.render('clusters', { user: req.user });
});

router.get('/discovery', function(req, res) {
    res.render('discovery');
});

/* GET advanced options for TcpDiscoveryVmIpFinder page. */
router.get('/tcpDiscoveryVmIpFinder', function(req, res) {
    res.render('tcpDiscoveryVmIpFinder');
});

/* GET advanced options for TcpDiscoveryMulticastIpFinder page. */
router.get('/tcpDiscoveryMulticastIpFinder', function(req, res) {
    res.render('tcpDiscoveryMulticastIpFinder');
});

/* GET caches page. */
router.get('/caches', function(req, res) {
    res.render('caches', { user: req.user });
});

/* GET persistence page. */
router.get('/persistence', function(req, res) {
    res.render('persistence', { user: req.user });
});

/* GET sql page. */
router.get('/sql', function(req, res) {
    res.render('sql', { user: req.user });
});

/* GET clients page. */
router.get('/clients', function(req, res) {
    res.render('clients', { user: req.user });
});

module.exports = router;
