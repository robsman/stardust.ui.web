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

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.mobile_workflow) {
	bpm.mobile_workflow = {};
}

bpm.mobile_workflow.NotesPage = function NotesPage(processInstanceOid) {
	this.id = "notesPage";
	this.processInstanceOid = processInstanceOid;

	/**
	 * 
	 */
	NotesPage.prototype.initialize = function() {
		$("#createNoteDialog").popup();

		var deferred = jQuery.Deferred();
		var self = this;

		getWorkflowService().getNotes(this.processInstanceOid).done(
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
		getWorkflowService().createNote(this.processInstanceOid,
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
		getDeck().pushPage(new bpm.mobile_workflow.NotePage(note));
	};

	/**
	 * 
	 */
	NotesPage.prototype.back = function() {
		getDeck().popPage();
	};

	/**
	 * 
	 */
	NotesPage.prototype.formatDateTime = function(timestamp) {
		return formatDateTime(new Date(timestamp));
	};
};
