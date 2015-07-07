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

	angular.module('viewscommon-ui').directive('sdToolTip',
			[ 'sdUtilService', ToolTip ]);

	/*
	 * 
	 */
	function ToolTip(sdUtilService) {

		return {
			restrict : 'EA',
			transclude : true,
			replace : true,
			templateUrl : sdUtilService.getBaseUrl()
					+ 'plugins/html5-views-common/scripts/directives/partials/toolTip.html',
			controller : [ '$scope', '$parse', '$attrs', '$element',
					ToolTipController ]
		};
	}
	/**
	 * 
	 */
	function ToolTipController($scope, $parse, $attrs, $element) {
		this.toolTip = {
			show : false
		};
		this.i18n = $scope.i18n;

		this.toolTipClass = 'toolTip-dlg';
		if (angular.isDefined($attrs.sdaToolTipClass)) {

			this.toolTipClass = this.toolTipClass + " "
					+ $attrs.sdaToolTipClass;
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

		this.showToolTip = function() {
			var marginTop = 0;
			var top = $element.offset().top;
			if (top > 200) {
				marginTop = -141;
			}

			var toolTipWidth = 300;
			var documentWidth = $(document).width();
			var left = $element.offset().left;
			var elementOffsetWidth = $element[0].offsetWidth;

			if (left + toolTipWidth > documentWidth) {
				// shift the tool tip position to the left of the object 
				// so it won't go out of width of current HTML document width
				// and show up in the correct place
				elementOffsetWidth = documentWidth - (toolTipWidth + left)
						- (2 * 10);
			}

			$('.toolTip-dlg').css({
				'margin-top' : marginTop,
				'margin-left' : elementOffsetWidth
			});
			this.toolTip.show = true;
		};

		$scope.toolTipCtrl = this;
	}
})();
