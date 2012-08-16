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
		initialize: function(require) {
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

			for ( var n = 0; n < extensions[extensionPoint].length; ++n) {

				if (property == null || extensions[extensionPoint][n][property] == value) {
					var extension = extensions[extensionPoint][n];
					if ( !extension.provider) {
						if (extension.pageJavaScriptUrl) {
							extension.provider = loadExtension(extension.pageJavaScriptUrl);
						} else if (extension.handler) {
							extension.provider = loadExtension(extension.handler);
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
			if (extensions[extensionPoint].length == 1) {
				var extension = extensions[extensionPoint][0];
				if ( !extension.provider) {
					if (extension.moduleUrl) {
						extension.provider = loadExtension(extension.moduleUrl);
					}
				}
				return extension;
			}

			throw "Cannot find default for Extension Point " + extensionPoint;
		}
	};
});
