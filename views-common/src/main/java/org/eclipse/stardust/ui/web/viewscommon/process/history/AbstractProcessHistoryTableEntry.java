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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.util.List;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.runtime.RuntimeObject;


/**
 * @author Yogesh.Manware
 * 
 */
public abstract class AbstractProcessHistoryTableEntry implements IProcessHistoryTableEntry
{
   private List<IProcessHistoryTableEntry> children;
   private boolean isNodePathToActivityInstance;
   private Long oid;
   private RuntimeObject runtimeObject;
   private String styleClass;

   /**
    * @param runtimeObject
    */
   public AbstractProcessHistoryTableEntry(RuntimeObject runtimeObject)
   {
      this(runtimeObject, null);
   }

   /**
    * @param runtimeObject
    * @param children
    */
   public AbstractProcessHistoryTableEntry(RuntimeObject runtimeObject, List<IProcessHistoryTableEntry> children)
   {
      this.runtimeObject = runtimeObject;
      this.children = children;
      oid = runtimeObject != null ? Long.valueOf(runtimeObject.getOID()) : null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.process.history.IProcessHistoryTableEntry#setRuntimeObject(org.eclipse.stardust.engine.api.runtime.RuntimeObject)
    */
   public void setRuntimeObject(RuntimeObject runtimeObject)
   {
      if (this.runtimeObject == null
            || (runtimeObject != null && runtimeObject.getOID() == this.runtimeObject.getOID()))
      {
         this.runtimeObject = runtimeObject;
         runtimeObjectChanged();
      }
   }

   abstract protected void runtimeObjectChanged();

   public boolean isNodePathToActivityInstance()
   {
      return isNodePathToActivityInstance;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.process.history.IProcessHistoryTableEntry#setNodePathToActivityInstance(boolean)
    */
   public void setNodePathToActivityInstance(boolean isNodePathToActivityInstance)
   {
      this.isNodePathToActivityInstance = isNodePathToActivityInstance;
      this.styleClass = isNodePathToActivityInstance ? "pathToActivityInstance" : "noPathToActivityInstance";
   }

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
      if (obj instanceof AbstractProcessHistoryTableEntry)
      {
         AbstractProcessHistoryTableEntry o = (AbstractProcessHistoryTableEntry) obj;
         return CompareHelper.areEqual(this.runtimeObject, o.runtimeObject)
               && CompareHelper.areEqual(this.styleClass, o.styleClass)
               && CompareHelper.areEqual(this.getRuntimeObjectType(), o.getRuntimeObjectType());
      }
      return false;
   }

   public int hashCode()
   {
      int hashCode = 0;
      hashCode |= runtimeObject != null ? runtimeObject.hashCode() : 0;
      hashCode |= styleClass != null ? styleClass.hashCode() : 0;
      hashCode |= getRuntimeObjectType() != null ? getRuntimeObjectType().hashCode() : 0;
      return hashCode;
   }

   public String getStyleClass()
   {
      return styleClass;
   }

   public boolean isActivityAbortable()
   {
      return false;
   }

   public boolean isMoreDetailsAvailable()
   {
      return false;
   }

   public Long getOID()
   {
      return oid;
   }

   public String getDetails()
   {
      return null;
   }

   public List<IProcessHistoryTableEntry> getChildren()
   {
      return children;
   }

   public void setChildren(List<IProcessHistoryTableEntry> children)
   {
      this.children = children;
   }

   public RuntimeObject getRuntimeObject()
   {
      return runtimeObject;
   }
}