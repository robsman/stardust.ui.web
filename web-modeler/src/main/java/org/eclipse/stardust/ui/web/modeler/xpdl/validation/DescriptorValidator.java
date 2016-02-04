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
package org.eclipse.stardust.ui.web.modeler.xpdl.validation;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContextHelper;
import org.eclipse.stardust.modeling.validation.*;

public class DescriptorValidator implements IModelElementValidator
{
   private Pattern pattern = Pattern.compile("(\\%\\{[^{}]+\\})"); //$NON-NLS-1$
   private Map<String, DataPathReference> refMap = null;
   
   public Issue[] validate(IModelElement element) throws ValidationException
   {
      List<Issue> issues = new ArrayList<Issue>();
      DataPathType dataPathType = (DataPathType) element;

      if (dataPathType.isDescriptor())
      {
         AttributeType attribute = AttributeUtil.getAttribute(dataPathType, "type");
         if (attribute != null)
         {
            refMap = new HashMap<String, DataPathReference>(); 
            DataPathReference reference = new DataPathReference(dataPathType, new ArrayList<DataPathReference>());
            refMap.put(dataPathType.getId(), reference);
            resolveReferences(reference, issues);
         }
      }
      return (Issue[]) issues.toArray(Issue.ISSUE_ARRAY);
   }
   
   private void resolveReferences(DataPathReference reference, List<Issue> issues)
   { 
      DataPathType dataPathType = reference.getDataPath();
      String value = VariableContextHelper.getInstance().getContext(dataPathType).replaceAllVariablesByDefaultValue(dataPathType.getDataPath());
      if (!this.hasVariabled(value))
      {
         return;
      }
      String id = null;
      Matcher matcher = pattern.matcher(value);
      while (matcher.find())
      {
         if ((matcher.start() == 0) || ((matcher.start() > 0)
               && (value.charAt(matcher.start() - 1) != '\\')))
         {
            String ref = value.substring(matcher.start(), matcher.end());
            ref = ref.trim();
            id = ref;
            id = id.replace("%{", "");
            id = id.replace("}", "");
            ProcessDefinitionType process = (ProcessDefinitionType) dataPathType.eContainer();
            DataPathType refDataPathType = findDataPath(process, id); 
            
            
            if (refDataPathType == null)
            {
               issues.add(Issue.error(dataPathType,
                     MessageFormat.format(
                           Validation_Messages.ERR_REFERENCED_DESCRIPTOR_DOES_NOT_EXIST,                           
                           new Object[] {dataPathType.getId(), ref}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
               return;
            }
            String refAccessPath = refDataPathType.getDataPath();
            
            if (refAccessPath == null)
            {
               issues.add(Issue.warning(dataPathType,
                     MessageFormat.format(
                           Validation_Messages.WR_REFERENCED_DESCRIPTOR_NO_DATAPATH,                           
                           new Object[] {ref}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
            }
                                                     
            DataPathReference refDataPathTypeReference = refMap.get(refDataPathType.getId());
            if (refDataPathTypeReference == null)
            {
               refDataPathTypeReference = new DataPathReference(refDataPathType,
                     new ArrayList<DataPathReference>());
               reference.getReferences().add(refDataPathTypeReference);
               refMap.put(refDataPathType.getId(), refDataPathTypeReference);
            }     
            if (this.hasCircularDependency(dataPathType.getId(), refDataPathTypeReference)) 
            {
               issues.add(Issue.error(dataPathType,
                     MessageFormat.format(
                           Validation_Messages.ERR_REFERENCED_DATAPTH_IS_A_CIRCULAR_DEPENDENCY,                           
                           new Object[] {dataPathType.getId()}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
               return;
            }
            resolveReferences(refDataPathTypeReference, issues);            
         }
      }
      return;
   }
   
   private boolean hasCircularDependency(String referencingDataPathID,
         DataPathReference referencedDataPath)
   {
      if (referencingDataPathID
            .equalsIgnoreCase(referencedDataPath.getDataPath().getId()))
      {
         return true;
      }
      List<DataPathReference> references = referencedDataPath.getReferences();
      for (Iterator<DataPathReference> i = references.iterator(); i.hasNext();)
      {
         DataPathReference reference = i.next();
         if (hasCircularDependency(referencedDataPath.getDataPath().getId(), reference))
         {
            return true;
         } else {
            if (this.hasCircularDependency(referencingDataPathID, reference)) 
            {
               return true;
            }
         }
      }
      return false;
   }
      
   private DataPathType findDataPath(ProcessDefinitionType process, String ref)
   {
      for (Iterator<DataPathType> i = process.getDataPath().iterator(); i.hasNext();)
      {
         DataPathType dataPathType = i.next();
         if (dataPathType.getId().equals(ref))
         {
            return dataPathType;
         }
      }
      return null;
   }
      
   private boolean hasVariabled(String value)
   {
      Matcher matcher = pattern.matcher(value);
      return matcher.find();
   }
   
   public class DataPathReference
   {
      private DataPathType dataPath;
      private List<DataPathReference> references = new ArrayList<DataPathReference>();
      
      public DataPathType getDataPath()
      {
         return dataPath;
      }
      public void setDataPath(DataPathType dataPath)
      {
         this.dataPath = dataPath;
      }
      public List<DataPathReference> getReferences()
      {
         return references;
      }
      public void setReferences(List<DataPathReference> references)
      {
         this.references = references;
      }
      public DataPathReference(DataPathType dataPath, List<DataPathReference> references)
      {
         super();
         this.dataPath = dataPath;
         this.references = references;
      }

   }

}