(function(){

	sdGenericRepositoryViewCtrl.$inject = ["sdI18nService"];

	function sdGenericRepositoryViewCtrl(sdI18nService){
		this.i18n = sdI18nService.getInstance("views-common-messages");
		this.documentsHeader = this.i18n.translate("views.genericRepositoryView.header");
	};

	//inject controller into viewscommon-ui
	angular.module("viewscommon-ui")
	.controller("sdGenericRepositoryViewCtrl",sdGenericRepositoryViewCtrl);

})();