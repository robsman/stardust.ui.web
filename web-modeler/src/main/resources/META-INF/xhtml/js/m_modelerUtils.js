/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Modeler specific utility functions.
 *
 * @author shrikant.gangal
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_model", "bpm-modeler/js/m_jsfViewManager" ],
		function(m_utils, m_model, m_jsfViewManager) {
			var SCROLL_PANE_ID = "scrollpane";
			var viewManager = m_jsfViewManager.create();

			return {
				getModelerScrollPosition : function() {
					return getScrollpaneScrollPosition();
				},

				closeAllModelerViews : closeAllModelerViews,

				disableToolbarControl : disableToolbarControl,

				enableToolbarControl : enableToolbarControl,

				getUniqueNameForElement : getUniqueNameForElement
			};

			function getScrollpaneScrollPosition() {
				var scrollPane = m_utils.jQuerySelect("#" + SCROLL_PANE_ID).get(0);
				if (scrollPane) {
					return {
						top : scrollPane.scrollTop,
						left : scrollPane.scrollLeft
					}
				}
			}

			/**
			 * Fire close views for all models so that only
			 * modeler related views will be closed.
			 */
			function closeAllModelerViews() {
				m_jsfViewManager
				var models = m_model.getModels();
				for (var i in models) {
					viewManager.closeViewsForElement(models[i].uuid);
				}
			}

			function disableToolbarControl(input) {
				input.attr("disabled", true);
				input.fadeTo(0, 0.5);
				input.removeClass("toolbarButton");
				input.css("cursor", "default");
				input.css("background", "none");
				input.css("border", "none");
			}

			function enableToolbarControl(input) {
				input.removeAttr("disabled");
				input.css("background", "");
				input.css("border", "");
				input.fadeTo(0, 1);
				input.css("cursor", "pointer");
				input.addClass("toolbarButton");
			}

			/**
			 *
			 */
			function getUniqueNameForElement(modelId, namePrefix) {
				var suffix = 0;
				var name = namePrefix + " " + (++suffix);
				var model = m_model.findModel(modelId);
				if (model) {
					while (model.findModelElementByName(name)) {
						var name = namePrefix + " " + (++suffix);
					}
				}

				return name;
			}
		});
