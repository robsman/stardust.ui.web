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
			'sdActivityResubmissionController',
			[ '$scope', 'sdUtilService', 'sdI18nService', 'sdModelerConstants',
					ActivityResubmissionController ]);

	/**
	 * 
	 */
	function ActivityResubmissionController($scope, sdUtilService,
			sdI18nService, sdModelerConstants) {
		var self = this;
		this.i18n = sdI18nService.getInstance('bpm-modeler-messages').translate;
		this.constants = sdModelerConstants;
		this.enableResubmission = false;
		this.isInteractive = false;
		this.delegateToDefaultPerformer = false;

		$scope.$on('REFRESH_PROPERTIES_PANEL',
				function(event, propertiesPanel) {
					self.propertiesPanel = propertiesPanel;
					self.refresh();
				});
	}

	/**
	 * 
	 */
	ActivityResubmissionController.prototype.refresh = function() {
		this.element = this.propertiesPanel.element;
		if (!this.element) {
			return;
		}

		this.modelElement = this.element.modelElement;
		this.isInteractive = (this.modelElement.taskType === this.constants.USER_TASK_TYPE || this.modelElement.taskType === this.constants.MANUAL_TASK_TYPE);

		var reSubHandler = this.modelElement.resubmissionHandler;
		if (reSubHandler) {
			this.enableResubmission = true;
			this.delegateToDefaultPerformer = reSubHandler.defaultPerformer ? reSubHandler.defaultPerformer : false;
		} else {
			this.enableResubmission = false;
		}
	};

	/**
	 * 
	 */
	ActivityResubmissionController.prototype.updateEnableResubmissionOption = function() {
		if (this.enableResubmission) {
			this.propertiesPanel.submitEnableResubmissionCommand({
				useData : false
			});
		} else {
			this.propertiesPanel.submitDisableResubmissionCommand({});
		}
	};

	/**
	 * 
	 */
	ActivityResubmissionController.prototype.updateDelegateOption = function() {
		var change = {
			defaultPerformer: this.delegateToDefaultPerformer
		};
		this.propertiesPanel.updateResubmissionHandler(change);
	};
})();