/**
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", ], function(m_utils) {
	var lastUuid = null;
	return {
		generate : function() {
			if (!lastUuid) {
				lastUuid = new Date().getMilliseconds();
			}

			lastUuid += lastUuid;
			
			m_utils.debug("Uuid = " + lastUuid);

			return lastUuid;
		}
	};
});