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
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdData', ['$parse', '$q', DataDirective]);

	/*
	 * 
	 */
	function DataDirective($parse, $q) {
		return {
			restrict : 'A',
			priority: 700,
			compile: function(elem, attr, transclude) {
				// TODO: pre-processing
				return function() {};
			},
			controller: ['$attrs', '$scope', '$element', DataController]
		};

		/*
		 * 
		 */
		function DataController($attrs, $scope, $element) {
			var self = this;

			// TODO: Support different modes to retrieve data
			var dataAsFunction = $attrs.sdData.indexOf('(') != -1;
			var currentScope = $scope;

			var dataGetter = $parse($attrs.sdData);

			self.retrieveData = retrieveData;

			/*
			 * params: Different params passed, like sort, filters, etc...
			 * Returns promise
			 */
			function retrieveData (params) {
				var deferred = $q.defer();
				
				if(dataAsFunction) {
					var object = dataGetter(currentScope, {params: params});

					// It's a function returning deferred promise
					if (angular.isFunction(object.then)) {
						object.then(function(data) {
							deferred.resolve(data);
						}, function(error) {
							deferred.reject(error);
						});
					} else { // It's a function returning data
						deferred.resolve(object);
					}
				} else { // It's a simple scope data
					deferred.resolve(object);
				}

				return deferred.promise;
			};
		}
	}
})();