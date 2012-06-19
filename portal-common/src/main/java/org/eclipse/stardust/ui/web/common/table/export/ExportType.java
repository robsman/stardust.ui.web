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
package org.eclipse.stardust.ui.web.common.table.export;

/**
 * @author Subodh.Godbole
 *
 */
public enum ExportType
{
   EXCEL ("EXCEL", ".xls"),
   CSV ("CSV", ".csv");
   
   private final String type;
   private final String extension;

   /**
    * @param type
    * @param extension
    */
   private ExportType(String type, String extension)
   {
      this.type = type;
      this.extension = extension;
   }

   public String getType()
   {
      return type;
   }

   public String getExtension()
   {
      return extension;
   }
}
