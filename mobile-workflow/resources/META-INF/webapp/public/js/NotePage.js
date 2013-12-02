/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * 
 */

define([ "js/Utils", "js/WorkflowService" ], function(Utils, WorkflowService) {
	return {
		create : function(deck) {
			var page = new NotePage();

			page.initialize(deck);

			return page;
		}
	};

	function NotePage() {
		this.id = "notePage";

		/**
		 * 
		 */
		NotePage.prototype.initialize = function(deck) {
			this.deck = deck;
		};

		/**
		 * 
		 */
		NotePage.prototype.show = function() {
			var deferred = jQuery.Deferred();

			deferred.resolve();

			return deferred.promise();
		};

		/**
		 * 
		 */
		NotePage.prototype.back = function() {
			this.deck.popPage();
		};
	}
});
