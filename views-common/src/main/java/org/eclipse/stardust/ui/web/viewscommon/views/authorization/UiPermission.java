/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

/**
 * @author Yogesh.Manware 
 * TODO: parentPermission and iconPath are currently not used.
 */
public class UiPermission
{
   private String permissionId;
   private String label;
   private UiPermission parent;
   private String iconPath;

   /**
    * @param permissionId
    * @param label
    * @param iconPath
    * @param parent
    */
   public UiPermission(String permissionId, String label, String iconPath, UiPermission parent)
   {
      super();
      this.permissionId = UiPermissionUtils.getPermissionIdAllow(permissionId);
      this.label = label;
      this.parent = parent;
      this.iconPath = iconPath;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((permissionId == null) ? 0 : permissionId.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      UiPermission other = (UiPermission) obj;
      if (permissionId == null)
      {
         if (other.permissionId != null)
            return false;
      }
      else if (!permissionId.equals(other.permissionId))
         return false;
      return true;
   }

   public String getPermissionId()
   {
      return permissionId;
   }

   public String getLabel()
   {
      return label;
   }

   public UiPermission getParent()
   {
      return parent;
   }

   public String getIconPath()
   {
      return iconPath;
   }
}
