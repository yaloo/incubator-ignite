var passport = require('passport');
var router = require('express').Router();

var db = require('../db');

var loginCallback = function(req, res, next) {
    return function(err, user) {
        if (err)
            return res.status(400).send(err.message);

        if (!user)
            return res.status(400).send('Account with this email not exist.');

        req.logIn(user, {}, function(err) {
            if (err)
                return res.status(400).send(err.message);

            res.redirect('/clusters');
        });
    };
};

router.post('/register', function(req, res, next) {
    var account = new db.Account(req.body);

    db.Account.register(account, req.body.password, loginCallback(req, res, next));
});

router.post('/login', function(req, res, next) {
    passport.authenticate('local', loginCallback(req, res, next))(req, res, next);
});

router.get('/logout', function(req, res) {
    req.logout();

    res.redirect('/');
});

module.exports = router;