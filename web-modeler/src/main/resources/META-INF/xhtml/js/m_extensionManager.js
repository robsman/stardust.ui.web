/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * Extension Management
 * 
 * @author Marc.Gille
 */
define(function(require) {
	var mainRequire = require;
	var cumulatedExtensions = null;

	var additionalExtensions = {
		propertiesPage : [ {
			panelId : "testPropertiesPanel",
			pageId : "basicPropertiesPage",
			pageJavaScriptUrl : "m_testBasicPropertiesPage",
			visibility : "always"
		} ],
		diagramToolbarPalette : [ {
			id : "testPalette",
			title : "Test",
			visibility : "always"
		} ],
		diagramToolbarPaletteEntry : [ {
			id : "testButton",
			paletteId : "testPalette",
			title : "Create Test Symbol",
			iconUrl : "../../images/icons/camunda.gif",
			handler : "m_testPaletteHandler",
			handlerMethod : "createTestSymbol",
			visibility : "always"
		} ]
	};

	function loadExtension(extensionUri) {
		var provider = mainRequire(extensionUri);

		return provider;
	}

	return {

		/**
		 * Initializes the extension manager, binding the environment used for
		 * resolving extensions.
		 * 
		 * @param require
		 *            the requirejs context to be used to resolve extensions
		 */
		initialize : function(require) {
			mainRequire = require;
		},

		/**
		 * 
		 * @param extensionPoint
		 * @param property
		 * @param value
		 * @returns
		 */
		findExtensions : function(extensionPoint, property, value) {
			var result = [];

			for ( var n = 0; n < getCumulatedExtensions()[extensionPoint].length; ++n) {

				if (property == null
						|| getCumulatedExtensions()[extensionPoint][n][property] == value) {
					var extension = getCumulatedExtensions()[extensionPoint][n];

					if (!extension.provider) {
						if (extension.pageJavaScriptUrl) {
							extension.provider = loadExtension(extension.pageJavaScriptUrl);
						} else if (extension.handler) {
							extension.provider = loadExtension(extension.handler);
						} else if (extension.controllerJavaScriptUrl) {
							extension.controller = loadExtension(extension.controllerJavaScriptUrl);
						}
					}

					result.push(extension);
				}
			}

			return result;
		},

		/**
		 * 
		 * @param extensionPoint
		 * @returns
		 */
		findExtension : function(extensionPoint) {
			if (getCumulatedExtensions()[extensionPoint].length == 1) {
				var extension = getCumulatedExtensions()[extensionPoint][0];
				if (!extension.provider) {
					if (extension.moduleUrl) {
						extension.provider = loadExtension(extension.moduleUrl);
					}
				}
				return extension;
			}

			throw "Cannot find default for Extension Point " + extensionPoint;
		}
	};

	/**
	 * 
	 */
	function getCumulatedExtensions() {
		if (cumulatedExtensions == null) {
			cumulatedExtensions = {};

			loadExtensions(extensions);
			//loadExtensions(additionalExtensions);
		}

		return cumulatedExtensions;
	}

	/**
	 * 
	 */
	function loadExtensions(extensions) {
		for ( var m in extensions) {
			console.log("Adding Extensions of Extension Point: " + m);

			var extensionsForExtensionPoint = extensions[m];

			for ( var n = 0; n < extensionsForExtensionPoint.length; ++n) {
				console.log("Extension: " + extensionsForExtensionPoint[n]);

				if (getCumulatedExtensions()[m] == null) {
					getCumulatedExtensions()[m] = [];
				}

				getCumulatedExtensions()[m]
						.push(extensionsForExtensionPoint[n]);
			}
		}
	}
});
