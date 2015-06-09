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
			[ '$q', '$scope', '$element', 'sdNotesService', 'sdLoggerService', 'sdViewUtilService',
					NotesCtrl ]);
	var _q;
	var _scope;
	var _element;
	var _sdNotesService;
	var _sdViewUtilService;
	var trace;

	/*
	 * 
	 */
	function NotesCtrl($q, $scope, $element, sdNotesService, sdLoggerService,
			sdViewUtilService) {
		trace = sdLoggerService.getLogger('workflow-ui.sdNotesCtrl');
		_q = $q;
		_scope = $scope;
		_element = $element;
		_sdNotesService = sdNotesService;
		_sdViewUtilService = sdViewUtilService;

		this.columnSelector = 'admin';
		this.notesTable = null;
		this.rowSelectionNotesTable = null;
        
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
			this.refreshOnlyActiveTab();
		} else if (event.type == "DEACTIVATED") {
			// TODO
		}
	};

	/**
	 * 
	 */
	NotesCtrl.prototype.getNotes = function() {
		var self = this;
		_sdNotesService.getNotes(self.viewParams.oid).then(function(data) {
			self.notes = data;
			self.showNotesTable = true;
		}, function(error) {
			trace.log(error);
		});

	};
	
	NotesCtrl.prototype.getNotesData = function(options){
		var self = this;
		return self.notes;
	};
	
	NotesCtrl.prototype.addNote= function(){
		var self = this;
		self.isAddMode = true;
		self.note = '';
	};
	
	NotesCtrl.prototype.onSelect = function(info) {
		var self = this;
		if (info.action == "select") {
			self.note = info.current.note;
			self.isAddMode = false;
		} else {
			self.note = '';
			self.isAddMode = false;
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


})();