/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "business-object-management/js/Utils" ],
		function(Utils) {
			return {
				instance : function() {

					if (!document.BusinessObjectManagementService) {
						document.BusinessObjectManagementService = new BusinessObjectManagementService();
					}

					return document.BusinessObjectManagementService;
				}
			};

			/**
			 * 
			 */
			function BusinessObjectManagementService() {

				/**
				 * 
				 */
				BusinessObjectManagementService.prototype.initialize = function() {
				};

				/**
				 * 
				 */
				BusinessObjectManagementService.prototype.getBusinessObjects = function() {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/business-object-management/businessObject.json",
										type : "GET",
										contentType : "application/json"
									}).done(function(result) {
								console.log("=======> Models");
								console.log(result.models);

								deferred.resolve(result.models);
							}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BusinessObjectManagementService.prototype.getBusinessObjectInstances = function(
						modelOid, businessObjectId, primaryKeyField, keyFields) {
					var queryString = "?";

					if (primaryKeyField && primaryKeyField.filterValue) {
						queryString += primaryKeyField.id;
						queryString += "=";
						queryString += primaryKeyField.filterValue;
						queryString += "&";
					}

					if (keyFields) {
						for (var n = 0; n < keyFields.length; ++n) {
							if (keyFields[n].filterValue) {
								queryString += keyFields[n].id;
								queryString += "=";
								queryString += keyFields[n].filterValue;
								queryString += "&";

							}
						}
					}

					console.log("Filter String");
					console.log(queryString);

					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/business-object-management/businessObject/"
												+ modelOid + "/"
												+ businessObjectId
												+ "/instance.json",
										type : "GET",
										contentType : "application/json"
									})
							.done(
									function(result) {
										deferred
												.resolve(result.businessObjectInstances);
									}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BusinessObjectManagementService.prototype.createBusinessObjectInstance = function(
						modelOid, businessObjectId, primaryKey,
						businessObjectInstance) {
					console.log("Model OID: " + modelOid);
					console.log("Business Object ID: " + businessObjectId);
					console.log("Primary Key: " + primaryKey);
					console.log("Business Object Instance:");
					console.log(businessObjectInstance);

					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/business-object-management/businessObject/"
												+ modelOid + "/"
												+ businessObjectId
												+ "/instance/" + primaryKey
												+ ".json",
										type : "PUT",
										contentType : "application/json",
										data : JSON
												.stringify(businessObjectInstance)
									})
							.done(
									function(result) {
										deferred
												.resolve(result.businessObjectInstances);
									}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BusinessObjectManagementService.prototype.updateBusinessObjectInstance = function(
						modelOid, businessObjectId, primaryKey,
						businessObjectInstance) {
					console.log("Model OID: " + modelOid);
					console.log("Business Object ID: " + businessObjectId);
					console.log("Primary Key: " + primaryKey);
					console.log("Business Object Instance:");
					console.log(businessObjectInstance);

					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/business-object-management/businessObject/"
												+ modelOid + "/"
												+ businessObjectId
												+ "/instance/" + primaryKey
												+ ".json",
										type : "POST",
										contentType : "application/json",
										data : JSON
												.stringify(businessObjectInstance)
									})
							.done(
									function(result) {
										deferred
												.resolve(result.businessObjectInstances);
									}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};
			}
		});
