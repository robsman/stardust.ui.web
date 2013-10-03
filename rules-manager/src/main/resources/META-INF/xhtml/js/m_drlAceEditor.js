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
		var editorRef;
		this.editor = null;
		this.disabled = false;
		this.globalVariables = null;

		this.editor = ace.edit(textArea);
		this.editor.getSession().setMode(mode);
		this.editor.setTheme("ace/theme/chrome");
		
		editorRef=this.editor;
		ace.config.loadModule("ace/ext/language_tools", function() {
			editorRef.setOptions({
	            enableSnippets: true,
	            enableBasicAutocompletion: true
	        });
	    });
		
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