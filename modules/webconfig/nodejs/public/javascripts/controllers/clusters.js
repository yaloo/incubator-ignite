// Controller for clusters page.
configuratorModule.controller('clustersController', ['$scope', '$modal', '$http',
    function($scope, $modal, $http) {
        $scope.selectedItem = {};

        $scope.backupItem = {};

        $scope.templates = [
            {value: {discovery: 'TcpDiscoveryVmIpFinder', addresses: ['127.0.0.1:47500..47510']}, label: 'Local'},
            {value: {discovery: 'TcpDiscoveryMulticastIpFinder'}, label: 'Basic'}
        ];

        $scope.discoveries = [
            {value: 'TcpDiscoveryVmIpFinder', label: 'Static IPs'},
            {value: 'TcpDiscoveryMulticastIpFinder', label: 'Multicast'},
            {value: 'TcpDiscoveryS3IpFinder', label: 'AWS S3'},
            {value: 'TcpDiscoveryCloudIpFinder', label: 'Apache jclouds'},
            {value: 'TcpDiscoveryGoogleStorageIpFinder', label: 'Google Cloud Storage'},
            {value: 'TcpDiscoveryJdbcIpFinder', label: 'JDBC'},
            {value: 'TcpDiscoverySharedFsIpFinder', label: 'Shared Filesystem'}
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

                //$scope.clustersTable = new ngTableParams({
                //    page: 1,                    // Show first page.
                //    count: Number.MAX_VALUE,    // Count per page.
                //    sorting: {name: 'asc'}      // Initial sorting.
                //}, {
                //    total: $scope.clusters.length, // Length of data.
                //    counts: [],
                //    getData: function($defer, params) {
                //        // Use build-in angular filter.
                //        var orderedData = params.sorting() ?
                //            $filter('orderBy')($scope.clusters, params.orderBy()) :
                //            $scope.clusters;
                //
                //        var page = params.page();
                //        var cnt = params.count();
                //
                //        $defer.resolve(orderedData.slice(page - 1 * cnt, page * cnt));
                //    }
                //});
            });

        // Create popup for tcpDiscoveryVmIpFinder advanced settings.
        var staticIpsModal = $modal({scope: $scope, template: '/staticIps', show: false});

        //$scope.editStaticIps = function(cluster) {
        //    $scope.staticIpsTable = new ngTableParams({
        //        page: 1,                    // Show first page.
        //        count: Number.MAX_VALUE     // Count per page.
        //    }, {
        //        total: cluster.addresses.length, // Length of data.
        //        counts: [],
        //        getData: function($defer, params) {
        //            var addresses = cluster.addresses;
        //
        //            var page = params.page();
        //            var cnt = params.count();
        //
        //            $defer.resolve(addresses.slice(page - 1 * cnt, page * cnt));
        //        }
        //    });
        //
        //    staticIpsModal.$promise.then(staticIpsModal.show);
        //};

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

        //function revertSelectedItem() {
        //    if ($scope.selectedItem) {
        //        //$scope.clusters[$scope.clusters.indexOf($scope.currentCluster)] = $scope.editCluster;
        //        //
        //        //$scope.currentCluster = undefined;
        //        //
        //        //$scope.editColumn = undefined;
        //        //
        //        //$scope.clustersTable.reload();
        //    }
        //};

        $scope.selectItem = function(item) {
            console.log(item);

            //revertSelectedItem();

            $scope.selectedItem = item;

            //$scope.backupItem = angular.copy(item);
        };

        // Add new cluster.
        $scope.createItem = function() {
            var item = angular.copy($scope.create.template);

            item.name = $scope.create.name;
            item.space = $scope.spaces[0]._id;

            $scope.create = {};

            saveCluster(item);
        };

        // Remove new cluster.
        $scope.removeItem = function(_id) {
            $http.post('/rest/clusters/remove', {_id: _id})
                .success(function(data) {
                    $scope.spaces = data.spaces;
                    $scope.clusters = data.clusters;

                    $scope.clustersTable.reload();
                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        };

        // Save cluster in db.
        function saveCluster(cluster) {
            $http.post('/rest/clusters/save', cluster)
                .success(function(data) {
                    $scope.spaces = data.spaces;
                    $scope.clusters = data.clusters;
                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        }
    }]
);