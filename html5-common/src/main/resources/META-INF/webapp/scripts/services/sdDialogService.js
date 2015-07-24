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
/*
 * options: {
 * 		show: ...,
 * 		title: ...,
 *		confirmActionLabel: ...,
 *		cancelActionLabel: ...,
 *		template: ...,
 *		draggable: ...,
 *		showClose: ...,
 *		width: ...,
 *		height: ...,
 *		onOpen: ...,
 *		onClose: ...,
 *		onConfirm: ...,
 *		modal: ...,
 *     dialogActionType:... can be (OK_CANCEL,OK_CLOSE,APPLY_CANCEL,APPLY_CLOSE,YES_NO,SUBMIT_CANCEL,SUBMIT_CLOSE,CONTINUE_CANCEL,CONTINUE_CLOSE)
 * }
 */

(function(){
	'use strict';

	angular.module('bpm-common.services').factory('sdDialogService', ['$compile', '$q', '$timeout', 'sdLoggerService', 'sgI18nService', function ($compile, $q, $timeout, sdLoggerService, sgI18nService) {
		
		var trace = sdLoggerService.getLogger('bpm-common.services.sdDialogService');
		
		function addAttribute(controller, attrSnakeCase, attrValue) {
			var attrKey = attrSnakeCase.substr('sda-'.length);
			var key = jQuery.camelCase(attrKey);
			if (angular.isDefined(controller[key]) && controller[key] != null) {
				if (!angular.isDefined(attrValue)) {
					attrValue = controller[key];
				}
				return ' ' + attrSnakeCase + '="' + attrValue + '"';
			}
			return '';
		}
		
		function createDialog(scope, options, html) {
			
			if (options.width == undefined) {
				options.width = '300';
			}			
			var dialogController = 'dialogControllerUnique_' + (Math.floor(Math.random() * 9000) + 1000);
			scope[dialogController] = angular.extend({dialog: {}}, options);
			
			scope[dialogController].onClose = function(res) {
				if (angular.isDefined(options.onClose) && angular.isFunction(options.onClose)) {
					options.onClose(scope, {res: res});
				}
				
				// cleanup the scope
				delete scope[dialogController];
			};
			
			scope[dialogController].onConfirm = function(res) {
				var returnFn;
				if (angular.isDefined(options.onConfirm) && angular.isFunction(options.onConfirm)) {
					returnFn = options.onConfirm(scope, {res: res});
				}
				
				// cleanup the scope
				delete scope[dialogController];
				
				return returnFn;
			};
			
			var dialogDivStr = '<span' 
				+ ' sd-dialog=' + dialogController + '.dialog'
				+ addAttribute(scope[dialogController], 'sda-show', dialogController + '.show')
				+ addAttribute(scope[dialogController], 'sda-title', '{{' + dialogController + '.title}}')
				+ addAttribute(scope[dialogController], 'sda-confirm-action-label', '{{' + dialogController + '.confirmActionLabel}}')
				+ addAttribute(scope[dialogController], 'sda-cancel-action-label', '{{' + dialogController + '.cancelActionLabel}}')
				+ addAttribute(scope[dialogController], 'sda-on-open', dialogController + '.onOpen(res)"')
				+ addAttribute(scope[dialogController], 'sda-on-confirm', dialogController + '.onConfirm(res)"')
				+ addAttribute(scope[dialogController], 'sda-on-close', dialogController + '.onClose(res)"')
				+ addAttribute(scope[dialogController], 'sda-template', dialogController + '.template')
				+ addAttribute(scope[dialogController], 'sda-show-close', dialogController + '.showClose')
				+ addAttribute(scope[dialogController], 'sda-type')
				+ addAttribute(scope[dialogController], 'sda-modal')
				+ addAttribute(scope[dialogController], 'sda-width')
				+ addAttribute(scope[dialogController], 'sda-height')
				+ addAttribute(scope[dialogController], 'sda-draggable')
				+ ' sda-scope="this">'
				+ html
				+ '</span>';
				
			var dialogElem = angular.element(dialogDivStr);
			$compile(dialogElem)(scope);
			
			return scope[dialogController];
		}
		
		return {
		    /*
		     * A simple alert dialog that takes message and title
		     */
			alert: function(scope, message, options) {
				if (scope === undefined || scope === null) {
					trace.error('No scope provided. Cannot open Alert dialog.');
					return;
				}
				
				if (!angular.isDefined(options.title)) {
					options.title = 'Alert';
				}
				
				options.type = 'alert';

				if (options.cancelActionLabel == undefined) {
					options.cancelActionLabel = 'Close';
				}
				
				var html = '<div>'
							+ message
						 + '</div>';
				
				this.dialog(scope, options, html);
			},
			error: function(scope, message, options) {

			    if (!angular.isDefined(options.title)) {
			    	options.title = 'Error';
			    }
			    var html = '<table style="width : 100%">'+
			    '<tr>'+
			    '<td style="width : 15% ; align:center"><i  class="glyphicon glyphicon-remove-sign popup-error-icon" ></i> </td>'+
			    '<td style="width : 85%">'+message +'</td>'+
			    '</tr>'+
			    '</table>';

			    this.alert(scope,html,options);
			},
			info: function(scope, message, options) {

			    if (!angular.isDefined(options.title)) {
			    	options.title = 'Information';
			    }

			    var html = '<table style="width : 100%">'+
			    '<tr>'+
			    '<td style="width : 15% ; align:center"><i  class="sc sc-info-circle popup-info-icon icon-lg" ></i> </td>'+
			    '<td style="width : 85%">'+message +'</td>'+
			    '</tr>'+
			    '</table>' 
			    this.alert(scope,html,options);
			},
			confirm: function(scope, message, options) {
				if (scope === undefined || scope === null) {
					trace.error('No scope provided. Cannot open Confirm dialog.');
					return;
				}
				
				var deferred = $q.defer();
				
				if (!angular.isDefined(options.title)) {
					options.title = 'Confirm';
				}

				options.onClose = function() {
					deferred.reject();
				};
				options.onConfirm = function() {
					deferred.resolve();
				};
				options.type = 'confirm';

				if (options.dialogActionType != undefined) {
					switch (options.dialogActionType) {
					case 'OK_CANCEL':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-ok');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-cancel');
						break;
					case 'OK_CLOSE':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-ok');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-close');
						break;
					case 'APPLY_CANCEL':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-apply');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-cancel');
						break;
					case 'APPLY_CLOSE':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-apply');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-close');
						break;
					case 'SUBMIT_CANCEL':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-submit');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-cancel');
						break;
					case 'YES_NO':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-yes');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-no');
						break;
					case 'SUBMIT_CLOSE':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-submit');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-close');
						break;
					case 'CONTINUE_CANCEL':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-continue');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-cancel');
						break;
					case 'CONTINUE_CLOSE':
						options.confirmActionLabel = sgI18nService.translate('html5-common.common-continue');
						options.cancelActionLabel = sgI18nService.translate('html5-common.common-close');
						break;
					}

				} else {
					if (options.confirmActionLabel == undefined) {

						options.confirmActionLabel = sgI18nService
								.translate('html5-common.common-yes');
					}

					if (options.cancelActionLabel == undefined) {
						options.cancelActionLabel = sgI18nService
								.translate('html5-common.common-no');
					}
				}

				var html = '<div>' + message + '</div>';

				this.dialog(scope, options, html);

				return deferred.promise;
			},
			dialog: function(scope, options, html) {
				if (scope === undefined || scope === null) {
					trace.error('No scope provided. Cannot open a dialog.');
					return;
				}
				
				var dialogController = createDialog(scope, options, html);
				$timeout(function() {
					dialogController.dialog.open();
				}, 100);
			}
		};
	}]);
})();
