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

package org.eclipse.stardust.ui.web.modeler.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.iterators.EntrySetMapIterator;
import org.eclipse.emf.ecore.EObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shrikant.Gangal
 * 
 */
@Component
@Scope("session")
public class EObjectUUIDMapper
{
   /**
    * 
    */
   private Map<UUID, EObject> uuidEObjectMap = new HashMap<UUID, EObject>();

   /**
    * @param obj
    */
   public void map(EObject obj)
   {
      uuidEObjectMap.put(UUID.randomUUID(), obj);
   }

   /**
    * @param uuid
    * @return
    */
   public EObject getEObject(UUID uuid)
   {

      return uuidEObjectMap.get(uuid);
   }

   /**
    * @param uuid
    * @return
    */
   public EObject getEObject(String uuid)
   {

      return uuidEObjectMap.get(UUID.fromString(uuid));
   }
   
   /**
    * @param obj
    * @return
    */
   public UUID getUUID(EObject obj)
   {
      if (null != obj)
      {
         Set<Map.Entry<UUID, EObject>> entrySet = uuidEObjectMap.entrySet();
         for (Map.Entry<UUID, EObject> e : entrySet)
         {
            if (obj.equals(e.getValue()))
            {
               return e.getKey();
            }
         }
      }

      return null;
   }
}
