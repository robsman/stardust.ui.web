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
 * View Management
 * 
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_dialog",
				"stardust-portal-shell/js/ProcessModelingLaunchPanel"/*,
				"stardust-portal-shell/js/RulesManagementLaunchPanel" */],
		function(m_utils, m_extensionManager, m_dialog,
				ProcessModelingLaunchPanel/*, RulesManagementLaunchPanel*/) {
			var perspectives = [ {
				id : "processModeling",
				name : "Process Modeling",
				pageHtmlUrl : "processModelingOutline.html",
				provider : ProcessModelingLaunchPanel,
				initial : true
			}/*, {
				id : "rulesManagement",
				name : "Rules Management",
				pageHtmlUrl : "rulesManagementOutline.html",
				provider : RulesManagementLaunchPanel,
				initial : false
			}*/ ];
			return {
				create : function(viewManager) {
					var perspectiveManager = new PerspectiveManager();

					perspectiveManager.initialize(viewManager);

					return perspectiveManager;
				}
			};

			/**
			 * 
			 */
			function PerspectiveManager() {
				/**
				 * 
				 */
				PerspectiveManager.prototype.toString = function() {
					return "Lightdust.PerspectiveManager";
				};

				/**
				 * 
				 */
				PerspectiveManager.prototype.initialize = function(viewManager) {
					this.viewManager = viewManager;
					this.perspectiveSelect = jQuery("#perspectiveSelect");
					this.launchPanelAnchor = jQuery("#launchPanelAnchor");
					this.perspectiveLaunchPanels = {};
					this.perspectiveLaunchPanelControllers = {};
					this.initialPerspectiveId = null;

					var perspectiveExtensions = {};
					var perspectiveManager = this;

					for ( var n = 0; n < perspectives.length; ++n) {
						var perspectiveExtension = perspectives[n];

						perspectiveExtensions[perspectiveExtension.id] = perspectiveExtension;

						var option = jQuery("<option value='"
								+ perspectiveExtension.id + "'>"
								+ perspectiveExtension.name + "</option>");

						this.perspectiveSelect.append(option);

						var perspectiveDiv = jQuery("<div id='"
								+ perspectiveExtension.id + "'></div>");

						this.launchPanelAnchor
						.append(perspectiveDiv);

						perspectiveDiv
								.load(
										perspectiveExtension.pageHtmlUrl,
										function(response, status, xhr) {
											if (status == "error") {
												var msg = "Properties Page Load Error: "
														+ xhr.status
														+ " "
														+ xhr.statusText;

												jQuery(this).append(msg);
												m_utils.debug(msg);
											} else {
												var perspectiveExtension = perspectiveExtensions[jQuery(
														this).attr("id")];
												var launchPanelController = perspectiveExtension.provider
														.create(perspectiveManager.viewManager);
												perspectiveManager.perspectiveLaunchPanelControllers[perspectiveExtension.id] = launchPanelController;
												perspectiveManager.perspectiveLaunchPanels[perspectiveExtension.id] = jQuery(this);

												if (perspectiveExtension.initial) {
													perspectiveManager
															.initialPerspectiveId = perspectiveExtension.id;
												}
											}
										});
					}

					this.perspectiveSelect.change({
						perspectiveManager : this
					}, function(event) {
						event.data.perspectiveManager.changePerspective(jQuery(
								this).val());
					});
				};

				/**
				 * 
				 */
				PerspectiveManager.prototype.changePerspective = function(id) {
					for ( var n in this.perspectiveLaunchPanels) {
						m_dialog.makeInvisible(this.perspectiveLaunchPanels[n]);
					}

					this.perspectiveSelect.val(id);
					m_dialog.makeVisible(this.perspectiveLaunchPanels[id]);
					this.perspectiveLaunchPanelControllers[id].activate();
				};

				/**
				 * 
				 */
				PerspectiveManager.prototype.activateInitialPerspective = function() {
					this.changePerspective(this.initialPerspectiveId);
				};
			}
		});
