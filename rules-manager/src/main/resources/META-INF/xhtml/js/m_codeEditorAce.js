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

define([ "jquery","bpm-modeler/js/m_utils" ], function(JQuery,m_utils) {
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
		
		/*Hanging an object off of window.top.ace that we can use 
		 *for global state related to our module.*/
		if(ace.hasOwnProperty("ext_userDefined")===false){
			ace["ext_userDefined"]={};/*collect here for sameness*/
			ace.ext_userDefined.completers=[]; /*collection to keep track of completers we add*/
		}
		
		/*Wrapper to addCompleters through our language tools extension.
		 *Any completer added through this mechanism will be stringified and tagged
		 *to our top level ace object. All completers which come through this function
		 *will have their string compared against the completers already added. Duplicates 
		 *are not passed onwards to the extension.*/
		CodeEditor.prototype.addCompleter=function(completer){
			var isPresent=false,
				temp,
				compString=completer.getCompletions.toString(),
				langTools,
				compLength=ace.ext_userDefined.completers.length;
			
			while(compLength--){
				temp=ace.ext_userDefined.completers[compLength];
				if(temp===compString){
					isPresent=true;
					console.log("Repeater found, will not be added.");
					break;
				}
			}
			if(isPresent===false){
				langTools=ace.define.modules["ace/ext/language_tools"];
				if(langTools){
					langTools.addCompleter(completer);
					ace.ext_userDefined.completers.push(completer.getCompletions.toString());
				}
			}
			
		};
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
		
		/*Wrap our loadModule function to support easy loading of language tools. 
		 *If a module is not loaded, the function will load it using ace.config. If the module
		 *is already loaded, the module will not be reloaded. However, in both cases a moduleLoaded
		 *event will be triggered to indicate that the module is ready for use */
		CodeEditor.prototype.loadLanguageTools=function(options){
			var langModule="ace/ext/language_tools";
			var defOptions={
					"enableSnippets": true,
					"enableBasicAutocompletion": true
            };
			if(options){
				defOptions.enableSnippets=options.enableSnippets || 
				                          defOptions.enableSnippets;
				defOptions.enableBasicAutocompletion=options.enableBasicAutocompletion || 
													defOptions.enableBasicAutocompletion;
			}
			if(ace.define.modules.hasOwnProperty(langModule)===false){
				this.loadModule(langModule, function(aceExt) {
					that.editor.setOptions(defOptions);
					JQuery(that).trigger("moduleLoaded",{
						"name": langModule,
						"reference": aceExt});
			    });
			}
			else{
				that.editor.setOptions(defOptions);
				JQuery(that).trigger("moduleLoaded",{
					"name": langModule,
					"reference": ace.define.modules[langModule]});
			}
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