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

configuratorModule.controller('clustersController', ['$scope', '$modal', '$http', function($scope, $modal, $http) {
        $scope.templates = [
            {value: {discovery: {kind: 'Vm', addresses: ['127.0.0.1:47500..47510']}}, label: 'Local'},
            {value: {discovery: {kind: 'Multicast'}}, label: 'Basic'}
        ];

        $scope.discoveries = [
            {value: 'Vm', label: 'Static IPs'},
            {value: 'Multicast', label: 'Multicast'},
            {value: 'S3', label: 'AWS S3'},
            {value: 'Cloud', label: 'Apache jclouds'},
            {value: 'GoogleStorage', label: 'Google Cloud Storage'},
            {value: 'Jdbc', label: 'JDBC'},
            {value: 'SharedFs', label: 'Shared Filesystem'}
        ];

        $scope.clusters = [];

        $scope.discoveryAsString = function(value) {
            var discovery = $scope.discoveries.find(function(discovery) {
                return discovery.value == value;
            });

            return discovery ? discovery.label : 'Wrong discovery';
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
            $scope.selectedCluster = item;

            $scope.backupItem = angular.copy(item);
        };

        // Add new cluster.
        $scope.createItem = function() {
            var item = angular.copy($scope.create.template);

            item.name = 'Cluster ' + ($scope.clusters.length + 1);
            item.space = $scope.spaces[0]._id;

            $http.post('/rest/clusters/save', item)
                .success(function(_id) {
                    item._id = _id;

                    $scope.clusters.push(item);
                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        };

        // Remove new cluster.
        $scope.removeItem = function(_id) {
            $http.post('/rest/clusters/remove', {_id: _id})
                .success(function(data) {
                    for (var i = 0; i < $scope.clusters.length; i++) {
                        if ($scope.clusters[i]._id == _id) {
                            $scope.clusters.slice(i, 1);

                            break;
                        }
                    }

                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        };

        // Save cluster in db.
        $scope.saveCluster = function (cluster) {
            //console.log(cluster);

            $http.post('/rest/clusters/save', cluster)
                .success(function() {
                    for (var i = 0; i < $scope.clusters.length; i++) {
                        if ($scope.clusters[i]._id == cluster._id) {
                            console.log($scope.clusters[i]);

                            $scope.clusters[i] = angular.copy(cluster);

                            break;
                        }
                    }
                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        };
    }]
);