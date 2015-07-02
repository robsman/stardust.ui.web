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

/**
 * @author Nikhil.Gahlot
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdParticipantSelector', ['$q', 'sdUtilService', 
	                                                                 ParticipantSelectorDirective]);

	/*
	 * Directive class
	 */
	function ParticipantSelectorDirective($q, sdUtilService) {
		
		var directiveDefObject = {
				restrict : 'AE',
				require: ['sdData'],
				scope: {  // Creates a new sub scope
					dataSelected: '=sdaSelectedData',
					allowMultiple: '=sdaMultiple',
					autoIdPrefix : '@sdaAidPrefix'
				},
				transclude: true,
				template: '<div sd-auto-complete class="sd-participant-selector"'
						  + ' sda-matches="participantSelectorCtlr.data"' 
				          + ' sda-match-str="participantSelectorCtlr.matchVal"' 
				          + ' sda-change="participantSelectorCtlr.getMatches(matchVal)"' 
				          + ' sda-text-property="{{participantSelectorCtlr.textProperty}}"' 
				          + ' sda-container-class="sd-ac-container"' 
				          + ' sda-item-hot-class="sd-ac-item-isActive"'
				          + ' sda-selected-matches="dataSelected"'
				          + ' sda-allow-multiple="allowMultiple"'
				          + ' sda-tag-pre-class="participantSelectorCtlr.tagPreMapper(item,index)"'
				          + ' sda-item-pre-class="participantSelectorCtlr.tagPreMapper(item,index)"'
				      	  + ' sda-aid-prefix="{{autoIdPrefix}}">'
				          + '</div>',
				link: function(scope, element, attrs, ctrl) {
					new ParticipantSelectorLink(scope, element, attrs, ctrl);
				}
			};
		
		/*
		 * Link class
		 */
		function ParticipantSelectorLink(scope, element, attrs, ctrl) {
			
			var self = this;
			
			var sdData = ctrl[0];
			
			scope.participantSelectorCtlr = self;
			
			initialize();

			ParticipantSelectorLink.prototype.safeApply = function() {
				sdUtilService.safeApply(scope);
			};

			/*
			 * Initialize the component
			 */
			function initialize() {
				// Make sure i18n is available in the current scope
				if (!angular.isDefined(scope.i18n)) {
					scope.i18n = scope.$parent.i18n;
				}

				self.fetchData = fetchParticipantData;

				// Initialize scope values for participant selector
				self.data = [];
				self.matchVal = '';
				self.textProperty = 'name';
				self.tagPreMapper = tagPreMapper;

				/* Retrieve data from the service */
				self.getMatches = function(v) {
					var options = {};
					var dataPromise = self.fetchData(options);
					dataPromise.then(function(data) {
						self.data = data;
					}, function() {
						self.data = [];
					});
				};
				
				if (!angular.isDefined(scope.dataSelected)) {
					scope.dataSelected = [];
				}
			}
			
			function tagPreMapper(item, index) {
				var tagClass = '';

				switch (item.type) {
				case 'USER':
					tagClass = 'sd-particpant-img-tag-user';
					break;
				case 'ROLE':
					tagClass = 'sd-particpant-img-tag-role';
					break;
				case 'ORGANIZATION':
					tagClass = 'sd-particpant-img-tag-org';
					break;
				case 'DEPARTMENT':
					tagClass = 'sd-particpant-img-tag-dept';
					break;
				case 'USERGROUP':
					tagClass = 'sd-particpant-img-tag-ugrp';
					break;
				}
				return tagClass;
			};
			
			function fetchParticipantData() {
				var deferred = $q.defer();
				var dataResult = sdData.retrieveData(self.matchVal);
				dataResult.then(function(data) {
					deferred.resolve(data);
					self.safeApply();
				}, function(error) {
					deferred.reject(error);
				});

				return deferred.promise;
			};
		}
		
		return directiveDefObject;
	}
})();