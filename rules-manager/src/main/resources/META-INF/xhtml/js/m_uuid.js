/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
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