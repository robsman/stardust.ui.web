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

/**
 * @author Johnson.Quadras
 * @version $Revision: $
 */
public class PreferenceDTO extends AbstractDTO
{

   public String scope;

   public String moduleId;

   public String preferenceId;

   public String preferenceName;

   public String preferenceValue;
   
   public boolean isPasswordType;
   
   public String partitionId;

   public PreferenceDTO(String scope, String moduleId, String preferenceId, String preferenceName,
         String preferenceValue, boolean isPasswordType, String partitionId)
   {
      super();
      this.scope = scope;
      this.moduleId = moduleId;
      this.preferenceId = preferenceId;
      this.preferenceName = preferenceName;
      this.preferenceValue = preferenceValue;
      this.isPasswordType = isPasswordType;
      this.partitionId = partitionId;
   }

   public PreferenceDTO()
   {}

}
