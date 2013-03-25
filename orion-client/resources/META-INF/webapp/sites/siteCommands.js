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

/*global console define document window*/
/*jslint laxbreak:true*/
define(['i18n!orion/sites/nls/messages', 'require', 'orion/commands', 'orion/sites/siteUtils', 'orion/sites/siteClient', 
			'orion/Deferred', 'orion/i18nUtil'],
		function(messages, require, mCommands, mSiteUtils, mSiteClient, Deferred, i18nUtil) {
	var Command = mCommands.Command;
	var formatMessage = i18nUtil.formatMessage;
	/**
	 * Creates & adds commands that act on an site service.
	 * @param {orion.serviceregistry.ServiceRegistry} serviceRegistry
	 * @param {Function} options.createCallback
	 * @param {Function} options.errorCallback
	 * @name orion.sites.siteCommands#createSiteServiceCommands
	 */
	function createSiteServiceCommands(serviceRegistry, options) {
		function getFileService(siteServiceRef) {
			return mSiteClient.getFileClient(serviceRegistry, siteServiceRef.getProperty('filePattern')); //$NON-NLS-0$
		}
		options = options || {};
		var commandService = serviceRegistry.getService("orion.page.command"); //$NON-NLS-0$
		var createCommand = new mCommands.Command({
			name : messages["Create"],
			tooltip: messages["Create a new site configuration"],
			id: "orion.site.create", //$NON-NLS-0$
			parameters: new mCommands.ParametersDescription([new mCommands.CommandParameter('name', 'string', 'Name:')]), //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-0$
			visibleWhen: function(bah) {
				return true;
			},
			callback : function(data) {
				var siteServiceRef = data.items, siteService = serviceRegistry.getService(siteServiceRef);
				var fileService = getFileService(siteServiceRef);
				var name = data.parameters && data.parameters.valueFor('name'); //$NON-NLS-0$
				fileService.loadWorkspaces().then(function(workspaces) {
			        var workspaceId = workspaces && workspaces[0] && workspaces[0].Id;
			        if (workspaceId && name) {
				        siteService.createSiteConfiguration(name, workspaceId).then(function(site) {
							options.createCallback(mSiteUtils.generateEditSiteHref(site), site);
						}, options.errorCallback);
			        }
				});
			}});
		commandService.addCommand(createCommand);
	}

	function wrap(i) {
		return i instanceof Array ? i : [i];
	}

	function unwrap(i) {
		return function(result) {
			if (result instanceof Array && result.length === 1 && !(i instanceof Array)) {
				return result[0];
			}
		};
	}

	/**
	 * Creates & adds commands that act on an individual site configuration.
	 * @param {orion.serviceregistry.ServiceRegistry} serviceRegistry
	 * @name orion.sites.siteCommands#createSiteCommands
	 */
	function createSiteCommands(serviceRegistry) {
		
		var commandService = serviceRegistry.getService("orion.page.command"), //$NON-NLS-0$
		    dialogService = serviceRegistry.getService("orion.page.dialog"), //$NON-NLS-0$
		    progressService = serviceRegistry.getService("orion.page.progress"); //$NON-NLS-0$
		
		var editCommand = new Command({
			name: messages["Edit"],
			tooltip: messages["Edit the site configuration"],
			imageClass: "core-sprite-edit", //$NON-NLS-0$
			id: "orion.site.edit", //$NON-NLS-0$
			visibleWhen: function(item) {
				if (item instanceof Array && item.length === 1) {
					item = item[0];
				}
				
				return item.HostingStatus;
			},
			hrefCallback: function(data) {
				var item = (data.items instanceof Array ? data.items[0] : data.items);
				return mSiteUtils.generateEditSiteHref(item);
			}
		});
		commandService.addCommand(editCommand);

		var userDataErr = 'userData.site requires a single site configuration to act upon'; //$NON-NLS-0$
		var startCommand = new Command({
			name: messages["Start"],
			tooltip: messages["Start the site"],
			imageClass: "core-sprite-start", //$NON-NLS-0$
			id: "orion.site.start", //$NON-NLS-0$
			visibleWhen: function(item) {
				return wrap(item).some(function(s) {
					return s.HostingStatus && s.HostingStatus.Status === "stopped"; //$NON-NLS-0$
				});
			},
			/**
			 * @param {SiteConfiguration} [userData.site] If passed, we'll mutate this site config.
			 * @param {Function} [userData.startCallback]
			 * @param {Function} [userData.errorCallback]
			 */
			callback: function(data) {
				var items = wrap(data.items);
				var userData = data.userData;
				if (userData.site && items.length > 1) {
					throw new Error(userDataErr);
				}
				var starts = items.map(function(item) {
					var newItem = userData.site || {} /* just update the HostingStatus */;
					newItem.HostingStatus = { Status: "started" }; //$NON-NLS-0$
					var location = item.Location;
					var siteService = mSiteClient.forLocation(serviceRegistry, location);
					return siteService.updateSiteConfiguration(location, newItem);
				});
				progressService.showWhile(Deferred.all(starts))
					.then(unwrap(data.items))
					.then(userData.startCallback, userData.errorCallback);
			}});
		commandService.addCommand(startCommand);

		var stopCommand = new Command({
			name: messages["Stop"],
			tooltip: messages["Stop the site"],
			imageClass: "core-sprite-stop", //$NON-NLS-0$
			id: "orion.site.stop", //$NON-NLS-0$
			visibleWhen: function(item) {
				return wrap(item).some(function(s) {
					return s.HostingStatus && s.HostingStatus.Status === "started"; //$NON-NLS-0$
				});
			},
			/**
			 * @param {SiteConfiguration} [data.userData.site] If passed, we'll mutate this site config.
			 * @param {Function} [data.userData.stopCallback]
			 * @param {Function} [data.userData.errorCallback]
			 */
			callback: function(data) {
				var items = wrap(data.items);
				var userData = data.userData;
				if (userData.site && items.length > 1) {
					throw new Error(userDataErr);
				}
				var stops = items.map(function(item) {
					var newItem = userData.site || {} /* just update the HostingStatus */;
					newItem.HostingStatus = { Status: "stopped" }; //$NON-NLS-0$
					var location = item.Location;
					var siteService = mSiteClient.forLocation(serviceRegistry, location);
					return siteService.updateSiteConfiguration(location, newItem);
				});
				progressService.showWhile(Deferred.all(stops))
					.then(unwrap(data.items))
					.then(userData.stopCallback, userData.errorCallback);
			}});
		commandService.addCommand(stopCommand);

		var deleteCommand = new Command({
			name: messages["Delete"],
			tooltip: messages["Delete the site configuration"],
			imageClass: "core-sprite-delete", //$NON-NLS-0$
			id: "orion.site.delete", //$NON-NLS-0$
			visibleWhen: function(items) {
				var siteArray = wrap(items);
				if (siteArray.length > 0) {
					return siteArray.every(function(item) {
						return item.HostingStatus && item.HostingStatus.Status === "stopped"; //$NON-NLS-0$
					});
				}
				return false;
			},
			/**
			 * @param {Function} [data.userData.deleteCallback]
			 * @param {Function} [data.userData.errorCallback]
			 */
			callback: function(data) {
				var items = wrap(data.items);
				var userData = data.userData;
				var msg = (items.length === 1)
					? formatMessage(messages.ConfirmDeleteSingle, items[0].Name)
					: formatMessage(messages.ConfirmDeleteMultiple, items.length);
				dialogService.confirm(msg, function(confirmed) {
					if (confirmed) {
						var deletes = items.map(function(item) {
							var location = item.Location;
							var siteService = mSiteClient.forLocation(serviceRegistry, location);
							return siteService.deleteSiteConfiguration(location);
						});
						Deferred.all(deletes).then(unwrap(data.items)).then(userData.deleteCallback, userData.errorCallback);
					}
				});
			}});
		commandService.addCommand(deleteCommand);
	}

// TODO deal with this
//	var workspacesCache = null;
//
//	function WorkspacesCache(fileService) {
//		var promise = null;
//		this.getWorkspaces = function() {
//			if (!promise) {
//				promise = fileService.loadWorkspaces();
//			}
//			return promise;
//		};
//	}
//
//	function initCache(serviceRegistry) {
//		if (!workspacesCache) {
//			workspacesCache = new WorkspacesCache(serviceRegistry.getService("orion.core.file"));
//		}
//	}
	/**
	 * @param {orion.serviceregistry.ServiceRegistry} serviceRegistry
	 * @name orion.sites.siteCommands#createViewOnSiteCommands
	 */
	function createViewOnSiteCommands(serviceRegistry, options) {
		var fileService = serviceRegistry.getService("orion.core.file"); //$NON-NLS-0$
		var commandService = serviceRegistry.getService("orion.page.command"); //$NON-NLS-0$
		commandService.addCommand(new Command({
			name: messages["Add to site"],
			tooltip: messages["Add the file to this site"],
			id: "orion.site.add-to", //$NON-NLS-0$
			imageClass: "core-sprite-add", //$NON-NLS-0$
			visibleWhen: function(item) {
				// Model tells us whether the file is running on the site configuration
				return !item.IsFileRunningOn;
			},
			/**
			 * @param {Function} data.userData.addToCallback
			 * @param {Function} data.userData.errorCallback
			 * @param {Object} data.userData.file
			 */
			callback: function(data) {
				var file = data.userData.file;
				var site = data.items.SiteConfiguration;
				return fileService.loadWorkspaces().then(function(workspaces) {
					return mSiteClient.forFileLocation(serviceRegistry, file.Location).mapOnSiteAndStart(site, file, workspaces[0].Id);
				}).then(data.userData.addToCallback, data.userData.errorCallback);
			}}));
		// Command that generates a href to view the file on the site if it's mapped
		commandService.addCommand(new Command({
			name: messages["View"],
			tooltip: messages["View the file on the site"],
			id: "orion.site.view-on-link", //$NON-NLS-0$
			visibleWhen: function(item) {
				return item.IsFileRunningOn;
			},
			hrefCallback: function(data) {
				var file = data.userData.file;
				var site = data.items.SiteConfiguration;
				return mSiteClient.forFileLocation(serviceRegistry, file.Location).getURLOnSite(site, file);
			}}));
	}
	return {
		createSiteServiceCommands: createSiteServiceCommands,
		createSiteCommands: createSiteCommands,
		createViewOnSiteCommands: createViewOnSiteCommands
	};
});