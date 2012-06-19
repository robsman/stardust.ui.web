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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.io.Serializable;

import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public final class MIMEType implements Serializable
{
   private static final long serialVersionUID = -6143018679151671459L;
   private String fileExtension[];
   private String iconPath;
   private String type;
   private String userFriendlyName;

   public MIMEType(String type, String[] fileExtension, String iconPath, String userFriendlyName)
   {
      super();
      this.fileExtension = fileExtension;
      this.type = type;
      this.iconPath = iconPath;
      this.userFriendlyName = userFriendlyName;
   }

   public MIMEType(String type, String fileExtension, String iconPath, String userFriendlyName)
   {
      this(type, new String[] {fileExtension}, iconPath, userFriendlyName);
   }
   
   public boolean containsExtension(String ext)
   {
      if (null != fileExtension)
      {
         for (int i = 0; i < fileExtension.length; i++)
         {
            if (fileExtension[i].equalsIgnoreCase(ext))
            {
               return true;
            }
         }
      }
      return false;
   }
   
   public String getFileExtension()
   {
      if (null != fileExtension && fileExtension.length > 0)
      {
         return fileExtension[0];
      }
      return "";
   }

   public String getType()
   {
      return type;
   }

   public String getIconPath()
   {
      return iconPath;
   }

   /**
    * Use this method only for IPP specific mime types
    * 
    * @return
    */
   public String getCompleteIconPath()
   {
      return ResourcePaths.I_DOCUMENT_PATH + iconPath;
   }

   public String getUserFriendlyName()
   {
      return userFriendlyName;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      MIMEType other = (MIMEType) obj;
      if (type == null)
      {
         if (other.type != null)
         {
            return false;
         }
      }
      else if (!type.equals(other.type))
      {
         return false;
      }
      else
      {
         return true;
      }
      return false;
   }
}