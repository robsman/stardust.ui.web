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
			var page = new ProcessPage();

			page.initialize(deck);

			return page;
		}
	};

	function ProcessPage(processInstanceOid) {
		this.id = "processPage";

		/**
		 * 
		 */
		ProcessPage.prototype.initialize = function(deck) {
			this.deck = deck;
		};

		/**
		 * 
		 */
		ProcessPage.prototype.show = function() {
			var deferred = jQuery.Deferred();
			var self = this;

			WorkflowService.instance().getProcessInstance(
					this.processInstanceOid).done(function(processInstance) {
				self.processInstance = processInstance;

				console.log("Process Instance");
				console.log(self.processInstance);

				deferred.resolve();
			}).fail(function() {
				deferred.reject();
			});

			return deferred.promise();
		};

		/**
		 * 
		 */
		ProcessPage.prototype.openUserPage = function(user) {
			this.deck.userPage.user = user;

			this.deck.pushPage(this.deck.userPage);
		};

		/**
		 * 
		 */
		ProcessPage.prototype.back = function() {
			this.deck.popPage();
		};
	}
});
