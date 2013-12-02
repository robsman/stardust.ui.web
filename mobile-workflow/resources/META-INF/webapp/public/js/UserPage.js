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
			var page = new UserPage();

			page.initialize(deck);

			return page;
		}
	};

	function UserPage() {
		this.id = "userPage";

		/**
		 * 
		 */
		UserPage.prototype.initialize = function(deck) {
			this.deck = deck;
		};

		/**
		 * 
		 */
		UserPage.prototype.show = function() {
			console.log("User:");
			console.log(this.user);

			var deferred = jQuery.Deferred();

			deferred.resolve();

			return deferred.promise();
		};

		/**
		 * 
		 */
		UserPage.prototype.back = function() {
			this.deck.popPage();
		};
	}
});
