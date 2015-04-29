/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
var IntegrationManagementCtrl = function($scope, $http, $q, $timeout,$parse, sdUtilService, sdViewUtilService, eventBus) {
	this.camelContextDataTable = null;
	this.selectionCamelContext;

	this.producerRoutesDataTable = null;
	this.producerRoutesTable = null;
	this.consumerRoutesDataTable = null;
	this.otherRoutesDataTable = null;
	
	// Tab initialisation
	this.tab = 1;
	this.expandCamelContextTable = true;
	this.expandRoutesTable = true;
	
	this.camelContextsPageSize = 4;
	this.routesPageSize = 5;

	$scope.sortOn = function(data, params) {
		return data.sort(
				function( a, b ) {
					if (params.order[0].dir=="asc")
					{
						if ( eval('a.'+params.order[0].name)<= eval('b.'+params.order[0].name)) 
						{
							return( -1 );
						}
							return( 1 );

					}
					else if (params.order[0].dir=="desc")
					{
						if ( eval('a.'+params.order[0].name)>= eval('b.'+params.order[0].name)) 
						{
							return( -1 );
						}
							return( 1 );
					}
					else
					{
						return( 1 );
					}
				}
            );
    }
	
	function ajaxCamelContext(params, restUrl) {
		var deferred = $q.defer();
		var httpResponse = $http.get(restUrl);
		var fromCamelItem = parseInt(params.skip, 10);
		var toCamelItem = fromCamelItem + parseInt(params.pageSize, 10);
		httpResponse.success(function(data) {
		console.info('DataTable Event - params.order[0].name', params.order[0].name);
		var sortedCamelContexts = $scope.sortOn( data, params );
			var filtredCamelContexts = {};
			filtredCamelContexts.list = [];
			filtredCamelContexts.totalCount = sortedCamelContexts.length;
			if (sortedCamelContexts.length > 0) {
				for (count = fromCamelItem; count < toCamelItem; count++) {
					if (sortedCamelContexts.length > count) {
						filtredCamelContexts.list.push(sortedCamelContexts[count]);
					}
				}
			} else {
				filtredCamelContexts.list = [];
				filtredCamelContexts.totalCount = 0;
			}
			deferred.resolve(filtredCamelContexts);
			this.safeApply();
		}).error(function(data) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	this.onCamelContextSorting = function(info) {
		console.info('DataTable Event - onCamelContextSorting info = ', info);
	}
	
	this.camelContextsDataTableOnPagination = function(info) {
		console.info('DataTable Event - onPagination Daemons', info);
	}

	this.fetchCamelContexts = function(options) {
		var restUrl = 'services/rest/integration-management/contexts';
		return ajaxCamelContext(options, restUrl);
	}

	this.startAllRoutes = function(contextId) {
		var restStartRouteUrl = 'services/rest/integration-management/context/'	+ contextId + '/startAllRoutes';
		updateRoutesStatus(restStartRouteUrl);
		var currentTab = this.tab;
		this.setTab(0);
		$scope.$apply();
		if (currentTab === 1)
			this.refreshProducerRoutesTable();
		else if (currentTab === 2)
			this.refreshConsumerRoutesTable();
		else if (currentTab === 3)
			this.refreshOtherRoutesTable();
		this.setTab(currentTab);
	}

	this.stopAllRoutes = function(contextId) {
		var restStopRouteUrl = 'services/rest/integration-management/context/' + contextId + '/stopAllRoutes';
		updateRoutesStatus(restStopRouteUrl);
		var currentTab = this.tab;
		this.setTab(0);
		$scope.$apply();
		if (currentTab === 1)
			this.refreshProducerRoutesTable();
		else if (currentTab === 2)
			this.refreshConsumerRoutesTable();
		else if (currentTab === 3)
			this.refreshOtherRoutesTable();
		this.setTab(currentTab);
	}

	function ajaxRoutes(restUrl, params) {
		var filtredRoutes = {};
		var fromProducerRouteItem = parseInt(params.skip, 10);
		var toProducerRouteItem = fromProducerRouteItem	+ parseInt(params.pageSize, 10);
		var deferred = $q.defer();
		var httpResponse = $http.get(restUrl);
		httpResponse
				.success(
						function(data) {
						
							var sortedCamelContexts = $scope.sortOn( data, params );
							filtredRoutes.list = [];
							filtredRoutes.totalCount = sortedCamelContexts.length;
							if (sortedCamelContexts.length > 0) {
								for (count = fromProducerRouteItem; count < toProducerRouteItem; count++) {
									if (sortedCamelContexts.length > count) {
										filtredRoutes.list.push(sortedCamelContexts[count]);
									}
								}
							} else {
								filtredRoutes.list = [];
								filtredRoutes.totalCount = 0;
							}
							deferred.resolve(filtredRoutes);
							this.safeApply();
						}).error(function(data) {
					deferred.reject(data);
				});
		return deferred.promise;
	};

	this.fetchProducerRoutes = function(camelContext, params) {
		console.info("DataTable Event - fetchProducerRoutes params.skip= " + params.skip);
		console.info("DataTable Event - fetchProducerRoutes params.pageSize= " + params.pageSize);
		var restUrl = 'services/rest/integration-management/context/' + camelContext + '/routes/producers';
		console.info("camelContext = " + camelContext);
		console.info("selectedCamelContext in fetchProducerRoutes = " + this.selectedCamelContext);
		return ajaxRoutes(restUrl, params);
	}

	this.fetchConsumerRoutes = function(camelContext, params) {
		var restUrl = 'services/rest/integration-management/context/' + camelContext + '/routes/consumers';
		console.info("camelContext = " + camelContext);
		console.info("selectedCamelContext in fetchConsumerRoutes = " + this.selectedCamelContext);
		return ajaxRoutes(restUrl, params);
	}

	this.fetchOtherRoutes = function(camelContext, params) {
		var restUrl = 'services/rest/integration-management/context/' + camelContext + '/routes/others';
		console.info("camelContext = " + camelContext);
		console.info("selectedCamelContext in fetchOtherRoutes = " + this.selectedCamelContext);
		return ajaxRoutes(restUrl, params);
	}

	this.onCamelContextSelection = function(info) {
		var currentTab = this.tab;
		this.setTab(0);
		$scope.$apply();
		if (currentTab === 1)
			this.refreshProducerRoutesTable();
		else if (currentTab === 2)
			this.refreshConsumerRoutesTable();
		else if (currentTab === 3)
			this.refreshOtherRoutesTable();
		this.setTab(currentTab);
	}

	function updateRoutesStatus(restUrl) {
		var deferred = $q.defer();

		var httpResponse = $http.get(restUrl);
		httpResponse.success(function(data) {
			deferred.resolve(data);
			console.debug("Done success updateRoutesStatus");

		}).error(function(data) {
			deferred.reject(data);
		});

		return deferred.promise;
	};
	
	this.refresh = function() {
		this.camelContextDataTable.refresh(true);
	};

	this.refreshProducers = function() {
		var currentTab = this.tab;
		if (currentTab === 1)
			this.refreshProducerRoutesTable();
		else if (currentTab === 2)
			this.refreshConsumerRoutesTable();
		else if (currentTab === 3)
			this.refreshOtherRoutesTable();
	};
	this.refreshProducerRoutesTable = function() {
		this.producerRoutesDataTable.refresh(true);
	};

	this.refreshConsumers = function() {
		var currentTab = this.tab;
		if (currentTab === 1)
			this.refreshProducerRoutesTable();
		else if (currentTab === 2)
			this.refreshConsumerRoutesTable();
		else if (currentTab === 3)
			this.refreshOtherRoutesTable();
	};

	this.refreshConsumerRoutesTable = function() {
		this.consumerRoutesDataTable.refresh(true);
	};

	this.refreshOthers = function() {
		var currentTab = this.tab;
		if (currentTab === 1)
			this.refreshProducerRoutesTable();
		else if (currentTab === 2)
			this.refreshConsumerRoutesTable();
		else if (currentTab === 3)
			this.refreshOtherRoutesTable();
	};

	this.refreshOtherRoutesTable = function() {
		this.otherRoutesDataTable.refresh(true);
	};

	this.setTab = function(newValue) {
		this.tab = newValue;
	};

	this.isSet = function(tabName) {
		return this.tab === tabName;
	};

	this.showRouteDetails = function() {
		return this.selectionCamelContext.contextId === "";
	};

	this.producerRoutesDataTableOnPagination = function(info) {
		console.info('DataTable Event - onPagination Producers', info);

	}

	this.consumerRoutesDataTableOnPagination = function(info) {
		console.info('DataTable Event - onPagination Consumers', info);
	}

	this.otherRoutesDataTableOnPagination = function(info) {
		console.info('DataTable Event - onPagination Others', info);
	}

	this.stopEvent = function(event) {
		sdUtilService.stopEvent(event);
	};
	
	function invoqueRestService(restUrl) {
		var deferred = $q.defer();

		var httpResponse = $http.get(restUrl);
		httpResponse.success(function(data) {
			deferred.resolve(data);
		}).error(function(data) {
			deferred.reject(data);
		});

		return deferred.promise;
	};
	
	// Start and stop common for all types of routes
	this.startOrStopRoute = function(contextId, id, status) {
		if (contextId != "") {
			if ((status === 'Started') && (id != undefined)) {
				var restUrl = "services/rest/integration-management/context/" + contextId + "/route/" + id + "/stop";
				invoqueRestService(restUrl);
			} else if ((status === 'Stopped') && (id != undefined)) {
				var restUrl = "services/rest/integration-management/context/" + contextId + "/route/" + id + "/start";
				invoqueRestService(restUrl);
			}
			var currentTab = this.tab;
			$scope.$apply();
			if (currentTab === 1)
				this.refreshProducerRoutesTable();
			else if (currentTab === 2)
				this.refreshConsumerRoutesTable();
			else if (currentTab === 3)
				this.refreshOtherRoutesTable();

		}
	};

}
IntegrationManagementCtrl.$inject = [ '$scope', '$http', '$q', '$timeout','$parse', 'sdUtilService',  'sdViewUtilService', 'eventBus' ];