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

import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
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

   private ProcessDefinitionType process = null;

   private DataPathType validatedDataPathType = null;

   public Issue[] validate(IModelElement element) throws ValidationException
   {
      List<Issue> issues = new ArrayList<Issue>();
      validatedDataPathType = (DataPathType) element;
      process = (ProcessDefinitionType) element.eContainer();

      if (process == null)
      {
         return (Issue[]) issues.toArray(Issue.ISSUE_ARRAY);
      }

      if (validatedDataPathType.isDescriptor())
      {
         String resolvedValue = null;
         AttributeType attribute = AttributeUtil.getAttribute(validatedDataPathType,
               "type");
         if (attribute != null)
         {
            refMap = new HashMap<String, DataPathReference>();
            DataPathReference reference = new DataPathReference(validatedDataPathType,
                  new ArrayList<DataPathReference>());
            refMap.put(validatedDataPathType.getId(), reference);
            resolvedValue = resolveReferences(reference, issues);
            if (attribute.getValue() != null && attribute.getValue().equals("Link"))
            {
               if (!hasVariable(resolvedValue))
               {
                  checkLinkUrl(issues, resolvedValue);
               }
            }
         }
         attribute = AttributeUtil.getAttribute(validatedDataPathType, "text");
         if (attribute != null)
         {
            String text = attribute.getAttributeValue();
            refMap = new HashMap<String, DataPathReference>();
            DataPathReference reference = new DataPathReference(validatedDataPathType,
                  text, new ArrayList<DataPathReference>());
            resolvedValue = resolveReferences(reference, issues);
         }
         attribute = AttributeUtil.getAttribute(validatedDataPathType, "text");
      }
      return (Issue[]) issues.toArray(Issue.ISSUE_ARRAY);
   }

   private String resolveReferences(DataPathReference reference, List<Issue> issues)
   {
      DataPathType dataPathType = reference.getDataPath();
      String value = VariableContextHelper.getInstance().getContext(dataPathType)
            .replaceAllVariablesByDefaultValue(reference.getValue());
      String result = value;
      if (!this.hasVariable(value))
      {
         return value;
      }
      String id = null;
      Matcher matcher = pattern.matcher(value);
      while (matcher.find())
      {
         if ((matcher.start() == 0)
               || ((matcher.start() > 0) && (value.charAt(matcher.start() - 1) != '\\')))
         {
            String ref = value.substring(matcher.start(), matcher.end());
            ref = ref.trim();
            id = ref;
            id = id.replace("%{", "");
            id = id.replace("}", "");
            DataPathType refDataPathType = findDataPath(process, id);

            if (refDataPathType == null)
            {
               issues.add(Issue.error(validatedDataPathType,
                     MessageFormat.format(
                           Validation_Messages.ERR_REFERENCED_DESCRIPTOR_DOES_NOT_EXIST,
                           new Object[] {validatedDataPathType.getId(), ref}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
               result = ModelUtils.replaceDescriptorVariable("%{" + id + "}", result, "");
               return result;
            }
            String refAccessPath = refDataPathType.getDataPath();

            if (refAccessPath == null)
            {
               issues.add(Issue.warning(validatedDataPathType,
                     MessageFormat.format(
                           Validation_Messages.WR_REFERENCED_DESCRIPTOR_NO_DATAPATH,
                           new Object[] {ref}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
               result = ModelUtils.replaceDescriptorVariable("%{" + id + "}", result, "");
               return result;
            }

            DataPathReference refDataPathTypeReference = refMap
                  .get(refDataPathType.getId());
            if (refDataPathTypeReference == null)
            {
               refDataPathTypeReference = new DataPathReference(refDataPathType,
                     new ArrayList<DataPathReference>());
               reference.getReferences().add(refDataPathTypeReference);
               refMap.put(refDataPathType.getId(), refDataPathTypeReference);
            }
            if (hasCircularDependency(dataPathType.getId(), refDataPathTypeReference))
            {
               issues.add(Issue.error(validatedDataPathType,
                     MessageFormat.format(
                           Validation_Messages.ERR_REFERENCED_DATAPTH_IS_A_CIRCULAR_DEPENDENCY,
                           new Object[] {validatedDataPathType.getId()}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
               result = ModelUtils.replaceDescriptorVariable("%{" + id + "}", result, "");
               return result;
            }
            result = ModelUtils.replaceDescriptorVariable("%{" + id + "}", result,
                  resolveReferences(refDataPathTypeReference, issues));
         }
      }
      return result;
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
         }
         else
         {
            if (hasCircularDependency(referencingDataPathID, reference))
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

   private boolean hasVariable(String value)
   {
      if (value == null)
      {
         return false;
      }
      Matcher matcher = pattern.matcher(value);
      while (matcher.find())
      {
         if ((matcher.start() == 0)
               || ((matcher.start() > 0) && (value.charAt(matcher.start() - 1) != '\\')))
         {
            return true;
         }
      }
      return false;
   }

   public class DataPathReference
   {
      private DataPathType dataPath = null;

      private String value;

      private List<DataPathReference> references = new ArrayList<DataPathReference>();

      public DataPathType getDataPath()
      {
         return dataPath;
      }

      public String getValue()
      {
         return value;
      }

      public List<DataPathReference> getReferences()
      {
         return references;
      }

      public DataPathReference(DataPathType dataPath, List<DataPathReference> references)
      {
         super();
         this.dataPath = dataPath;
         this.references = references;
         this.value = dataPath.getDataPath();
      }

      public DataPathReference(DataPathType dataPath, String value,
            List<DataPathReference> references)
      {
         super();
         this.value = value;
         this.references = references;
         this.dataPath = dataPath;
      }

   }

   private void checkLinkUrl(List<Issue> issues, String uri)
   {
      if (!StringUtils.isEmpty(uri))
      {
         try
         {
            new URL(XmlUtils.resolveResourceUri(uri));
         }
         catch (Exception ex)
         {
            issues.add(Issue.error(validatedDataPathType,
                  MessageFormat.format(
                        "Link descriptor ''{0}'' contains invalid URL: ''{1}''",
                        new Object[] {validatedDataPathType.getId(), uri}),
                  ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
         }
      }
   }

}