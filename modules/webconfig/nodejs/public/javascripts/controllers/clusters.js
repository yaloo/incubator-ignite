configuratorModule.controller('clustersController', [ '$scope', '$modal', '$http', '$filter', 'ngTableParams',
    function ($scope, $modal, $http, $filter, ngTableParams) {
        $scope.edit = {};
        $scope.editRow = {};
        $scope.editIdx = false;

        $scope.discoveries = [
            {value: 'TcpDiscoveryVmIpFinder', label: 'Static IPs'},
            {value: 'TcpDiscoveryMulticastIpFinder', label: 'Multicast'}
        ];

        $scope.discoveryAsString = function (value) {
            for (var i in $scope.discoveries) {
                if ($scope.discoveries[i].value == value)
                    return $scope.discoveries[i].label;
            }

            return 'Wrong discovery';
        };

        // When landing on the page, get clusters and show them.
        $http.get('/rest/clusters')
            .success(function (data) {
                $scope.spaces = data.spaces;
                $scope.clusters = data.clusters;

                $scope.clustersTable = new ngTableParams({
                    page: 1,                    // Show first page.
                    count: Number.MAX_VALUE,    // Count per page.
                    sorting: {name: 'asc'}     // Initial sorting.
                }, {
                    total: $scope.clusters.length, // Length of data.
                    counts: [],
                    getData: function ($defer, params) {
                        // Use build-in angular filter.
                        var orderedData = params.sorting() ?
                            $filter('orderBy')($scope.clusters, params.orderBy()) :
                            $scope.clusters;

                        var page = params.page();
                        var cnt = params.count();

                        $defer.resolve(orderedData.slice(page - 1 * cnt, page * cnt));
                    }
                });
            });

        // Create popup for discovery advanced settings.
        var discoveryModal = $modal({scope: $scope, template: '/discovery', show: false});

        $scope.editDiscovery = function (cluster) {
            discoveryModal.$promise.then(discoveryModal.show);
        };

        $scope.submit = function () {
            if ($scope.editIdx !== false) {
                console.log($scope.$data);

                var cluster = $scope.clusters[$scope.editIdx];

                var data = {
                    _id: cluster._id,
                    space: cluster.space,
                    name: cluster.name,
                    discovery: cluster.discovery,
                    addresses: ['127.0.0.1', '192.168.1.1']
                };

                $scope.editIdx = false;

                $http.post('/rest/clusters/save', data)
                    .success(function (data) {
                        myOtherModal.hide();

                        $scope.spaces = data.spaces;
                        $scope.clusters = data.clusters;

                        $scope.clustersTable.reload();
                    })
                    .error(function (errorMessage) {
                        console.log('Error: ' + errorMessage);
                    });
            }
        };

        // Add new cluster.
        $scope.add = function () {
            // $scope.clusters.push({name: 'Cluster', discovery: 'TcpDiscoveryVmIpFinder'});
            $scope.clusters.push({space: $scope.spaces[0]._id});

            $scope.clustersTable.reload();
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
            $http.post('/rest/clusters/remove', {_id: cluster._id})
                .success(function (data) {
                    $scope.spaces = data.spaces;
                    $scope.clusters = data.clusters;

                    $scope.clustersTable.reload();
                })
                .error(function (errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        };
    }]
);