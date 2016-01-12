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
 * @author Aditya.Gaikwad
 */

(function() {
	'use strict';

	angular.module('bpm-common').directive('sdFocus', [ FocusDirective ]);

	/*
	 * 
	 */
	function FocusDirective() {
		return {
			restrict : 'A',
			link : function(scope, element, attrs, controller) {
				element[0].focus();
			}
		};
	}
})();