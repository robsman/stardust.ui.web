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
	$scope.currentPageConsumerApplicationRoute = 0;
	$scope.startItemConsumerApplicationRoute = 0;
	$scope.filtredConsumerApplicationRoute = [];

	// variable for trigger consumers
	$scope.currentPageConsumerTriggerRoute = 0;
	$scope.startItemConsumerTriggerRoute = 0;
	$scope.filtredConsumerTriggerRoute = [];
	// init search value
	// camel context table variables
	$scope.searchKeywordInCamelContext = "";
	$scope.filtredCamelContextsListForPagination = [];
	// camel producer routes table variables
	$scope.searchKeywordInProducerApplication = "";
	$scope.filtredApplicationProducersListForPagination = [];
	// camel application consumer routes table variables
	$scope.searchKeywordInConsumerApplication = "";
	$scope.filtredApplicationConsumersListForPagination = [];
	// camel trigger consumer routes table variables
	$scope.searchKeywordInConsumerTrigger = "";
	$scope.filtredTriggerConsumersListForPagination = [];
	

	$scope.showRouteDeatilsTable = false;
	$scope.selectedConsumerAppliocationRoute = null;
	$scope.showDetailsConsumerApplicationRoute = false;
	$scope.selectedConsumerAppliocationRouteId ="";
	
	
	/// selected Consumer Trigger Route 
	$scope.selectedConsumerTriggerRoute = "";
	$scope.selectedConsumerTriggerRouteId ="";
	$scope.showDetailsConsumerTriggerRoute = false;
	
	/// selected Producer Route 
	$scope.selectedProducerRoute = "";
	$scope.selectedProducerRouteId ="";
	$scope.showDetailsProducerRoute = false;	
	
	
	
	$scope.cleanSelectedRoutesDetails = function() {
		// clean Consumer Trigger Routes details table
		$scope.selectedConsumerAppliocationRoute ="";
		$scope.selectedConsumerAppliocationRouteId ="";
		$scope.showDetailsConsumerApplicationRoute = false;
		// end clean Consumer Trigger Routes details table
		
		// clean Consumer Trigger Routes details table
		$scope.selectedConsumerTriggerRoute = "";
		$scope.selectedConsumerTriggerRouteId ="";
		$scope.showDetailsConsumerTriggerRoute = false;
		// end clean Consumer Trigger Routes details table
		
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

	// Application Consumer
	$scope.refreshApplicationConsumers = function() {
		if ($scope.cametContextId != "") {
			var rootUrl = location.href.substring(0, location.href
					.indexOf("/main.html"));
			$http
					.get(
							rootUrl
									+ "/services/rest/integration-management/context/"
									+ $scope.cametContextId
									+ "/routes/consumers/application")
					.success(
							function(response) {
								$scope.applicationConsumers = response;
								$scope.filtredConsumerApplicationRoute = response;
								$scope.filtredApplicationConsumersListForPagination = response;
							});
			$scope.currentPageConsumerApplicationRoute = 0;
			$scope.searchKeywordInConsumerApplication = "";
		}
	};
	$scope.refreshApplicationConsumers();

	// Trigger Consumer
	$scope.refreshTriggerConsumers = function() {
		if ($scope.cametContextId != "") {
			var rootUrl = location.href.substring(0, location.href
					.indexOf("/main.html"));
			$http.get(
					rootUrl + "/services/rest/integration-management/context/"
							+ $scope.cametContextId
							+ "/routes/consumers/trigger").success(
					function(response) {
						$scope.triggerConsumers = response;
						$scope.filtredConsumerTriggerRoute = response;
						$scope.filtredTriggerConsumersListForPagination = response;
					});
			$scope.currentPageConsumerTriggerRoute = 0;
			$scope.searchKeywordInConsumerTrigger = "";

		}
	};
	$scope.refreshTriggerConsumers();


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
						$scope.refreshApplicationConsumers();
						$scope.refreshTriggerConsumers();
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
						$scope.refreshApplicationConsumers();
						$scope.refreshTriggerConsumers();
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
							+ "/routes/consumers/application")
			.success(
					function(response) {
						$scope.applicationConsumers = response;
						$scope.updatePaginationApplicationConsumerRoutes($scope.searchKeywordInConsumerApplication);
						$scope.filtredConsumerApplicationRoute = $scope.filtredApplicationConsumersListForPagination.slice(	$scope.startItemConsumerApplicationRoute );

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
			
			
			$http.get(
					rootUrl + "/services/rest/integration-management/context/"
							+ $scope.cametContextId
							+ "/routes/consumers/trigger").success(
					function(response) {
						$scope.triggerConsumers = response;
						$scope.updatePaginationTriggerConsumerRoutes($scope.searchKeywordInConsumerTrigger);
						$scope.filtredConsumerTriggerRoute = $scope.filtredTriggerConsumersListForPagination.slice($scope.startItemConsumerTriggerRoute);
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
			$scope.currentPageConsumerTriggerRoute = 0;
			$scope.startItemConsumerTriggerRoute = 0;
			$scope.currentPageConsumerApplicationRoute = 0;
			$scope.startItemConsumerApplicationRoute = 0;
			$scope.cametContextId = contextId; // for Selection Style
			$scope.idSelectedCamelContext = contextId;
			$scope.showRoutesTable = true;
			$scope.refreshApplicationProducers();
			$scope.refreshApplicationConsumers();
			$scope.refreshTriggerConsumers();
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
		//$scope.refresh();
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
	//   pagination for consumer application   //
	//*****************************************//
	$scope.rangeConsumerApplicationRoute = function() {
		var rangeSize = 3;
		var ret = [];
		var start;

		start = $scope.currentPageConsumerApplicationRoute;
		if (start > $scope.pageCountConsumerApplicationRoute() - rangeSize) {
			start = $scope.pageCountConsumerApplicationRoute() - rangeSize + 1;
		}

		for ( var i = start; i < start + rangeSize; i++) {
			if (i >= 0) {
				ret.push(i);
			}
		}

		return ret;
	};

	$scope.prevPageConsumerApplicationRoute = function() {
		if ($scope.currentPageConsumerApplicationRoute > 0) {
			$scope.currentPageConsumerApplicationRoute--;
			$scope.startItemConsumerApplicationRoute = $scope.currentPageConsumerApplicationRoute
					* $scope.itemsPerPage;
			$scope.filtredConsumerApplicationRoute = $scope.filtredApplicationConsumersListForPagination
					.slice($scope.startItemConsumerApplicationRoute);
		}
	};

	$scope.prevPageDisabledConsumerApplicationRoute = function() {
		return $scope.currentPageConsumerApplicationRoute === 0 ? "disabled"
				: "";
	};

	$scope.pageCountConsumerApplicationRoute = function() {
		return Math.ceil($scope.filtredApplicationConsumersListForPagination.length
				/ $scope.itemsPerPage) - 1;
	};

	$scope.nextPageConsumerApplicationRoute = function() {
		if ($scope.currentPageConsumerApplicationRoute < $scope
				.pageCountConsumerApplicationRoute()) {
			$scope.currentPageConsumerApplicationRoute++;
			$scope.startItemConsumerApplicationRoute = $scope.currentPageConsumerApplicationRoute
					* $scope.itemsPerPage;
			$scope.filtredConsumerApplicationRoute = $scope.filtredApplicationConsumersListForPagination
					.slice($scope.startItemConsumerApplicationRoute);
		}
	};

	$scope.nextPageDisabledConsumerApplicationRoute = function() {
		return $scope.currentPageConsumerApplicationRoute === $scope
				.pageCountConsumerApplicationRoute() ? "disabled" : "";
	};

	$scope.setPageConsumerApplicationRoute = function(n) {
		$scope.currentPageConsumerApplicationRoute = n;
		$scope.startItemConsumerApplicationRoute = $scope.currentPageConsumerApplicationRoute
				* $scope.itemsPerPage;
		$scope.filtredConsumerApplicationRoute = $scope.filtredApplicationConsumersListForPagination
				.slice($scope.startItemConsumerApplicationRoute);
	};
	
	
	$scope.updatePaginationApplicationConsumerRoutes = function(searchKeywordInConsumerApplication) {
		$scope.filtredConsumerApplicationRoute = [];
		for ( var i = 0; i < $scope.applicationConsumers.length; i++) {
			if (($scope.applicationConsumers[i].id.indexOf(searchKeywordInConsumerApplication) > -1) || ($scope.applicationConsumers[i].status.indexOf(searchKeywordInConsumerApplication) > -1) || ($scope.applicationConsumers[i].description.indexOf(searchKeywordInConsumerApplication) > -1)) {
				$scope.filtredConsumerApplicationRoute.push($scope.applicationConsumers[i]);
			}
		}
		$scope.filtredApplicationConsumersListForPagination = $scope.filtredConsumerApplicationRoute;
	}
	
	$scope.changeConsumerApplicationSearchValue = function(searchKeywordInConsumerApplication) {
		$scope.filtredConsumerApplicationRoute = [];
		$scope.searchKeywordInConsumerApplication = searchKeywordInConsumerApplication;
		for ( var i = 0; i < $scope.applicationConsumers.length; i++) {
			if (($scope.applicationConsumers[i].id.indexOf(searchKeywordInConsumerApplication) > -1) || ($scope.applicationConsumers[i].status.indexOf(searchKeywordInConsumerApplication) > -1) || ($scope.applicationConsumers[i].description.indexOf(searchKeywordInConsumerApplication) > -1)) {
				$scope.filtredConsumerApplicationRoute.push($scope.applicationConsumers[i]);
			}
		}

		$scope.filtredApplicationConsumersListForPagination = $scope.filtredConsumerApplicationRoute;
		$scope.currentPageConsumerApplicationRoute = 0;
		$scope.startItemConsumerApplicationRoute = 0;
		$scope.rangeConsumerApplicationRoute();
	}


	//*****************************************//
	// 	   pagination for consumer trigger     //
	//*****************************************//
	$scope.rangeConsumerTriggerRoute = function() {
		var rangeSize = 3;
		var ret = [];
		var start;

		start = $scope.currentPageConsumerTriggerRoute;
		if (start > $scope.pageCountConsumerTriggerRoute() - rangeSize) {
			start = $scope.pageCountConsumerTriggerRoute() - rangeSize + 1;
		}

		for ( var i = start; i < start + rangeSize; i++) {
			if (i >= 0) {
				ret.push(i);
			}
		}

		return ret;
	};

	$scope.prevPageConsumerTriggerRoute = function() {
		if ($scope.currentPageConsumerTriggerRoute > 0) {
			$scope.currentPageConsumerTriggerRoute--;
			$scope.startItemConsumerTriggerRoute = $scope.currentPageConsumerTriggerRoute
					* $scope.itemsPerPage;
			$scope.filtredConsumerTriggerRoute = $scope.filtredTriggerConsumersListForPagination
					.slice($scope.startItemConsumerTriggerRoute);
		}
	};

	$scope.prevPageDisabledConsumerTriggerRoute = function() {
		return $scope.currentPageConsumerTriggerRoute === 0 ? "disabled" : "";
	};

	$scope.pageCountConsumerTriggerRoute = function() {
		return Math.ceil($scope.filtredTriggerConsumersListForPagination.length
				/ $scope.itemsPerPage) - 1;
	};

	$scope.nextPageConsumerTriggerRoute = function() {
		if ($scope.currentPageConsumerTriggerRoute < $scope
				.pageCountConsumerTriggerRoute()) {
			$scope.currentPageConsumerTriggerRoute++;
			$scope.startItemConsumerTriggerRoute = $scope.currentPageConsumerTriggerRoute
					* $scope.itemsPerPage;
			$scope.filtredConsumerTriggerRoute = $scope.filtredTriggerConsumersListForPagination
					.slice($scope.startItemConsumerTriggerRoute);
		}
	};

	$scope.nextPageDisabledConsumerTriggerRoute = function() {
		return $scope.currentPageConsumerTriggerRoute === $scope
				.pageCountConsumerTriggerRoute() ? "disabled" : "";
	};

	$scope.setPageConsumerTriggerRoute = function(n) {
		$scope.currentPageConsumerTriggerRoute = n;
		$scope.startItemConsumerTriggerRoute = $scope.currentPageConsumerTriggerRoute
				* $scope.itemsPerPage;
		$scope.filtredConsumerTriggerRoute = $scope.filtredTriggerConsumersListForPagination
				.slice($scope.startItemConsumerTriggerRoute);
	};
	
	$scope.updatePaginationTriggerConsumerRoutes = function(searchKeywordInConsumerTrigger) {
		$scope.filtredConsumerTriggerRoute = [];
		
		for ( var i = 0; i < $scope.triggerConsumers.length; i++) {
			if (($scope.triggerConsumers[i].id.indexOf(searchKeywordInConsumerTrigger) > -1) || ($scope.triggerConsumers[i].status.indexOf(searchKeywordInConsumerTrigger) > -1) || ($scope.triggerConsumers[i].description.indexOf(searchKeywordInConsumerTrigger) > -1)) {
				$scope.filtredConsumerTriggerRoute.push($scope.triggerConsumers[i]);
			}
		}
		$scope.filtredTriggerConsumersListForPagination = $scope.filtredConsumerTriggerRoute;
	}
	
	$scope.changeConsumerTriggerSearchValue = function(searchKeywordInConsumerTrigger) {
		$scope.filtredConsumerTriggerRoute = [];
		$scope.searchKeywordInConsumerTrigger = searchKeywordInConsumerTrigger;
		for ( var i = 0; i < $scope.triggerConsumers.length; i++) {
			if (($scope.triggerConsumers[i].id.indexOf(searchKeywordInConsumerTrigger) > -1) || ($scope.triggerConsumers[i].status.indexOf(searchKeywordInConsumerTrigger) > -1) || ($scope.triggerConsumers[i].description.indexOf(searchKeywordInConsumerTrigger) > -1)) {
				$scope.filtredConsumerTriggerRoute.push($scope.triggerConsumers[i]);
			}
		}

		$scope.filtredTriggerConsumersListForPagination = $scope.filtredConsumerTriggerRoute;
		$scope.currentPageConsumerTriggerRoute = 0;
		$scope.startItemConsumerTriggerRoute = 0;
		$scope.rangeConsumerTriggerRoute();
	}

	// row selection code
	$scope.idSelectedCamelContext = null;
	$scope.setSelected = function(idSelectedCamelContext) {
		$scope.idSelectedCamelContext = idSelectedCamelContext;
		console.log(idSelectedCamelContext);
		$scope.showRoutesDetails(idSelectedCamelContext);
		$scope.cleanSelectedRoutesDetails();
	}
	
	$scope.showConsumerApplicationRouteDetails = function(selectedCamelRoute) {
		if (($scope.showDetailsConsumerApplicationRoute == true ) && ($scope.selectedConsumerAppliocationRoute===selectedCamelRoute)){
			$scope.selectedConsumerAppliocationRoute ="";
			$scope.selectedConsumerAppliocationRouteId ="";
			$scope.showDetailsConsumerApplicationRoute = false;
		}else {
		$scope.selectedConsumerAppliocationRoute = selectedCamelRoute;
		$scope.selectedConsumerAppliocationRouteId =selectedCamelRoute.id;
		$scope.showDetailsConsumerApplicationRoute = true;
		}
	}
	
	$scope.showConsumerTriggerRouteDetails = function(selectedCamelRoute) {
		if (($scope.showDetailsConsumerTriggerRoute == true ) && ($scope.selectedConsumerTriggerRoute===selectedCamelRoute)){
			$scope.selectedConsumerTriggerRoute = "";
			$scope.selectedConsumerTriggerRouteId = "";
			$scope.showDetailsConsumerTriggerRoute = false;
		}else {
			$scope.selectedConsumerTriggerRoute = selectedCamelRoute;
			$scope.selectedConsumerTriggerRouteId =selectedCamelRoute.id;
			$scope.showDetailsConsumerTriggerRoute = true;
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