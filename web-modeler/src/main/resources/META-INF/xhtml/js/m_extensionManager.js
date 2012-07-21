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
define([ "m_utils" ], function(m_utils) {
	return {
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

				if (extensions[extensionPoint][n][property] == value) {
					result.push(extensions[extensionPoint][n]);
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
				return extensions[extensionPoint][0];
			}

			throw "";
		}
	};
});
