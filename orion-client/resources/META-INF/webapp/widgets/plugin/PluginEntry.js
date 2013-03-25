/*******************************************************************************
 * @license
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 * 
 * Contributors: Anton McConville - IBM Corporation - initial API and implementation
 ******************************************************************************/
/*global dojo dijit widgets orion  window console define*/
/*jslint browser:true*/

/* This PluginEntry widget supplies the HTML and related interactivity to display
   detailed information about a single plugin. It is designed to work with the
   PluginList widget, but each plugin entry element should fit into any HTML list
   or table */

define(['i18n!orion/settings/nls/messages', 'require', 'dojo', 'dijit', 'orion/PageUtil', 'dijit/TooltipDialog', 'orion/widgets/plugin/ServiceCarousel'], function(messages, require, dojo, dijit, PageUtil) {

		/*	formatLocationAsPluginName - temporary function - 
		the current plugins don't provide useful enough, or 
		consistent meta-data to use for descriptions - so this 
		function derives what it can from naming patterns seen in
		existing commonly used plugin urls. */

	function formatLocationAsPluginName(location) {
	
		function wordToUpper(strSentence) {
	
			function convertToUpper() {
				return arguments[0].toUpperCase();
			}
	
			return strSentence.toLowerCase().replace(/\b[a-z]/g, convertToUpper);
		}
	
		var divides = location.split("/"); //$NON-NLS-0$
		var last = divides[divides.length - 1];
		last = last.split(".html")[0]; //$NON-NLS-0$
		last = last.replace(/([a-z])([A-Z])/, "$1 $2"); //$NON-NLS-0$
		last = wordToUpper(last);
		last = last.replace('plugin', ''); //$NON-NLS-0$
		last = last.replace('Plugin', ''); //$NON-NLS-0$
	
		if (last === '') {
			last = location;
		}
		return last;
	}
	
	function createServiceDescriptions(plugin) {
		var serviceDescriptions = [];
		plugin.getServiceReferences().forEach(function(reference) {
			var serviceDescription = {
				service: reference.getProperty("objectClass").join(" "),
				items: []
			};
	
			reference.getPropertyKeys().forEach(function(name) {
				var item = {
					item: name,
					value: reference.getProperty(name)
				};
				serviceDescription.items.push(item);
			});
			serviceDescriptions.push(serviceDescription);
		});
		return serviceDescriptions;
	}

	
	dojo.declare("orion.widgets.plugin.PluginEntry", [dijit._Widget, dijit._Templated], { //$NON-NLS-0$
	
		iconSource: "data:image/png;base64,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", //$NON-NLS-0$
	
		templateString: '<list style="overflow:hidden;">' + //$NON-NLS-0$
							'<div class="plugin-list-item">' + //$NON-NLS-0$
								'<div data-dojo-attach-point="pluginContainer">' + //$NON-NLS-0$
									'<div class="plugin-icon"></div>' + //$NON-NLS-0$
									'<div class="stretch" data-dojo-attach-point="detailsView">' + //$NON-NLS-0$
										'<span data-dojo-attach-point="pluginTitle" class="plugin-title"></span>' + //$NON-NLS-0$
										'<div></div>' + //$NON-NLS-0$
										'<span class="plugin-description" data-dojo-attach-point="pluginDescription"></span>' +  //$NON-NLS-2$ //$NON-NLS-0$
										'<div data-dojo-attach-point="pluginLinks" style="font-size:11px"></div>' + //$NON-NLS-0$
									'</div>' + //$NON-NLS-0$
									'<span class="plugin-commands" data-dojo-attach-point="commandSpan"></span>' + //$NON-NLS-0$
								'</div>' + //$NON-NLS-0$
							'</div>'+ //$NON-NLS-0$
							'<div class="plugin-service-item" data-dojo-attach-point="serviceContainer"></div>' + //$NON-NLS-0$
						'</list>', //$NON-NLS-0$
		serviceDescriptions: null,
		addPluginLink: function(name, url)  {
			if (!PageUtil.validateURLScheme(url)) {
				console.log("Illegal "+ name +" URL: " + url);
				return;
			}
			var link = dojo.create("a",{'class': "plugin-link", style: "margin-right: 5px;", href: url, title: name}, this.pluginLinks);
			link.textContent = name;
		},
		postCreate: function(){	
			var headers = this.plugin.getHeaders();
			var location = this.plugin.getLocation();
			this.pluginTitle.textContent = headers.name || formatLocationAsPluginName(location);
			this.pluginDescription.textContent = headers.description || messages['A plugin for Eclipse Orion'];

			// Plugin Links
			this.addPluginLink("Plugin", location);
			if (headers.website) {
				this.addPluginLink("Website", headers.website);
			}
			if (headers.license) {
				this.addPluginLink("License", headers.license);
			}
			if (headers.login) {
				this.addPluginLink("Login", headers.login);
			}
			
			this.commandSpan.id = location;
			this.commandService.renderCommands("pluginCommand", this.commandSpan, location, this, "tool"); //$NON-NLS-1$ //$NON-NLS-0$
			this.serviceDescriptions = createServiceDescriptions(this.plugin);				
			this.carousel = new orion.widgets.plugin.ServiceCarousel({serviceData:this.serviceDescriptions}, this.serviceContainer );
			this.carousel.startup();
		}
	});
});
