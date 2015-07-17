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
		this.useDataOrConstant = this.constants.RESUBMISSION_TIMER_DATA;
		this.selectedData = null;
		this.dataItems = [];
		this.dataPath = null;
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

		this.populateAvailableDataList();

		var reSubHandler = this.modelElement.resubmissionHandler;
		if (reSubHandler) {
			this.enableResubmission = true;
			if (reSubHandler.useData) {
				this.useDataOrConstant = this.constants.RESUBMISSION_TIMER_DATA;
				this.selectedData = reSubHandler.dataFullId;
				this.dataPath = reSubHandler.dataPath;
				this.delayValue = null;
				this.delayUnit = null;
			} else {
				this.useDataOrConstant = this.constants.RESUBMISSION_TIMER_CONSTANT;
				this.delayValue = (reSubHandler.delayValue ? parseInt(reSubHandler.delayValue)
						: 0);
				this.delayUnit = reSubHandler.delayUnit;
				this.selectedData = null;
				this.dataPath = null;
			}
			this.delegateToDefaultPerformer = reSubHandler.defaultPerformer ? reSubHandler.defaultPerformer : false;
		} else {
			this.enableResubmission = false;
		}
	};

	/**
	 * 
	 */
	ActivityResubmissionController.prototype.populateAvailableDataList = function() {
		this.dataItems.length = 0;
		var self = this;
		var dataItems = this.propertiesPanel.propertiesPage.getModel().dataItems;
		jQuery.each(dataItems, function(_, data) {
			self.dataItems.push({
				id : data.id,
				fullId : data.getFullId(),
				name : data.name,
				uuid : data.uuid,
				oid : data.oid
			});
		});
	};

	/**
	 * 
	 */
	ActivityResubmissionController.prototype.updateUseDataOrConstantOption = function() {
		var change = {
			useData : (this.useDataOrConstant === this.constants.RESUBMISSION_TIMER_DATA)
		};
		this.propertiesPanel.updateResubmissionHandler(change);
	};

	/**
	 * 
	 */
	ActivityResubmissionController.prototype.updateData = function() {
		var change = {
			dataFullId : this.selectedData,
			dataPath : this.dataPath ? this.dataPath : null
		};
		this.propertiesPanel.updateResubmissionHandler(change);
	};

	/**
	 * 
	 */
	ActivityResubmissionController.prototype.updateConstant = function() {
		var change = {
			delayValue : this.delayValue,
			delayUnit : this.delayUnit
		};
		this.propertiesPanel.updateResubmissionHandler(change);
	};

	/**
	 * 
	 */
	ActivityResubmissionController.prototype.updateEnableResubmissionOption = function() {
		if (this.enableResubmission) {
			this.propertiesPanel.submitEnableResubmissionCommand({
				useData : true
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