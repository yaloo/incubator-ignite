// Controller for clusters page.
configuratorModule.controller('clustersController', ['$scope', '$modal', '$http', '$filter', 'ngTableParams',
    function($scope, $modal, $http, $filter, ngTableParams) {
        $scope.editColumn = {};

        $scope.editCluster = {};

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

        // When landing on the page, get clusters and show them.
        $http.get('/rest/clusters')
            .success(function(data) {
                $scope.spaces = data.spaces;
                $scope.clusters = data.clusters;

                $scope.clustersTable = new ngTableParams({
                    page: 1,                    // Show first page.
                    count: Number.MAX_VALUE,    // Count per page.
                    sorting: {name: 'asc'}      // Initial sorting.
                }, {
                    total: $scope.clusters.length, // Length of data.
                    counts: [],
                    getData: function($defer, params) {
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

        // Create popup for tcpDiscoveryVmIpFinder advanced settings.
        var tcpDiscoveryVmIpFinderModal = $modal({scope: $scope, template: '/tcpDiscoveryVmIpFinder', show: false});

        $scope.editTcpDiscoveryVmIpFinder = function(cluster) {
            tcpDiscoveryVmIpFinderModal.$promise.then(tcpDiscoveryVmIpFinderModal.show);
        };

        $scope.tcpDiscoveryVmIpFinderTable = new ngTableParams({
            page: 1,                    // Show first page.
            count: Number.MAX_VALUE,    // Count per page.
        }, {
            total: $scope.editCluster.addresses.length, // Length of data.
            counts: [],
            getData: function($defer, params) {
                var addresses = $scope.editCluster.addresses;

                var page = params.page();
                var cnt = params.count();

                $defer.resolve(addresses.slice(page - 1 * cnt, page * cnt));
            }
        });

        // Create popup for tcpDiscoveryMulticastIpFinder advanced settings.
        var tcpDiscoveryMulticastIpFinder = $modal({scope: $scope, template: '/tcpDiscoveryMulticastIpFinder', show: false});

        $scope.editTcpDiscoveryMulticastIpFinder = function(cluster) {
            tcpDiscoveryMulticastIpFinderModal.$promise.then(tcpDiscoveryMulticastIpFinderModal.show);
        };

        // Add new cluster.
        $scope.add = function() {
            $scope.clusters.push({space: $scope.spaces[0]._id, discovery: 'TcpDiscoveryVmIpFinder'});

            $scope.clustersTable.reload();
        };

        $scope.beginEdit = function(name, cluster) {
            $scope.revert();

            $scope.currentRow = cluster;

            $scope.editColumn = name;

            $scope.editCluster = angular.copy(cluster);

            $scope.editIdx = $scope.clusters.indexOf(cluster);
        };

        $scope.revert = function() {
            if ($scope.editColumn && $scope.currentRow) {
                $scope.clusters[$scope.clusters.indexOf($scope.currentRow)] = $scope.editCluster;

                $scope.currentRow = undefined;

                $scope.editColumn = undefined;

                $scope.clustersTable.reload();
            }
        };

        $scope.submit = function() {
            if ($scope.editColumn && $scope.currentRow) {
                var cluster = $scope.currentRow;

                var data = {
                    _id: cluster._id,
                    space: cluster.space,
                    name: cluster.name,
                    discovery: cluster.discovery,
                    addresses: ['127.0.0.1', '192.168.1.1']
                };

                $scope.currentRow = undefined;

                $scope.editColumn = undefined;

                $http.post('/rest/clusters/save', data)
                    .success(function(data) {
                        $scope.spaces = data.spaces;
                        $scope.clusters = data.clusters;

                        $scope.clustersTable.reload();
                    })
                    .error(function(errorMessage) {
                        console.log('Error: ' + errorMessage);
                    });
            }
        };

        $scope.delete = function(cluster) {
            $http.post('/rest/clusters/remove', {_id: cluster._id})
                .success(function(data) {
                    $scope.spaces = data.spaces;
                    $scope.clusters = data.clusters;

                    $scope.clustersTable.reload();
                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        };
    }]
);