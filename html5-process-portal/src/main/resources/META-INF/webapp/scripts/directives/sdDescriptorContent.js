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

	angular.module('workflow-ui').directive('sdDescriptorContent', [ DesciptorContentDirective]);

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
							'{{descriptorCtrl.descriptorValue.value}}<\/div>'+
						'<div ng-if="descriptorCtrl.dataType == \'DATE\' "> ' +
						'{{descriptorCtrl.descriptorValue.value | sdDateFilter}}<\/div>'+
            '<div ng-if="descriptorCtrl.dataType == \'DATETIME\' "> ' +
            '{{descriptorCtrl.descriptorValue.value | sdDateTimeFilter}}<\/div>'+
						'<div ng-if="(descriptorCtrl.dataType == \'STRING\' && !descriptorCtrl.descriptorValue.isLink) || descriptorCtrl.dataType == \'BOOLEAN\' ">'+
							'{{descriptorCtrl.descriptorValue.value}}'+
						'<\/div>'+
						'<div ng-if="(descriptorCtrl.dataType == \'STRING\' && descriptorCtrl.descriptorValue.isLink)">'+
							'<a href="{{descriptorCtrl.descriptorValue.value}}" title="{{descriptorCtrl.descriptorValue.value}}" target="_blank">{{descriptorCtrl.descriptorValue.linkText}}</a>'+
						'<\/div>'+
						'<div style="text-align:left;" ng-if="(descriptorCtrl.dataType == \'DOCUMENT\') || (descriptorCtrl.dataType == \'LIST\' && descriptorCtrl.descriptorValue.isDocument ) ">'+
								'<div ng-repeat="document in  descriptorCtrl.descriptorValue.documents">'+
									'<div sd-open-document-link sda-name="document.name" '+
										'sda-mime-type="document.contentType" sda-document-id="document.uuid">' +
									'<\/div>'+
								'<\/div>'+
						'<\/div>'+
						'<div style="text-align:left;" ng-if="descriptorCtrl.dataType == \'LIST\' && !descriptorCtrl.descriptorValue.isDocument">'+
							'{{descriptorCtrl.descriptorValue.value}}'+
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
