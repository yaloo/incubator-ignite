var passport = require('passport');
var router = require('express').Router();

var db = require('../db');

/**
 * Register new account.
 */
router.post('/register', function(req, res, next) {
    db.Account.register(new db.Account(req.body), req.body.password, function(err, account) {
        if (err)
            return res.status(401).send(err.message);

        if (!account)
            return res.status(500).send('Failed to create account.');

        new db.Space({name: 'Personal space', owner: account._id}).save();

        req.logIn(account, {}, function(err) {
            if (err)
                return res.status(401).send(err.message);

            return res.redirect('/clusters');
        });
    });
});

/**
 * Login in exist account.
 */
router.post('/login', function(req, res, next) {
    passport.authenticate('local', function(err, user) {
        if (err)
            return res.status(401).send(err.message);

        if (!user)
            return res.status(401).send('Account with this email not exist.');

        req.logIn(user, {}, function(err) {
            if (err)
                return res.status(401).send(err.message);

            res.redirect('/clusters');
        });
    })(req, res, next);
});

/**
 * Logout.
 */
router.get('/logout', function(req, res) {
    req.logout();

    res.redirect('/');
});

module.exports = router;