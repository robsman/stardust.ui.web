/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Shrikant.Gangal
 */

(function() {
	'use strict';

	angular.module('modeler-ui').controller(
			'sdApplicationRetriesController',
			[ '$scope', 'sdUtilService', 'sdI18nService', 'sdModelerConstants',
					ApplicationRetriesController ]);

	/**
	 * 
	 */
	function ApplicationRetriesController($scope, sdUtilService, sdI18nService,
			sdModelerConstants) {
		var self = this;
		this.i18n = sdI18nService.getInstance('bpm-modeler-messages').translate;
		this.constants = sdModelerConstants;
		this.noOfRetries = null;
		this.retryInterval = null;

		$scope.$on('REFRESH_PROPERTIES_PANEL',
				function(event, propertiesPanel) {
					self.propertiesPanel = propertiesPanel;
					self.refresh();
				});
	}

	/**
	 * 
	 */
	ApplicationRetriesController.prototype.refresh = function() {
		var app = this.propertiesPanel.modelElement;
		if (app.attributes && app.attributes['synchronous:retry:enable']) {
			this.noOfRetries = parseInt(app.attributes['synchronous:retry:number']);
			this.retryInterval = parseInt(app.attributes['synchronous:retry:time']);
		} else {
			this.noOfRetries = null;
			this.retryInterval = null;
		}
	};

	/**
	 * 
	 */
	ApplicationRetriesController.prototype.onRetryChange = function() {
		var app = this.propertiesPanel.modelElement;
		if (this.noOfRetries > 0) {
			if (this.noOfRetries !== app.attributes['synchronous:retry:number']) {
				app.attributes['synchronous:retry:number'] = this.noOfRetries;
			}
			if (!app.attributes['synchronous:retry:enable']) {
				app.attributes['synchronous:retry:enable'] = true;
			}
			if (this.retryInterval
					&& this.retryInterval != app.attributes['synchronous:retry:time']) {
				app.attributes['synchronous:retry:time'] = this.retryInterval;
			} else if (!this.retryInterval) {
				this.retryInterval = 5;
				app.attributes['synchronous:retry:time'] = 5;
			}
		} else {
			this.retryInterval = "";
			app.attributes['synchronous:retry:enable'] = false;
			app.attributes['synchronous:retry:number'] = null;
			app.attributes['synchronous:retry:time'] = null;
		}
		this.submitRetryChanges();
	};

	/**
	 * 
	 */
	ApplicationRetriesController.prototype.submitRetryChanges = function() {
		this.propertiesPanel.submitChanges({
			attributes : this.propertiesPanel.modelElement.attributes
		});
	};
})();