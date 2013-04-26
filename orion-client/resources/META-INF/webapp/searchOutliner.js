/*******************************************************************************
 * @license
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
 
/*global window define setTimeout */
/*jslint forin:true*/

define(['i18n!orion/search/nls/messages', 'require', 'dojo', 'orion/section', 'orion/commands', 'orion/selection', 'orion/explorers/explorer', 'orion/EventTarget'], function(messages, require, dojo, mSection, mCommands, mSelection, mExplorer, EventTarget){

	/**
	 * Instantiates the saved search service. This service is used internally by the
	 * search outliner and is not intended to be used as API.  It is serving as
	 * a preference holder and triggers listeners when the preference changes.
	 * When preference changes trigger listeners, this class would no longer be needed.
	 *
	 * @name orion.searches.SavedSearches
	 * @class A service for creating and managing saved searches.
	 */
	function SavedSearches(serviceRegistry) {
		this._searches = [];
		EventTarget.attach(this);
		this._init(serviceRegistry);
		this._initializeSearches();
	}
	
	SavedSearches.prototype = /** @lends orion.searches.SavedSearches.prototype */ {
		_init: function(options) {
			this._registry = options.serviceRegistry;
			this._serviceRegistration = this._registry.registerService("orion.core.savedSearches", this); //$NON-NLS-0$
		},
		
		_notifyListeners: function() {
			this.dispatchEvent({type:"searchesChanged", searches: this._searches, registry: this._registry}); //$NON-NLS-0$
		},

		
		_initializeSearches: function () {
			var savedSearches = this;
			this._registry.getService("orion.core.preference").getPreferences("/window/favorites").then(function(prefs) {  //$NON-NLS-1$ //$NON-NLS-0$
				var i;
				var searches = prefs.get("search"); //$NON-NLS-0$
				if (typeof searches === "string") { //$NON-NLS-0$
					searches = JSON.parse(searches);
				}
				if (searches) {
					for (i in searches) {
						savedSearches._searches.push(searches[i]);
					}
				}
				savedSearches._notifyListeners();
			});
		}, 
				
		_storeSearches: function() {
			var storedSearches = this._searches;
			this._registry.getService("orion.core.preference").getPreferences("/window/favorites").then(function(prefs){ //$NON-NLS-1$ //$NON-NLS-0$
				prefs.put("search", storedSearches); //$NON-NLS-0$
			}); 
		},

		addSearch: function(theName, theQuery) {
			var alreadyFound = false;
			for (var i in this._searches) {
				if (this._searches[i].query === theQuery) {
					this._searches[i].name = theName;
					alreadyFound = true;
				}
			}
			if (alreadyFound) {
				this._registry.getService("orion.page.message").setMessage(theName + " is already saved.", 2000); //$NON-NLS-1$ //$NON-NLS-0$
			} else {
				this._searches.push({ "name": theName, "query": theQuery}); //$NON-NLS-1$ //$NON-NLS-0$
			}
			this._searches.sort(this._sorter);
			this._storeSearches();
			this._notifyListeners();
		},
		
		removeSearch: function(query) {
			for (var i in this._searches) {
				if (this._searches[i].query === query) {
					this._searches.splice(i, 1);
					break;
				}
			}
			this._searches.sort(this._sorter);
			this._storeSearches();
			this._notifyListeners();
		},
		
		renameSearch: function(query, newName) {
			var changed = false;
			for (var i in this._searches) {
				if (this._searches[i].query === query) {
					var search = this._searches[i];
					if (search.name !== newName) {
						search.name = newName;
						changed = true;
					}
				}
			}
			if (changed) {
				this._searches.sort(this._sorter);
				this._storeSearches();
				this._notifyListeners();
			}
		}, 
		
		getSearches: function() {
			return {searches: this._searches};
		}, 
		
		_sorter: function(fav1,fav2) {
			var name1 = fav1.name.toLowerCase();
			var name2 = fav2.name.toLowerCase();
			if (name1 > name2) {
				return 1;
			} else if (name1 < name2) {
				return -1;
			} else {
				return 0;
			}
		}
	};
	function SearchRenderer (options, explorer) {
		this.explorer = explorer;
		this._init(options);
	}
	SearchRenderer.prototype = mExplorer.SelectionRenderer.prototype;
	SearchRenderer.prototype.constructor = SearchRenderer;
	SearchRenderer.prototype.getLabelColumnIndex = function() {
		return 0;
	};
	SearchRenderer.prototype.getCellElement = function(col_no, item, tableRow){
		var href;
		if (item.query) {
			href=require.toUrl("search/search.html") + "#" + item.query; //$NON-NLS-1$ //$NON-NLS-0$
			var col = dojo.create("td", null, tableRow, "last"); //$NON-NLS-1$ //$NON-NLS-0$
			dojo.addClass(col, "mainNavColumn singleNavColumn"); //$NON-NLS-0$
			var link = dojo.create("a", {href: href, className: "navlinkonpage"}, col, "only"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
			dojo.place(window.document.createTextNode(item.name), link, "only"); //$NON-NLS-0$
		} 
	};

	function SearchExplorer(serviceRegistry, selection) {
		this.selection = selection;
		this.registry = serviceRegistry;
		this.renderer = new SearchRenderer({checkbox: false, decorateAlternatingLines: false}, this);
	}
	SearchExplorer.prototype = mExplorer.Explorer.prototype;	
	SearchExplorer.prototype.constructor = SearchExplorer;

	

	/**
	 * Creates a new user interface element showing stored searches
	 *
	 * @name orion.Searches.SearchList
	 * @class A user interface element showing a list of saved searches.
	 * @param {Object} options The service options
	 * @param {Object} options.parent The parent 
	 * @param {orion.serviceregistry.ServiceRegistry} options.serviceRegistry The service registry
	 */
	function SearchOutliner(options) {
		var parent = options.parent;
		if (typeof(parent) === "string") { //$NON-NLS-0$
			parent = dojo.byId(parent);
		}
		if (!parent) { throw "no parent"; } //$NON-NLS-0$
		if (!options.serviceRegistry) {throw "no service registry"; } //$NON-NLS-0$
		this._parent = parent;
		this._registry = options.serviceRegistry;
		var reg = options.serviceRegistry;
		
		var renameSearchCommand = new mCommands.Command({
			name: messages["Rename"],
			imageClass: "core-sprite-rename", //$NON-NLS-0$
			id: "eclipse.renameSearch", //$NON-NLS-0$
			parameters: new mCommands.ParametersDescription([new mCommands.CommandParameter("name", "text", 'Name:', '')]), //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
			visibleWhen: function(items) {
				items = dojo.isArray(items) ? items : [items];
				return items.length === 1 && items[0].query;
			},
			callback: dojo.hitch(this, function(data) {
				var item = dojo.isArray(data.items) ? data.items[0] : data.items;
				if (data.parameters && data.parameters.valueFor('name')) { //$NON-NLS-0$
					reg.getService("orion.core.savedSearches").renameSearch(item.query, data.parameters.valueFor('name')); //$NON-NLS-1$ //$NON-NLS-0$
				}
			})
		});
		var deleteSearchCommand = new mCommands.Command({
			name: "Delete", //$NON-NLS-0$
			imageClass: "core-sprite-delete", //$NON-NLS-0$
			id: "eclipse.deleteSearch", //$NON-NLS-0$
			visibleWhen: function(items) {
				items = dojo.isArray(items) ? items : [items];
				if (items.length === 0) {
					return false;
				}
				for (var i=0; i<items.length; i++) {
					if (!items[i].query) {
						return false;
					}
				}
				return true;
			},
			callback: function(data) {
				var items = dojo.isArray(data.items) ? data.items : [data.items];
				var confirmMessage = items.length === 1 ? dojo.string.substitute("Are you sure you want to delete '${0}' from the searches?", [items[0].name]) : dojo.string.substitute("Are you sure you want to delete these ${0} searches?", [items.length]); //$NON-NLS-1$ //$NON-NLS-0$
				if(window.confirm(confirmMessage)) {
					for (var i=0; i<items.length; i++) {
						options.serviceRegistry.getService("orion.core.savedSearches").removeSearch(items[i].query); //$NON-NLS-0$
					}
				}
			}
		});
		this.commandService = this._registry.getService("orion.page.command"); //$NON-NLS-0$
		// register commands 
		this.commandService.addCommand(renameSearchCommand);	
		this.commandService.addCommand(deleteSearchCommand);	
		var savedSearches = this._registry.getService("orion.core.savedSearches"); //$NON-NLS-0$
		var searchOutliner = this;
		if (savedSearches) {
			// render the searches
			var registry = this._registry;
			savedSearches.getSearches().then(dojo.hitch(searchOutliner, function(searches) {
				this.render(searches.searches, registry);
			}));

			savedSearches.addEventListener("searchesChanged", dojo.hitch(searchOutliner, //$NON-NLS-0$
				function(event) {
					this.render(event.searches, event.registry);
				}));
		}
	}
	SearchOutliner.prototype = /** @lends orion.navoutliner.SearchOutliner.prototype */ {

		render: function(searches, serviceRegistry) {
			// Searches if we have them
			var commandService = this.commandService;
			// first time setup
			if (!this.searchesSection) {
				this.searchesSection = new mSection.Section(this._parent, {
					id: "searchSection", //$NON-NLS-0$
					title: messages["Searches"],
					content: '<div id="searchContent"></div>', //$NON-NLS-0$
					useAuxStyle: true,
					preferenceService: serviceRegistry.getService("orion.core.preference"), //$NON-NLS-0$
					slideout: true
				});
				this.searchSelection = new mSelection.Selection(serviceRegistry, "orion.searches.selection"); //$NON-NLS-0$
				// add commands to the search section heading
				var selectionId = this.searchesSection.selectionNode.id;
				this.commandService.registerCommandContribution(selectionId, "eclipse.renameSearch", 1, null, false, new mCommands.CommandKeyBinding(113, false, false, false, false, "searchContent"));//$NON-NLS-0$//$NON-NLS-1$	
				this.commandService.registerCommandContribution(selectionId, "eclipse.deleteSearch", 2, null, false, new mCommands.CommandKeyBinding(46, false, false, false, false, "searchContent"));//$NON-NLS-0$//$NON-NLS-1$	
				commandService.registerSelectionService(selectionId, this.searchSelection);
				serviceRegistry.getService("orion.searches.selection").addEventListener("selectionChanged", function(event) { //$NON-NLS-1$ //$NON-NLS-0$
					var selectionTools = dojo.byId(selectionId);
					if (selectionTools) {
						commandService.destroy(selectionTools);
						commandService.renderCommands(selectionId, selectionTools, event.selections, this, "button"); //$NON-NLS-0$
					}
				});
			}
			if (searches.length > 0) {
				var explorer = new SearchExplorer(serviceRegistry, this.searchSelection);
				this.searchTable = explorer.createTree("searchContent", new mExplorer.SimpleFlatModel(searches, "srch", function(item) { //$NON-NLS-1$ //$NON-NLS-0$
					return item.query;
				}));	
			} else {
				dojo.place("<p>"+dojo.string.substitute(messages["You can save frequently used by searches by choosing ${0} in the search toolbar."], ["<b>"+"Save Search"+"</b>"])+"</p>", "searchContent", "only"); //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-0$
			}
		}
	};//end navigation outliner prototype
	SearchOutliner.prototype.constructor = SearchOutliner;

	//return module exports
	return {
		SavedSearches: SavedSearches,
		SearchOutliner: SearchOutliner
	};
});
