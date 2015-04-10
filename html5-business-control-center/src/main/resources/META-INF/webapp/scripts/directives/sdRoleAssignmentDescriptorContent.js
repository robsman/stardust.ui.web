/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Abhay.Thappan
 */
(function() {
	'use strict';

	angular.module('bcc-ui').directive('sdRoleAssignmentDescriptorContent', [ RoleAssignmentDescriptorContent]);

	/*
	 * 
	 */
	function RoleAssignmentDescriptorContent( ) {

		return {
			restrict : 'A',
			scope : {
				descriptor : '=sdaDescriptor'
			},
			
			template : '<div><i ng-if="descriptorCtrl.descriptorValue == true" class="sc sc-fw sc-2x sc-check"  style="color: green"><\/i>'
			+ '<i ng-if="descriptorCtrl.descriptorValue == false" class="sc sc-fw sc-2x sc-close" style="color: green"><\/i>'
		    +'<\/div>',
		
			/*template : '<div>{{descriptorCtrl.descriptorValue}}<\/div>',*/
			controller : [ '$scope', '$parse', '$attrs', RoleAssignmentDescriptorContentCtrl ]
		};
	};
	/**
	 * 
	 */
	function RoleAssignmentDescriptorContentCtrl( $scope, $parse, $attrs) {
		this.descriptorValue = $scope.descriptor;
		$scope.descriptorCtrl = this;
	};

})();
