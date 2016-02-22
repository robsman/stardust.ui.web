define(["bpm-modeler/js/m_urlUtils"],function(m_urlUtils){
	
	//constructor
	function rulesManagerService(){
		this.name = "rulesManagerService";
	};
	
	
	/**
	 * Retrieve supported artifact types from rest common
	 * @returns
	 */
	rulesManagerService.prototype.getSupportedArtifactTypes = function(){
		var url,
			deferred;
		
		deferred = jQuery.Deferred();
		
		//rest-common.../artifact types URL
		url = m_urlUtils.getContextName();
		url += "/services/rest/portal";
		url += "/artifact-types/run-time";
		
		jQuery.ajax({
			url: url,
			type: "GET",
			success : function(data){
				deferred.resolve(data);
			},
			error: function(err){
				deferred.reject(err);
			}
		});
		
		return deferred.promise();
	};
	
	return new rulesManagerService();
	
});