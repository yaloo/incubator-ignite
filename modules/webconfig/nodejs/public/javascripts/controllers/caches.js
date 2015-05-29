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

configuratorModule.controller('cachesController', ['$scope', '$modal', '$http', '$filter', 'ngTableParams',
    function($scope, $modal, $http, $filter, ngTableParams) {
        $scope.editColumn = {};

        $scope.editCache = {};

        $scope.modes = [
            {value: 'PARTITIONED', label: 'PARTITIONED'},
            {value: 'REPLICATED', label: 'REPLICATED'},
            {value: 'LOCAL', label: 'LOCAL'}
        ];

        $scope.atomicities = [
            {value: 'ATOMIC', label: 'ATOMIC'},
            {value: 'TRANSACTIONAL', label: 'TRANSACTIONAL'}
        ];

        // When landing on the page, get caches and show them.
        $http.get('/rest/caches')
            .success(function(data) {
                $scope.spaces = data.spaces;
                $scope.caches = data.caches;

                $scope.cachesTable = new ngTableParams({
                    page: 1,                    // Show first page.
                    count: Number.MAX_VALUE,    // Count per page.
                    sorting: {name: 'asc'}      // Initial sorting.
                }, {
                    total: $scope.caches.length, // Length of data.
                    counts: [],
                    getData: function($defer, params) {
                        // Use build-in angular filter.
                        var orderedData = params.sorting() ?
                            $filter('orderBy')($scope.caches, params.orderBy()) :
                            $scope.caches;

                        var page = params.page();
                        var cnt = params.count();

                        $defer.resolve(orderedData.slice(page - 1 * cnt, page * cnt));
                    }
                });
            });

        // Add new cache.
        $scope.addCache = function() {
            $scope.caches.push({space: $scope.spaces[0]._id, mode: 'PARTITIONED', backups: 1, atomicity: 'ATOMIC'});

            $scope.cachesTable.reload();
        };

        $scope.beginEditCache = function(column, cache) {
            $scope.revertCache();

            $scope.currentCache = cache;

            $scope.editColumn = column;

            $scope.editCache = angular.copy(cache);
        };

        $scope.revertCache = function() {
            if ($scope.editColumn && $scope.currentCache) {
                $scope.caches[$scope.caches.indexOf($scope.currentCache)] = $scope.editCache;

                $scope.currentCache = undefined;

                $scope.editColumn = undefined;

                $scope.cachesTable.reload();
            }
        };

        $scope.submit = function() {
            if ($scope.editColumn && $scope.currentCache) {
                var cache = $scope.currentCache;

                var data = {
                    _id: cache._id,
                    space: cache.space,
                    name: cache.name,
                    mode: cache.mode,
                    backups: cache.backups,
                    atomicity: cache.atomicity
                };

                $scope.currentCache = undefined;

                $scope.editColumn = undefined;

                $http.post('/rest/caches/save', data)
                    .success(function(data) {
                        $scope.spaces = data.spaces;
                        $scope.caches = data.caches;

                        $scope.cachesTable.reload();
                    })
                    .error(function(errorMessage) {
                        console.log('Error: ' + errorMessage);
                    });
            }
        };

        $scope.deleteCache = function(cache) {
            $http.post('/rest/caches/remove', {_id: cache._id})
                .success(function(data) {
                    $scope.spaces = data.spaces;
                    $scope.caches = data.caches;

                    $scope.cachesTable.reload();
                })
                .error(function(errorMessage) {
                    console.log('Error: ' + errorMessage);
                });
        };
    }]
);