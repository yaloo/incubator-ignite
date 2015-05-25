var passport = require('passport');
var router = require('express').Router();

var db = require('../db');

router.post('/register', function(req, res, next) {
    console.log('registering user');

    var account = new db.Account(req.body);

    db.Account.register(account, req.body.password, function(err, user) {
        if (err)
            return next(err);

        req.logIn(user, {}, function(err) {
            if (err)
                return next(err);

            res.send(user);
        });
    });
});

router.post('/login', passport.authenticate('local', function(req, res) {
    req.redirect('/clusters');
}));

router.get('/logout', function(req, res) {
    req.logout();

    res.redirect('/');
});

module.exports = router;