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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.xml.type.internal.RegEx;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContextHelper;
import org.eclipse.stardust.modeling.validation.*;

public class DescriptorValidator implements IModelElementValidator
{
   private Pattern pattern = Pattern.compile("(\\%\\{[^{}]+\\})"); //$NON-NLS-1$
   
   public Issue[] validate(IModelElement element) throws ValidationException
   {
      List<Issue> issues = new ArrayList<Issue>();
      DataPathType dataPathType = (DataPathType) element;

      if (dataPathType.isDescriptor())
      {
         AttributeType attribute = AttributeUtil.getAttribute(dataPathType, "type");
         if (attribute != null)
         {
            String value = dataPathType.getDataPath();
            value = VariableContextHelper.getInstance().getContext(element)
                  .replaceAllVariablesByDefaultValue(value);
            validateDescriptor(value, dataPathType, issues);
         }
      }
      return (Issue[]) issues.toArray(Issue.ISSUE_ARRAY);
   }

   
   private String validateDescriptor(String value, DataPathType dataPathType, List<Issue> issues)
   {
      
      if (value == null)
      {         
         issues.add(Issue.warning(dataPathType,
               MessageFormat.format(      
                     Validation_Messages.WR_COMPOSITE_LINK_DESCRIPTOR_NO_DATAPATH,                     
                     new Object[] {dataPathType.getId()}),
               ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
         return null;
      }
  
      String id = null;
      String newValue = value;
      Matcher matcher = pattern.matcher(newValue);
      while (matcher.find())
      {
         if ((matcher.start() == 0) || ((matcher.start() > 0)
               && (newValue.charAt(matcher.start() - 1) != '\\')))
         {
            String ref = newValue.substring(matcher.start(), matcher.end());
            ref = ref.trim();
            id = ref;
            id = id.replace("%{", "");
            id = id.replace("}", "");
            if (id.equals(dataPathType.getId()))
            {
               issues.add(Issue.error(dataPathType,
                     MessageFormat.format(
                           Validation_Messages.ERR_REFERENCED_DATAPTH_IS_A_CIRCULAR_DEPENDENCY,                           
                           new Object[] {dataPathType.getId()}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
               return value;
            }

            ProcessDefinitionType process = (ProcessDefinitionType) dataPathType.eContainer();
            DataPathType refDataPath = findDataPath(process, id);
            if (refDataPath == null)
            {
               issues.add(Issue.error(dataPathType,
                     MessageFormat.format(
                           Validation_Messages.ERR_REFERENCED_DESCRIPTOR_DOES_NOT_EXIST,                           
                           new Object[] {dataPathType.getId(), ref}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
               return value;
            }
            String refAccessPath = refDataPath.getDataPath();
            if (refAccessPath == null)
            {
               issues.add(Issue.warning(dataPathType,
                     MessageFormat.format(
                           Validation_Messages.WR_REFERENCED_DESCRIPTOR_NO_DATAPATH,                           
                           new Object[] {ref}),
                     ValidationService.PKG_CWM.getProcessDefinitionType_DataPath()));
               refAccessPath = "";
            }
            value = this.replaceLiteral("%{" + id + "}", value, refAccessPath);
         }
         else
         {
            if (newValue.charAt(matcher.start() - 1) == '\\')
            {
               value = value.replaceFirst("\\\\\\%\\{", "*0*0*0*0*");
            }
         }
      }
      if (value.indexOf("%") > -1)
      {
         return validateDescriptor(value, dataPathType, issues);
      }      
      return value;
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
   
   private String replaceLiteral(String literal, String value, String newValue)
   {      
      String tobeReplaced = "";
      String replacement = "";
      if (!newValue.startsWith("%{")) //$NON-NLS-1$
      {
         tobeReplaced = literal.substring(2, literal.length() - 1);
         replacement = newValue;
         if (replacement.indexOf("%") > -1) //$NON-NLS-1$
         {
            replacement = replacement.replace("%", "\\%"); //$NON-NLS-1$ //$NON-NLS-2$
         }
         List<String> list1 = new ArrayList<String>();
         while (value.indexOf("%{" + tobeReplaced + "}") > -1) //$NON-NLS-1$ //$NON-NLS-2$
         {
            int idx = value.indexOf("%{" + tobeReplaced + "}"); //$NON-NLS-1$ //$NON-NLS-2$
            if (idx == 0 || (idx > 0 && value.charAt(idx - 1) != '\\'))
            {
               value = value.replaceFirst("(\\%\\{" + tobeReplaced + "\\})", replacement); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
               list1.add("\\%\\{" + tobeReplaced + "\\}"); //$NON-NLS-1$ //$NON-NLS-2$
               value = value.replaceFirst("(\\%\\{" + tobeReplaced + "\\})", "*0*0*0*0*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
         }
         for (Iterator<String> i = list1.iterator(); i.hasNext();)
         {
            String string = i.next();
            value = value.replaceFirst("(\\*0\\*0\\*0\\*0\\*)", string); //$NON-NLS-1$
         }
      }
      else
      {
         tobeReplaced = literal.substring(2, literal.length() - 1);
         replacement = newValue.substring(2, newValue.length() - 1);
         if (replacement.indexOf("%") > -1) //$NON-NLS-1$
         {
            replacement = replacement.replace("%", "\\%"); //$NON-NLS-1$ //$NON-NLS-2$
         }
         tobeReplaced = RegEx.REUtil.quoteMeta(tobeReplaced);
         value = value.replaceAll("(\\%\\{" + tobeReplaced + "\\})", "\\%\\{" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
               + replacement + "\\}"); //$NON-NLS-1$
      }
      return value;
   }

}