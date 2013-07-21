/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.mobile_workflow) {
	bpm.mobile_workflow = {};
}

if (!window.bpm.mobile_workflow.LoginPage) {
	bpm.mobile_workflow.Deck = function Deck() {
		this.pages = [];

		/**
		 * 
		 */
		Deck.prototype.initialize = function(angular, page) {
			console.log("Angular");
			console.log(angular);

			this.pages.push(page);

			var angularModule = angular.module('angularApp', []);

			angular.bootstrap(document, [ 'angularApp' ]);

			this.scope = angular.element(document.body).scope();

			console.log("Scope");
			console.log(this.scope);

			this.externalWebAppUrl = "./empty.html";

			jQuery.extend(this.scope, this);
			inheritMethods(this.scope, this);

			page.initialize();
			
			return this.scope;
		};

		/**
		 * 
		 */
		Deck.prototype.getExternalWebAppUrl = function() {
			console.log("Hallo" + this.getTopPage().id);
			console.log(this.getTopPage().externalWebAppUrl);
			return this.getTopPage().externalWebAppUrl == null ? './empty.html'
					: this.getTopPage().externalWebAppUrl;
		};

		/**
		 * 
		 */
		Deck.prototype.pushPage = function(page) {
			this.pages.push(page);

			console.log("\nPush Page - Page Stack:");

			for ( var n = 0; n < this.pages.length; ++n) {
				console.log("#" + n + ": " + this.pages[n].id);
			}

			var self = this;

			page.initialize().done(function() {
				self.$apply();

				$.mobile.changePage("#" + page.id, {
					transition : "none"
				});
			}).fail();
		};

		/**
		 * 
		 */
		Deck.prototype.popPage = function() {
			this.pages.pop();

			console.log("\nPop Page - Page Stack:");

			for ( var n = 0; n < this.pages.length; ++n) {
				console.log("#" + n + ": " + this.pages[n].id);
			}

			var self = this;

			this.getTopPage().initialize().done(function() {
				self.$apply();

				$.mobile.changePage("#" + self.getTopPage().id, {
					transition : "none"
				});
			}).fail();
		};

		/**
		 * 
		 */
		Deck.prototype.getTopPage = function() {
			return this.pages[this.pages.length - 1];
		};
	};
}

/**
 * Singleton function
 */
function getDeck() {
	return window.top.deck;
}

/**
 * 
 * @returns
 */
function getWorkflowService() {
	if (window.top.workflowService == null) {
		window.top.workflowService = new WorkflowService();
		// window.top.workflowService = new TestWorkflowService();
	}

	return window.top.workflowService;
}

/**
 * Auxiliary function to copy all methods from the parentObject to the
 * childObject.
 */
function inheritMethods(childObject, parentObject) {
	for ( var member in parentObject) {
		if (parentObject[member] instanceof Function) {
			childObject[member] = parentObject[member];
		}
	}
}

// TODO Utils

/**
 * 
 */
function formatDateTime(dateTime) {
	return pad(dateTime.getUTCDate(), 2) + "."
			+ pad(dateTime.getUTCMonth() + 1, 2) + "."
			+ dateTime.getUTCFullYear() + " " + pad(dateTime.getUTCHours(), 2)
			+ ":" + pad(dateTime.getUTCMinutes(), 2);
};

function pad(number, characters) {
	return (1e15 + number + // combine with large number
	"" // convert to string
	).slice(-characters); // cut leading "1"
};
