/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_communicationController" ],
		function(m_utils, m_constants, m_urlUtils, m_communicationController) {
			window.top.currentRole = m_constants.BUSINESS_ANALYST_ROLE;
			//window.top.currentRole = m_constants.INTEGRATOR_ROLE
			
			return {
				initializeCurrentUser : initializeCurrentUser,
				createUser : function(account, firstName, lastName, email, imageUrl, color) {
					var user = new User();
					
					user.account = account;
					user.firstName = firstName;
					user.lastName = lastName;
					user.imageUrl = imageUrl;
					user.email = email;
					user.color = color;
					user.isInvited = false;
					
					return user;
				},
				getCurrentUser : getCurrentUser,
				getCurrentRole : getCurrentRole,
				setCurrentRole : setCurrentRole,
				currentUserHasRole : function(role) {
					return getCurrentUser().hasRole(role);
				},
				currentUserHasProfileRole : function(role) {
					return getCurrentUser().hasProfileRole(role);
				}
			};

			/**
			 * 
			 */
			function User() {
				this.firstName = null;
				this.lastName = null;
				this.account = null;
				this.roles = {};
				this.profileRoles = {};

				/**
				 * 
				 */
				User.prototype.toString = function() {
					return "Lightdust.User";
				};

				/**
				 * 
				 */
				User.prototype.hasRole = function(role) {
					return this.roles[role] != null;
				};

				/**
				 * 
				 */
				User.prototype.hasProfileRole = function(role) {
					return this.profileRoles[role] != null;
				};
			}

			/**
			 * 
			 */
			function initializeCurrentUser() {
				var user = new User();
				m_communicationController.syncGetData({
					url : (m_communicationController.getEndpointUrl()+"/whoAmI") }, new function() {
					return {
						success : function(json) {
							user.firstName = json.firstName;
							user.lastName = json.lastName;
							user.account = json.account;
							user.isInvited = false;
							window.top.currentUser = user;
						},
						failure : function() {
							alert('Hey');
						}
					};
				});
				
				
			}

			/**
			 * 
			 */
			function getCurrentUser() {
				if (window.top.currentUser == null) {
					this.initializeCurrentUser();
				}
			
				m_utils.debug("Current User: ");
				m_utils.debug(window.top.currentUser);
				
				return window.top.currentUser;
			}

			/**
			 * 
			 */
			function getCurrentRole() {
				return window.top.currentRole;
			}

			/**
			 * 
			 */
			function setCurrentRole(role) {
				return window.top.currentRole = role;
			}
		});