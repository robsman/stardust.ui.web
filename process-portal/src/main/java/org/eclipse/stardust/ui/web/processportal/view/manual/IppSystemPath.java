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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.common.introspection.Path;
import org.eclipse.stardust.ui.web.processportal.view.manual.ModelUtils.SystemDefinedDataType;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;


/**
 * @author Subodh.Godbole
 *
 */
public class IppSystemPath extends Path
{
   private SystemDefinedDataType dataType;

   /**
    * @param parentPath
    * @param id
    * @param readonly
    */
   public IppSystemPath(Path parentPath, String id, boolean readonly)
   {
      super(parentPath, id, readonly);
      dataType = ModelUtils.getSystemDefinedDataType(id);
   }

   @Override
   public boolean isPrimitive()
   {
      switch (dataType)
      {
      case PROCESS_ID:
      case ROOT_PROCESS_ID:
      case CURRENT_LOCALE:
      case CURRENT_DATE:
      case CURRENT_MODEL:
      case CURRENT_USER:
      case STARTING_USER:
      case LAST_ACTIVITY_PERFORMER:
      case PROCESS_PRIORITY:
         return true;

      case PROCESS_ATTACHMENTS:
         return false;
      }

      return false;
   }

   @Override
   public boolean isEnumeration()
   {
      switch (dataType)
      {
      case PROCESS_ID:
      case ROOT_PROCESS_ID:
      case CURRENT_LOCALE:
      case CURRENT_DATE:
      case CURRENT_MODEL:
      case CURRENT_USER:
      case STARTING_USER:
      case LAST_ACTIVITY_PERFORMER:
      case PROCESS_ATTACHMENTS:
         return false;

      case PROCESS_PRIORITY:
         return true;
      }

      return false;
   }

   @Override
   public boolean isList()
   {
      switch (dataType)
      {
      case PROCESS_ID:
      case ROOT_PROCESS_ID:
      case CURRENT_LOCALE:
      case CURRENT_DATE:
      case CURRENT_MODEL:
      case CURRENT_USER:
      case STARTING_USER:
      case LAST_ACTIVITY_PERFORMER:
      case PROCESS_PRIORITY:
         return false;
      case PROCESS_ATTACHMENTS:
         return true;
      }

      return false;
   }

   @Override
   public boolean isNumber()
   {
      switch (dataType)
      {
      case PROCESS_ID:
      case ROOT_PROCESS_ID:
         return true;
      case CURRENT_LOCALE:
      case CURRENT_DATE:
      case CURRENT_MODEL:
      case CURRENT_USER:
      case STARTING_USER:
      case LAST_ACTIVITY_PERFORMER:
      case PROCESS_ATTACHMENTS:
      case PROCESS_PRIORITY:
         return false;
      }

      return false;
   }

   @Override
   public Class< ? > getJavaClass()
   {
      return Object.class;
   }

   @Override
   public String getTypeName()
   {
      return getId();
   }

   @Override
   public List<String> getEnumerationValues()
   {
      switch (dataType)
      {
      case PROCESS_ID:
      case ROOT_PROCESS_ID:
      case CURRENT_LOCALE:
      case CURRENT_DATE:
      case CURRENT_MODEL:
      case CURRENT_USER:
      case STARTING_USER:
      case LAST_ACTIVITY_PERFORMER:
      case PROCESS_ATTACHMENTS:
         throw new RuntimeException("Enumeration not supported for " + getId());
      case PROCESS_PRIORITY:
         return new ArrayList<String>(PriorityConverter.getPossibleValues().keySet());
      }

      return null;
   }

   @Override
   public Object mapToObject(Map<String, Object> object)
   {
      return null;
   }

   @Override
   public Map<String, Object> objectToMap(Object object)
   {
      return null;
   }
}