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
        var staticIpsModal = $modal({scope: $scope, template: '/staticIps', show: false});

        $scope.editStaticIps = function(cluster) {
            $scope.staticIpsTable = new ngTableParams({
                page: 1,                    // Show first page.
                count: Number.MAX_VALUE     // Count per page.
            }, {
                total: cluster.addresses.length, // Length of data.
                counts: [],
                getData: function($defer, params) {
                    var addresses = cluster.addresses;

                    var page = params.page();
                    var cnt = params.count();

                    $defer.resolve(addresses.slice(page - 1 * cnt, page * cnt));
                }
            });

            staticIpsModal.$promise.then(staticIpsModal.show);
        };

        // Add new cluster.
        $scope.addStaticIp = function(cluster) {
            cluster.push({space: $scope.spaces[0]._id, discovery: 'TcpDiscoveryVmIpFinder'});

            $scope.clustersTable.reload();
        };

        $scope.beginEditStaticIp = function(address) {
            $scope.revertStaticIp();

            $scope.editAddress = angular.copy(address);
        };

        // Create popup for tcpDiscoveryMulticastIpFinder advanced settings.
        var multicastModal = $modal({scope: $scope, template: '/staticIps', show: false});

        $scope.editMulticast = function(cluster) {
            multicastModal.$promise.then(multicastModal.show);
        };

        // Add new cluster.
        $scope.addCluster = function() {
            $scope.clusters.push({space: $scope.spaces[0]._id, discovery: 'TcpDiscoveryVmIpFinder'});

            $scope.clustersTable.reload();
        };

        $scope.beginEditCluster = function(column, cluster) {
            $scope.revertCluster();

            $scope.currentCluster = cluster;

            $scope.editColumn = column;

            $scope.editCluster = angular.copy(cluster);
        };

        $scope.revertCluster = function() {
            if ($scope.editColumn && $scope.currentCluster) {
                $scope.clusters[$scope.clusters.indexOf($scope.currentCluster)] = $scope.editCluster;

                $scope.currentCluster = undefined;

                $scope.editColumn = undefined;

                $scope.clustersTable.reload();
            }
        };

        $scope.submit = function() {
            if ($scope.editColumn && $scope.currentCluster) {
                var cluster = $scope.currentCluster;

                var data = {
                    _id: cluster._id,
                    space: cluster.space,
                    name: cluster.name,
                    discovery: cluster.discovery,
                    addresses: cluster.addresses
                };

                $scope.currentCluster = undefined;

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

        $scope.deleteCluster = function(cluster) {
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