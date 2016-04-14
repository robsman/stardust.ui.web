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
						'<button ng-click="suspendAndSave();" title="Complete" ng-disabled="!renderActivityUi">\n' +
							'<span>Suspend & Save</span>\n' +
						'</button>\n' +
						'<button ng-click="suspendAndSave(true);" title="Complete" ng-disabled="!renderActivityUi">\n' +
							'<span>Suspend & Save (User)</span>\n' +
						'</button>\n' +
						'<button ng-click="suspend()" title="Complete" ng-disabled="!renderActivityUi">\n' +
							'<span>Suspend</span>\n' +
						'</button>\n' +
						'<button ng-click="suspend(true)" title="Complete" ng-disabled="!renderActivityUi">\n' +
							'<span>Suspend (User)</span>\n' +
						'</button>\n' +
					'</div>' +
					'<div style="height: 25px;">' +
						'<div><b>{{actionStatus}}</b></div>' +
					'<div>' +
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
			var MA_REST_END_POINT = sdUtilService.getBaseUrl() + 'services/rest/process-portal/manualActivity/';
			var AI_REST_END_POINT = sdUtilService.getBaseUrl() + 'services/rest/portal/activity-instances/';

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
					var iFrameScope = getActivityIframeScope();
					if (iFrameScope.saveData()) {
						var httpResponse = $http.post(AI_REST_END_POINT + 'complete/' + activityOid);
						httpResponse.success(function(data) {
							scope.actionStatus = 'Complete Success';
							scope.renderActivityUi = false;
							console.log(scope.actionStatus, data);
						}).error(function(data) {
							scope.actionStatus = 'Complete Failed';
							console.log(scope.actionStatus, data);
						});
					} else {
						iFrameScope.$apply();
					}
				}

				/*
				 * 
				 */
				scope.suspendAndSave = function(toUser) {
					var iFrameScope = getActivityIframeScope();
					if (iFrameScope.saveData()) {
						var url = AI_REST_END_POINT + 'suspend-and-save/' + activityOid;
						if (toUser) {
							url += '?toUser=true';
						}
						var httpResponse = $http.post(url);
						httpResponse.success(function(data) {
							scope.actionStatus = 'Suspend And Save Success To ' + (toUser ? 'User' : 'Participant');
							scope.renderActivityUi = false;
							console.log(scope.actionStatus, data);
						}).error(function(data) {
							scope.actionStatus = 'Suspend And Save Failed To ' + (toUser ? 'User' : 'Participant');
							console.log(scope.actionStatus, data);
						});
					} else {
						iFrameScope.$apply();
					}
				}

				/*
				 * 
				 */
				scope.suspend = function(toUser) {
					var url = AI_REST_END_POINT + 'suspend/' + activityOid;
					if (toUser) {
						url += '?toUser=true';
					}
					var httpResponse = $http.post(url);
					httpResponse.success(function(data) {
						scope.actionStatus = 'Suspend Success To ' + (toUser ? 'User' : 'Participant');
						scope.renderActivityUi = false;
						console.log(scope.actionStatus, data);
					}).error(function(data) {
						scope.actionStatus = 'Suspend Failed To ' + (toUser ? 'User' : 'Participant');
						console.log(scope.actionStatus, data);
					}); 
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
					if (interactionId) {
						interactionUri = MA_REST_END_POINT + interactionId;
					} else {
						interactionUri = sdUtilService.extractParamsFromUri(params)['ippInteractionUri'];
						if (interactionUri.indexOf('/') == 0) {
							interactionUri = interactionUri.substring(1);
						}
						interactionUri = sdUtilService.getBaseUrl() + interactionUri;

						interactionId = interactionUri.substring(interactionUri.lastIndexOf('/') + 1);
					}

					var decodedId = window.atob(interactionId);
					activityOid = decodedId.substring(0, decodedId.indexOf('|'));
				});
			}

			/*
			 * 
			 */
			function getActivityIframeScope() {
				var activityIframe = element.find('iframe');
				try {
					var iFrameScope = activityIframe[0].contentWindow.angular.element('html').scope();
					return iFrameScope;
				} catch (e) {
					throw 'Error while accessing activity iFrame. Might not be accessible due to cross domain scenario.';
				}
			}
		}
	
		return directiveDefObject;
	}
})();
