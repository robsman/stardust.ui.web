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
package org.eclipse.stardust.ui.web.rest.util;

import java.util.List;

import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public interface IDataPathValueFilter
{
   /**
    * return true if it needs to be considered
    * 
    * @param dataPath
    * @param dataValue
    * @return
    */
   List<? extends AbstractDTO> filter(DataPath dataPath, Object dataValue);
}
