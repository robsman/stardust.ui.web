
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
	angular.module('workflow-ui').directive('sdOpenDocumentLink',
			[ 'sdUtilService', 'sdCommonViewUtilService', 'sdMimeTypeService', OpenDocumentDirective ]);

	/*
	 * 
	 */
	function OpenDocumentDirective(sdUtilService) {

		return {
			restrict : 'A',
			scope : {
				name : '=sdaName',
				mimeType : '=sdaMimeType',
				documentId : '=sdaDocumentId',
				params : '=sdaParams',
				stopEvent : '=sdaStopEvent' 
			},
			template : '<a href="#"  ng-click="docLinkCtrl.openDocument($event);">'
					+ '<i ng-class="docLinkCtrl.mimeIcon" class="pi-lg spacing-right"> </i> <span ng-bind="name"></span>'
					+ '</a>',
			controller : [ '$scope', '$parse', '$attrs', 'sdUtilService', 'sdCommonViewUtilService', 'sdMimeTypeService',
					DocumentLinkController ]
		};
	}
	;
	/**
	 * 
	 */
	function DocumentLinkController($scope, $parse, $attrs, sdUtilService, sdCommonViewUtilService, sdMimeTypeService) {

		this.mimeIcon = sdMimeTypeService.getIcon($scope.mimeType);

		//ZZM: default to true, if false allows event to bubble so that user can handle the click.
		//For example, use in a dialog where clicking a dialog link opens a view and closes the dialog.
		if(angular.isUndefined($scope.stopEvent)){
			$scope.stopEvent = true;
		}

		/**
		 * Declared here for accessing the $scope variable
		 */
		this.openDocument = function($event) {
			if($scope.stopEvent === true){
				sdUtilService.stopEvent($event);
			}
			this.openDocumentView($scope.documentId, sdCommonViewUtilService, $scope.params);
		};

		$scope.docLinkCtrl = this;
	}
	;

	/**
	 * 
	 */
	DocumentLinkController.prototype.openDocumentView = function(documentId, sdCommonViewUtilService, params) {
		sdCommonViewUtilService.openDocumentView(documentId, true, params);
	};

})();
