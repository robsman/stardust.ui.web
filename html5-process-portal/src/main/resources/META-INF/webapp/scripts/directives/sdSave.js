/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Johnson.Quadras
 */

(function() {
    'use strict';

    angular.module('bpm-common').directive('sdSave', [ SaveDirective ]);

    /*
     * 
     */
    function SaveDirective() {
	return {
	    restrict : 'A',
	    scope : {
		save : "&sdaOnSave"
	    },
	    template : 	'<button '+
		    		' title="{{saveController.i18n(\'admin-portal-messages.views-participantManagement-configuration-saveButton\')}}"'+
	    			' ng-click="save();" class="button-link" aid="Save">'+
	    			'<i class="pi pi-save icon-lg"> </i>' + 
	    		      '</button>',
	    controller : [ '$scope', SaveController ]
	};
    }

    /**
     * 
     */
    function SaveController($scope) {
	// Make sure i18n is available in the current scope
	if (!angular.isDefined($scope.i18n)) {
	    this.i18n = $scope.$parent.i18n;
	}
	$scope.saveController = this;
    }

})();