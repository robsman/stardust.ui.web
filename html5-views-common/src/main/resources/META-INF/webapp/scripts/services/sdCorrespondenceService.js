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
define([],function(){
	
	/*
	 * 
	 */
	function CorrespondenceService($resource, $q, sdUtilService) {
		
		this.getAddressBook = function (aiOid){
			var url = sdUtilService.getBaseUrl() + "services/rest/portal/activity-instances/"+aiOid+"/address-book";
			return $resource(url).query().$promise;
		}
		
	}
	
	//Dependency injection array for our controller.
	CorrespondenceService.$inject = ['$resource','$q','sdUtilService'];
	
	//Require capable return object to allow our angular code to be initialized
	//from a require-js injection system.
	return {
		init: function(angular,appName){
			angular.module(appName)
			.service("sdCorrespondenceService", CorrespondenceService);
		}
	};
});