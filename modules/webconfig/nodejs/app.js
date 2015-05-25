var flash = require('connect-flash');
var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var session = require('express-session');

var pageRoutes = require('./routes/pages');
var clustersRouter = require('./routes/clusters');
var authRouter = require('./routes/auth');

var passport = require('passport');

var db = require('./db');

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// Site favicon
app.use(favicon(__dirname + '/public/favicon.ico'));

app.use(logger('dev'));

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));

app.use(require('less-middleware')(path.join(__dirname, 'public')));

app.use(express.static(path.join(__dirname, 'public')));

app.use(cookieParser('keyboard cat'));

app.use(session({
    secret: 'keyboard cat',
    resave: false,
    saveUninitialized: true
}));

app.use(flash());

app.use(passport.initialize());
app.use(passport.session());

passport.serializeUser(db.Account.serializeUser());
passport.deserializeUser(db.Account.deserializeUser());

passport.use(db.Account.createStrategy());

var mustAuthenticated = function (req, res, next) {
    console.log('isAuthenticated = ' + req.isAuthenticated());

    req.isAuthenticated() ? next() : res.redirect('/');
};

app.all('/clusters', mustAuthenticated);

app.use('/', pageRoutes);
app.use('/rest/clusters', clustersRouter);
app.use('/rest/auth', authRouter);

// catch 404 and forward to error handler
//app.use(function (req, res, next) {
//    var err = new Error('Not Found');
//    err.status = 404;
//    next(err);
//});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
    app.use(function (err, req, res, next) {
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: err
        });
    });
}

// production error handler
// no stacktraces leaked to user
app.use(function (err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
        message: err.message,
        error: {}
    });
});

module.exports = app;
