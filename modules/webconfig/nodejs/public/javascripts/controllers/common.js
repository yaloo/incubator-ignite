var configuratorModule =  angular.module('ignite-web-configurator', ['ngTable', 'mgcrea.ngStrap']);

configuratorModule.controller('activeLink', ['$scope', function($scope) {
    $scope.isActive = function(path) {
        return window.location.pathname.substr(0, path.length) == path;
    };
}]);

configuratorModule.controller('auth', ['$scope', '$modal', '$http', function($scope, $modal, $http) {
    $scope.action = 'login';

    $scope.message = '';

    $scope.valid = false;

    // Pre-fetch an external template populated with a custom scope
    var authModal = $modal({scope: $scope, template: '/login', show: false});

    $scope.login = function() {
        // Show when some event occurs (use $promise property to ensure the template has been loaded)
        authModal.$promise.then(authModal.show);
    };

    $scope.auth = function(action, user_info) {
        if (action == 'signup') {
            $http.post('/rest/auth/register', user_info)
                .error(function (data) {
                    $scope.message = data;
                });
        }
    };
}]);