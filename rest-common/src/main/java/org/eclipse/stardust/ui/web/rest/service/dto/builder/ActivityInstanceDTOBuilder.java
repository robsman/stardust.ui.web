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
package org.eclipse.stardust.ui.web.rest.service.dto.builder;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class ActivityInstanceDTOBuilder
{
   public static ActivityInstanceDTO build(ActivityInstance ai)
   {
      ActivityInstanceDTO aiDTO = new ActivityInstanceDTO();

      if (ai != null)
      {

      }

      return aiDTO;
   }

   /**
    * Prevent instantiation
    */
   private ActivityInstanceDTOBuilder()
   {

   }

}
