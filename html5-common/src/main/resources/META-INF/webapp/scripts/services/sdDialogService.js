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

(function(){
	'use strict';

	angular.module('bpm-common.services').factory('sdDialogService', ['$compile', '$q', '$timeout', 'sdLoggerService', function ($compile, $q, $timeout, sdLoggerService) {
		
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
			alert: function(scope, message, title) {
				if (scope === undefined || scope === null) {
					trace.error('No scope provided. Cannot open Alert dialog.');
					return;
				}
				
				if (!angular.isDefined(title)) {
					title = 'Alert';
				}
				
				var options = {
					title: title,
					type: 'alert',
					cancelActionLabel: 'Close'
				};
				
				var html = '<div>'
							+ message
						 + '</div>';
				
				this.dialog(scope, options, html);
			},
			error: function(scope, message, title) {
				if (scope === undefined || scope === null) {
					trace.error('No scope provided. Cannot open Error dialog.');
					return;
				}
				
				if (!angular.isDefined(title)) {
					title = 'Error';
				}
				
				var options = {
					title: title,
					type: 'alert',
					cancelActionLabel: 'Close'
				};
				var html = '<table style="width : 100%">'+
								'<tr>'+
									'<td style="width : 15% ; align:center"><i  class="glyphicon glyphicon-remove-sign popup-error-icon" ></i> </td>'+
									'<td style="width : 85%">'+message +'</td>'+
								'</tr>'+
							'</table>';
				
				this.dialog(scope, options, html);
			},
			confirm: function(scope, message, title) {
				if (scope === undefined || scope === null) {
					trace.error('No scope provided. Cannot open Confirm dialog.');
					return;
				}
				
				var deferred = $q.defer();
				
				if (!angular.isDefined(title)) {
					title = 'Confirm';
				}
				
				var options = {
					title: title,
					type: 'confirm',
					onClose: function() {
						deferred.reject();
					},
					onConfirm: function() {
						deferred.resolve();
					}
				};
				
				var html = '<div>'
					+ message
				 + '</div>';
				
				this.dialog(scope, options, html);
				
				return deferred.promise;
			},
			
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
			 * }
			 */
			dialog: function(scope, options, html) {
				if (scope === undefined || scope === null) {
					trace.error('No scope provided. Cannot open a dialog.');
					return;
				}
				
				var dialogController = createDialog(scope, options, html);
				$timeout(function() {
					dialogController.dialog.open();
				});
			}
		};
	}]);
})();
