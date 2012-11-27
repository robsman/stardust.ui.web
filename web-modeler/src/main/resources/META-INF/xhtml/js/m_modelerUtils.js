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
		["bpm-modeler/js/m_utils"],
		function(m_utils) {
			var SCROLL_PANE_ID = "scrollpane";

			return {
				getModelerScrollPosition : function() {
					return getScrollpaneScrollPosition();
				}
			};

			function getScrollpaneScrollPosition() {
				var scrollPane = document.getElementById(SCROLL_PANE_ID);
				if (scrollPane) {
					return {
						top : scrollPane.scrollTop,
						left : scrollPane.scrollLeft
					}
				}
			}
		});
