function integrationManagementController($scope, $http) {

	// variable for camel contexts
	$scope.itemsPerPage = 5;
	$scope.currentPage = 0;
	$scope.items = [];
	$scope.startItem = 0;
	$scope.filtredCamelContexts = [];

	// variable for application producers
	$scope.currentPageProducerRoute = 0;
	$scope.startItemProducerRoute = 0;
	$scope.filtredProducerRoute = [];

	// variable for application consumers
	$scope.currentPageConsumerRoute = 0;
	$scope.startItemConsumerRoute = 0;
	$scope.filtredConsumerRoute = [];

	// init search value
	// camel context table variables
	$scope.searchKeywordInCamelContext = "";
	$scope.filtredCamelContextsListForPagination = [];
	// camel producer routes table variables
	$scope.searchKeywordInProducerApplication = "";
	$scope.filtredApplicationProducersListForPagination = [];
	// camel application consumer routes table variables
	$scope.searchKeywordInConsumer = "";
	$scope.filtredConsumersListForPagination = [];

	$scope.showRouteDeatilsTable = false;
	$scope.selectedConsumerRoute = null;
	$scope.showDetailsConsumerRoute = false;
	$scope.selectedConsumerRouteId ="";
	
	/// selected Producer Route 
	$scope.selectedProducerRoute = "";
	$scope.selectedProducerRouteId ="";
	$scope.showDetailsProducerRoute = false;	
	
	
	
	$scope.cleanSelectedRoutesDetails = function() {
		// clean Consumer Routes details table
		$scope.selectedConsumerRoute ="";
		$scope.selectedConsumerRouteId ="";
		$scope.showDetailsConsumerRoute = false;
		// end clean Consumer Routes details table		
	
		// clean Producer Routes details table
		$scope.selectedProducerRoute = "";
		$scope.selectedProducerRouteId ="";
		$scope.showDetailsProducerRoute = false;
		// end clean Producer Routes details table
	}
	$scope.refresh = function() {
		var rootUrl = location.href.substring(0, location.href
				.indexOf("/main.html"));
		$http.get(rootUrl + "/services/rest/integration-management/contexts")
				.success(function(response) {
					$scope.camelContexts = response;
					$scope.filtredCamelContexts = response;
					$scope.filtredCamelContextsListForPagination = response;
				});
		$scope.currentPage = 0;
		$scope.startItem = 0;
		$scope.searchKeywordInCamelContext = "";
		$scope.cleanSelectedRoutesDetails();
	};

	$scope.refresh();

	$scope.refreshApplicationProducers = function() {
		if ($scope.cametContextId != "") {
			var rootUrl = location.href.substring(0, location.href
					.indexOf("/main.html"));
			$http.get(
					rootUrl + "/services/rest/integration-management/context/"
							+ $scope.cametContextId + "/routes/producers")
					.success(
							function(response) {
								$scope.applicationProducers = response;
								$scope.filtredProducerRoute = response;
								$scope.filtredApplicationProducersListForPagination = response;

							});
			$scope.currentPageProducerRoute = 0;
			$scope.searchKeywordInProducerApplication = "";
	
		}
		
	};
	$scope.refreshApplicationProducers();

	// Consumer
	$scope.refreshConsumers = function() {
		if ($scope.cametContextId != "") {
			var rootUrl = location.href.substring(0, location.href
					.indexOf("/main.html"));
			$http
					.get(
							rootUrl
									+ "/services/rest/integration-management/context/"
									+ $scope.cametContextId
									+ "/routes/consumers")
					.success(
							function(response) {
								$scope.consumers = response;
								$scope.filtredConsumerRoute = response;
								$scope.filtredConsumersListForPagination = response;
							});
			$scope.currentPageConsumerRoute = 0;
			$scope.searchKeywordInConsumer = "";
		}
	};
	$scope.refreshConsumers();

	// Start and stop common for all types of routes
	$scope.startAllRoutes = function(id) {
		var rootUrl = location.href.substring(0, location.href
				.indexOf("/main.html"));
		$http
			.get(
					rootUrl
							+ "/services/rest/integration-management/context/"
							+ id + "/startAllRoutes").success(
					function(response) {
						$scope.names = response;
						$scope.refreshApplicationProducers();
						$scope.refreshConsumers();
					});

	}
	
	// Start and stop common for all types of routes
	$scope.stopAllRoutes = function(id) {
		var rootUrl = location.href.substring(0, location.href
				.indexOf("/main.html"));
		$http
			.get(
					rootUrl
							+ "/services/rest/integration-management/context/"
							+ id + "/stopAllRoutes").success(
					function(response) {
						$scope.names = response;
						$scope.refreshApplicationProducers();
						$scope.refreshConsumers();
					});
	}
	
	
	// Start and stop common for all types of routes
	$scope.startOrStopRoute = function(id, status) {
		if ($scope.cametContextId != "") {
			var rootUrl = location.href.substring(0, location.href
					.indexOf("/main.html"));
			if ((status === 'Started') && (id != undefined)) {
				$http
						.get(
								rootUrl
										+ "/services/rest/integration-management/context/"
										+ $scope.cametContextId + "/route/"
										+ id + "/stop").success(
								function(response) {
									$scope.names = response;
								});
			} else if ((status === 'Stopped') && (id != undefined)) {
				$http
						.get(
								rootUrl
										+ "/services/rest/integration-management/context/"
										+ $scope.cametContextId + "/route/"
										+ id + "/start").success(
								function(response) {
									$scope.names = response;
								});
			}
			
			$http
			.get(
					rootUrl
							+ "/services/rest/integration-management/context/"
							+ $scope.cametContextId
							+ "/routes/consumers")
			.success(
					function(response) {
						$scope.consumers = response;
						$scope.updatePaginationConsumerRoutes($scope.searchKeywordInConsumer);
						$scope.filtredConsumerRoute = $scope.filtredConsumersListForPagination.slice(	$scope.startItemConsumerRoute );

					});
			
			
			$http.get(
					rootUrl + "/services/rest/integration-management/context/"
							+ $scope.cametContextId + "/routes/producers")
					.success(
							function(response) {
								$scope.applicationProducers = response;
								$scope.updatePaginationProducerRoutes($scope.searchKeywordInProducerApplication);
								$scope.filtredProducerRoute = $scope.filtredApplicationProducersListForPagination.slice($scope.startItemProducerRoute);
								
			});

			$scope.updatePagination($scope.searchKeywordInCamelContext);
			$scope.filtredCamelContexts = $scope.filtredCamelContextsListForPagination.slice($scope.startItem);
		}
	};
	
	

	// Tab initialisation
	$scope.tab = 1;

	$scope.setTab = function(newValue) {
		$scope.tab = newValue;
	};

	$scope.isSet = function(tabName) {
		return $scope.tab === tabName;
	};

	// selected camel context
	$scope.cametContextId = "";
	$scope.showRoutesTable = false;

	$scope.showRoutesDetails = function(contextId) {
		if ($scope.cametContextId === contextId) {
			$scope.cametContextId = "";
			$scope.idSelectedCamelContext = ""; // for Selection Style
			$scope.showRoutesTable = false;
		} else {

			// init all routes table index!
			$scope.currentPageProducerRoute = 0;
			$scope.startItemProducerRoute = 0;
			$scope.currentPageConsumerRoute = 0;
			$scope.startItemConsumerRoute = 0;
			$scope.cametContextId = contextId; // for Selection Style
			$scope.idSelectedCamelContext = contextId;
			$scope.showRoutesTable = true;
			$scope.refreshApplicationProducers();
			$scope.refreshConsumers();
			$scope.updatePagination($scope.searchKeywordInCamelContext);
			$scope.filtredCamelContexts = $scope.filtredCamelContextsListForPagination.slice($scope.startItem);
		}

	};

	$scope.showTable = function() {
		return showRoutesTable;
	};

	// pagination for camel context table
	$scope.range = function() {
		var rangeSize = 3;
		var ret = [];
		var start;

		start = $scope.currentPage;
		if (start > $scope.pageCount() - rangeSize) {
			start = $scope.pageCount() - rangeSize + 1;
		}

		for ( var i = start; i < start + rangeSize; i++) {
			if (i >= 0) {
				ret.push(i);
			}
		}
		return ret;
	};

	$scope.prevPage = function() {
		if ($scope.currentPage > 0) {
			$scope.currentPage--;
			$scope.startItem = $scope.currentPage * $scope.itemsPerPage;
			$scope.filtredCamelContexts = $scope.filtredCamelContextsListForPagination
					.slice($scope.startItem);
		}
	};

	$scope.prevPageDisabled = function() {
		return $scope.currentPage === 0 ? "disabled" : "";
	};

	$scope.pageCount = function() {
		return Math.ceil($scope.filtredCamelContextsListForPagination.length
				/ $scope.itemsPerPage) - 1;
	};

	$scope.nextPage = function() {
		if ($scope.currentPage < $scope.pageCount()) {
			$scope.currentPage++;
			$scope.startItem = $scope.currentPage * $scope.itemsPerPage;
			$scope.filtredCamelContexts = $scope.filtredCamelContextsListForPagination
					.slice($scope.startItem);
		}
	};

	$scope.nextPageDisabled = function() {
		return $scope.currentPage === $scope.pageCount() ? "disabled" : "";
	};

	$scope.setPage = function(n) {
		$scope.currentPage = n;
		$scope.startItem = $scope.currentPage * $scope.itemsPerPage;
		$scope.filtredCamelContexts = $scope.filtredCamelContextsListForPagination
				.slice($scope.startItem);
	};

	$scope.updatePagination = function(searchKeywordInCamelContext) {
		$scope.filtredCamelContexts = [];
		for ( var i = 0; i < $scope.camelContexts.length; i++) {
			if ($scope.camelContexts[i].contextId
					.indexOf(searchKeywordInCamelContext) > -1) {
				$scope.filtredCamelContexts.push($scope.camelContexts[i]);
			}
		}
		$scope.filtredCamelContextsListForPagination = $scope.filtredCamelContexts;
	}

	$scope.changeSearchValue = function(searchKeywordInCamelContext) {
		$scope.filtredCamelContexts = [];
		$scope.searchKeywordInCamelContext = searchKeywordInCamelContext;
		for ( var i = 0; i < $scope.camelContexts.length; i++) {
			if ($scope.camelContexts[i].contextId
					.indexOf(searchKeywordInCamelContext) > -1) {
				$scope.filtredCamelContexts.push($scope.camelContexts[i]);
			}
		}
		$scope.filtredCamelContextsListForPagination = $scope.filtredCamelContexts;
		$scope.currentPage = 0;
		$scope.startItem = $scope.currentPage * $scope.itemsPerPage;
		$scope.range();
	}

	// pagination for producer application
	$scope.rangeProducerRoute = function() {
		var rangeSize = 3;
		var ret = [];
		var start;

		start = $scope.currentPageProducerRoute;
		if (start > $scope.pageCountProducerRoute() - rangeSize) {
			start = $scope.pageCountProducerRoute() - rangeSize + 1;
		}

		for ( var i = start; i < start + rangeSize; i++) {
			if (i >= 0) {
				ret.push(i);
			}
		}

		return ret;
	};

	$scope.prevPageProducerRoute = function() {
		if ($scope.currentPageProducerRoute > 0) {
			$scope.currentPageProducerRoute--;
			$scope.startItemProducerRoute = $scope.currentPageProducerRoute
					* $scope.itemsPerPage;
			$scope.filtredProducerRoute = $scope.filtredApplicationProducersListForPagination
					.slice($scope.startItemProducerRoute);
		}
	};

	$scope.prevPageDisabledProducerRoute = function() {
		return $scope.currentPageProducerRoute === 0 ? "disabled" : "";
	};

	$scope.pageCountProducerRoute = function() {
		return Math.ceil($scope.filtredApplicationProducersListForPagination.length
				/ $scope.itemsPerPage) - 1;
	};

	$scope.nextPageProducerRoute = function() {
		if ($scope.currentPageProducerRoute < $scope.pageCountProducerRoute()) {
			$scope.currentPageProducerRoute++;
			$scope.startItemProducerRoute = $scope.currentPageProducerRoute
					* $scope.itemsPerPage;
			$scope.filtredProducerRoute = $scope.filtredApplicationProducersListForPagination
					.slice($scope.startItemProducerRoute);
		}
	};

	$scope.nextPageDisabledProducerRoute = function() {
		return $scope.currentPageProducerRoute === $scope
				.pageCountProducerRoute() ? "disabled" : "";
	};

	$scope.setPageProducerRoute = function(n) {
		$scope.currentPageProducerRoute = n;
		$scope.startItemProducerRoute = $scope.currentPageProducerRoute
				* $scope.itemsPerPage;
		$scope.filtredProducerRoute = $scope.filtredApplicationProducersListForPagination
				.slice($scope.startItemProducerRoute);
	};

	
		
	$scope.updatePaginationProducerRoutes = function(searchKeywordInProducerApplication) {
		$scope.filtredProducerRoute = [];
		for ( var i = 0; i < $scope.applicationProducers.length; i++) {
			if (($scope.applicationProducers[i].id.indexOf(searchKeywordInProducerApplication) > -1) || ($scope.applicationProducers[i].status.indexOf(searchKeywordInProducerApplication) > -1) || ($scope.applicationProducers[i].description.indexOf(searchKeywordInProducerApplication) > -1)) {
				$scope.filtredProducerRoute.push($scope.applicationProducers[i]);
			}
		}
		$scope.filtredApplicationProducersListForPagination = $scope.filtredProducerRoute;
	}
	
	$scope.changeProducerApplicationSearchValue = function(searchKeywordInProducerApplication) {
		$scope.filtredProducerRoute = [];
		$scope.searchKeywordInProducerApplication = searchKeywordInProducerApplication;
		for ( var i = 0; i < $scope.applicationProducers.length; i++) {
			if (($scope.applicationProducers[i].id.indexOf(searchKeywordInProducerApplication) > -1) || ($scope.applicationProducers[i].status.indexOf(searchKeywordInProducerApplication) > -1) || ($scope.applicationProducers[i].description.indexOf(searchKeywordInProducerApplication) > -1)) {
				$scope.filtredProducerRoute.push($scope.applicationProducers[i]);
			}
		}

		$scope.filtredApplicationProducersListForPagination = $scope.filtredProducerRoute;
		$scope.currentPageProducerRoute = 0;
		$scope.startItemProducerRoute = 0;
		$scope.rangeProducerRoute();
	}
	
	//*****************************************//
	//   pagination for consumer 			   //
	//*****************************************//
	$scope.rangeConsumerRoute = function() {
		var rangeSize = 3;
		var ret = [];
		var start;

		start = $scope.currentPageConsumerRoute;
		if (start > $scope.pageCountConsumerRoute() - rangeSize) {
			start = $scope.pageCountConsumerRoute() - rangeSize + 1;
		}

		for ( var i = start; i < start + rangeSize; i++) {
			if (i >= 0) {
				ret.push(i);
			}
		}

		return ret;
	};

	$scope.prevPageConsumerRoute = function() {
		if ($scope.currentPageConsumerRoute > 0) {
			$scope.currentPageConsumerRoute--;
			$scope.startItemConsumerRoute = $scope.currentPageConsumerRoute
					* $scope.itemsPerPage;
			$scope.filtredConsumerRoute = $scope.filtredConsumersListForPagination
					.slice($scope.startItemConsumerRoute);
		}
	};

	$scope.prevPageDisabledConsumerRoute = function() {
		return $scope.currentPageConsumerRoute === 0 ? "disabled"
				: "";
	};

	$scope.pageCountConsumerRoute = function() {
		return Math.ceil($scope.filtredConsumersListForPagination.length
				/ $scope.itemsPerPage) - 1;
	};

	$scope.nextPageConsumerRoute = function() {
		if ($scope.currentPageConsumerRoute < $scope
				.pageCountConsumerRoute()) {
			$scope.currentPageConsumerRoute++;
			$scope.startItemConsumerRoute = $scope.currentPageConsumerRoute
					* $scope.itemsPerPage;
			$scope.filtredConsumerRoute = $scope.filtredConsumersListForPagination
					.slice($scope.startItemConsumerRoute);
		}
	};

	$scope.nextPageDisabledConsumerRoute = function() {
		return $scope.currentPageConsumerRoute === $scope
				.pageCountConsumerRoute() ? "disabled" : "";
	};

	$scope.setPageConsumerRoute = function(n) {
		$scope.currentPageConsumerRoute = n;
		$scope.startItemConsumerRoute = $scope.currentPageConsumerRoute
				* $scope.itemsPerPage;
		$scope.filtredConsumerRoute = $scope.filtredConsumersListForPagination
				.slice($scope.startItemConsumerRoute);
	};
	
	
	$scope.updatePaginationConsumerRoutes = function(searchKeywordInConsumer) {
		$scope.filtredConsumerRoute = [];
		for ( var i = 0; i < $scope.consumers.length; i++) {
			if (($scope.consumers[i].id.indexOf(searchKeywordInConsumer) > -1) || ($scope.consumers[i].status.indexOf(searchKeywordInConsumer) > -1) || ($scope.consumers[i].description.indexOf(searchKeywordInConsumer) > -1)) {
				$scope.filtredConsumerRoute.push($scope.consumers[i]);
			}
		}
		$scope.filtredConsumersListForPagination = $scope.filtredConsumerRoute;
	}
	
	$scope.changeConsumerSearchValue = function(searchKeywordInConsumer) {
		$scope.filtredConsumerRoute = [];
		$scope.searchKeywordInConsumer = searchKeywordInConsumer;
		for ( var i = 0; i < $scope.consumers.length; i++) {
			if (($scope.consumers[i].id.indexOf(searchKeywordInConsumer) > -1) || ($scope.consumers[i].status.indexOf(searchKeywordInConsumer) > -1) || ($scope.consumers[i].description.indexOf(searchKeywordInConsumer) > -1)) {
				$scope.filtredConsumerRoute.push($scope.consumers[i]);
			}
		}

		$scope.filtredConsumersListForPagination = $scope.filtredConsumerRoute;
		$scope.currentPageConsumerRoute = 0;
		$scope.startItemConsumerRoute = 0;
		$scope.rangeConsumerRoute();
	}

	// row selection code
	$scope.idSelectedCamelContext = null;
	$scope.setSelected = function(idSelectedCamelContext) {
		$scope.idSelectedCamelContext = idSelectedCamelContext;
		console.log(idSelectedCamelContext);
		$scope.showRoutesDetails(idSelectedCamelContext);
		$scope.cleanSelectedRoutesDetails();
	}
	
	$scope.showConsumerRouteDetails = function(selectedCamelRoute) {
		if (($scope.showDetailsConsumerRoute == true ) && ($scope.selectedConsumerRoute===selectedCamelRoute)){
			$scope.selectedConsumerRoute ="";
			$scope.selectedConsumerRouteId ="";
			$scope.showDetailsConsumerRoute = false;
		}else {
		$scope.selectedConsumerRoute = selectedCamelRoute;
		$scope.selectedConsumerRouteId =selectedCamelRoute.id;
		$scope.showDetailsConsumerRoute = true;
		}
	}

	$scope.showProducerRouteDetails = function(selectedCamelRoute) {
		if (($scope.showDetailsProducerRoute == true ) && ($scope.selectedProducerRoute===selectedCamelRoute)){
			$scope.selectedProducerRoute = "";
			$scope.selectedProducerRouteId ="";
			$scope.showDetailsProducerRoute = false;
		}else {
			$scope.selectedProducerRoute = selectedCamelRoute;
			$scope.selectedProducerRouteId =selectedCamelRoute.id;
			$scope.showDetailsProducerRoute = true;
		}
	}	
	
}