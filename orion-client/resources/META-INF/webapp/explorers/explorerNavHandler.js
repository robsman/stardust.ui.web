/*******************************************************************************
 * @license
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

/*global define window */
/*jslint regexp:false browser:true forin:true*/

define(['i18n!orion/nls/messages', 'dojo', 'orion/treeModelIterator'], function(messages, dojo, mTreeModelIterator){

var exports = {};
var userAgent = navigator.userAgent;
var isPad = userAgent.indexOf("iPad") !== -1; //$NON-NLS-0$
var isMac = window.navigator.platform.indexOf("Mac") !== -1; //$NON-NLS-0$

exports.ExplorerNavHandler = (function() {
	/**
	 * Creates a new tree iteration handler
	 * 
	 * @name orion.explorers.ExplorerNavHandler.ExplorerNavHandler
	 * @class A tree iteration handler based on an explorer.
	 * @param {Object} explorer The orion.explorer.Explorer instance.
	 * @param {Object} options The options object which provides iterate patterns and all call back functions when iteration happens.
	 */
	function ExplorerNavHandler(explorer, navDict, options) {
		this.explorer = explorer;
		this.model = this.explorer.model;
		this._navDict = navDict;
		
	    this._listeners = [];
	    this._selections = [];
	    
	    this._currentColumn = 0;
	    var parentDiv = this._getEventListeningDiv();
	    dojo.attr(parentDiv, "tabIndex", 0); //$NON-NLS-0$
		dojo.addClass(parentDiv, "selectionModelContainer"); //$NON-NLS-0$
		
		this._modelIterator = new mTreeModelIterator.TreeModelIterator([],
		   		   {isExpanded: dojo.hitch(this, function(model) {
		   						 	return this.isExpanded(model);
		   						 }),
		   						 
		   			isExpandable: this.explorer.renderer.isExpandable ? dojo.hitch(this, function(model) {
					 				return this.explorer.renderer.isExpandable(model);
								 }) : dojo.hitch(this, function(model) {
					 				return this.isExpandable(model);
								 }),				   						 
		   			
					forceExpandFunc: this.explorer.forceExpandFunc? dojo.hitch(this, function(modelToExpand, childPosition, callback) {
			 						return this.explorer.forceExpandFunc(modelToExpand, childPosition, callback);
						 		 }) : undefined
		   		   });
		this._init(options);
		
	    if(!options || options.setFocus !== false){
	    	parentDiv.focus();
	    }
		var keyListener = dojo.connect(parentDiv, "onkeydown", dojo.hitch(this, function (e) { //$NON-NLS-0$
            if (e.target) {// Firefox,  Chrome and Safari
                if(e.target !== parentDiv){
    				return true;
    			}
            } else if (e.srcElement){// Internet Explorer
                if(e.srcElement !== parentDiv){
    				return true;
    			}
            }
			if(this.explorer.preventDefaultFunc && this.explorer.preventDefaultFunc(e, this._modelIterator.cursor())){
				return true;
			}
			if(e.keyCode === dojo.keys.DOWN_ARROW){
				return this.onDownArrow(e);
			} else if(e.keyCode === dojo.keys.UP_ARROW){
				return this.onUpArrow(e);
			} else if(e.keyCode === dojo.keys.RIGHT_ARROW){
				return this.onRightArrow(e);
			} else if(e.keyCode === dojo.keys.LEFT_ARROW){
				return this.onLeftArrow(e);
			} else if(e.keyCode === dojo.keys.SPACE){
				return this.onSpace(e);
			} else if(e.keyCode === dojo.keys.ENTER) {
				return this.onEnter(e);
			}
		}));
		this._listeners.push(keyListener);
		var l1 = dojo.connect(parentDiv, "onblur", dojo.hitch(this, function (e) { //$NON-NLS-0$
			if(this.explorer.onFocus){
				this.explorer.onFocus(false);
			} else {
				this.toggleCursor(null, false);
			}
		}));
		var l2 = dojo.connect(parentDiv, "onfocus", dojo.hitch(this, function (e) { //$NON-NLS-0$
			if(this.explorer.onFocus){
				this.explorer.onFocus(true);
			} else {
				this.toggleCursor(null, true);
			}
		}));
		this._listeners.push(l1);
		this._listeners.push(l2);
	};
	
	ExplorerNavHandler.prototype = /** @lends orion.ExplorerNavHandler.ExplorerNavHandler.prototype */ {
		
		_init: function(options){
			this._linearGridMove = false;//temporary. If true right key on the last grid will go to first grid of next row
			                            // Left key on the first grid will go to the last line grid of the previous line
			if(!options){
				return;
			}
			this._selectionPolicy = options.selectionPolicy;
			this.preventDefaultFunc = options.preventDefaultFunc;//optional callback. If this function returns true then the default behavior of all key press will stop at this time.
			                                                     //The key event is passed to preventDefaultFunc. It can implement its own behavior based o nteh key event.
			this.postDefaultFunc = options.postDefaultFunc;//optional callback. If this function provides addtional behaviors after the default behavior.
			                                               //Some explorers may want to do something else whne the cursor is changed, etc.
		},
		
		_ctrlKeyOn: function(e){
			return 	isMac ? e.metaKey : e.ctrlKey;
		},
		
		removeListeners: function(){
			if(this._listeners){
				for (var i=0; i < this._listeners.length; i++) {
					dojo.disconnect(this._listeners[i]);
				}
			}
		},
		
		focus: function(){
		    var parentDiv = this._getEventListeningDiv();
		    if(parentDiv){
				parentDiv.focus();
		    }
		},
		
		_getEventListeningDiv: function(secondLevel){
			if(this.explorer.keyEventListeningDiv && typeof this.explorer.keyEventListeningDiv === "function"){ //$NON-NLS-0$
				return this.explorer.keyEventListeningDiv(secondLevel);
			}
			return dojo.byId(this.explorer._parentId);
		},
		
		isExpandable: function(model){
			if(!model){
				return false;
			}
			var expandImage = dojo.byId(this.explorer.renderer.expandCollapseImageId(this.model.getId(model)));
			return expandImage ? true: false;
		},
		
		isExpanded: function(model){
			if(!model){
				return false;
			}
			return this.explorer.myTree.isExpanded(this.model.getId(model));
		},
		
		refreshSelection: function(noScroll){
			var that = this;
			if(this.explorer.selection){
				this.explorer.selection.getSelections(function(selections) {
					that._clearSelection();
					for (var i = 0; i < selections.length; i++){
						that._selections.push(selections[i]);
					}
					if(that._selections.length > 0){
						that.cursorOn(that._selections[0], true, false, noScroll);
					}
				});
			}
		},
		
		refreshModel: function(navDict, model, topIterationNodes, noReset){
		    this._currentColumn = 0;
			this.topIterationNodes = [];
			this.model = model;
			this._navDict = navDict;
			if(this.model.getTopIterationNodes){
				this.topIterationNodes = this.model.getTopIterationNodes();
			} else if(topIterationNodes){
				this.topIterationNodes = topIterationNodes;
			}
			this._modelIterator.setTree(this.topIterationNodes);
			if(!noReset && this.explorer.selection){
				this._modelIterator.reset();
			}
			this.refreshSelection(true);
		},
		
		getTopLevelNodes: function(){
			return this._modelIterator.firstLevelChildren;
		},
		
		_inSelection: function(model){
			for(var i = 0; i < this._selections.length; i++){
				if(model === this._selections[i]){
					return i;
				}
			}
			return -1;
		},
		
		
		_clearSelection: function(visually){
			if(visually){
				for(var i = 0; i < this._selections.length; i++){
					this._checkRow(this._selections[i], true);
				}
			}
			this._selections = [];
			//this._selections.splice(0, this._selections.length);
		},
		
		setSelection: function(model, toggling){
			if(this._selectionPolicy === "cursorOnly"){ //$NON-NLS-0$
				if(toggling && this.explorer.renderer._useCheckboxSelection){
					this._checkRow(model,true);
				}
				return;
			}
			if(!this._isRowSelectable(model)){
				return;
			}
			if(!toggling || this._selectionPolicy === "singleSelection"){//$NON-NLS-0$
				this._clearSelection(true);
				this._checkRow(model,false);		
				this._selections.push(model);
				this._lastSelection = model;
			} else{
				var index = this._inSelection(model);
				if(index >= 0){
					this._checkRow(model, true);
					this._selections.splice(index, 1);
				} else {
					this._checkRow(model,false);		
					this._selections.push(model);
					this._lastSelection = model;
				}
			}
			if (this.explorer.selection) {
				this.explorer.renderer.storeSelections();
				this.explorer.selection.setSelections(this._selections);		
			}
		},
		
		moveColumn: function(model, offset){
			if(!model){
				model = this.currentModel();
			}
			var gridChildren = this._getGridChildren(model);
			if((gridChildren && gridChildren.length > 1) || (offset === 0 && gridChildren)){
				if(offset !== 0){
					this.toggleCursor(model, false);
				}
				var column = this._currentColumn;
				var rowChanged= true;
				column = column + offset;
				if(column < 0){
					if(this._linearGridMove && offset !== 0){
						if(this._modelIterator.iterate(false)){
							model = this.currentModel();
						} else {
							rowChanged = false;
						}
					}
					column = rowChanged ? gridChildren.length - 1 : this._currentColumn;
				} else if(column >= gridChildren.length){
					if(this._linearGridMove && offset !== 0){
						if(this._modelIterator.iterate(true)){
							model = this.currentModel();
						} else {
							rowChanged = false;
						}
					}
					column = rowChanged ? 0 : this._currentColumn;
				}
				this._currentColumn = column;
				if(offset !== 0){
					this.toggleCursor(model, true);
				}
				return true;
			}
			return false;
		},
		
		_getGridChildren: function(model){
			if(this._navDict){
				return this._navDict.getGridNavHolder(model);
			}
			return null;
		},
		
		getCurrentGrid:  function(model){
			if(!model){
				model = this.currentModel();
			}
			var gridChildren = this._getGridChildren(model);
			if(gridChildren && gridChildren.length > 0){
				return gridChildren[this._currentColumn];
			}
			return null;
		},
		
		toggleCursor:  function(model, on){
			var currentRow = this.getRowDiv(model);
			var currentgrid = this.getCurrentGrid(model);
			if(currentgrid) {
				if(currentRow){
					dojo.toggleClass(currentRow, "treeIterationCursorRow", on); //$NON-NLS-0$
				}
				if(currentgrid.domNode){
					dojo.toggleClass(currentgrid.domNode, "treeIterationCursor", on); //$NON-NLS-0$
				}
			} else {
				if(currentRow){
					dojo.toggleClass(currentRow, "treeIterationCursorRow_Dotted", on); //$NON-NLS-0$
				}
			}
		},
		
		currentModel: function(){
			return this._modelIterator.cursor();
		},
		
		cursorOn: function(model, force, next, noScroll){
			var forward = next === undefined ? false : next;
			var previousModel, currentModel;
			if(model || force){
				if(currentModel === this._modelIterator.cursor()){
					return;
				}
				previousModel = this._modelIterator.cursor();
				currentModel = model;
				this._modelIterator.setCursor(currentModel);
			} else {
				previousModel = this._modelIterator.prevCursor();
				currentModel = this._modelIterator.cursor();
			}
			if(previousModel === currentModel && !force){
				return;
			}
			this.toggleCursor(previousModel, false);
			if(force && !currentModel){
				return;
			}
			this.moveColumn(null, 0);
			this.toggleCursor(currentModel, true);
			var currentRowDiv = this.getRowDiv();
			if(currentRowDiv && !noScroll && !this._visible(currentRowDiv)) {
				currentRowDiv.scrollIntoView(!forward);
			}
			if(this.explorer.onCursorChanged){
				this.explorer.onCursorChanged(previousModel, currentModel);
			}
		},
		
		getSelection: function(){
			return this._selections;
		},
		
		getSelectionIds: function(){
			var ids = [];
			for (var i = 0; i < this._selections.length; i++) {
				ids.push(this.model.getId(this._selections[i]));
			}
			return ids;
		},
		
		getRowDiv: function(model){
			var rowModel = model ? model: this._modelIterator.cursor();
			if(!rowModel){
				return null;
			}
			var modelId = this.model.getId(rowModel);
			var value = this._navDict.getValue(modelId);
			return value && value.rowDomNode ? value.rowDomNode :  dojo.byId(modelId);
		},
		
		iterate: function(forward, forceExpand, selecting)	{
			if(this.topIterationNodes.length === 0){
				return;
			}
			if(this._modelIterator.iterate(forward, forceExpand)){
				this.cursorOn(null, false, forward);
				if(selecting){
					this.setSelection(this.currentModel(), true);
				}
			}
		},
		
		_checkRow: function(model, toggle) {
			if(this.explorer.renderer._useCheckboxSelection){
				var tableRow = this.getRowDiv(model);
				if(!tableRow){
					return;
				}
				var checkBox  = dojo.byId(this.explorer.renderer.getCheckBoxId(tableRow.id));
				var checked = toggle ? !checkBox.checked : true;
				if(checked !== checkBox.checked){
					this.explorer.renderer.onCheck(tableRow, checkBox, checked, true);
				}
			} else {
				this._select(model, toggle);
			}
		},
		
		_vpWidth: function() {
			return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
		},
		
		_vpHeight: function(){
			return window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
		},
		
		_getToolBarHeight: function(toolBarId){
			if(dojo.byId(toolBarId)){
				mBox = dojo.marginBox(toolBarId);
				if(mBox){
					return mBox.h;
				}
			}
			return 0;
		},
		
		_visible: function(rowDiv) {
			if (rowDiv.offsetWidth === 0 || rowDiv.offsetHeight === 0) {
				return false;
			}
			
			var headerHeight = 0;
			headerHeight += this._getToolBarHeight("staticBanner");
			headerHeight += this._getToolBarHeight("titleArea");
			headerHeight += this._getToolBarHeight("pageToolbar");
			
		    var parentNode = this._getEventListeningDiv(true);
			var parentRect = parentNode.getClientRects()[0];
			var vPortRect = {top: headerHeight, left: 0, bottom : this._vpHeight(), right: this._vpWidth()};
			
			var vTop = parentRect.top >  vPortRect.top ? parentRect.top :  vPortRect.top;
			var vBottom = parentRect.bottom <  vPortRect.bottom ? parentRect.bottom :  vPortRect.bottom;

			var rects = rowDiv.getClientRects();
			for (var i = 0, l = rects.length; i < l; i++) {
				var r = rects[i];
			    var in_viewport = (r.top >= vTop && r.top <= vBottom && r.bottom >= vTop && r.bottom <= vBottom);
			    if (in_viewport ) {
					return true;
			    }
			}
			return false;
		},
			
		_select: function(model, toggling){
			if(!model){
				model = this._modelIterator.cursor();
			}
			var rowDiv = this.getRowDiv(model);
			if(rowDiv){
				dojo.toggleClass(rowDiv, "checkedRow", this._inSelection(model) < 0); //$NON-NLS-0$
			}
		},
		
		_onModelGrid: function(model, mouseEvt){
			var gridChildren = this._getGridChildren(model);
			if(gridChildren){
				for(var i = 0; i < gridChildren.length; i++){
					if(mouseEvt.target === gridChildren[i].domNode){
						return true;
					}
				}
			}
			return false;
		},
		
		onClick: function(model, mouseEvt)	{
			var twistieSpan = dojo.byId(this.explorer.renderer.expandCollapseImageId(this.model.getId(model)));
			if(mouseEvt.target === twistieSpan){
				return;
			}
			if(this._onModelGrid(model, mouseEvt)){
				return;
			}
			this.cursorOn(model, true, false, true);
			if(isPad){
				this.setSelection(model, true);
			} else if(this._ctrlKeyOn(mouseEvt)){
				this.setSelection(model, true);
			} else if(mouseEvt.shiftKey && this._lastSelection){
				var scannedSel = this._modelIterator.scan(this._lastSelection, model);
				if(scannedSel){
					this._clearSelection(true);
					for(var i = 0; i < scannedSel.length; i++){
						this.setSelection(scannedSel[i], true);
					}
				}
			} else {
				this.setSelection(model, false);
			}
		},
		
		onCollapse: function(model)	{
			if(this._modelIterator.collapse(model)){
				this.cursorOn();
			}
		},
		
		//Up arrow key iterates the current row backward. If control key is on, browser's scroll up behavior takes over.
		//If shift key is on, it toggles the check box and iterates backward.
		onUpArrow: function(e) {
			this.iterate(false, false, e.shiftKey);
			if(!this._ctrlKeyOn(e) && !e.shiftKey){
				this.setSelection(this.currentModel(), false);
			}
			e.preventDefault();
			return false;
		},

		//Down arrow key iterates the current row forward. If control key is on, browser's scroll down behavior takes over.
		//If shift key is on, it toggles the check box and iterates forward.
		onDownArrow: function(e) {
			this.iterate(true, false, e.shiftKey);
			if(!this._ctrlKeyOn(e) && !e.shiftKey){
				this.setSelection(this.currentModel(), false);
			}
			e.preventDefault();
			return false;
		},

		_shouldMoveColumn: function(e){
			var model = this.currentModel();
			var gridChildren = this._getGridChildren(model);
			if(gridChildren && gridChildren.length > 1){
				if(this.isExpandable(model)){
					return this._ctrlKeyOn(e);
				}
				return true;
			} else {
				return false;
			}
		},
		
		//Left arrow key collapses the current row. If current row is not expandable(e.g. a file in file navigator), move the cursor to its parent row.
		//If current row is expandable and expanded, collapse it. Otherwise move the cursor to its parent row.
		onLeftArrow:  function(e) {
			if(this._shouldMoveColumn(e)){
				this.moveColumn(null, -1);
				e.preventDefault();
				return true;
			}
			var curModel = this._modelIterator.cursor();
			if(!curModel){
				return false;
			}
			if(this.isExpandable(curModel)){
				if(this.isExpanded(curModel)){
					this.explorer.myTree.collapse(curModel);
					e.preventDefault();
					return true;
				}
			}
			if(!this._modelIterator.topLevel(curModel)){
				this.cursorOn(curModel.parent);
				this.setSelection(curModel.parent, false);
			//The cursor is now on a top level item which is collapsed. We need to ask the explorer is it wants to scope up.	
			} else if (this.explorer.scopeUp && typeof this.explorer.scopeUp === "function"){ //$NON-NLS-0$
				this.explorer.scopeUp();
			}
		},
		
		//Right arrow key expands the current row if it is expandable and collapsed.
		onRightArrow: function(e) {
			if(this._shouldMoveColumn(e)){
				this.moveColumn(null, 1);
				e.preventDefault();
				return true;
			}
			var curModel = this._modelIterator.cursor();
			if(!curModel){
				return false;
			}
			if(this.isExpandable(curModel)){
				if(!this.isExpanded(curModel)){
					this.explorer.myTree.expand(curModel);
					e.preventDefault();
					return false;
				}
			}
		},
		
		_isRowSelectable: function(model){
			return this.explorer.isRowSelectable ? this.explorer.isRowSelectable(model) : true;
		},

		//Space key toggles the check box on the current row if the renderer uses check box
		onSpace: function(e) {
			this.setSelection(this.currentModel(), true);
			e.preventDefault();
		},
		
		//Enter key simulates a href call if the current row has an href link rendered. The render has to provide the getRowActionElement function that returns the href DIV.
		onEnter: function(e) {
			var currentGrid = this.getCurrentGrid(this._modelIterator.cursor());
			if(currentGrid){
				if(currentGrid.widget){
					if(typeof currentGrid.onClick === "function"){ //$NON-NLS-0$
						currentGrid.onClick();
					} else if(typeof currentGrid.widget.focus === "function"){ //$NON-NLS-0$
						currentGrid.widget.focus();
					}
				} else {
					var evt = document.createEvent("MouseEvents"); //$NON-NLS-0$
					evt.initMouseEvent("click", true, true, window, //$NON-NLS-0$
							0, 0, 0, 0, 0, this._ctrlKeyOn(e), false, false, false, 0, null);
					currentGrid.domNode.dispatchEvent(evt);
				}
				return;
			}
			if(this.explorer.renderer.getRowActionElement){
				var curModel = this._modelIterator.cursor();
				if(!curModel){
					return;
				}
				var div = this.explorer.renderer.getRowActionElement(this.model.getId(curModel));
				if(div.href){
					if(this._ctrlKeyOn(e)){
						window.open(div.href);
					} else {
						window.location.href = div.href;
					}
				}
			}
		}
	};
	return ExplorerNavHandler;
}());

exports.ExplorerNavDict = (function() {
	/**
	 * Creates a new explorer navigation dictionary. The key of the dictionary is the model id. The value is a wrapper object that holds .modelItem, .rowDomNode and .gridChildren properties.
	 * The .modelItem property helps quickly looking up a model object by a given id. The .rowDomNode also helps to find out the row DOM node instead of doing dojo.byId(). 
	 * The .gridChildren is an array representing all the grid navigation information, which the caller has to fill the array out.
	 *
	 * @name orion.ExplorerNavHandler.ExplorerNavDict
	 * @class A explorer navigation dictionary.
	 * @param {Object} model The model object that represent the overall explorer.
	 */
	function ExplorerNavDict(model) {
		this._dict= {};
		this._model = model;
	}
	ExplorerNavDict.prototype = /** @lends orion.ExplorerNavHandler.ExplorerNavDict.prototype */ {
		
		/**
		 * Add a row to the dictionary.
		 * @param {Object} modelItem The model item object that represent a row.
		 * @param {domNode} rowDomNode optional The DOM node that represent a row. If 
		 */
		addRow: function(modelItem, rowDomNode){
			var modelId = this._model.getId(modelItem);
			this._dict[modelId] = {model: modelItem, rowDomNode: rowDomNode};
		},
			
		/**
		 * Get the value of a key by model id. 		 
		 *  @param {String} id The model id.
		 * @returns {Object} The value of the id from the dictionary.
		 */
		getValue: function(id) {
			return this._dict[id];
		},
		
		/**
		 * Get the grid navigation holder from a row navigation model. 		 
		 *  @param {Object} modelItem The model item object that represent a row.
		 * @returns {Array} The .gridChildren property of the value keyed by the model id.
		 */
		getGridNavHolder: function(modelItem, lazyCreate) {
			if(!modelItem){
				return null;
			}
			var modelId = this._model.getId(modelItem);
			if(this._dict[modelId]){
				if(!this._dict[modelId].gridChildren && lazyCreate){
					this._dict[modelId].gridChildren = [];
				}
				return this._dict[modelId].gridChildren;
			}
			return null;
		},
		
		/**
		 * Initialize the grid navigation holder to null. 		 
		 *  @param {Object} modelItem The model item object that represent a row.
		 */
		initGridNavHolder: function(modelItem) {
			if(!modelItem){
				return null;
			}
			var modelId = this._model.getId(modelItem);
			if(this._dict[modelId]){
				this._dict[modelId].gridChildren = null;
			}
		}
	};
	return ExplorerNavDict;
}());

return exports;
});
