/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Subodh.Godbole
 */

(function() {
    'use strict';

    angular.module('bpm-common').directive(
	    'sdActivityPanel',
	    [ '$parse', '$q', '$timeout', '$http', 'sdUtilService', 'sdViewUtilService', 'sdLoggerService', ActivityPanelDirective ]);

    /*
     *
     */
    function ActivityPanelDirective($parse, $q, $timeout, $http, sdUtilService, sdViewUtilService, sdLoggerService) {
		var trace = sdLoggerService.getLogger('bpm-common.sdActivityPanel');
	
		var directiveDefObject = {
			restrict : 'AE',
			require: ['sdData'],
			scope : { // Creates a new sub scope
				reinit: '=sdaReinit',
				activityUiStyle: '@sdaUiStyle'
			},
			template :
				'<div>' +
					'<div>' +
						'<button ng-click="complete();" title="Complete" ng-disabled="!renderActivityUi">\n' +
							'<span>Complete</span>\n' +
						'</button>\n' +
					'</div>' +
					'<div>{{actionStatus}}</div>' +
					'<div ng-if="renderActivityUi">' +
 						'<iframe frameborder="0" marginwidth="0" marginheight="0" scrolling="auto" ' +
 							'style="{{activityUiStyle}}" ' +
 							'ng-src="{{panelUrl}}" ' +
						'</iframe>' +
					'</div>' +
				'</div>',
			compile : function(elem, attr, transclude) {
				return {
					post : function(scope, element, attr, ctrl) {
						new ActivityPanelCompiler(scope, element, attr, ctrl);
					}
				};
			}
		};

		/*
		 * 
		 */
		function showError (e, element) {
			
		}

		/*
		 *
		 */
		function ActivityPanelCompiler(scope, element, attr, ctrl) {
			var MA_REST_END_POINT = sdUtilService.getBaseUrl() + "services/rest/process-portal/manualActivity/";
			var AI_REST_END_POINT = sdUtilService.getBaseUrl() + "services/rest/portal/activity-instances/";

			var sdData = ctrl[0];
		
			var interactionId, activityOid, interactionUri;

			initialize();

			/*
			 * 
			 */
			function initialize() {
				fetData();

				if (scope.reinit) {
					scope.$watch('reinit', function(newVal, oldVal) {
						if(newVal != undefined && oldVal != newVal) {
							trace.log('sda-reinit flag is triggered, reinitializing activity panel');
							fetData();
						}
					});
				}

				/*
				 * 
				 */
				scope.complete = function() {
					var activityIframe = element.find('iframe');
					var iFrameScope = activityIframe[0].contentWindow.angular.element('html').scope();
					if (iFrameScope.saveData()) {
						var httpResponse = $http.post(AI_REST_END_POINT + "complete/" + activityOid);
						httpResponse.success(function(data) {
							scope.actionStatus = 'Complete Success.';
							scope.renderActivityUi = false;
							console.log('Complete Success', data);
						}).error(function(data) {
							scope.actionStatus = 'Complete Failed.';
							console.log('Complete Failed', data);
						});
					} else {
						iFrameScope.$apply();
					}
				}
			}

			/*
			 * 
			 */
			function fetData() {
				sdData.retrieveData(null).then(function(result) {
					scope.renderActivityUi = true;
					scope.actionStatus = '';

					if (scope.activityUiStyle == undefined || scope.activityUiStyle == '') {
						scope.activityUiStyle = 'width: 850px; height: 350px;';
					}
					
					if (angular.isString(result)) {
						scope.panelUrl = result;
					} else {
						scope.panelUrl = result.panelUrl;
					}

					var params = scope.panelUrl.substring(scope.panelUrl.indexOf('?') + 1);
					
					interactionId = sdUtilService.extractParamsFromUri(params)['interactionId'];
					var decodedId = window.atob(interactionId);
					activityOid = decodedId.substring(0, decodedId.indexOf('|'));

					var urlPrefix = scope.panelUrl.substring(0, scope.panelUrl.indexOf("/plugins"));
					interactionUri = MA_REST_END_POINT + interactionId;
				});
			}
		}
	
		return directiveDefObject;
	}
})();
