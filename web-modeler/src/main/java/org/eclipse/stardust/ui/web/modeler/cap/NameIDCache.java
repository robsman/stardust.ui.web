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
package org.eclipse.stardust.ui.web.modeler.cap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.cap.MergerUtil.MergerEntry;


public class NameIDCache
{
   public static final int DUPLICATE_NAME = 1;
   public static final int DUPLICATE_ID = 2;
   public static final int DUPLICATE_BOTH = 3;

   private StoreObject storage;
   private Map nameIdCache;
   private Map changedCache;
   // Content for the viewer
   private InputContainer referenceValueInput;

   public NameIDCache(StoreObject storage, Map changedCache, Map nameIdCache, InputContainer referenceValueInput)
   {
      this.storage = storage;
      this.changedCache = changedCache;
      // new - do not change the original Cache
      this.nameIdCache = new HashMap(nameIdCache);
      this.referenceValueInput = referenceValueInput;
   }

   // called after uncheck and modify
   public Integer checkElement(Entry entry)
   {
      Integer returnValue = null;

      // key is the original (from the copied model)
      EObject raw = (EObject) entry.getKey();
      // we must compare the copy because we may have changed the values already in the other dialog
      EObject copy = (EObject) entry.getValue();
      EClass eClass = copy.eClass();

      EObject parent = raw.eContainer();
      EObject checkParent = (EObject) changedCache.get(parent);
      if(checkParent != null)
      {
         parent = checkParent;
      }

      parent = CopyPasteUtil.getSameModelElement(parent, storage.getTargetModel(), null);
      String id = null;
      String name = null;
      if (copy instanceof IIdentifiableElement)
      {
         id = ((IIdentifiableElement) copy).getId();
         name = ((IIdentifiableElement) copy).getName();
      }

      if(storage.isDiagram())
      {
         if(parent instanceof ProcessDefinitionType)
         {
            parent = storage.getTargetProcess();
         }
      }
      Map eClassNameIdCache = new HashMap();
      Object localNameIdCache = null;

      if(nameIdCache.containsKey(parent))
      {
         eClassNameIdCache = (HashMap) nameIdCache.get(parent);
         if(eClassNameIdCache.containsKey(eClass))
         {
            if (copy instanceof IIdentifiableElement)
            {
               boolean duplicateId = false;
               boolean duplicateName = false;

               localNameIdCache = (HashMap) eClassNameIdCache.get(eClass);
               Iterator it = ((HashMap) localNameIdCache).entrySet().iterator();
               while(it.hasNext())
               {
                  Map.Entry cacheEntry = (Map.Entry) it.next();
                  String cacheId = (String) cacheEntry.getKey();
                  String cacheName = (String) cacheEntry.getValue();
                  if(id.equals(cacheId))
                  {
                     duplicateId = true;
                  }
                  if(name.equals(cacheName))
                  {
                     duplicateName = true;
                  }
               }
               if(duplicateId && duplicateName)
               {
                  returnValue = new Integer(DUPLICATE_BOTH);
               }
               else if(duplicateId)
               {
                  returnValue = new Integer(DUPLICATE_ID);
               }
               else if(duplicateName)
               {
                  returnValue = new Integer(DUPLICATE_NAME);
               }
            }
         }
      }
      // check referenceValueInput (content of dialog)
      if(returnValue == null)
      {
         List contentList = (List) referenceValueInput.getContainer().getAllContent();
         for(int i = 0; i < contentList.size(); i++)
         {
            ContentDecorator containerEntry = (ContentDecorator) contentList.get(i);
            // copy by value
            if(!containerEntry.isChecked())
            {
               // value has a new Name/ID already
               MergerEntry dialogEntry = (MergerEntry) containerEntry.getContent();

               EObject containerCopy = (EObject) dialogEntry.getValue();
               EClass containereClass = containerCopy.eClass();

               if(!dialogEntry.equals(entry))
               {
                  if (containerCopy instanceof IIdentifiableElement)
                  {
                     String containerId = ((IIdentifiableElement) containerCopy).getId();
                     String containerName = ((IIdentifiableElement) containerCopy).getName();

                     if(eClass.equals(containereClass))
                     {
                        if(id.equals(containerId) && name.equals(containerName))
                        {
                           returnValue = new Integer(DUPLICATE_BOTH);
                        }
                        else if(id.equals(containerId))
                        {
                           returnValue = new Integer(DUPLICATE_ID);
                        }
                        else if(name.equals(containerName))
                        {
                           returnValue = new Integer(DUPLICATE_NAME);
                        }
                        else
                        {
                           returnValue = null;
                        }
                     }
                  }
               }
            }
         }
      }
      return returnValue;
   }
}