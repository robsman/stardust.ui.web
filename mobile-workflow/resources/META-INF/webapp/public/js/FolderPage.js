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

bpm.mobile_workflow.FolderPage = function FolderPage(folder) {
	this.id = "folderPage";
	this.folder = folder;

	/**
	 * 
	 */
	FolderPage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();
		var self = this;

		getWorkflowService().getFolders(this.folder).done(function(folder) {

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
		getDeck().popPage();
	};

	/**
	 * 
	 */
	FolderPage.prototype.openFolderPage = function(folder) {
		getDeck().pushPage(new bpm.mobile_workflow.FolderPage(folder));
	};

	/**
	 * 
	 */
	FolderPage.prototype.openDocumentContentPage = function(document) {
		getDeck().pushPage(new bpm.mobile_workflow.DocumentContentPage(document));
	};
};
