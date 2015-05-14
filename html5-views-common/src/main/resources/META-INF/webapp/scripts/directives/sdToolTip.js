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
 * @author Abhay.Thappan
 */
(function() {
	'use strict';

	angular.module('viewscommon-ui').directive('sdToolTip', [ 'sdUtilService', ToolTip]);

	/*
	 * 
	 */
	function ToolTip(sdUtilService) {

		return {
			restrict : 'EA',
			transclude : true,
			replace : true,
			templateUrl : sdUtilService.getBaseUrl() + 'plugins/html5-views-common/scripts/directives/partials/toolTip.html',
			controller : [ '$scope', '$parse', '$attrs', ToolTipController ]
		};
	}
	/**
	 * 
	 */
	function ToolTipController($scope, $parse, $attrs) {
		this.toolTip = {
			show : false
		};
		this.i18n = $scope.i18n;

		this.toolTipClass = 'popup-dlg';
		if (angular.isDefined($attrs.sdaToolTipClass)) {

			this.toolTipClass = this.toolTipClass + " " + $attrs.sdaToolTipClass;
		}

		if (angular.isDefined($attrs.sdaToolTipUrl)) {
			var binding = $parse($attrs.sdaToolTipUrl);
			this.userTemplate = binding($scope);
			this.showUserTemplate = true;
		} else {
			this.showUserTemplate = false;
		}
		var title = $parse($attrs.sdaTitle);
		this.title = title($scope);

		$scope.toolTipCtrl = this;
	}
})();
