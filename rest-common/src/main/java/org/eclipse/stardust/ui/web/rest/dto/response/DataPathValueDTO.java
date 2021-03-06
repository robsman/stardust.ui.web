/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto.response;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataPathDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DataPathValueDTO extends AbstractDTO
{
   public DataPathDTO dataPath;
   public List<DocumentDTO> documents;
   public String value;
}
