define(
		[ 'm_modelElementCommentsPropertiesPage' ],
		function(m_modelElementCommentsPropertiesPage) {
			return {
				propertiesPage : [
						{
							panelId : "dataView",
							pageId : "dataCommentsPropertiesPage",
							pageHtmlUrl : "modelElementCommentsPropertiesPage.html",
							provider : m_modelElementCommentsPropertiesPage,
							visibility : "always",
							pageName : "Comments",
							pageIconUrl : "../../images/icons/comments-properties-page.png"
						}, ]
			};
		});