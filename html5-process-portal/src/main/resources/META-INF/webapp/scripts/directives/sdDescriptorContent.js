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
 * @author Johnson.Quadras
 */
(function() {
	'use strict';

	angular.module('bpm-common').directive('sdDescriptorContent', [ DesciptorContentDirective]);

	/*
	 * 
	 */
	function DesciptorContentDirective( ) {

		return {
			restrict : 'A',
			scope : {
				value : '=sdaValue',
				type : '=sdaDataType'
			},
			template : '<div ng-if="descriptorCtrl.dataType ==\'NUMBER\' ">'+
							'{{descriptorCtrl.descriptorValue.value | number}}<\/div>'+
						'<div ng-if="descriptorCtrl.dataType == \'DATE\' ">'+
						'{{descriptorCtrl.descriptorValue.value | date: "shortDate"}}<\/div>'+
						'<div ng-if="descriptorCtrl.dataType == \'STRING\' ">'+
							'{{descriptorCtrl.descriptorValue.value}}'+
						'<\/div>'+
						'<div style="text-align:left;" ng-if="(descriptorCtrl.dataType == \'DOCUMENT\') || (descriptorCtrl.dataType == \'LIST\' && descriptorCtrl.descriptorValue.isDocument ) ">'+
								'<div ng-repeat="document in  descriptorCtrl.descriptorValue.documents">'+
									'<div sd-open-document-link sda-name="document.name" '+
										'sda-mime-type="document.contentType" sda-document-id="document.uuid">' +
									'<\/div>'+
								'<\/div>'+
						'<\/div>',
			controller : [ '$scope', '$parse', '$attrs', DesciptorContentController ]
		};
	};
	/**
	 * 
	 */
	function DesciptorContentController( $scope, $parse, $attrs) {
		this.dataType = $scope.type;
		this.descriptorValue = $scope.value;
		$scope.descriptorCtrl = this;
	};

})();
