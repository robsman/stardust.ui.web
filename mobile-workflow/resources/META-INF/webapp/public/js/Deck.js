/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "js/Utils", "js/LoginPage", "js/DashboardPage",
		"js/StartableProcessesPage", "js/WorklistPage",
		"js/ActivityInstancePage", "js/ReportsPage", "js/ReportPage",
		"js/FolderPage", "js/DocumentContentPage", "js/SearchPage", "js/NotesPage", "js/NotePage", "js/ProcessPage", "js/UserPage" ], function(
		Utils, LoginPage, DashboardPage, StartableProcessesPage, WorklistPage,
		ActivityInstancePage, ReportsPage, ReportPage, FolderPage,
		DocumentContentPage, SearchPage, NotesPage, NotePage, ProcessPage, UserPage) {
	return {
		create : function() {
			return new Deck();
		}
	};

	function Deck() {
		this.pages = [];

		/**
		 * 
		 */
		Deck.prototype.initialize = function() {
			this.loginPage = LoginPage.create(this);
			this.dashboardPage = DashboardPage.create(this);
			this.startableProcessesPage = StartableProcessesPage.create(this);
			this.worklistPage = WorklistPage.create(this);
			this.activityInstancePage = ActivityInstancePage.create(this);
			this.reportsPage = ReportsPage.create(this);
			this.reportPage = ReportPage.create(this);
			this.folderPage = FolderPage.create(this);
			this.documentPage = DocumentContentPage.create(this);
			this.searchPage = SearchPage.create(this);
			this.notesPage = NotesPage.create(this);
			this.notePage = NotePage.create(this);
			this.processPage = ProcessPage.create(this);
			this.userPage = UserPage.create(this);

			this.pushPage(this.loginPage);
			this.externalWebAppUrl = "./empty.html";
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
			var self = this;

			page.show().done(function() {
				self.pages.push(page);

				console.log("\nPush Page - Page Stack:");

				for ( var n = 0; n < self.pages.length; ++n) {
					console.log("#" + n + ": " + self.pages[n].id);
				}

				self.safeApply();
			}).fail();
		};

		/**
		 * 
		 */
		Deck.prototype.popPage = function() {
			var self = this;

			this.getTopPage().show().done(function() {
				self.pages.pop();

				console.log("\nPop Page - Page Stack:");

				for ( var n = 0; n < self.pages.length; ++n) {
					console.log("#" + n + ": " + self.pages[n].id);
				}

				self.safeApply();
			}).fail();
		};

		/**
		 * 
		 */
		Deck.prototype.getTopPage = function() {
			return this.pages[this.pages.length - 1];
		};

		/**
		 * 
		 */
		Deck.prototype.safeApply = function(fn) {
			var phase = this.$root.$$phase;

			if (phase == '$apply' || phase == '$digest') {
				if (fn && (typeof (fn) === 'function')) {
					fn();
				}
			} else {
				this.$apply(fn);
			}
		};

		Deck.prototype.formatDateTime = function(dateTime) {
			if (dateTime) {
				return Utils.formatDateTime(new Date(dateTime));
			} else {
				return "-";
			}
		};
	}

});