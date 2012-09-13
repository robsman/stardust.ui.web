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
		[ "m_utils", "m_constants" ],
		function(m_utils, m_constants) {
			var managerMap = [];
			var PANNING_INCREMENT = 10;
			var PANNING_INTERVAL = 50;
			var PANNING_SENSOR_WIDTH = 50;

			return {
				initScrollManager : function(divId, inAutoScrollModeCallback) {
					if (!managerMap[divId]) {
						var scrollManager = new AutoScrollManager(divId, inAutoScrollModeCallback);
						scrollManager.intialize();
						managerMap[divId] = scrollManager;
					}

					return managerMap[divId];
				}
			};

			function AutoScrollManager(divId, inAutoScrollModeCallback) {
				this.divId = divId;
				this.inAutoScrollModeCallback = inAutoScrollModeCallback;

				var thisCallbackRef = this;
				var scrollDiv = $("#" + divId);
				var scrollRightEnabled = false;
				var scrollLeftEnabled = false;
				var scrollTopEnabled = false;
				var scrollBottomEnabled = false;
				var pageX;
				var pageY;

				AutoScrollManager.prototype.intialize = function() {
					this.setupEventHandling();
				};

				AutoScrollManager.prototype.setupEventHandling = function() {
					$(document).mousemove(function(event) {
						pageX = event.pageX;
						pageY = event.pageY;
						if (true == thisCallbackRef.inAutoScrollModeCallback()) {
							if ((true == thisCallbackRef.isCursorInRightMargin()) && (false == scrollRightEnabled)) {
								thisCallbackRef.triggerScrollRight();
							}
							if ((true == thisCallbackRef.isCursorInLeftMargin()) && (false == scrollLeftEnabled)) {
								thisCallbackRef.triggerScrollLeft();
							}
							if ((true == thisCallbackRef.isCursorInTopMargin()) && (false == scrollTopEnabled)) {
								thisCallbackRef.triggerScrollTop();
							}
							if ((true == thisCallbackRef.isCursorInBottomMargin()) && (false == scrollBottomEnabled)) {
								thisCallbackRef.triggerScrollBottom();
							}
						}
					});
				};

				AutoScrollManager.prototype.isCursorInRightMargin = function() {
					if (pageX > parseInt(scrollDiv.position().left + scrollDiv.width() - PANNING_SENSOR_WIDTH)
							&& pageX < parseInt(scrollDiv.position().left + scrollDiv.width())) {
						return true;
					}

					return false;
				};

				AutoScrollManager.prototype.isCursorInLeftMargin = function() {
					if (pageX < parseInt(scrollDiv.position().left + PANNING_SENSOR_WIDTH)
							&& pageX > parseInt(scrollDiv.position().left)) {
						return true;
					}

					return false;
				};

				AutoScrollManager.prototype.isCursorInTopMargin = function() {
					if (pageY > parseInt(scrollDiv.position().top + scrollDiv.height() - PANNING_SENSOR_WIDTH)
							&& pageY < parseInt(scrollDiv.position().top + scrollDiv.height())) {
						return true;
					}

					return false;
				};

				AutoScrollManager.prototype.isCursorInBottomMargin = function() {
					if (pageY < parseInt(scrollDiv.position().top + PANNING_SENSOR_WIDTH)
							&& pageY > parseInt(scrollDiv.position().top)) {
						return true;
					}

					return false;
				};

				AutoScrollManager.prototype.triggerScrollRight = function() {
					scrollRightEnabled = true;
					setTimeout(function() {
						if (true == thisCallbackRef.isCursorInRightMargin()) {
							thisCallbackRef.scrollRight();
							thisCallbackRef.triggerScrollRight();
						} else {
							scrollRightEnabled = false;
						}
					}, PANNING_INTERVAL);
				};

				AutoScrollManager.prototype.triggerScrollLeft = function() {
					scrollLeftEnabled = true;
					setTimeout(function() {
						if (true == thisCallbackRef.isCursorInLeftMargin()) {
							thisCallbackRef.scrollLeft();
							thisCallbackRef.triggerScrollLeft();
						} else {
							scrollLeftEnabled = false;
						}
					}, PANNING_INTERVAL);
				};

				AutoScrollManager.prototype.triggerScrollTop = function() {
					scrollTopEnabled = true;
					setTimeout(function() {
						if (true == thisCallbackRef.isCursorInTopMargin()) {
							thisCallbackRef.scrollTop();
							thisCallbackRef.triggerScrollTop();
						} else {
							scrollTopEnabled = false;
						}
					}, PANNING_INTERVAL);
				};

				AutoScrollManager.prototype.triggerScrollBottom = function() {
					scrollBottomEnabled = true;
					setTimeout(function() {
						if (true == thisCallbackRef.isCursorInBottomMargin()) {
							thisCallbackRef.scrollBottom();
							thisCallbackRef.triggerScrollBottom();
						} else {
							scrollBottomEnabled = false;
						}
					}, PANNING_INTERVAL);
				};

				AutoScrollManager.prototype.scrollRight = function() {
					scrollDiv.animate({scrollLeft : '+=' + PANNING_INCREMENT + 'px'}, PANNING_INTERVAL);
				};

				AutoScrollManager.prototype.scrollLeft = function() {
					scrollDiv.animate({scrollLeft : '-=' + PANNING_INCREMENT + 'px'}, PANNING_INTERVAL);
				};

				AutoScrollManager.prototype.scrollTop = function() {
					scrollDiv.animate({scrollTop : '+=' + PANNING_INCREMENT + 'px'}, PANNING_INTERVAL);
				};

				AutoScrollManager.prototype.scrollBottom = function() {
					scrollDiv.animate({scrollTop : '-=' + PANNING_INCREMENT + 'px'}, PANNING_INTERVAL);
				};
			}
		});