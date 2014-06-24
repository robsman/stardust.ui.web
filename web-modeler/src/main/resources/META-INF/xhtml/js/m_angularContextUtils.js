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
 * Used to run a piece of code (usually non-angular) in angular context
 *
 * @author Shrikant.Gangal
 * @author Subodh.Godbole
 */
define(["bpm-modeler/js/m_utils"], function(m_utils) {
	return {
		runInAngularContext : runInAngularContext,
		runInActiveViewContext : runInActiveViewContext
	};

	/*
	 * 
	 */
	function runInAngularContext(func, div) {
		var scope = angular.element(div == undefined ? document.body : div).scope();
		if (!scope.$$phase) {
			scope.$apply(func);
		} else {
			func(scope);
		}
	}

	/*
	 * 
	 */
	function runInActiveViewContext(func) {
		runInAngularContext(func, m_utils.activeViewElement());
	}
});