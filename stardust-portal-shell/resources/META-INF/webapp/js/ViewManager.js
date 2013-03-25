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
		[ "bpm-modeler/js/m_utils",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_urlUtils" ],
		function(Utils, m_extensionManager, m_urlUtils) {

			var TAB_TEMPLATE = "";

			return {
				create : function() {
					var viewManager = new ViewManager();

					viewManager.initialize();

					return viewManager;
				}
			};

			/**
			 * 
			 */
			function ViewManager() {
				/**
				 * 
				 */
				ViewManager.prototype.toString = function() {
					return "Lightdust.ViewManager";
				};

				/**
				 * 
				 */
				ViewManager.prototype.initialize = function() {
					this.views = []
					this.viewsByUri = {};
					this.tabsAnchor = jQuery("#tabsAnchor");
				};

				/**
				 * 
				 */
				ViewManager.prototype.openView = function(viewId, queryString,
						objectId) {
					var extension = m_extensionManager.findExtensions("view",
							"viewId", viewId)[0];
					var viewUrl = m_urlUtils.getPlugsInRoot()
							+ extension.viewHtmlUrl;
					var viewUri = viewUrl + "?" + queryString;
					var viewId = viewId + "?" + queryString;

					Utils.debug("View ID: " + viewId);
					Utils.debug("View URI: " + viewUri);

					var view = {
						id : viewId,
						uri : viewUri,
						label : this.determineLabel(extension.label,
								queryString),
						iconUrl : extension.iconUrl
					};

					this.views.push(view);
					this.viewsByUri[viewId] = view;

					this.refreshViewTabs();
				};

				/**
				 * 
				 */
				ViewManager.prototype.determineLabel = function(label,
						queryString) {
					Utils.debug("label = " + label);
					Utils.debug("queryString = " + queryString);
					
					var parameters = queryString.split("&");

					for ( var n = 0; n < parameters.length; ++n) {
						Utils.debug("parameters[n] = " + parameters[n]);
						
						var nameValue = parameters[n].split("=");
						var name = nameValue[0];
						var value = nameValue[1];

						Utils.debug("name = " + name);
						Utils.debug("value = " + value);

						var token = "${viewParams." + name + "}";

						if (label.indexOf(token) >= 0) {
							var prefix = label.substring(0, label
									.indexOf(token));
							var suffix = label.substring(label
									.indexOf(token)
									+ token.length);
							var infix = value;

							Utils.debug("prefix = " + prefix);
							Utils.debug("suffix = " + suffix);

							if (suffix.indexOf("[") == 0) {
								var length = suffix.substring(1, suffix
										.indexOf("]"));

								Utils.debug("length = " + length);

								suffix = suffix.substring(length.length + 2);

								if (length < value.length) {
									infix = value.substring(0, length) + "...";
								}

								Utils.debug("suffix = " + suffix);
								Utils.debug("infix = " + infix);
							}

							label = prefix + infix + suffix;
						}
					}
					
					return label;
				}

				/**
				 * 
				 */
				ViewManager.prototype.refreshViewTabs = function() {
					this.tabsAnchor.empty();

					this.tabs = jQuery("<div id='viewTabs'></div>");

					this.tabsAnchor.append(this.tabs);

					this.tabHeaders = jQuery("<ul id='viewTabsHeaders'></ul>");

					this.tabs.append(this.tabHeaders);

					for ( var n = 0; n < this.views.length; ++n) {
						this.tabHeaders
								.append(jQuery("<li><div class='tabHeader'><a href='#view"
										+ n
										+ "'><img src='"
										+ m_urlUtils.getPlugsInRoot()
										+ this.views[n].iconUrl
										+ "' class='tabIcon' ><span class='label'>"
										+ this.views[n].label
										+ "</span></a><span class='ui-icon ui-icon-close'>Remove Tab</span></div></li>"));

						// Content

						this.tabs
								.append(jQuery("<div id='view"
										+ n
										+ "' class='viewContent' style='width: 100%; height: 800px;'><iframe src='"
										+ this.views[n].uri
										+ "' class='viewFrame' width='1650' height='800'></iframe></div>"));

						var viewManager = this;

						jQuery("#viewTabsHeaders span.ui-icon-close").live(
								"click",
								function() {
									var viewContentId = jQuery(this).closest(
											"li").find("a").attr("href");
									var viewIndex = jQuery(this).closest("li")
											.find("a").attr("href")
											.substring(5);

									jQuery(this).closest("li").remove();

									Utils.debug("Panel ID: " + viewContentId);

									jQuery(viewContentId).remove();

									var view = viewManager.views[viewIndex];

									delete viewManager.viewsByUri[view.id];

									Utils.removeItemFromArray(
											viewManager.views, view);

									viewManager.refreshViewTabs();
								});
					}

					this.tabs.tabs();
					this.tabs.tabs("select", this.views.length - 1);
				};
			}
		});
