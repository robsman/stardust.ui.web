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
			var page = new FolderPage();

			page.initialize(deck);

			return page;
		}
	};

	function FolderPage(folder) {
		this.id = "folderPage";

		/**
		 * 
		 */
		FolderPage.prototype.initialize = function(deck) {
			this.deck = deck;
		};

		/**
		 * 
		 */
		FolderPage.prototype.show = function() {
			var deferred = jQuery.Deferred();
			var self = this;

			WorkflowService.instance().getFolders(this.folder).done(
					function(folder) {

						console.debug("Folder");
						console.debug(folder);

						self.folder = folder;

						deferred.resolve();
					}).fail(function() {
				deferred.reject();
			});

			return deferred.promise();
		};

		/**
		 * 
		 */
		FolderPage.prototype.back = function() {
			this.deck.popPage();
		};

		/**
		 * 
		 */
		FolderPage.prototype.openFolderPage = function(folder) {
			this.deck.folderPage.folder = folder;
			this.deck.pushPage(this.deck.folderPage);
		};

		/**
		 * 
		 */
		FolderPage.prototype.openDocumentContentPage = function(document) {
			this.deck.documentContentPage.document = document;
			this.deck.pushPage(this.deck.documentContentPage);
		};
	}
});
