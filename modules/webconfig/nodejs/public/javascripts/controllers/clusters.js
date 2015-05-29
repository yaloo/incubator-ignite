/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            for (var i = 0; i < $scope.discoveries.length; i++) {
                var discovery = $scope.discoveries[i];

                if (discovery.value == value)
                    return discovery.label;
            }

            return 'Wrong discovery';
        };

        // Create popup for discovery advanced settings.
        var discoveryModal = $modal({scope: $scope, template: '/discovery', show: false});

        $scope.editDiscovery = function(cluster) {
            discoveryModal.$promise.then(discoveryModal.show);
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