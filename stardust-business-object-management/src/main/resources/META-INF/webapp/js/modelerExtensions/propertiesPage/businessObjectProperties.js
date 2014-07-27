define(
		[
				'business-object-management/js/BusinessObjectManagementDataPropertiesPage',
				'bpm-modeler/js/m_urlUtils' ],
		function(BusinessObjectManagementDataPropertiesPage, m_urlUtils) {
			return {
				propertiesPage : [

				{
					panelId : "dataView",
					id : "businessObjectManagementDataPropertiesPage",
					provider : BusinessObjectManagementDataPropertiesPage,
					pageHtmlUrl : m_urlUtils.getContextName()
							+ "/plugins/business-object-management/properties-pages/businessObjectManagementDataPropertiesPage.html",
					pageName : "BusinessObject Management",
					pageIconUrl : "plugins/business-object-management/css/images/business-object.png",
					visibility : "always"
				} ]
			};
		});