/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "js/WorkflowService", "js/FolderPage" ], function(WorkflowService, FolderPage) {
	return {
		create : function(deck) {
			var page = new DashboardPage();

			page.initialize(deck);

			return page;
		}
	};

	function DashboardPage() {
		this.id = "dashboardPage";

		/**
		 * 
		 */
		DashboardPage.prototype.initialize = function(deck) {
			this.deck = deck;
		};

		/**
		 * 
		 */
		DashboardPage.prototype.show = function() {
			var deferred = jQuery.Deferred();
			var self = this;

			// TODO Replace by more efficient call

			WorkflowService.instance().getWorklist().done(function(worklist) {
				self.worklistSize = worklist.length;

				deferred.resolve();
			}).fail(function() {
				deferred.reject();
			});

			return deferred.promise();
		};

		/**
		 * 
		 */
		DashboardPage.prototype.logout = function() {
			var self = this;

			WorkflowService.instance().logout().done(function() {
				self.deck.popPage();
			});
		};

		/**
		 * 
		 */
		DashboardPage.prototype.openWorklistPage = function() {
			this.deck.pushPage(this.deck.worklistPage);
		};

		/**
		 * 
		 */
		DashboardPage.prototype.openStartableProcessesPage = function() {
			this.deck.pushPage(this.deck.startableProcessesPage);
		};

		/**
		 * 
		 */
		DashboardPage.prototype.openReportsPage = function() {
			this.deck.pushPage(this.deck.reportsPage);
		};

		/**
		 * 
		 */
		DashboardPage.prototype.openSearchPage = function() {
			this.deck.pushPage(this.deck.searchPage);
		};

		/**
		 * 
		 */
		DashboardPage.prototype.openFolderPage = function() {
			this.deck.pushPage(FolderPage.create(this.deck));
		};
	}
});
