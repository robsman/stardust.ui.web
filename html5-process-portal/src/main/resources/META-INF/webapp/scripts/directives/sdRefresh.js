/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Aditya.Gaikwad
 */

(function() {
	'use strict';

	angular.module('bpm-common').directive('sdRefresh', [ RefreshDirective ]);

	/*
	 * 
	 */
	function RefreshDirective() {
		return {
			restrict : 'AE',
			scope : {
				refresh : "&sdaOnRefresh"
			},
			template : '<button ng-click="refresh()"'+
					' title="{{refreshController.i18n(\'processportal.launchPanels-worklists-button-refresh\')}}"'+
					' aid="Refresh"' +
					' class="button-link">'+
					' <i class="glyphicon glyphicon-refresh icon-lg"></i>'+
				    '</button>',
			controller : [ '$scope', RefreshController ]
		};
	}

	/**
	 * 
	 */
	function RefreshController($scope) {
		// Make sure i18n is available in the current scope
		if (!angular.isDefined($scope.i18n)) {
			this.i18n = $scope.$parent.i18n;
		}
		$scope.refreshController = this;
	}

})();