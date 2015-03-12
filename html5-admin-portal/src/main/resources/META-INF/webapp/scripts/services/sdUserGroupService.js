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
 * @author Aditya.Gaikwad
 */

(function() {
	'use strict';

	angular.module('admin-ui.services').provider('sdUserGroupService',
			function() {
				this.$get = [ '$resource', function($resource) {
					var service = new UserGroupService($resource);
					return service;
				} ];
			});

	/*
	 * 
	 */
	function UserGroupService($resource) {
		var BASE_URL = 'services/rest/portal/user-group';

		UserGroupService.prototype.fetchUserGroups = function(query) {
			var restUrl = BASE_URL + '/all';

			var options = '';
			if (query.options.skip != undefined) {
				options += '&skip=' + query.options.skip;
			}
			if (query.options.pageSize != undefined) {
				options += '&pageSize=' + query.options.pageSize;
			}
			if (query.options.order != undefined) {
				// Supports only single column sort
				var index = query.options.order.length - 1;
				options += '&orderBy=' + query.options.order[index].name;
				options += '&orderByDir=' + query.options.order[index].dir;
			}

			if (options.length > 0) {
				restUrl = restUrl + '?' + options.substr(1);
			}

			return $resource(restUrl).get().$promise;
		}

		/*
		 * 
		 */
		UserGroupService.prototype.createUserGroup = function(newUserGroup) {
			var restUrl = BASE_URL + '/create';

			newUserGroup.description = (newUserGroup.description) ? newUserGroup.description
					: '';

			var postData = {
				id : newUserGroup.id,
				name : newUserGroup.name,
				validFrom : newUserGroup.validFrom,
				validTo : newUserGroup.validTo,
				description : newUserGroup.description
			};

			var userGroup = $resource(restUrl, {}, {
				create : {
					method : 'POST'
				}
			});
			var self = this;
			return userGroup.create({}, postData).$promise;
		}

		/*
		 * 
		 */
		UserGroupService.prototype.modifyUserGroup = function(newUserGroup) {
			var restUrl = BASE_URL + '/modify';

			newUserGroup.description = (newUserGroup.description) ? newUserGroup.description
					: '';

			var postData = {
				id : newUserGroup.id,
				name : newUserGroup.name,
				validFrom : newUserGroup.validFrom,
				validTo : newUserGroup.validTo,
				description : newUserGroup.description
			};

			var userGroup = $resource(restUrl, {}, {
				update : {
					method : 'PUT'
				}
			});
			return userGroup.update({}, postData).$promise;
		}

		/*
		 * 
		 */
		UserGroupService.prototype.invalidateUserGroup = function(userGroupId) {
			var restUrl = BASE_URL + '/delete/:userGroupId';

			var userGroup = $resource(restUrl, {
				userGroupId : '@userGroupId'
			}, {
				invalidate : {
					method : 'DELETE'
				}
			});
			var urlTemplateParams = {};
			urlTemplateParams.userGroupId = userGroupId;
			return userGroup.invalidate(urlTemplateParams).$promise;
		}
	}

})();
