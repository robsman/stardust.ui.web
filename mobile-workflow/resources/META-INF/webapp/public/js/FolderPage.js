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

define([ "js/Utils", "js/WorkflowService", "js/DocumentContentPage" ],
		function(Utils, WorkflowService, DocumentContentPage) {
			return {
				create : function(deck) {
					var page = new FolderPage();

					page.initialize(deck);

					return page;
				}
			};

			function FolderPage() {
				this.id = "folderPage";

				/**
				 * 
				 */
				FolderPage.prototype.initialize = function(deck, folder) {
					this.deck = deck;

					if (folder) {
						this.folder = folder;
					} else {
						this.root = true;
					}
				};

				/**
				 * 
				 */
				FolderPage.prototype.show = function() {
					var deferred = jQuery.Deferred();
					var self = this;

					WorkflowService.instance().getFolders(this.root ? null : this.folder).done(
							function(folder) {
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
				FolderPage.prototype.openFolderPage = function(subfolder) {
					var subfolderPage = new FolderPage();

					subfolderPage.initialize(this.deck, subfolder);

					this.deck.pushPage(subfolderPage);
				};

				/**
				 * 
				 */
				FolderPage.prototype.openDocumentContentPage = function(
						document) {
					this.deck.pushPage(DocumentContentPage.create(this.deck,
							document));
				};
			}
		});
