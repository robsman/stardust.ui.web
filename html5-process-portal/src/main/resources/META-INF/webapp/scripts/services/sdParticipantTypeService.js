/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Johnson.Quadras
 */

(function() {
    'use strict';

    angular.module('workflow-ui.services').provider('sdParticipantTypeService', function() {
	this.$get = [ function($q, $http) {
	    var service = new PaticipantTypeService();
	    return service;
	} ];
    });

    // TODO Replace icons when available for SCOPED_ORGANIZATION SCOPED_ROLE
    // ROLE
    var iconsForPaticipantType = {
	'ORGANIZATION' : 'sc sc-organization',
	'SCOPED_ORGANIZATION' : 'sc sc-organization',
	'ROLE' : 'sc sc-user',
	'SCOPED_ROLE' : 'sc sc-user',
	'USER' : 'sc sc-user',
	'USERGROUP' : 'sc sc-users',
    };
    /**
     * 
     */
    function PaticipantTypeService() {
	/**
	 * 
	 */
	PaticipantTypeService.prototype.getIcon = function(participantType) {
	    var icon = iconsForPaticipantType[participantType];
	    if (angular.isUndefined(icon)) {
		// Default Icon
		icon = "sc  sc-user";
	    }
	    return icon;
	};
    }
    ;
})();
