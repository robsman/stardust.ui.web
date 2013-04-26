/*******************************************************************************
 * @license
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
/*global define setTimeout*/
define(['orion/Deferred'], function(Deferred) {
	/**
	 * Helper for generating a setup-invoke-teardown test case.
	 * @name orion.test.makeTest
	 * @param {Function} setUp Invoked before the testBody is attempted. This function can return a promise.
	 * @param {Function} tearDown Invoked after the testBody has been attempted.
	 * @param {Function} testBody The test body. This can return a promise or an immediate result.
	 * @returns {Function} An asynchronous test function.
	 */
	function makeTest(setUp, tearDown, testBody) {
		return function() {
			var d = new Deferred();
			Deferred.when(setUp(), function() {
				try {
					var result = testBody();
					if (result && result.then) {
						return result.then(
							function(r) {
								tearDown();
								d.resolve(r);
							},
							function(e) {
								tearDown();
								d.reject(e);
							});
					} else {
						tearDown();
						d.resolve(result);
					}
				} catch(e) {
					tearDown();
					d.reject(e);
				}
			});
			return d;
		};
	}
	return {
		makeTest: makeTest
	};
});