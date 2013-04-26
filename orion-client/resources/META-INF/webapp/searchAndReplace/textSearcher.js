/*******************************************************************************
 * @license
 * Copyright (c) 2010, 2012 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html).
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
/*global define window document navigator*/

define(['i18n!orion/search/nls/messages', 'require', 'orion/textview/annotations', 'dojo', 'dijit', 'orion/commands', 'orion/editor/regex', 'orion/searchUtils', 'dijit/Menu', 'dijit/MenuItem', 'dijit/form/DropDownButton' ], 
	function(messages, require, mAnnotations, dojo, dijit, mCommands, mRegex, mSearchUtils){
	
var orion = orion || {};

orion.TextSearcher = (function() {
	function TextSearcher(editor, cmdservice, undoStack, options) {
		this._editor = editor;
		this._commandService = cmdservice;
		this._undoStack = undoStack;
		
		this._showAllOccurrence = true;
		this._ignoreCase = true;
		this._wrapSearch = true;
		this._wholeWord = false;
		this._incremental = true;
		this._useRegExp = false;
		this._findAfterReplace = true;
		
		this._reverse = false;
		this.isMac = navigator.platform.indexOf("Mac") !== -1; //$NON-NLS-0$
		
		this._searchRange = null;
		this._timer = null;
		this._searchOnRange = false;
		this._lastSearchString = "";
		var that = this;
		this._listeners = {
			onEditorFocus: function(e) {
				that.removeCurrentAnnotation(e);
			}
		};
		this.setOptions(options);
	}
	TextSearcher.prototype = {
		_createActionTable : function() {
			var that = this;
			this._commandService.openParameterCollector("pageNavigationActions", function(parentDiv) { //$NON-NLS-0$
	
				// create the input box for searchTerm
				var searchStringDiv = document.createElement('input'); //$NON-NLS-0$
				searchStringDiv.type = "text"; //$NON-NLS-0$
				searchStringDiv.name = messages["Find:"];
				searchStringDiv.id = "localSearchFindWith"; //$NON-NLS-0$
				searchStringDiv.placeholder=messages["Find With"];
				searchStringDiv.onkeyup = function(evt){
					return that._handleKeyUp(evt);
				};
				searchStringDiv.onkeydown = function(evt){
					return that._handleKeyDown(evt,true);
				};
				parentDiv.appendChild(searchStringDiv);
				
				that.createButton("Next", parentDiv, function() {that.findNext(true);});				
				that.createButton("Previous", parentDiv, function() {that.findNext(false);});	
				
				// create replace text
				var replaceStringDiv = document.createElement('input'); //$NON-NLS-0$
				replaceStringDiv.type = "text"; //$NON-NLS-0$
				replaceStringDiv.name = messages["ReplaceWith:"];
				replaceStringDiv.id = "localSearchReplaceWith"; //$NON-NLS-0$
				replaceStringDiv.placeholder=messages["Replace With"];
				dojo.addClass(replaceStringDiv, 'searchCmdGroupMargin'); //$NON-NLS-0$
				replaceStringDiv.onkeydown = function(evt){
					return that._handleKeyDown(evt, false);
				};
				parentDiv.appendChild(replaceStringDiv);
				
				that.createButton(messages["Replace"], parentDiv, function() {that.replace();});				
				that.createButton(messages["Replace All"], parentDiv, function() {that.replaceAll();});	

				var optionMenu = dijit.byId("searchOptMenu"); //$NON-NLS-0$
				if (optionMenu) {
					optionMenu.destroy();
				}
				var newMenu = new dijit.Menu({
					style : "display: none;", //$NON-NLS-0$
					id : "searchOptMenu" //$NON-NLS-0$
				});
				
				newMenu.addChild(new dijit.CheckedMenuItem({
					label: messages["Show all"],
					checked: that._showAllOccurrence,
					onChange : function(checked) {
						that.setOptions({showAllOccurrence: checked});
						if(checked){
							that.markAllOccurrences(true);
						} else {
							var annotationModel = that._editor.getAnnotationModel();
							if(annotationModel){
								annotationModel.removeAnnotations(mAnnotations.AnnotationType.ANNOTATION_MATCHING_SEARCH);
							}
						}
					}
				}));
				
				newMenu.addChild(new dijit.CheckedMenuItem({
					label: messages["Case sensitive"],
					checked: !that._ignoreCase,
					onChange : function(checked) {
						that.setOptions({ignoreCase: !checked});
					}
				}));
				
				newMenu.addChild(new dijit.CheckedMenuItem({
					label: messages["Wrap search"],
					checked: that._wrapSearch,
					onChange : function(checked) {
						that.setOptions({wrapSearch: checked});
					}
				}));
				newMenu.addChild(new dijit.CheckedMenuItem({
					label: messages["Incremental search"],
					checked: that._incremental,
					onChange : function(checked) {
						that.setOptions({incremental: checked});
					}
				}));
				
				newMenu.addChild(new dijit.CheckedMenuItem({
					label: messages["Whole Word"],
					checked: that._wholeWord,
					onChange : function(checked) {
						that.setOptions({wholeWord: checked});
					}
				}));
				
				newMenu.addChild(new dijit.CheckedMenuItem({
					label: messages["Regular expression"],
					checked: that._useRegExp,
					onChange : function(checked) {
						that.setOptions({useRegExp: checked});
					}
				}));
				
				newMenu.addChild(new dijit.CheckedMenuItem({
					label: messages["Find after replace"],
					checked: that._findAfterReplace,
					onChange : function(checked) {
						that.setOptions({findAfterReplace: checked});
					}
				}));
				
				var menuButton = new dijit.form.DropDownButton({
					label : messages["Options"],
					dropDown : newMenu
				});
				dojo.addClass(menuButton.domNode, "findSlideoutMenu"); //$NON-NLS-0$
				dojo.place(menuButton.domNode, parentDiv, "last"); //$NON-NLS-0$
				return searchStringDiv;
			},
			function(){that.closeUI();});
		},
		
		visible: function(){
			return document.getElementById("localSearchFindWith") ? true : false; //$NON-NLS-0$
		},
		
		_handleKeyUp: function(evt){
			if(this._incremental && !this._keyUpHandled){
				if(!this._noSelection){
					this.findNext(true, null, true);
				} else {
					this._noSelection = false;
				}
			}
			this._keyUpHandled = false;
			return true;
		},
		
		_handleKeyDown: function(evt, fromSearch){
			var ctrlKeyOnly = (this.isMac ? evt.metaKey : evt.ctrlKey) && !evt.altKey && !evt.shiftKey;
			if(ctrlKeyOnly && evt.keyCode === 70/*"f"*/ ) {
				this._keyUpHandled = fromSearch;
				if( evt.stopPropagation ) { 
					evt.stopPropagation(); 
				}
				evt.cancelBubble = true;
				return false;
			}
			//We can't use ctrlKeyOnly on "k" because ctrl+shift+k means find previous match when the find bar gets focus
			if(((this.isMac ? evt.metaKey : evt.ctrlKey) && !evt.altKey && evt.keyCode === 75/*"k"*/) || evt.keyCode === 13/*enter*/){
				if( evt.stopPropagation ) { 
					evt.stopPropagation(); 
				}
				evt.cancelBubble = true;
				this.findNext(!evt.shiftKey);
				this._keyUpHandled = fromSearch;
				return false;
			}
			if( ctrlKeyOnly &&  evt.keyCode === 82 /*"r"*/){
				if( evt.stopPropagation ) { 
					evt.stopPropagation(); 
				}
				evt.cancelBubble = true;
				if(!fromSearch) {
					this.replace();
				}
				this._keyUpHandled = fromSearch;
				return false;
			}
			if( evt.keyCode === 27/*ESC*/ ){
				this.closeUI();
				this._keyUpHandled = fromSearch;
				return false;
			}
			return true;
		},
		
		closeUI : function() {
			if(this.visible()){
				this._commandService.closeParameterCollector();
			}
			this._editor.getTextView().removeEventListener("Focus", this._listeners.onEditorFocus); //$NON-NLS-0$
			this._editor.getTextView().focus();
			var annotationModel = this._editor.getAnnotationModel();
			if (annotationModel) {
				annotationModel.removeAnnotations(mAnnotations.AnnotationType.ANNOTATION_CURRENT_SEARCH);
				annotationModel.removeAnnotations(mAnnotations.AnnotationType.ANNOTATION_MATCHING_SEARCH);
			}
		},

		removeCurrentAnnotation: function(evt){
			var annotationModel = this._editor.getAnnotationModel();
			if (annotationModel) {
				annotationModel.removeAnnotations(mAnnotations.AnnotationType.ANNOTATION_CURRENT_SEARCH);
			}
		},
		
		createButton: function(text, parent, callback) {
			var button  = dojo.create("span", {tabindex: "0", role: "button"}); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
			dojo.connect(button, "onclick", this, callback); //$NON-NLS-0$
			dojo.connect(button, "onkeypress", this, function(e) { //$NON-NLS-0$
				if (e.keyCode === dojo.keys.ENTER || e.charCode === dojo.keys.SPACE) {						
					callback();			
				} else if (e.keyCode === dojo.keys.ESCAPE) {
					this.closeUI();
				}
			});
			dojo.place(window.document.createTextNode(text), button, "last"); //$NON-NLS-0$
			dojo.addClass(button, "findSlideoutButton"); //$NON-NLS-0$
			
			dojo.place(button, parent, "last"); //$NON-NLS-0$
		},
		
		buildToolBar : function(defaultSearchStr, defaultReplaceStr) {
			this._keyUpHandled = true;
			var that = this;
			this._editor.getTextView().addEventListener("Focus", this._listeners.onEditorFocus); //$NON-NLS-0$
			var findDiv = document.getElementById("localSearchFindWith"); //$NON-NLS-0$
			if (this.visible()) {
				if(defaultSearchStr.length > 0){
					findDiv.value = defaultSearchStr;
				} else {
					this._noSelection = true;
				}
				window.setTimeout(function() {
						findDiv.select();
						findDiv.focus();
				}, 10);				
				return;
			}

			this._createActionTable();

			// set the default value of replace string
			if (typeof(defaultReplaceStr) === "string") { //$NON-NLS-0$
				var replaceDiv = document.getElementById("localSearchReplaceWith"); //$NON-NLS-0$
				replaceDiv.value = defaultReplaceStr;
			}
			// set the default value of search string
			findDiv = document.getElementById("localSearchFindWith"); //$NON-NLS-0$
			findDiv.value = defaultSearchStr;
			window.setTimeout(function() {
				findDiv.select();
				findDiv.focus();
			}, 10);				

		},

		setOptions : function(options) {
			if (options) {
				if (options.showAllOccurrence === true || options.showAllOccurrence === false) {
					this._showAllOccurrence = options.showAllOccurrence;
				}
				if (options.ignoreCase === true || options.ignoreCase === false) {
					this._ignoreCase = options.ignoreCase;
				}
				if (options.wrapSearch === true || options.wrapSearch === false) {
					this._wrapSearch = options.wrapSearch;
				}
				if (options.wholeWord === true || options.wholeWord === false) {
					this._wholeWord = options.wholeWord;
				}
				if (options.incremental === true || options.incremental === false) {
					this._incremental = options.incremental;
				}
				if (options.useRegExp === true || options.useRegExp === false) {
					this._useRegExp = options.useRegExp;
				}
				if (options.findAfterReplace === true || options.findAfterReplace === false) {
					this._findAfterReplace = options.findAfterReplace;
				}
				
				if (options.reverse === true || options.reverse === false) {
					this._reverse = options.reverse;
				}
				
				if (options.toolBarId) {
					this._toolBarId = options.toolBarId;
				}
				if (options.searchRange) {
					this._searchRange = options.searchRange;
				}
				if (options.searchOnRange === true || options.searchOnRange === false) {
					this._searchOnRange = options.searchOnRange;
				}
			}
		},

		getSearchStartIndex: function(reverse, flag) {
			var currentCaretPos = this._editor.getCaretOffset();
			if(reverse) {
				var selection = this._editor.getSelection();
				var selectionSize = (selection.end > selection.start) ? selection.end - selection.start : 0;
				if(!flag){
					return (currentCaretPos- selectionSize - 1) > 0 ? (currentCaretPos- selectionSize - 1) : 0;
				}
				return selection.end > 0 ? selection.end : 0;
			}
			return currentCaretPos > 0 ? currentCaretPos : 0;
		},
		
		findNext : function(next, searchStr, incremental) {
			this.setOptions({
				reverse : !next
			});
			var findTextDiv = document.getElementById("localSearchFindWith"); //$NON-NLS-0$
			var startIndex = this.getSearchStartIndex(incremental ? true : !next);
			if(!searchStr){
				searchStr = findTextDiv ? findTextDiv.value : this._lastSearchString;
			}
			return this._doFind(searchStr, startIndex, !next, this._wrapSearch);
		},

		startUndo: function() {
			if (this._undoStack) {
				this._undoStack.startCompoundChange();
			}
		}, 
		
		endUndo: function() {
			if (this._undoStack) {
				this._undoStack.endCompoundChange();
			}
		}, 
	
		replace: function() {
			this.startUndo();
			var newStr = document.getElementById("localSearchReplaceWith").value; //$NON-NLS-0$
			var editor = this._editor;
			var selection = editor.getSelection();
			var searchStr = document.getElementById("localSearchFindWith").value; //$NON-NLS-0$
			var start = selection ? selection.start : 0;
			var end = selection ? selection.end : 0;
			if(searchStr){
				var result = editor.getModel().find({
					string: searchStr,
					start: start,
					reverse: false,
					wrap: this._wrapSearch,
					regex: this._useRegExp,
					wholeWord: this._wholeWord,
					caseInsensitive: this._ignoreCase
				}).next();
				if(result){
					start = result.start;
					end = result.end;
				}
			}
			editor.setText(newStr, start, end);
			editor.setSelection(start , start + newStr.length, true);
			this.endUndo();
			if (this._findAfterReplace && searchStr){
				this._doFind(searchStr, this.getSearchStartIndex(false), false, this._wrapSearch);
			}
		},
		
		_doFind: function(searchStr, startIndex, reverse, wrapSearch) {
			var editor = this._editor;
			var annotationModel = editor.getAnnotationModel();
			if(!searchStr){
				if(annotationModel){
					annotationModel.removeAnnotations(mAnnotations.AnnotationType.ANNOTATION_CURRENT_SEARCH);
					annotationModel.removeAnnotations(mAnnotations.AnnotationType.ANNOTATION_MATCHING_SEARCH);
				}
				return null;
			}
			this._lastSearchString = searchStr;
			var result = editor.getModel().find({
				string: searchStr,
				start: startIndex,
				reverse: reverse,
				wrap: wrapSearch,
				regex: this._useRegExp,
				wholeWord: this._wholeWord,
				caseInsensitive: this._ignoreCase
			}).next();
			if (!this._replacingAll) {
				if (result) {
					this._editor.reportStatus("");
				} else {
					this._editor.reportStatus(messages["Not found"], "error"); //$NON-NLS-1$
				}
				var visible = this.visible();
				if (visible) {
					var type = mAnnotations.AnnotationType.ANNOTATION_CURRENT_SEARCH;
					if (annotationModel) {
						annotationModel.removeAnnotations(type);
						if (result) {
							annotationModel.addAnnotation(mAnnotations.AnnotationType.createAnnotation(type, result.start, result.end));
						}
					}
					if(this._showAllOccurrence){
						if(this._timer){
							window.clearTimeout(this._timer);
						}
						var that = this;
						this._timer = window.setTimeout(function(){
							that.markAllOccurrences(result);
							that._timer = null;
						}, 500);
					}
				}
			}
			if (result) {
				editor.moveSelection(result.start, result.end, null, false);
			}
			return result;
		},

		replaceAll : function() {
			var searchStr = document.getElementById("localSearchFindWith").value; //$NON-NLS-0$
			if(searchStr){
				this._replacingAll = true;
				var editor = this._editor;
				editor.reportStatus("");
				editor.reportStatus(messages["Replacing all..."], "progress"); //$NON-NLS-1$
				var newStr = document.getElementById("localSearchReplaceWith").value; //$NON-NLS-0$
				window.setTimeout(dojo.hitch(this, function() {
					var startPos = 0;
					var number = 0, lastResult;
					while(true){
						var result = this._doFind(searchStr, startPos);
						if(!result) {
							break;
						}
						lastResult = result;
						number++;
						if(number === 1) {
							this.startUndo();
						}
						var selection = editor.getSelection();
						editor.setText(newStr, selection.start, selection.end);
						editor.setSelection(selection.start , selection.start + newStr.length, true);
						startPos = this.getSearchStartIndex(true, true);
					}
					if(number > 0) {
						this.endUndo();
					}
					editor.reportStatus("", "progress"); //$NON-NLS-0$
					if(startPos > 0) {
						editor.reportStatus(messages["Replaced "]+number+messages[" matches"]);
					} else {
						editor.reportStatus(messages["Nothing replaced"], "error"); //$NON-NLS-1$
					}
					this._replacingAll = false;
				}), 100);				
				
			}
		},
		
		markAllOccurrences: function(singleResult) {
			var annotationModel = this._editor.getAnnotationModel();
			if(!annotationModel){
				return;
			}
			var type = mAnnotations.AnnotationType.ANNOTATION_MATCHING_SEARCH;
			var iter = annotationModel.getAnnotations(0, annotationModel.getTextModel().getCharCount());
			var remove = [], add;
			while (iter.hasNext()) {
				var annotation = iter.next();
				if (annotation.type === type) {
					remove.push(annotation);
				}
			}
			var searchStr = document.getElementById("localSearchFindWith").value; //$NON-NLS-0$
			if(singleResult && searchStr) {
				iter = this._editor.getModel().find({
					string: searchStr,
					regex: this._useRegExp,
					wholeWord: this._wholeWord,
					caseInsensitive: this._ignoreCase
				});
				add = [];
				while (iter.hasNext()) {
					var match = iter.next();
					add.push(mAnnotations.AnnotationType.createAnnotation(type, match.start, match.end));
				}
			}
			annotationModel.replaceAnnotations(remove, add);
		}
	};
	return TextSearcher;
}());

return orion;
});
