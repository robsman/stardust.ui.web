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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationContextTypeType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.util.ActivityUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.modeling.validation.IModelElementValidator;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.stardust.modeling.validation.ValidationException;
import org.eclipse.stardust.modeling.validation.ValidationService;
import org.eclipse.stardust.modeling.validation.Validation_Messages;

public class DataMappingValidator implements IModelElementValidator
{
   public Issue[] validate(IModelElement element) throws ValidationException
   {
      //rp: This is a stripped down version of the DataMappingValidator which is used within
      //eclipse modeler. It only checks on validity of access points (CRNT-31290)
      List<Issue> result = new ArrayList<Issue>();
      DataMappingType dataMapping = (DataMappingType) element;
      ApplicationContextTypeType ctxType = checkContext(result, dataMapping);

      if ( !StringUtils.isEmpty(dataMapping.getApplicationAccessPoint()))
      {
         checkAccessPoint(result, dataMapping, ctxType);
      }

      return (Issue[]) result.toArray(Issue.ISSUE_ARRAY);
   }

   private void checkAccessPoint(List<Issue> result, DataMappingType dataMapping,
         ApplicationContextTypeType ctxType)
   {
      if (ctxType != null && dataMapping.getData() != null)
      {
         AccessPointType point = getActivityAccessPoint(dataMapping);
         if (point == null)
         {
            result.add(Issue.warning(dataMapping, MessageFormat.format(
                  Validation_Messages.MSG_DATAMAPPING_ApplicationAccessPointWarning,
                  new Object[] {dataMapping.getId()}),
                  ValidationService.PKG_CWM.getDataMappingType_ApplicationAccessPoint()));
         }

      }
   }

   private ApplicationContextTypeType checkContext(List<Issue> result,
         DataMappingType dataMapping)
   {
      ActivityType activity = (ActivityType) dataMapping.eContainer();
      ApplicationContextTypeType ctxType = null;
      if (StringUtils.isEmpty(dataMapping.getContext()))
      {
         result.add(Issue.warning(dataMapping,
               Validation_Messages.MSG_DATAMAPPING_NoContextSet,
               ValidationService.PKG_CWM.getDataMappingType_Context()));
      }
      else
      {
         ctxType = (ApplicationContextTypeType) ModelUtils.findIdentifiableElement(
               ActivityUtil.getContextTypes(activity, dataMapping.getDirection()),
               dataMapping.getContext());
         if (null == ctxType)
         {
            // every data mapping must have a valid context
            result.add(Issue.warning(dataMapping, MessageFormat.format(
                  Validation_Messages.MSG_DATAMAPPING_ContextInvalid,
                  new Object[] {dataMapping.getContext()}),
                  ValidationService.PKG_CWM.getDataMappingType_Context()));
         }
      }
      return ctxType;
   }

   private AccessPointType getActivityAccessPoint(DataMappingType dataMapping)
   {
      ActivityType activity = (ActivityType) dataMapping.eContainer();
      Collection<AccessPointType> accessPoints = ActivityUtil.getAccessPoints(activity,
            DirectionType.IN_LITERAL.equals(dataMapping.getDirection()),
            dataMapping.getContext());
      for (Iterator<AccessPointType> iter = accessPoints.iterator(); iter.hasNext();)
      {
         AccessPointType ap = (AccessPointType) iter.next();
         if (ap.getId().equals(dataMapping.getApplicationAccessPoint()))
         {
            return ap;
         }
      }
      return null;
   }

}