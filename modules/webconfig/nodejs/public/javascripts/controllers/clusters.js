configuratorModule.controller('clustersController', ['$scope', '$modal', '$http', '$filter', 'ngTableParams', function($scope, $modal, $http, $filter, ngTableParams) {
    $scope.edit = { };

    $scope.editRow = {};
    $scope.editIdx = false;

    $scope.discoveries = [
        {value: 'TcpDiscoveryVmIpFinder', label: 'Static IPs'},
        {value: 'TcpDiscoveryMulticastIpFinder', label: 'Multicast'}
    ];

    $scope.discoveryAsString = function(value) {
        for (var i in $scope.discoveries) {
            if ($scope.discoveries[i].value == value)
                return $scope.discoveries[i].label;
        }

        return 'Wrong discovery';
    };

    // when landing on the page, get all settings and show them
    $http.get('/rest/clusters')
        .success(function(data) {
            $scope.clusters = data;

            $scope.clustersTable = new ngTableParams({
                page: 1,                    // show first page
                count: Number.MAX_VALUE,        // count per page
                sorting: {
                    name: 'asc'             // initial sorting
                }
            }, {
                total: $scope.clusters.length, // length of data
                counts: [],
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
        if ($scope.editIdx !== false) {
            var cluster = $scope.clusters[$scope.editIdx];

            var data = {
                _id: cluster._id,
                name: cluster.name,
                discovery: cluster.discovery,
                addresses: ['127.0.0.1', '192.168.1.1']
            };

            $scope.editIdx = false;

            $http.post('/rest/clusters/save', data)
                .success(function (data) {
                    myOtherModal.hide();

                    $scope.clusters = data;

                    $scope.clustersTable.reload();
                })
                .error(function (data) {
                    console.log('Error: ' + data);
                });
        }
    };

    $scope.add = function () {
        $scope.clusters.push({});

        $scope.clustersTable.reload();

        // Show when some event occurs (use $promise property to ensure the template has been loaded)
        //myOtherModal.$promise.then(myOtherModal.show);
    };

    $scope.beginEdit = function (cluster) {
        $scope.editIdx = $scope.clusters.indexOf(cluster);

        $scope.editRow = angular.copy(cluster);

        //// Show when some event occurs (use $promise property to ensure the template has been loaded)
        //myOtherModal.$promise.then(myOtherModal.show);
    };

    $scope.revert = function () {
        if ($scope.editIdx !== false) {
            $scope.clusters[$scope.editIdx] = $scope.editRow;

            $scope.editIdx = false;

            $scope.clustersTable.reload();
        }

        //// Show when some event occurs (use $promise property to ensure the template has been loaded)
        //myOtherModal.$promise.then(myOtherModal.show);
    };

    $scope.delete = function (cluster) {
        $http.post('/rest/clusters/remove', { _id: cluster._id })
            .success(function(data) {
                $scope.clusters = data;

                $scope.clustersTable.reload();
            })
            .error(function(data) {
                console.log('Error: ' + data);
            });
    };
}]);