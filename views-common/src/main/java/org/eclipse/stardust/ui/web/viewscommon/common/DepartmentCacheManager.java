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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;

/**
 * This class assist caching Departments to avoid engine calls for user session. If Engine
 * starts providing Department instance instead of DepartmentInfoDetails, this class may
 * be depreciated/removed
 * 
 * @author Yogesh.Manware
 * 
 */
public class DepartmentCacheManager implements Serializable
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_ID = "departmentCacheManager";
   public static final Logger trace = LogManager.getLogger(SpecialWorklistCacheManager.class);

   private Map<Long, Department> departmentCache = new HashMap<Long, Department>();

   private static DepartmentCacheManager getInstance()
   {
      return (DepartmentCacheManager) FacesUtils.getBeanFromContext(BEAN_ID);
   }

   public static void reset()
   {
      getInstance().departmentCache = new HashMap<Long, Department>();
   }

   /**
    * @param deptOId
    * @return
    */
   public static Department getDepartment(Long deptOId)
   {
      DepartmentCacheManager dcm = getInstance();
      try
      {
         if (!dcm.departmentCache.containsKey(deptOId))
         {
            dcm.departmentCache.put(deptOId, ServiceFactoryUtils.getAdministrationService().getDepartment(deptOId));
         }
      }
      catch (Exception e)
      {
         trace.warn(e.getMessage());
      }
      
      return dcm.departmentCache.get(deptOId);
   }
}
