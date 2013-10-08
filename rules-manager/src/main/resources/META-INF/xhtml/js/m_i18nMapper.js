define(["rules-manager/js/m_i18nUtils"],function(m_i18nUtils){
	return {
		map: function(options,uiElements,loggingOn){
			var tempProp,
				i18nMap,
			    logging = false || loggingOn;
			
			for(var key in options.i18nMaps){
				if(uiElements[key] && uiElements.hasOwnProperty(key)){
					    i18nMap=options.i18nMaps[key];
						if(i18nMap){
							tempProp=m_i18nUtils.getProperty(i18nMap.path,i18nMap.defaultText);
							uiElements[key].attr(i18nMap.attr,tempProp);
							if(logging){
								console.log("Mapping: " + key + " / " + tempProp);
							}
						}
						else{
							if(logging){
								console.log("Map not present for: " + key + " / " + tempProp);
							}
						}
				}
				else{
					if(logging){
						console.log("ERROR (m_i18nMapper)-> Map Key hs no corresponding UIElement.\nKey Value = " + key);
					}
				}
			}
		}
	};
});