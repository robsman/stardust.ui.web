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
package org.eclipse.stardust.ui.web.rest.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
public class TrivialActivityInstanceDTO extends ActivityInstanceDTO
{
   public Boolean trivial;
   
   public List<PathDTO> dataMappings;
   
   public Map<String, Serializable> inOutData;
}
