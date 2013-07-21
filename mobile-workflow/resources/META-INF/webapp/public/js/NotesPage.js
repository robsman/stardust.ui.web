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

bpm.mobile_workflow.NotesPage = function NotesPage() {
	this.id = "notesPage";

	/**
	 * 
	 */
	NotesPage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();
	};

	/**
	 * 
	 */
	NotesPage.prototype.createNote = function(process) {
		getWorkflowService().createNote(process).done(
				function(note) {
						getDeck().pushPage(
								new bpm.mobile_workflow.NotePage(note));
				});
	};

	/**
	 * 
	 */
	NotesPage.prototype.back = function() {
		getDeck().popPage();				
	};
};
