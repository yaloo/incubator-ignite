// Controller for clusters page.
configuratorModule.controller('clustersController', ['$scope', '$modal', '$http',
    function($scope, $modal, $http) {
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
            });

        $scope.selectItem = function(item) {
            $scope.backupItem = item.isSelected ? angular.copy(item) : undefined;
        };

        // Add new cluster.
        $scope.createItem = function() {
            var item = angular.copy($scope.create.template);

            item.name = $scope.create.name;
            item.space = $scope.spaces[0]._id;

            $scope.create = {};

            $scope.saveCluster(item);
        };

        // Remove new cluster.
        $scope.removeItem = function(_id) {
            $http.post('/rest/clusters/remove', {_id: _id})
                .success(function(data) {
                    $scope.spaces = data.spaces;
                    $scope.clusters = data.clusters;
                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        };

        // Save cluster in db.
        $scope.saveCluster = function (cluster) {
            $http.post('/rest/clusters/save', cluster)
                .success(function(data) {
                    $scope.spaces = data.spaces;

                    $scope.clusters = data.clusters;

                    $scope.backupItem = undefined;
                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        }
    }]
);