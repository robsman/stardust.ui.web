/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_urlUtils", "m_communicationController" ],
		function(m_utils, m_constants, m_urlUtils, m_communicationController) {

			// For testing

			var BUSINESS_ANALYST = "BusinessAnalyst";
			var INTEGRATOR = "Integrator";

			return {
				initializeCurrentUser : initializeCurrentUser,
				createUser : function(account, firstName, lastName, email, imageUrl){
					var user = new User();
					
					user.account = account;
					user.firstName = firstName;
					user.lastName = lastName;
					user.imageUrl = imageUrl;
					user.email = email;
					
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
				},
				BUSINESS_ANALYST : BUSINESS_ANALYST,
				INTEGRATOR : INTEGRATOR
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
				// TODO Call ModelerResource
				// window.top.currentUser =;
			}

			/**
			 * 
			 */
			function getCurrentUser() {
				if (window.top.currentUser == null) {
					window.top.currentUser = new User();
					window.top.currentUser.firstName = "Sheldon";
					window.top.currentUser.lastName = "Cooper";
					window.top.currentUser.account = "sheldor";
					window.top.currentUser.imageUrl = "../images/test-image.jpg";
					window.top.currentUser.roles[BUSINESS_ANALYST] = {};
					window.top.currentUser.roles[INTEGRATOR] = {};
					window.top.currentUser.profileRoles[BUSINESS_ANALYST] = {};
					window.top.currentRole = "BusinessAnalyst";
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