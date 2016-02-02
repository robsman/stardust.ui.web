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

import java.util.Map;
import java.util.Set;

/**
 * @author Abhay.Thappan
 * @version $Revision: $
 */
public class GenericQueryResultDTO extends QueryResultDTO
{
   public Set<String> columns;
   public Map<String, ?> columnsDefinition;
}
