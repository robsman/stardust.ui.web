/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

public class StatusDTO extends AbstractDTO{
	
	@DTOAttribute(value = "state.value")
	public Integer value;
	
	public String label;
	
	public StatusDTO() {
		// TODO Auto-generated constructor stub
	}
	
	public StatusDTO(Integer value, String label) {
		this.value = value;
		this.label = label;
	}

}
