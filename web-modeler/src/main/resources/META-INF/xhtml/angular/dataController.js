/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Subodh.Godbole
 */
'use strict';

define(["bpm-modeler/angular/app"],
		function(app) {
			var controller = app.angularApp.controller('DataCtrl', DataCtrl);
			DataCtrl.$inject = ['$scope'];
			return {ctrl : controller};
		
			/**
			 *
			 */
			function DataCtrl($scope) {
			};
		});
