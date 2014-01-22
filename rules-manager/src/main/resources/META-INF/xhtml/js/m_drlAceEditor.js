/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 *
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "jquery","bpm-modeler/js/m_utils" ], function(jquery,m_utils) {
	// Interface
	return {
		getCodeEditor : function(textArea) {
			return new CodeEditor(textArea, "ace/mode/html");

		},
		getDrlEditor : function(textArea) {
			return new CodeEditor(textArea, "ace/mode/drl");
		},
		getJSCodeEditor : function(textArea) {
			return new CodeEditor(textArea, "ace/mode/javascript");
		}
	};
	
	function CodeEditor(textArea, mode,options) {
		var that=this; /*For times when this isn't good enough!*/
		
		this.editor = null;
		this.disabled = false;
		this.globalVariables = null;

		this.editor = ace.edit(textArea);
		this.editor.getSession().setMode(mode);
		this.editor.setTheme("ace/theme/chrome");
		
		
		
		/*Base/Wrapper function to load any ace module through the ace.config.loadModule
		 *mechanism. Multiple calls to this with the same module name will only result in
		 *the callback being fired once (the first time the moduel is loaded).*/
		CodeEditor.prototype.loadModule=function(module,callback){
			ace.config.loadModule(module,callback);
		};
		
		/*Tagging a hashMap to our session object to coordinate anything
		 *we may wish to append to our session. Keep in mind that modules are loaded
		 *once per scope of the ace library, so data which you want available to the ace internals
		 *must be scoped by session unless you want that data to be global to all sessions.*/
		CodeEditor.prototype.setSessionData=function(key,val){
			var session=this.editor.getSession();
			if(session.hasOwnProperty("ext_userDefined")===false){
				session["ext_userDefined"]={};
			}
			session["ext_userDefined"][key]=val;
		};
		
		/*Simple retrieval function to get data set by our setSessionData function*/
		CodeEditor.prototype.getSessionData=function(key){
			var session=this.editor.getSession();
			var ret=undefined;
			if(session.hasOwnProperty("ext_userDefined")){
				ret=session["ext_userDefined"][key];
			}
			return ret;
		};
		
		/*Wrap our loadModule function to support easy loading of language tools*/
		CodeEditor.prototype.loadLanguageTools=function(options){
			var defOptions={
					"enableSnippets": false,
					"enableBasicAutocompletion": true
            };
			options=$.extend(defOptions,options);
			this.loadModule("ace/ext/language_tools", function(aceExt) {
				that.editor.setOptions(options);
				$(that).trigger("moduleLoaded",{
					"name": "ace/ext/language_tool",
					"reference": aceExt});
		    });
		};
		
		CodeEditor.prototype.getEditor = function() {
			return this.editor;
		};
		
		
		CodeEditor.prototype.getValue = function() {
			return this.editor.getSession().getDocument().getValue();
		};

		CodeEditor.prototype.setValue = function(val) {
			this.editor.getSession().getDocument().setValue(val);
		};

		CodeEditor.prototype.disable = function() {
			if (!this.disabled) {
				this.editor.setReadOnly(true);
				this.disabled = true;
			}
		};

		CodeEditor.prototype.enable = function() {
			if (this.disabled) {
				this.editor.setReadOnly(false);
				this.disabled = false;
			}
		};

		CodeEditor.prototype.resize = function() {
			this.editor.resize(true);
		};

		CodeEditor.prototype.showGutter = function() {
			this.editor.renderer.setShowGutter(true);
		};

		CodeEditor.prototype.hideGutter = function() {
			this.editor.renderer.setShowGutter(false);
		};

		CodeEditor.prototype.gotoLine = function(lineNo) {
			this.editor.gotoLine(lineNo);
		};
		
		CodeEditor.prototype.setGlobalVariables = function(data) {
			this.globalVariables = data;
			
			// Bind the Model Data as top "window" level objects to be used for Code Editor auto-complete
			for (var key in this.globalVariables) {
				window[key] = this.globalVariables[key];
			}
		};
	}
	

	/**
	 * Temporary workaround for HTML mode Ace editor.
	 * Here we return a simple text area wrapped as an editor.
	 */
	function TextAreaEditor(textAreaId) {
		var textArea = m_utils.jQuerySelect("#" + textAreaId);
		textArea.css("display", "inline");
		
		TextAreaEditor.prototype.getValue = function() {
			return textArea.val();
		};
		
		TextAreaEditor.prototype.setValue = function(value) {
			textArea.val(value);
		};
		
		TextAreaEditor.prototype.getEditor = function () {
			var self = this;
			return {
				on : function(eventType, eventHandler) {
					textArea.change(eventHandler);
				},
				getSession : function() {
					return self;
				}
			};
		};
	}
});