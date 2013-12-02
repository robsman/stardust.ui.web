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
			var page = new NotesPage();

			page.initialize(deck);

			return page;
		}
	};

	function NotesPage(processInstanceOid) {
		this.id = "notesPage";

		/**
		 * 
		 */
		NotesPage.prototype.initialize = function(deck) {
			this.deck = deck;
		};

		/**
		 * 
		 */
		NotesPage.prototype.show = function() {
			$("#createNoteDialog").popup();

			var deferred = jQuery.Deferred();
			var self = this;

			WorkflowService.instance().getNotes(this.processInstanceOid).done(
					function(notes) {
						self.notes = notes;

						console.log("Notes");
						console.log(self.notes);

						deferred.resolve();
					}).fail(function() {
				deferred.reject();
			});

			return deferred.promise();
		};

		/**
		 * 
		 */
		NotesPage.prototype.createNote = function(content) {
			WorkflowService.instance().createNote(this.processInstanceOid,
					this.newNoteContent).done(function(note) {
				$("#createNoteDialog").popup("close");
			});
		};

		/**
		 * 
		 */
		NotesPage.prototype.openCreateNoteDialog = function() {
			$("#createNoteDialog").popup("open");
		};

		/**
		 * 
		 */
		NotesPage.prototype.openNotePage = function(note) {
			this.deck.notePage.not = note;
			this.deck.pushPage(this.deck.notePage);
		};

		/**
		 * 
		 */
		NotesPage.prototype.back = function() {
			this.deck.popPage();
		};
	}
});
