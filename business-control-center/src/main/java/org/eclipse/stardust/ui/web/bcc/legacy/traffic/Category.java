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
package org.eclipse.stardust.ui.web.bcc.legacy.traffic;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;


/**
 * Represents a category item in Traffic Light View. Wraps a descriptor to get its name
 * considering internationalization.
 * 
 * @author fuhrmann
 * @version $Revision$
 */
public class Category implements Serializable
{
   private final static long serialVersionUID = 1l;

   private final DataPath dataPath;

   public Category(DataPath dataPath)
   {
      this.dataPath = dataPath;
   }

   public String getId()
   {
      return dataPath.getId();
   }

   public String getQualifiedId()
   {
      return dataPath.getQualifiedId();
   }

   public String getName()
   {
      return I18nUtils.getDataPathName(dataPath);
   }

   public DataPath getDataPath()
   {
      return dataPath;
   }
}
