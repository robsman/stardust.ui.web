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

	angular.module('bpm-common').directive('sdNumberFilter', [NumberFilterDirective]);

	/*
	 * 
	 */
	function NumberFilterDirective() {
		return {
			restrict: 'A',
			template: 
				'<table cellspacing="2" cellpadding="2" style="white-space: nowrap;">' +
					'<tr>' +
						'<td><label class="label-form">From:</label></td>' +
						'<td><input type="number" ng-model="rowData[colData.name].from" /></td>' +
					'</tr>' +
					'<tr>' +
						'<td><label class="label-form">To:</label></td>' +
						'<td><input type="number" ng-model="rowData[colData.name].to" /></td>' +
					'</tr>' +
				'</table>'
		};
	}
})();