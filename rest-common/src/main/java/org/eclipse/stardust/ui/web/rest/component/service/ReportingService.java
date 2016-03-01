/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
public interface ReportingService
{
   /**
    * @return
    */
   Map<String, List<FolderDTO>> getPersonalReports();
}