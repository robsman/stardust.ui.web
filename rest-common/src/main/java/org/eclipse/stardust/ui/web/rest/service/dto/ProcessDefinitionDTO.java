/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

/**
 * @author Anoop.Nair
 * @author Johnson.Quadras
 * @version $Revision: $
 */
public class ProcessDefinitionDTO extends AbstractDTO
 {

	@DTOAttribute("qualifiedId")
	public String id;

	@DTOAttribute("name")
	public String name;

	@DTOAttribute("description")
	public String description;

	@DTOAttribute("modelOID")
	public long modelOid;

	public String modelName;

	public boolean auxillary;

	@DTOAttribute("allActivities")
	public List<ActivityDTO> activities;
	
	public List<DataPathDTO> dataPaths;
	
 }