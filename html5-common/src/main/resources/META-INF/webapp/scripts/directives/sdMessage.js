/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/
/*
 * Usage: <div sd-message="<optional-namespace>"></div>
 */
angular
		.module('bpm-common.directives')
		.directive(
				'sdMessage',
				[
						'eventBus',
						function(eventBus) {
							return {
								scope : {
									messageKey : '@sdMessage'
								},
								template : '<table ng-show="showError" style="width : 100%">'
										+ '<tr>'
											+ '<td style="width : 1em ; align:center">'
												+ '<i ng-if="type == \'error\'" class="glyphicon glyphicon-remove-sign msg-{{type}}"></i>'
												+ '<i ng-if="type == \'info\'" class="glyphicon glyphicon-exclamation-sign msg-{{type}}"></i>'
												+ '<i ng-if="type == \'warn\'" class="glyphicon glyphicon-exclamation-sign msg-{{type}}"></i>'
											+ '</td>'
											+ '<td><span ng-bind="message" class="msg-{{type}}"></span</td>'
										+ '</tr>'
										+ '</table>',
								link : function($scope) {

									var initScope = function(e, m) {
										console.log(m);
										
										$scope.message = m;
										if (angular.isObject($scope.message)) {
											$scope.type = m.type;
											$scope.message = m.message;
										}
										
										if (!angular.isDefined($scope.type)) {
											$scope.type = 'error';
										}
										
										$scope.showError = true;
									};

									var resetScope = function(e, m) {
										console.log(m);

										$scope.type = undefined;
										$scope.message = undefined;
										
										$scope.showError = false;
									};
									
									if (!angular.isDefined($scope.messageKey)) {
										$scope.messageKey = 'global.error';
									}

									eventBus.onMsg($scope.messageKey, initScope, $scope);

									eventBus.onMsg($scope.messageKey + ".reset", resetScope, $scope);
								}
							};
						} ]);