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

define([ "jquery" ], function(jquery) {
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

	function CodeEditor(textArea, mode) {

		var editor = null;
		var disabled = false;
		var globalVariables = null;

		editor = ace.edit(textArea);
		editor.getSession().setMode(mode);
		editor.setTheme("ace/theme/chrome");

		CodeEditor.prototype.getEditor = function() {
			return editor;
		};

		CodeEditor.prototype.getValue = function() {
			return editor.getSession().getDocument().getValue();
		};

		CodeEditor.prototype.setValue = function(val) {
			editor.getSession().getDocument().setValue(val);
		};

		CodeEditor.prototype.disable = function() {
			if (!disabled) {
				editor.setReadOnly(true);
				disabled = true;
			}
		};

		CodeEditor.prototype.enable = function() {
			if (disabled) {
				editor.setReadOnly(false);
				disabled = false;
			}
		};

		CodeEditor.prototype.resize = function() {
			editor.resize(true);
		};

		CodeEditor.prototype.showGutter = function() {
			editor.renderer.setShowGutter(true);
		};

		CodeEditor.prototype.hideGutter = function() {
			editor.renderer.setShowGutter(false);
		};

		CodeEditor.prototype.gotoLine = function(lineNo) {
			editor.gotoLine(lineNo);
		};

		CodeEditor.prototype.setGlobalVariables = function(data) {
			globalVariables = data;

			// Bind the Model Data as top "window" level objects to be used for Code Editor auto-complete
			for (var key in globalVariables) {
				window[key] = globalVariables[key];
			}
		};
	}
});