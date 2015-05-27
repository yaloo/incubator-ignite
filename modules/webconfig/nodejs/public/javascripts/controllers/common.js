var configuratorModule =  angular.module('ignite-web-configurator', ['ngTable', 'mgcrea.ngStrap']);

configuratorModule.controller('activeLink', ['$scope', function($scope) {
    $scope.isActive = function(path) {
        return window.location.pathname.substr(0, path.length) == path;
    };
}]);

configuratorModule.controller('auth', ['$scope', '$modal', '$http', '$window', function($scope, $modal, $http, $window) {
    $scope.action = 'login';

    $scope.errorMessage = '';

    $scope.valid = false;

    // Pre-fetch an external template populated with a custom scope
    var authModal = $modal({scope: $scope, template: '/login', show: false});

    $scope.login = function() {
        // Show when some event occurs (use $promise property to ensure the template has been loaded)
        authModal.$promise.then(authModal.show);
    };

    $scope.auth = function(action, user_info) {
        $http.post('/rest/auth/' + action, user_info)
            .success(function(data) {
                authModal.hide();

                $window.location = '/clusters';
            })
            .error(function (data) {
                $scope.errorMessage = data;
            });
    };
}]);