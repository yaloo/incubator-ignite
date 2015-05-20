angular.module('ignite-web-configurator', [])
    .controller('activeLink', ['$scope', function($scope) {
        $scope.isActive = function(path) {
            return window.location.pathname.substr(0, path.length) == path;
        };
    }]);