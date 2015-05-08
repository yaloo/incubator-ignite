angular.module('ignite-web-configurator', [])
    .controller('cluster', ['$scope', '$http', function($scope, $http) {
        $scope.clusters = [{"name": "n1", "discovery": "d1"}, {"name": "n2", "discovery": "d2"}];;

        $scope.list = [];

        // when landing on the page, get all settings and show them
        $http.get('/rest/cluster')
            .success(function(data) {
                $scope.text = data;
            })
            .error(function(data) {
                $scope.text = data;
            });

        $scope.submit = function() {
            var json = { "name" : $scope.text };

            $http.post('/rest/cluster/save', json)
                .success(function(data) {
                    console.log(data);
                })
                .error(function(data) {
                    console.log('Error: ' + data);
                });
        }
    }]);