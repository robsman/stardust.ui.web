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
define(
		[ "bpm-modeler/js/m_utils" ],
		function(m_utils) {
			var cumulatedExtensions = {};

			return {
				registerViewManager : function(extensionsConfig) {
					loadExtensions({
						viewManager : extensionsConfig.viewManager
					});
				},
				registerOutlineExtensions : function(extensionsConfig) {
					loadExtensions({
						menuOption : extensionsConfig.menuOption
					});
				},
				registerToolbarExtensions : function(extensionsConfig) {
					loadExtensions({
						diagramToolbarPalette : extensionsConfig.diagramToolbarPalette
					});
					loadExtensions({
						diagramToolbarPaletteEntry : extensionsConfig.diagramToolbarPaletteEntry
					});
				},

				registerPropertyPageExtensions : function(extensionsConfig) {
					loadExtensions({
						propertiesPage : extensionsConfig.propertiesPage
					});
				},

				registerMetaModelExtensions : function(extensionsConfig) {
					loadExtensions({
						applicationType : extensionsConfig.applicationType
					});
					loadExtensions({
						dataType : extensionsConfig.dataType
					});
				},

				registerIntegrationOverlayExtensions : function(extensionsConfig) {
					loadExtensions({
						applicationIntegrationOverlay : extensionsConfig.applicationIntegrationOverlay
					});
					loadExtensions({
						eventIntegrationOverlay : extensionsConfig.eventIntegrationOverlay
					});
				},

				registerModelDecorationExtensions : function(extensionsConfig) {
					loadExtensions({
						modelDecoration : extensionsConfig.modelDecoration
					});
				},

				registerViewExtensions : function(extensionsConfig) {
					loadExtensions({
						view : extensionsConfig.view
					});
				},

				registerRuleSetProviderExtensions : function(extensionsConfig) {
					loadExtensions({
						ruleSetProvider : extensionsConfig.ruleSetProvider
					});
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

					if (getCumulatedExtensions()[extensionPoint] != null) {
						for ( var n = 0; n < getCumulatedExtensions()[extensionPoint].length; ++n) {

							if (property == null
									|| getCumulatedExtensions()[extensionPoint][n][property] == value) {
								var extension = getCumulatedExtensions()[extensionPoint][n];

								result.push(new Extension(extension));
							}
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
					m_utils.debug(extensionPoint);
					m_utils.debug(getCumulatedExtensions());
					m_utils.debug(getCumulatedExtensions()[extensionPoint]);

					if (getCumulatedExtensions()[extensionPoint].length >= 1) {
						var extension = getCumulatedExtensions()[extensionPoint][0];
						return new Extension(extension);
					}

					throw "Cannot find default for Extension Point "
							+ extensionPoint;
				}
			};

			/**
			 *
			 */
			function getCumulatedExtensions() {
				return cumulatedExtensions;
			}

			/**
			 *
			 */
			function loadExtensions(extensions) {
				for ( var m in extensions) {
					if (!extensions[m]) {
						continue;
					}

					console.log("Adding Extensions of Extension Point: " + m);

					var extensionsForExtensionPoint = extensions[m];

					if (!m_utils.isArray(extensionsForExtensionPoint)) {
						extensionsForExtensionPoint = [ extensionsForExtensionPoint ];
					}

					for ( var n = 0; n < extensionsForExtensionPoint.length; ++n) {
						if (getCumulatedExtensions()[m] == null) {
							getCumulatedExtensions()[m] = [];
						}

						getCumulatedExtensions()[m]
								.push(extensionsForExtensionPoint[n]);
					}
				}
			}

			/**
			 *
			 */
			function Extension(data) {
				m_utils.inheritFields(this, data);

				/**
				 *
				 */
				Extension.prototype.toString = function() {
					return "Lightdust.Extension";
				};

				/**
				 *
				 */
				Extension.prototype.supportedInProfile = function(profile) {
					m_utils.debug("===> Checking profile " + profile);
					if (this.profiles == null) {
						return true;
					}

					for ( var n = 0; n < this.profiles.length; ++n) {
						m_utils.debug("... against " + this.profiles[n]);
						if (profile == this.profiles[n]) {
							return true;
						}
					}

					return false;
				};
			}
		});
