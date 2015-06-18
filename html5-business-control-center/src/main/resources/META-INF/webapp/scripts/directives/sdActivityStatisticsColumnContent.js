/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Johnson.Quadras
 */
(function() {
	'use strict';

	angular.module('bcc-ui').directive('sdActivitiesStatisticsColumnContent', [ Directive ]);

	/*
	 * 
	 */
	function Directive() {

		return {
			restrict : 'A',
			scope : {
				value : '=sdaValue'
			},

			template : '<div > <b>{{ctrl.i18n("business-control-center-messages.views-common-column-today")}} : </b><span ng-bind="ctrl.value.day"></span> </div>'+
						'<div > <b>{{ctrl.i18n("business-control-center-messages.views-common-column-week")}} : </b> <span ng-bind="ctrl.value.week"></span>  </div>'+
						'<div > <b>{{ctrl.i18n("business-control-center-messages.views-common-column-month")}} : </b> <span ng-bind="ctrl.value.month"></span> </div> ',
			controller : [ '$scope', ActivityStatisticsContentCtrl ]
		};
	}
	
	/**
	 * 
	 */
	function ActivityStatisticsContentCtrl($scope) {
		this.value = $scope.value;
		this.i18n = $scope.$parent.i18n;
		$scope.ctrl = this;
	}

})();
