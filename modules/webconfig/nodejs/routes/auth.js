var passport = require('passport');
var router = require('express').Router();

var db = require('../db');

router.post('/register', function(req, res, next) {
    console.log('registering user');

    var account = new db.Account(req.body);

    db.Account.register(account, req.body.password, function(err) {
        if (err) {
            console.log('error while user register!', err);

            return next(err);
        }

        console.log('user registered!');

        passport.authenticate('local')(req, res, function () {


            res.redirect('/clusters');
        });
    });
});

router.post('/login', passport.authenticate('local', function(req, res) {
    req.redirect('/clusters');
}));
//
//router.post('/register', function(req, res, next) {
//    var user = new db.Account(req.body);
//
//    user.save(function(err) {
//        return err
//            ? next(err)
//            : req.logIn(user, function(err) {
//            return err
//                ? next(err)
//                : res.redirect('/clusters');
//        });
//    });
//});

router.get('/logout', function(req, res) {
    req.logout();

    res.redirect('/');
});

module.exports = router;