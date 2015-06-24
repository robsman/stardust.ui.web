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
 * @author Johnson.Quadras
 */
(function() {
	'use strict';

	/**
	 * 
	 */
	angular.module('workflow-ui.services').provider( 'sdStatusService', function() {
		this.$get = [ '$q', 'sgI18nService', function ( $q, sgI18nService ) {
			var service = new StatusService( $q, sgI18nService );
			return service;
		}];
		
	});
	
	var ACTIVITY_STATUSES = null;
	var PROCESS_STATUSES = null;
	
	/**
	 * 
	 */
	function StatusService( $q, sgI18nService ) {
		
		if(!ACTIVITY_STATUSES) {
			ACTIVITY_STATUSES = [
		             			{
		             				value : 6,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-activityTable-statusFilter-aborted')
		             			},
		             			{
		             				value : 8,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-activityTable-statusFilter-aborting')
		             			},
		             			{
		             				value : 1,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-activityTable-statusFilter-application')
		             			},
		             			{
		             				value : 2,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-activityTable-statusFilter-completed')
		             			},
		             			{
		             				value : 0,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-activityTable-statusFilter-created')
		             			},
		             			{
		             				value : 7,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-activityTable-statusFilter-hibernated')
		             			},
		             			{
		             				value : 4,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-activityTable-statusFilter-interrupted')
		             			},
		             			{
		             				value : 5,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-activityTable-statusFilter-suspended')
		             			}
		             	];
		}
		
		if(!PROCESS_STATUSES) {
			PROCESS_STATUSES = [
		             			{
		             				value : -1,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-processTable-statusFilter-created')
		             			},
		             			{
		             				value : 0,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-processTable-statusFilter-active')
		             			},
		             			{
		             				value : 1,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-processTable-statusFilter-aborted')
		             			},
		             			{
		             				value : 2,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-processTable-statusFilter-completed')
		             			},
		             			{
		             				value : 3,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-processTable-statusFilter-interrupted')
		             			},
		             			{
		             				value : 4,
		             				label : sgI18nService
		             						.translate('views-common-messages.views-processTable-statusFilter-aborting')
		             			},
		             			
		             	];
		}
		
		/**
		 * 
		 */
		StatusService.prototype.getAllActivityStates = function() {
			var deferred = $q.defer();
			deferred.resolve(ACTIVITY_STATUSES);
			return deferred.promise;
		};
		/**
		 * 
		 */
		StatusService.prototype.getAllProcessStates = function() {
			var deferred = $q.defer();
			deferred.resolve(PROCESS_STATUSES);
			return deferred.promise;
		};
	};
})();

