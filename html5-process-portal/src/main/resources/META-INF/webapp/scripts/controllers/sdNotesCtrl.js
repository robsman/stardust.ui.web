/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 */

(function() {
	'use strict';

	angular.module("workflow-ui").controller(
			'sdNotesCtrl',
			[ '$scope', 'sdNotesService', 'sdLoggerService', 'sdViewUtilService', 'sdUtilService',
					'sdLoggedInUserService', NotesCtrl ]);
	var _scope;
	var _sdNotesService;
	var _sdViewUtilService;
	var _sdUtilService;
	var trace;
	var rootURL;
	var _sdLoggedInUserService;
	/*
	 * 
	 */
	function NotesCtrl($scope, sdNotesService, sdLoggerService, sdViewUtilService, sdUtilService, sdLoggedInUserService) {
		trace = sdLoggerService.getLogger('workflow-ui.sdNotesCtrl');
		_scope = $scope;
		_sdNotesService = sdNotesService;
		_sdViewUtilService = sdViewUtilService;
		_sdUtilService = sdUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		rootURL = _sdUtilService.getRootUrl();

		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.notesTable = null;

		this.initialize();

		// Register for View Events
		_sdViewUtilService.registerForViewEvents(_scope, this.handleViewEvents, this);

	}

	/**
	 * 
	 * @param event
	 */
	NotesCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			// TODO
		} else if (event.type == "DEACTIVATED") {
			// TODO
		}
	};

	/**
	 * 
	 */
	NotesCtrl.prototype.initialize = function() {
		var self = this;
		self.viewParams = _sdViewUtilService.getViewParams(_scope);
		self.getNotes();
	};
	/**
	 * 
	 */
	NotesCtrl.prototype.getNotes = function() {
		var self = this;
		_sdNotesService.getNotes(self.viewParams.oid).then(function(data) {
			self.notes = data;
			if(self.viewParams.createNote === true && data.totalCount == 0){
				self.addNote();
				delete self.viewParams.createNote;
			}else if(self.viewParams.noteTimestamp != undefined){
				self.initialSelection = {'created' : self.viewParams.noteTimestamp};
				delete self.viewParams.noteTimestamp;
			}else{
				self.initialSelection = {'noteNumber' : data.totalCount };
			}
			self.showNotesTable = true;
		}, function(error) {
			trace.log(error);
		});

	};
	/**
	 * 
	 * @param options
	 * @returns
	 */
	NotesCtrl.prototype.getNotesData = function(options) {
		var self = this;
		return self.notes;
	};

	/**
	 * 
	 */
	NotesCtrl.prototype.addNote = function() {
		var self = this;
		self.isAddMode = true;
		self.note = '';
	};

	/**
	 * 
	 * @param info
	 */
	NotesCtrl.prototype.onSelect = function(info) {
		var self = this;		
		if(self.viewParams.createNote === true){
			self.addNote();
			delete self.viewParams.createNote;
		}else if (info.action == "select") {
			self.note = info.current.note;
			self.isAddMode = false;
		} else {
			self.note = '';
			self.isAddMode = false;
		}
	};

	/**
	 * 
	 * @param userImageURI
	 * @returns
	 */
	NotesCtrl.prototype.getUserImageURL = function(userImageURI) {
		return  (userImageURI.indexOf("/") > -1) ? rootURL + userImageURI : userImageURI;
	};

	/**
	 * 
	 * @returns {Boolean}
	 */
	NotesCtrl.prototype.saveNote = function() {
		var self = this;
		if (_sdUtilService.isEmpty(self.note)) {
			return false;
		} else {
			_sdNotesService.saveNote(self.note, self.viewParams.oid).then(function(success) {
				trace.info("Note saved sucessfully.");
				_sdNotesService.getNotes(self.viewParams.oid).then(function(data) {
					self.notes = data;
					self.notesTable.refresh();
					setTimeout(function() {
						self.notesTable.setSelection({
							'noteNumber' : data.totalCount
						});
					}, 100);
				}, function(error) {
					trace.log(error);
				});

			}, function(error) {
				trace.error("Error Occured when saving the note : ", error);
			});
		}
	};
	/**
	 * 
	 */
	NotesCtrl.prototype.cancelNote = function() {
		var self = this;
		if (self.notes.totalCount > 0) {
			self.notesTable.setSelection({
				'noteNumber' : self.notes.totalCount
			});
		} else {
			self.isAddMode = false;
		}

	};

})();