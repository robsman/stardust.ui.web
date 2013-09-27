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
			this.pages.push(page);

			angular.module('angularApp', []);
			angular.bootstrap(document, [ 'angularApp' ]);

			this.scope = angular.element(document.body).scope();

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
				try {
					self.$apply();
				} catch (x) {
					console.log(x);
				}

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
				// try {
				// self.$apply();
				// } catch (x) {
				// console.log(x);
				// }

				console.log("Changing page to " + self.getTopPage().id);

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