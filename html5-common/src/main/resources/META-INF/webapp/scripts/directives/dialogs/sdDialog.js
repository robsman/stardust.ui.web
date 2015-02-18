(function(){
	'use strict';

	angular.module('bpm-common.directives').directive('sdDialog', ['sdLoggerService', '$compile', '$document', DialogDirectiveFn]);
	
	/*
	 * Directive class
	 * Attributes supported:
	 * 		sda-show (=)
	 * 		sda-title (@)
	 * 		sda-type (@)
	 * 		sda-scope: ($Scope object)
	 * 		sda-confirm-action-label: (@)	
	 * 		sda-cancel-action-label: (@)
	 * 		sda-template: (@)
	 * 		sda-draggable: (@)
	 * 		sda-show-close: (@)
	 * 		sda-width: (@)
	 * 		sda-height: (@)
	 * 		sda-on-open: (@) Eg: func(res)
	 * 		sda-on-close: (@) Eg: func(res)
	 * 		sda-on-confirm: (@) Eg: func(res)
	 */
	function DialogDirectiveFn(sdLoggerService, $compile, $document) {
		var trace = sdLoggerService.getLogger('bpm-common.directives.sdDialog');
		
		var SUPPORTED_TYPES = {
				ALERT: 'alert',        // Alert popup with one button
				CONFIRM: 'confirm',    // Confirm popup with two action buttons
				CUSTOM: 'custom'       // Customized popup with no action button
			};
		
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
			templateUrl: 'plugins/html5-common/scripts/directives/dialogs/templates/dialog.html',
			controller: ['$attrs', '$scope', '$element', '$parse', '$timeout', '$q', DialogControllerFn],
			compile: DialogCompilerFn
		};
		
		/*
		 *Compiler Function class
		 */
		function DialogCompilerFn(elem, attr, transcludeFn) {
			
			var x0, //Inital x
            y0, //Inital y
            x1, //tracked x pos
            y1; //tracked y pos
			
			var dialogElem = elem.find('#modal');
			
			if (attr.sdaDraggable == 'true') {
				bindDraggable();
			}
			
			function bindDraggable() {
				// Make sure to bind mouse event only on the header
				var headerElem = dialogElem.find('.modal-header');
				headerElem.css({cursor: 'all-scroll'});
				headerElem.bind('mousedown', function($event) {
					// Now make the dialog draggable by noting down the position of the mouse cursor
			          x1 = dialogElem.prop('offsetLeft');
			          y1 = dialogElem.prop('offsetTop');
			          x0 = $event.clientX;
			          y0 = $event.clientY;
			          $document.bind('mousemove', mouseMove);
			          $document.bind('mouseup', mouseUp);
			          return false;
		        });
			}
		 
	        function mouseMove($event) {
	          var dx = $event.clientX - x0,
	              dy = $event.clientY - y0;
	              
	          dialogElem.css({
	            top:  y1 + dy + 'px',
	            left: x1 + dx + 'px'
	          });
	          return false;
	        }
	 
	        function mouseUp() {
	          $document.unbind('mousemove', mouseMove);
	          $document.unbind('mouseup', mouseUp);
	        }
			
			return function (scope, element, attrs) { // Link Function
				DialogLinkFn(scope, transcludeFn);
			}
		}
		
		/*
		 * Link function class
		 */
		function DialogLinkFn(scope, transcludeFn) {
			var templateUrl = scope.template;
			
			var dialogElem = scope.dialogController.dialogElem;
			//scope.dialogController.dialogElem.detach();
			
			// Transclude using 2 options - by use of template and trancluded content
			// If 'sda-template' is provided, it is the content of the template that gets appended to the popup body
			// else actual directive body contents will be appended.
			transcludeFn(scope.dialogController.dialogScope, function(clone, innerScope) {
				var templatePlaceHolder = dialogElem.find('.transclude');
				var template = '';
				if (scope.dialogController.useTransclude === false) {
					template = $compile(angular.element('<div ng-include="\'' + templateUrl + '\'"></div>'))(innerScope);
				} else if (scope.dialogController.useTransclude === true) {
					template = clone;
				}
				templatePlaceHolder.append(template);
			});
		}
		
		/*
		 *Controller Function class
		 */
		function DialogControllerFn($attrs, $scope, $element, $parse, $timeout, $q) {
			
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
				self.useTransclude = !angular.isDefined($scope.template);
				
				// Dialog type. Default is 'custom'
				self.dialogType = $attrs.sdaType;
				if (!angular.isDefined(self.dialogType)) {
					self.dialogType = SUPPORTED_TYPES.CUSTOM;
				}
				
				setContentWidth($attrs.sdaWidth);
				setContentHeight($attrs.sdaHeight);
				
				self.onOpenFn = $parse($attrs.sdaOnOpen);
				self.onCloseFn = $parse($attrs.sdaOnClose);
				self.onConfirmFn = $parse($attrs.sdaOnConfirm);
				self.confirmAction = confirmDialog;
				self.cancelAction = closeDialog;
				self.isOpen = false;
				
				$scope.$watch('showDialog', function(showDialog) {
					if(showDialog === true){
						openDialog();
						$scope.showDialog=false;
					}
			    });
				
				exposeAPI($attrs.sdDialog);
				
				//TODO deprecated - to be removed
				self.dialogScope.closeThisDialog = closeDialog;
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
					self.dialogId = 'modal' + _.uniqueId();
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
				
				self.isOpen = true;
				
				self.dialogElem.modal({
					keyboard: false,
					backdrop: 'static'
				})
				self.dialogElem.modal('show');
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