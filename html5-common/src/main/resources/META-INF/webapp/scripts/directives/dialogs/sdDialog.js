/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
(function(){
	'use strict';

	angular.module('bpm-common.directives').directive('sdDialog', ['sdLoggerService', '$compile', '$document', DialogDirectiveFn]);
	
	/*
	 * Directive class
	 * Attributes supported:
	 * 		sda-show (=)
	 * 		sda-title (@)
	 * 		sda-type (String)
	 * 		sda-scope: ($Scope object)
	 * 		sda-confirm-action-label: (@)	
	 * 		sda-cancel-action-label: (@)
	 * 		sda-template: (@)
	 * 		sda-draggable: (String)
	 * 		sda-show-close: (@)
	 * 		sda-width: (String)
	 * 		sda-height: (String)
	 * 		sda-on-open: (@) Eg: func(res)
	 * 		sda-on-close: (@) Eg: func(res)
	 * 		sda-on-confirm: (@) Eg: func(res)
	 * 		sda-modal (String)
	 */
	function DialogDirectiveFn(sdLoggerService, $compile, $document) {
		var trace = sdLoggerService.getLogger('bpm-common.directives.sdDialog');
		
		var SUPPORTED_TYPES = {
				ALERT: 'alert',        // Alert popup with one button
				CONFIRM: 'confirm',    // Confirm popup with two action buttons
				CUSTOM: 'custom'       // Customized popup with no action button
			};
		

		var templateURL = null;
		if (location.href.indexOf("plugins") > -1) {
			templateURL = location.href.substring(0, location.href
					.indexOf("plugins"))
					+ 'plugins/html5-common/scripts/directives/dialogs/templates/dialog.html';
		} else {
			// When loaded from framework i.e index.html, location.href points
			// to contextRoot
			templateURL = 'plugins/html5-common/scripts/directives/dialogs/templates/dialog.html';
		}
		

		var directiveDefObject = {
			restrict : 'A',
			scope: {  // Creates a new sub scope
				title: '@sdaTitle',
				template: '@sdaTemplate',
				confirmActionLabel: '@sdaConfirmActionLabel',
				cancelActionLabel: '@sdaCancelActionLabel',
				dialogScope: '=sdaScope',
				showDialog: '=sdaShow'
			},
			transclude: true,
			templateUrl: templateURL,
			controller: ['$attrs', '$scope', '$element', '$parse', '$transclude', '$q', '$timeout', DialogControllerFn],
			compile: DialogCompilerFn
		};
		
		/*
		 *Compiler Function class
		 */
		function DialogCompilerFn(elem, attr, transcludeFn) {
			
			var dialogElem = elem.find('#modal');
			
			if (attr.sdaModal == 'false') {
				dialogElem.addClass('no-modal');
				dialogElem.removeClass('modal');
			}
			
			if (attr.sdaDraggable == 'true') {
				var dialogBlock = dialogElem.find('.modal-dialog');
				var dialogHeader = dialogElem.find('.modal-header');
				
				dialogBlock.attr('sd-moveable', '');
				dialogHeader.addClass('drag-area');
			}
			
			return function (scope, element, attrs) { // Link Function
				DialogLinkFn(scope, element, attrs);				
			}
		}
		
		/*
		 * Link function class
		 */
		function DialogLinkFn(scope, element, attrs) {
			// Detach the element from the DOM to hide it from the document.
			// it will be attached when openDialog is called
			scope.dialogController.dialogElem.detach();
			
			// Dialog block element
			var dialogContent = scope.dialogController.dialogElem.find('.modal-dialog');
			
			// Creating this in the scope to track changes in the height of the dialog content
			scope.getContentHeight = function () {
	            return dialogContent.outerHeight();
	        };
	        
	        scope.$watch(scope.getContentHeight, function(height) {
	        	if (height === 0) {
	        		return;
	        	}
	        	// Now determine and set correct position for the dialog.
	        	var w = jQuery(window);
	        	dialogContent.css({
				    'position':'absolute',
				    'top':Math.abs(((w.height() - dialogContent.outerHeight()) / 2) + w.scrollTop()),
				    'left':Math.abs(((w.width() - dialogContent.outerWidth()) / 2) + w.scrollLeft())
				 });
	        });
		}
		
		/*
		 *Controller Function class
		 */
		function DialogControllerFn($attrs, $scope, $element, $parse, $transclude, $q, $timeout) {
			
			var self = this;
			$scope.dialogController = self;
			
			initialize();
			
			/*
			 * Initialize the component
			 */
			function initialize() {
				configureDialog();
				
				// Static variables for defined dialog types to be used in the directive template, for better readability.
				$scope.TYPE_ALERT = SUPPORTED_TYPES.ALERT;
				$scope.TYPE_CONFIRM = SUPPORTED_TYPES.CONFIRM;
				$scope.TYPE_CUSTOM = SUPPORTED_TYPES.CUSTOM;
				
				// Scope isolation requires i18n function to be exposed from the parent scope
				if (!angular.isDefined($scope.i18n)) {
					$scope.i18n = $scope.$parent.i18n;
				}
				
				// Dialog scope is used for binding body/user template contents in the desired scope (user provided or parent by default)
				self.dialogScope = angular.isDefined($scope.dialogScope) ? $scope.dialogScope : $scope.$parent;
				
				self.showClose = $attrs.sdaShowClose;
				if (!angular.isDefined(self.showClose)) {
					self.showClose = true;
				}
				
				// Determines which transclude strategy to be used (See #DialogCompilerFn)
				self.template = $scope.template;
				self.useTransclude = !angular.isDefined($scope.template);
				
				// modal or no modal
				self.modal = $attrs.sdaModal;
				
				// Dialog type. Default is 'custom'
				self.dialogType = $attrs.sdaType;
				if (!angular.isDefined(self.dialogType)) {
					self.dialogType = SUPPORTED_TYPES.CUSTOM;
				}
				
				self.onOpenFn = $parse($attrs.sdaOnOpen);
				self.onCloseFn = $parse($attrs.sdaOnClose);
				self.onConfirmFn = $parse($attrs.sdaOnConfirm);
				self.confirmAction = confirmDialog;
				self.cancelAction = closeDialog;
				self.isOpen = false;
				
				self.onKeyUp = function(evt){
					if (angular.equals(evt.keyCode, 13)){
						self.confirmAction(); 
					}
					if (angular.equals(evt.keyCode, 27)){
						self.cancelAction(); 
					}
				}
				
				$scope.$watch('showDialog', function(showDialog) {
					if(showDialog === true){
						openDialog();
						$scope.showDialog=false;
					}
			    });
				
				exposeAPI($attrs.sdDialog);

				// Expose closeDialog to the provided scope.
				self.dialogScope.closeThisDialog = closeDialog;
				
				$scope.$on('$destroy', onDestroyDialog);
			}
			
			// Scope destroy function
			function onDestroyDialog() {
				self.dialogElem.detach();
				self.dialogElem.empty();
				self.dialogElem = null;
			}
			
			function setContentWidth(width) {
				if (width) {
					self.dialogElem.find('.modal-dialog').width(width);
				}
			}
			
			function setContentHeight(height) {
				if (height) {
					self.dialogElem.find('.modal-body').height(height);
				}
			}
			
			// API with open & close functions
			function DialogApi() {
				this.open = openDialog;
				
				this.close = closeDialog;
				
				this.confirm = confirmDialog;
			}
			
			// Expose the api to allow open & close events from the parent scope
			// Attribute value sd-dialog='someDialog' will be assigned the api object
			// A button with ng-click='someDialog.open()' would open the dialog
			function exposeAPI(dialogAttr) {
				if (angular.isDefined(dialogAttr) && dialogAttr != '') {
					var dialogAttrAssignable = $parse(dialogAttr).assign;
					if (dialogAttrAssignable) {
						dialogAttrAssignable(self.dialogScope, new DialogApi());
					} else {
						trace.info('Could not expose API for: ' + $attrs.sdDialog + ', expression is not an assignable.');
					}
				}
			}
			
			// Initial config of the dialog.
			function configureDialog() {
				if (!angular.isDefined(self.dialogElem)) {
					self.dialogId = 'modal'
							+ (Math.floor(Math.random() * 9000) + 1000);
					self.dialogElem = $element.find('#modal');
					// Change to unique id
					self.dialogElem.attr('id', self.dialogId);
				}
			}
			
			// Attribute on-open-dialog='someFn(res)' is a function which expects/receives an object with promise
			// promise returns with success if confirm is clicked and fails if dialog is cancelled/closed
			function onOpenDialog() {
				if (angular.isDefined(self.onOpenFn) && angular.isFunction(self.onOpenFn)) {
					self.onOpenFn(self.dialogScope, {res: {promise: self.deferred.promise}});
				}
			}
			
			// Opens the dialog
			function openDialog() {
				// create a new defer on open
				self.deferred = $q.defer();
				
				onOpenDialog();
				
				// Compiles and adds the body content in the right scope
				transcludeBody();
				
				self.isOpen = true;
				
				var backdrop = 'static'; //Default
				if (self.modal == 'false') {
					backdrop = false;
				}
				
				self.dialogElem.modal({
					keyboard: false,
					backdrop: backdrop,
					show: false
				});
				
				self.dialogElem.modal('show');
			}
			
			function transcludeBody() {
				// Transclude using 2 options - by use of template and trancluded content
				// If 'sda-template' is provided, it is the content of the template that gets appended to the popup body
				// else actual directive body contents will be appended.
				self.cloneScope = self.dialogScope.$new();
				$transclude(self.cloneScope, function(clone) {
					var dialogElem = self.dialogElem;
					var templatePlaceHolder = dialogElem.find('.transclude');
					var template = '';
					if (self.useTransclude === false) {
						template = $compile(angular.element('<div ng-include="\'' + self.template + '\'"></div>'))(self.cloneScope);
					} else if (self.useTransclude === true) {
						template = clone;
					}
					templatePlaceHolder.append(template);
					
					setContentWidth($attrs.sdaWidth);
					setContentHeight($attrs.sdaHeight);
				});
			}
			
			// Removes the body content since it is no longer needed in the DOM. 
			// It will be retrieved again when needed ie. Dialog is opened.
			function removeBody() {
				self.cloneScope.$destroy();
				var templatePlaceHolder = self.dialogElem.find('.transclude');
				templatePlaceHolder.empty();
				
				// Remove the element itself from the dom.
				self.dialogElem.detach();
			}
			
			// Close the dialog. Promise returned in on-open-dialog function will get fail function called.
			function closeDialog() {
				if (angular.isDefined(self.onCloseFn) && angular.isFunction(self.onCloseFn)) {
					self.onCloseFn(self.dialogScope, {res: false});
				}
				
				hideDialog();
				if (self.deferred) {
					self.deferred.reject();
				}
			}
			
			function hideDialog() {
				// Remove the heavy body content first.
				removeBody();
				
				if (angular.isDefined(self.dialogElem)) {
					self.dialogElem.modal('hide');
				}
				self.isOpen = false;
			}
			
			// Confirm the dialog. Promise returned in on-open-dialog function will get Success function called.
			function confirmDialog() {
				var userPromise = true;
				if (angular.isDefined(self.onConfirmFn) && angular.isFunction(self.onConfirmFn)) {
					userPromise = self.onConfirmFn(self.dialogScope, {res: true});
					trace.log('Promise returned by the onConfirm: ' + userPromise);
					if (userPromise === undefined) {
						userPromise = true;
					}
				}
				
				if (userPromise === true) {
					hideDialog();
					if (self.deferred) {
						self.deferred.resolve();
					}
				} else if (userPromise.then) {
					trace.log('Waiting for user to resolve the promise returned by onConfirm...');
					userPromise.then(function() {
						hideDialog();
						if (self.deferred) {
							self.deferred.resolve();
						}	
					});
				}
			}
		}
		
		return directiveDefObject;
	}

})();