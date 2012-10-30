/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_model" ],
		function(m_utils, m_constants, m_model) {

			return {
				create : function() {
					var Parameter = new Parameter();

					return Parameter;
				}
			};

			/**
			 * 
			 */
			function Parameter() {
				/**
				 * 
				 */
				Parameter.prototype.initializeFromJson = function() {
				};
			}
		});