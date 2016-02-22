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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;


// adds a checkbox field/flags to content of type IIdentifiableElement
public class ContentDecorator implements ICheck
{
   private final String namePrefix = "Copy Of "; //$NON-NLS-1$
   private final String idPrefix = "CopyOf"; //$NON-NLS-1$
   // also keep a copy of name and id
   private String nameCopy;
   private String idCopy;
   // is this a duplicate (name/id/both)?
   private Integer duplicate = null;
   
   private Object content;
   private boolean checked = false;
   
   public ContentDecorator(Object content)
   {
      this.content = content;
      if(this.content instanceof Map.Entry)
      {
         content = ((Map.Entry) this.content).getValue();                  
      }      
      if(content instanceof IIdentifiableElement)
      {
         nameCopy = ((IIdentifiableElement) content).getName();
         idCopy = ((IIdentifiableElement) content).getId();
      }
   }
   public boolean isChecked()
   {
      return checked;
   }
   
   public boolean isDuplicateId()
   {      
      if(duplicate != null && (duplicate.intValue() == NameIDCache.DUPLICATE_BOTH || duplicate.intValue() == NameIDCache.DUPLICATE_ID))
      {
         return true;
      }         
      return false;
   }

   public void setDuplicate(Integer duplicate)
   {
      this.duplicate = duplicate;
   }   
   
   public void setChecked(boolean checked, NameIDCache localNameIdCache)
   {
      this.checked = checked;
      
      Entry entry = null;
      EObject copy = null;      
      
      if(this.content instanceof Map.Entry)
      {
         entry = (Map.Entry) this.content;
         copy = (EObject) entry.getValue();         
      }
      if(checked)
      {
         ((IIdentifiableElement) copy).setName(nameCopy);
         ((IIdentifiableElement) copy).setId(idCopy); 
      }
      else if(!checked)
      {
         ((IIdentifiableElement) copy).setName(namePrefix + nameCopy);
         ((IIdentifiableElement) copy).setId(idPrefix + idCopy);   
      }
   }
   public Object getContent()
   {
      return content;
   }
}