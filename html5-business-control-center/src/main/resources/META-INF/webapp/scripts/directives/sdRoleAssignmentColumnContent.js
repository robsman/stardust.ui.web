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
 * @author Abhay.Thappan
 */
(function() {
	'use strict';

	angular.module('bcc-ui').directive('sdRoleAssignmentColumnContent', [ RoleAssignmentColumnContent ]);

	/*
	 * 
	 */
	function RoleAssignmentColumnContent() {

		return {
			restrict : 'A',
			scope : {
				value : '=sdaValue'
			},

			template : '<div><i ng-if="ctrl.value == true" class="pi pi-lg pi-check"><\/i>'
					+ '<i ng-if="ctrl.value == false" class="pi p-lg pi-close pi-danger"><\/i>'
					+ '<\/div>',
			controller : [ '$scope', '$parse', '$attrs', RoleAssignmentColumnContentCtrl ]
		};
	}
	;
	/**
	 * 
	 */
	function RoleAssignmentColumnContentCtrl($scope, $parse, $attrs) {
		this.value = $scope.value;
		$scope.ctrl = this;
	}
	;

})();
