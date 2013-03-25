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
/*global define orion*/
/*jslint browser:true sub:true*/

define(['i18n!orion/sites/nls/messages', 'i18n!orion/widgets/nls/messages', 'require', 'dojo', 'dijit', 'orion/commands', 'orion/section',
	'orion/sites/siteMappingsTable', 'orion/i18nUtil', 'orion/widgets/DirectoryPrompterDialog', 'dijit/Dialog',
	'orion/widgets/_OrionDialogMixin',
	'text!orion/widgets/templates/SiteEditor.html',
	'dojo/DeferredList', 'dijit/layout/ContentPane', 'dijit/Tooltip', 'dijit/_Templated',
	'dijit/form/Form', 'dijit/form/TextBox', 'dijit/form/ValidationTextBox'],
	function(messages, widgetsMessages, require, dojo, dijit, mCommands, mSection, mSiteMappingsTable, i18nUtil, DirectoryPrompterDialog,
		Dialog, _OrionDialogMixin) {

var ConvertToSelfHostingDialog = dojo.declare("orion.widgets.ConvertToSelfHostDialog", [Dialog, _OrionDialogMixin], {
	DEFAULT_PORT: 8080,
	widgetsInTemplate: true,
	templateString: dojo.cache('orion', 'widgets/templates/ConvertToSelfHostingDialog.html'), //$NON-NLS-1$ //$NON-NLS-0$

	constructor: function(options) {
		this.options = options || {};
	},
	postMixInProperties: function() {
		this.options.title = messages['Convert to Self-Hosting']; //$NON-NLS-1$
		this.message = i18nUtil.formatMessage(messages["SelectRepoSourceFolder"], ["<b>org.eclipse.orion.client</b>"]); //$NON-NLS-1$ //$NON-NLS-0$
		this.browseMessage = widgetsMessages['Browse...']; //$NON-NLS-1$
		this.portMessage = i18nUtil.formatMessage(messages["EnterPortNumber"], this.DEFAULT_PORT); //$NON-NLS-1$
		this.inherited(arguments);
	},
	postCreate: function() {
		this.inherited(arguments);
		dojo.connect(this.browseButton, 'click', function() { //$NON-NLS-1$
			var dialog = new DirectoryPrompterDialog({
				title: messages["Choose Orion Source Folder"], //$NON-NLS-1$
				serviceRegistry: this.serviceRegistry,
				fileClient: this.fileClient,
				func: this.onFolderChosen.bind(this)
			});
			dialog.startup();
			dialog.show();
		}.bind(this));
		dojo.connect(this.port, 'onchange', function() { //$NON-NLS-1$
			this.portNumber = parseInt(this.port.value, 10);
			this.validate();
		}.bind(this));
		this.portNumber = this.port.value = this.DEFAULT_PORT;
		this.validate();
	},
	onFolderChosen: function(folder) {
		this.folder = folder;
		this.folderText.textContent = folder ? folder.Name : ''; //$NON-NLS-1$
		this.validate();
	},
	validate: function() {
		var isValid = (this.folder && !isNaN(this.portNumber) && this.portNumber > 0);
		this.okButton.set('disabled', !isValid); //$NON-NLS-1$
	},
	execute: function() {
		this.onHide();
		if (typeof this.options.func === 'function') { //$NON-NLS-1$
			this.options.func(this.folder, this.portNumber);
		}
	}
});

var AUTOSAVE_INTERVAL = 8000;
var ROOT = "/"; //$NON-NLS-0$

/**
 * @name orion.widgets.SiteEditor
 * @class Editor for an individual site configuration.
 * @param {Object} options Options bag for creating the widget.
 */
var SiteEditor = dojo.declare("orion.widgets.SiteEditor", [dijit.layout.ContentPane, dijit._Templated], { //$NON-NLS-0$
	widgetsInTemplate: true,
	templateString: dojo.cache('orion', 'widgets/templates/SiteEditor.html'), //$NON-NLS-1$ //$NON-NLS-0$

	/** SiteConfiguration */
	_siteConfiguration: null,
	
	/** Array */
	_modelListeners: null,
	
	/** MappingsTable */
	mappings: null,

	_mappingProposals: null,

	_isSelfHostingSite: false,

	_isDirty: false,
	
	_autoSaveTimer: null,

	constructor: function() {
		this.inherited(arguments);
		this.options = arguments[0] || {};
		this.checkOptions(this.options, ["serviceRegistry", "fileClient", "siteClient", "commandService", "statusService", "progressService"]); //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$

		this._fileClient = this.options.fileClient;
		this._siteClient = this.options.siteClient;
		this._commandService = this.options.commandService;
		this._statusService = this.options.statusService;
		this._progressService = this.options.progressService;
		
		this._commandsContainer = this.options.commandsContainer;
		
		if (this.options.location) {
			this.load(this.options.location);
		}
	},
	
	postMixInProperties: function() {
		this.inherited(arguments);
		this.siteConfigNameLabelText = messages["Name:"];
		this.mappingsLabelText = messages["Mappings:"];
		this.hostHintLabelText = messages["Hostname hint:"];
		this.hostingStatusLabelText = messages["Status:"];
		
		this.hostHintEm = messages["Optional; used to determine the URL where a started site can be accessed."];
		this.siteStartedWarningEm = messages["Changes you make here won't affect the running site."];
	},
	
	postCreate: function() {
		this.inherited(arguments);
		this.refocus = false; // Dojo 10654
		
		// Validation
		this.name.set("invalidMessage", messages["Not a valid name"]); //$NON-NLS-0$
		this.name.set("isValid", dojo.hitch(this, function(focused) { //$NON-NLS-0$
			return focused || dojo.trim(this.name.get("value")) !== ""; //$NON-NLS-0$
		}));
		this.hostHint.set("invalidMessage", messages["Not a valid hostname"]); //$NON-NLS-0$
		this.hostHint.set("isValid", dojo.hitch(this, function(focused) { //$NON-NLS-0$
			var hostish = /^(?:\s*|[A-Za-z0-9-_]+)$/;
			return focused || hostish.test(this.hostHint.get("value")); //$NON-NLS-0$
		}));
		
		dijit.byId("siteForm").onSubmit = dojo.hitch(this, this.save); //$NON-NLS-0$

		// "Convert to self hosting" command
		var self = this;
		dojo.when(this.siteClient._canSelfHost(), function(canSelfHost) {
			var convertCommand = new mCommands.Command({
				name: messages["Convert to Self-Hosting"],
				tooltip: messages["Enable the site configuration to launch an Orion server running your local client code"],
				imageClass: "core-sprite-add", //$NON-NLS-0$
				id: "orion.site.convert", //$NON-NLS-0$
				visibleWhen: function(item) {
					return !!item.Location && canSelfHost && !self._isSelfHostingSite;
				},
				// FIXME selfhosting 
				callback: dojo.hitch(self, self.convertToSelfHostedSite)});
			self._commandService.addCommand(convertCommand);
		});

		this._autoSaveTimer = setTimeout(dojo.hitch(this, this.autoSave), AUTOSAVE_INTERVAL);
	},
	
	checkOptions: function(options, names) {
		for (var i=0; i < names.length; i++) {
			if (typeof options[names[i]] === "undefined") { //$NON-NLS-0$
				throw new Error("options." + names[i] + " is required"); //$NON-NLS-1$ //$NON-NLS-0$
			}
		}
	},
	
	/**
	 * @param {Array} proposals 
	 * @param {Array|Object} items
	 * @param {Object} userData
	 * @returns {Array}
	 */
	_makeAddMenuChoices: function(proposals, items, userData) {
		items = dojo.isArray(items) ? items[0] : items;
		proposals = proposals.sort(function(a, b) {
				return a.FriendlyPath.toLowerCase().localeCompare(b.FriendlyPath.toLowerCase());
			});
		var self = this;
		function addMapping(mapping) {
			// If there is no root, use the root as the Virtual Path
			var hasRoot = self.getSiteConfiguration().Mappings.some(function(m) {
				return m.Source === ROOT;
			});
			if (!hasRoot) {
				mapping.Source = ROOT; //$NON-NLS-0$
			}
			self.mappings.addMapping(mapping);
		}
		/**
		 * @this An object from the choices array with shape {name:String, mapping:Object}
		 */
		var callback = function(data) {
			addMapping(this.mapping);
		};
		var addUrl = function() {
			addMapping({
				Source: "/web/somePath", //$NON-NLS-0$
				Target: "http://", //$NON-NLS-0$
				FriendlyPath: "http://" //$NON-NLS-0$
			});
		};
		var choices = proposals.map(function(proposal) {
				return {
					name: proposal.FriendlyPath,
					imageClass: "core-sprite-folder", //$NON-NLS-0$
					mapping: proposal,
					callback: callback
				};
			});
		if (proposals.length > 0) {
			choices.push({}); // Separator
		}
		choices.push({
			name: messages["Choose folder&#8230;"],
			imageClass: "core-sprite-folder", //$NON-NLS-0$
			callback: dojo.hitch(this, function() {
				var dialog = new DirectoryPrompterDialog({
					serviceRegistry: this.serviceRegistry,
					fileClient: this.fileClient,
					func: dojo.hitch(this, function(folder) {
						if (!!folder) {
							this._siteClient.getMappingObject(this.getSiteConfiguration(), folder.Location, folder.Name).then(
								function(mapping) {
									callback.call({mapping: mapping});
								});
						}
					})});
				dialog.startup();
				dialog.show();
			})});
		choices.push({name: "URL", imageClass: "core-sprite-link", callback: addUrl}); //$NON-NLS-1$ //$NON-NLS-0$
		return choices;
	},

	// Special feature for setting up self-hosting
	// TODO ideally this logic would be defined entirely by a plugin. It is here because of the dialog (UI) dependency
	convertToSelfHostedSite: function(items, userData) {
		var self = this;
		var dialog = new ConvertToSelfHostingDialog({
			serviceRegistry: this.serviceRegistry,
			fileClient: this.fileClient,
			siteClient: this._siteClient,
			func: function(folder, port) {
				self._siteClient.convertToSelfHosting(self.getSiteConfiguration(), folder.Location, port).then(
					function(updatedSite) {
						self.mappings.deleteAllMappings();
						self.mappings.addMappings(updatedSite.Mappings);
						self.save();
					});
			}
		});
		dialog.startup();
		dialog.show();
	},
	
	/**
	 * Loads site configuration from a URL into the editor.
	 * @param {String} location URL of the site configuration to load.
	 * @returns {dojo.Deferred} A deferred, resolved when the editor has loaded & refreshed itself.
	 */
	load: function(location) {
		var deferred = new dojo.Deferred();
		this._busyWhile(deferred, "Loading..."); //$NON-NLS-0$
		this._siteClient.loadSiteConfiguration(location).then(
			dojo.hitch(this, function(siteConfig) {
				this._setSiteConfiguration(siteConfig);
				this.setDirty(false);
				deferred.callback(siteConfig);
			}),
			function(error) {
				deferred.errback(error);
			});
		return deferred;
	},

	_setSiteConfiguration: function(siteConfiguration) {
		this._detachListeners();
		this._siteConfiguration = siteConfiguration;

		// Ask the service for the proposals to put in the dropdown menu
		if (!this._mappingProposals) {
			this._mappingProposals = this._siteClient.getMappingProposals(siteConfiguration).then(dojo.hitch(this, function(proposals) {
				// Register command used for adding mapping
				var addMappingCommand = new mCommands.Command({
					name: messages["Add"],
					tooltip: messages["Add a directory mapping to the site configuration"],
					imageClass: "core-sprite-add", //$NON-NLS-0$
					id: "orion.site.mappings.add", //$NON-NLS-0$
					visibleWhen: function(item) {
						return true;
					},
					choiceCallback: dojo.hitch(this, this._makeAddMenuChoices, proposals)});
				this._commandService.addCommand(addMappingCommand);
				var toolbar = this.titleWrapper.actionsNode;
				this._commandService.registerCommandContribution(toolbar.id, "orion.site.mappings.add", 1); //$NON-NLS-0$
				// do we really have to render here
				this._commandService.renderCommands(toolbar.id, toolbar, this.mappings, this, "button"); //$NON-NLS-0$
			}));
		}

		this._refreshCommands();
		this._refreshFields();
	},
	
	setDirty: function(value) {
		this._isDirty = value;
	},
	
	isDirty: function() {
		return this._isDirty;
	},

	// Called after setSiteConfiguration and after every save/autosave
	_refreshCommands: function() {
		var self = this;
		function errorHandler(err) {
			self._onError(err);
		}
		function reload(site) {
			self._setSiteConfiguration(site);
			self.setDirty(false);
		}
		this._siteClient.isSelfHostingSite(this.getSiteConfiguration()).then(function(isSelfHostingSite) {
			self._isSelfHostingSite = isSelfHostingSite;
			self._commandService.destroy(self._commandsContainer);
			var userData = {
				site: self._siteConfiguration,
				startCallback: reload,
				stopCallback: reload,
				errorCallback: errorHandler
			};
			self._commandService.renderCommands(self._commandsContainer.id, self._commandsContainer, self._siteConfiguration, {}, "button", userData); //$NON-NLS-0$
		});
	},

	_refreshFields: function() {
		this.name.set("value", this._siteConfiguration.Name); //$NON-NLS-0$
		this.hostHint.set("value", this._siteConfiguration.HostHint); //$NON-NLS-0$

		if (!this.mappings) {
			this.titleWrapper = new mSection.Section(this.mappingsPlaceholder.id, {
				id: "workingDirectorySection", //$NON-NLS-0$
				title: "Mappings", //$NON-NLS-0$
				content: '<div id="mappingsNode"/>', //$NON-NLS-0$
				canHide: true
			});
			
			this.mappings = new mSiteMappingsTable.MappingsTable({serviceRegistry: this.serviceRegistry,
					siteClient: this._siteClient, fileClient: this._fileClient, selection: null, 
					parentId: "mappingsNode", siteConfiguration: this._siteConfiguration
				});
		} else {
			this.mappings._setSiteConfiguration(this._siteConfiguration);
		}

		var hostStatus = this._siteConfiguration.HostingStatus;
		if (hostStatus && hostStatus.Status === "started") { //$NON-NLS-0$
			dojo.style(this.siteStartedWarning, {display: "block"}); //$NON-NLS-0$
			this.hostingStatus.textContent = messages["Started at "];
			var a = dojo.create("a", {href: hostStatus.URL, target: "_new"}, this.hostingStatus, "last"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
			a.textContent = hostStatus.URL;
		} else if (hostStatus && hostStatus.Status === "stopped") {
			dojo.style(this.siteStartedWarning, {display: "none"}); //$NON-NLS-0$
			this.hostingStatus.textContent = messages["Stopped"];
		}

		setTimeout(dojo.hitch(this, function() {
			this._attachListeners(this._siteConfiguration);
		}), 0);
	},
	
	/**
	 * Hook up listeners that perform form widget -> model updates.
	 * @param site {SiteConfiguration}
	 */
	_attachListeners: function(site) {
		this._detachListeners();
		this._modelListeners = this._modelListeners || [];
		
		var editor = this;
		function bindText(widget, modelField) {
			function commitWidgetValue() {
				var value = widget.get("value"); //$NON-NLS-0$
				var oldValue = site[modelField];
				site[modelField] = value;
				var isChanged = oldValue !== value;
				editor.setDirty(isChanged || editor.isDirty());
			}
			editor._modelListeners.push(dojo.connect(widget, "onChange", commitWidgetValue)); //$NON-NLS-0$
			editor._modelListeners.push(dojo.connect(widget, "onKeyUp", commitWidgetValue)); //$NON-NLS-0$
		}
		
		bindText(this.name, messages["Name"]);
		bindText(this.hostHint, messages["HostHint"]);
		
		this._modelListeners.push(dojo.connect(this.mappings, "setDirty", this, function(dirty) { //$NON-NLS-0$
			this.setDirty(dirty);
		}));
	},
	
	_detachListeners: function() {
		if (this._modelListeners) {
			for (var i=0; i < this._modelListeners.length; i++) {
				dojo.disconnect(this._modelListeners[i]);
			}
			this._modelListeners.splice(0);
		}
	},
	
	/**
	 * @returns {SiteConfiguration} The site configuration that is being edited.
	 */
	getSiteConfiguration: function() {
		return this._siteConfiguration;
	},
	
	getResource: function() {
		return this._siteConfiguration && this._siteConfiguration.Location;
	},

	/**
	 * Callback when 'save' is clicked.
	 * @Override
	 * @returns True to allow save to proceed, false to prevent it.
	 */
	save: function(refreshUI) {
		refreshUI = typeof refreshUI === "undefined" ? true : refreshUI; //$NON-NLS-0$
		var form = dijit.byId("siteForm"); //$NON-NLS-0$
		if (form.isValid()) {
			var siteConfig = this._siteConfiguration;
			// Omit the HostingStatus field from the object we send since it's likely to be updated from the
			// sites page, and we don't want to overwrite
			var status = siteConfig.HostingStatus;
			delete siteConfig.HostingStatus;
			var self = this;
			var deferred = this._siteClient.updateSiteConfiguration(siteConfig.Location, siteConfig).then(
				function(updatedSiteConfig) {
					self.setDirty(false);
					if (refreshUI) {
						self._setSiteConfiguration(updatedSiteConfig);
						return updatedSiteConfig;
					} else {
						siteConfig.HostingStatus = status;
						self._refreshCommands();
						return siteConfig;
					}
				});
			this._busyWhile(deferred);
			return true;
		} else {
			return false;
		}
	},

	autoSave: function() {
		if (this.isDirty()) {
			this.save(false);
		}
		setTimeout(dojo.hitch(this, this.autoSave), AUTOSAVE_INTERVAL);
	},

	_busyWhile: function(deferred, msg) {
		deferred.then(dojo.hitch(this, this._onSuccess), dojo.hitch(this, this._onError));
		this.progressService.showWhile(deferred, msg);
	},
	
	_onSuccess: function(deferred) {
		this.onSuccess(deferred);
	},
	
	_onError: function(deferred) {
		this._statusService.setErrorMessage(deferred);
		this.onError(deferred);
	},
	
	/**
	 * Clients can dojo.connect() to this function to receive notifications about server calls that succeeded.
	 * @param {dojo.Deferred} deferred The deferred that succeeded.
	 */
	onSuccess: function(deferred) {
	},
		
	/**
	 * Clients can dojo.connect() to this function to receive notifications about server called that failed.
	 * @param {dojo.Deferred} deferred The deferred that errback'd.
	 */
	onError: function(deferred) {
	}
});
	return SiteEditor;
});
