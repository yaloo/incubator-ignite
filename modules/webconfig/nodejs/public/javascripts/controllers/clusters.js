angular.module('ignite-web-configurator', ['ngTable', 'mgcrea.ngStrap', 'ngSanitize'])
    .controller('clusterRouter', ['$scope', '$modal', '$http', '$filter', 'ngTableParams', function($scope, $modal, $http, $filter, ngTableParams) {

        $scope.discoveries = [
            {value: 'VM', label: 'VM'},
            {value: 'Multicast', label: 'Multicast'}
        ];

        // when landing on the page, get all settings and show them
        $http.get('/rest/cluster')
            .success(function(data) {
                $scope.clusters = data;

                $scope.clustersTable = new ngTableParams({
                    page: 1,            // show first page
                    count: 10,          // count per page
                    sorting: {
                        name: 'asc'     // initial sorting
                    }
                }, {
                    total: $scope.clusters.length, // length of data
                    getData: function($defer, params) {
                        // use build-in angular filter
                        var orderedData = params.sorting() ?
                            $filter('orderBy')($scope.clusters, params.orderBy()) :
                            $scope.clusters;

                        $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
                    }
                });
            })
            .error(function(data) {
                $scope.text = data;
            });

        // Pre-fetch an external template populated with a custom scope
        var myOtherModal = $modal({scope: $scope, template: '/cluster/edit', show: false});

        $scope.submit = function() {
            var data = {
                _id: $scope.cluster._id,
                name: $scope.cluster.name,
                caches: ['cache1', 'cache2', 'cache2'],
                discovery: $scope.cluster.discovery,
                addresses: ['127.0.0.1', '192.168.1.1']
            };

            $http.post('/rest/cluster/save', data)
                .success(function(data) {
                    myOtherModal.hide();

                    $scope.clusters = data;

                    $scope.clustersTable.reload();
                })
                .error(function(data) {
                    console.log('Error: ' + data);
                });
        };

        $scope.create = function () {
            $scope.cluster = { discovery: 'VM' };

            // Show when some event occurs (use $promise property to ensure the template has been loaded)
            myOtherModal.$promise.then(myOtherModal.show);
        };

        $scope.edit = function (cluster) {
            $scope.cluster = JSON.parse(JSON.stringify(cluster));

            // Show when some event occurs (use $promise property to ensure the template has been loaded)
            myOtherModal.$promise.then(myOtherModal.show);
        };

        $scope.delete = function (cluster) {
            $http.post('/rest/cluster/remove', { _id: cluster._id })
                .success(function(data) {
                    $scope.clusters = data;

                    $scope.clustersTable.reload();
                })
                .error(function(data) {
                    console.log('Error: ' + data);
                });
        };
    }]);