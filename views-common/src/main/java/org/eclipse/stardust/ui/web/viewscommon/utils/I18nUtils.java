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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelElement;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;



/**
 * @author rsauer
 * @author Subodh.Godbole
 * @version $Revision$
 */
public class I18nUtils
{
   public static final String USER_NAME_DISPLAY_FORMAT_PREF_ID = "ipp-admin-portal.userNameDisplayFormat.prefs.displayFormat";  
   
   protected final static Logger trace = LogManager.getLogger(I18nUtils.class);   
   
   public static String getModelName(DeployedModelDescription model)
   {
      return getLabel(model, model.getName(), ModelElementLocalizerKey.KEY_NAME);
   }
   
   public static String getParticipantName(Participant participant)
   {
      return getLabel(participant, "<Unknown Participant>", ModelElementLocalizerKey.KEY_NAME);
   }

   public static String getParticipantDescription(ModelParticipant participant)
   {
      return getParticipantLabel(participant, "<Unknown Participant Description>", ModelElementLocalizerKey.KEY_DESC);
   }
   
   private static String getParticipantLabel(Participant participant, String defaultLabel, int mode)
   {
      return getLabel(participant, defaultLabel, mode);
   }

   public static String getUserGroupLabel(UserGroupInfo userGroup)
   {
      String label = "";
      if(userGroup != null)
      {
         label = userGroup.getName();
      }
      return label;
   }   

   /**
    * @param user
    * @return
    */
   public static String getUserLabelDefault(User user)
   {
      String label = "";
      
      if (null != user)
      {
         label = UserUtils.formatUserName(user, (String) user.getProperty(USER_NAME_DISPLAY_FORMAT_PREF_ID));
      }

      if (StringUtils.isEmpty(label))
      {
         label = "<Unknown User>";
      }

      return label;
   }
   
   public static String getUserLabel(User user)
   {
      String label = "";
      
      if (null != user)
      {
         label = UserUtils.formatUserName(user, (String) user.getProperty(USER_NAME_DISPLAY_FORMAT_PREF_ID));
      }

      if (StringUtils.isEmpty(label))
      {
         label = getUserLabelDefault(user);
      }

      return label;
   }
   
   public static String getProcessName(ProcessDefinition process)
   {
      return getProcessLabel(process, "<Unknown Process>", ModelElementLocalizerKey.KEY_NAME);
   }
   
   public static String getProcessDescription(ProcessDefinition process)
   {
      return getLabel(process, "<Unknown Process Description>", ModelElementLocalizerKey.KEY_DESC);
   }
   
   public static String getProcessDescription(ProcessDefinition process,
         String defaultDesc)
   {
      return getDescription(process, defaultDesc);
   }

   public static String getParticipantDescription(Participant participant,
         String defaultDesc)
   {
      return getDescription(participant, defaultDesc);
   }
   
   /**
    * 
    * @param qualityAC
    * @param modelOID
    * @return
    */
   public static String getQualityAssuranceDesc(QualityAssuranceCode qaCode, long modelOID)
   {
      String label = null;
      LocalizerKey key = new LocalizerKey(ModelElementUtils.getBundleName(modelOID), "QualityAssuranceCode." + qaCode.getCode()
            + ".Description");
      if (trace.isDebugEnabled())
      {
         trace.debug("use '" + key.getBundleName() + "' for label receivement");
      }
      try
      {
         label = Localizer.getString(key);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      if (StringUtils.isEmpty(label))
      {
         label = qaCode.getDescription();
      }
      return label;
   }
   
   private static String getProcessLabel(ProcessDefinition process, String defaultLabel, int mode)
   {
      return getLabel(process, defaultLabel, mode);
   }

   public static String getActivityName(Activity activity)
   {
      return getActivityLabel(activity, "<Unknown Activity>", ModelElementLocalizerKey.KEY_NAME);
   }
   
   public static String getActivityDescription(Activity activity)
   {
      return getActivityLabel(activity, "<Unknown Activity Description>", ModelElementLocalizerKey.KEY_DESC);
   }
   
   public static String getActivityDescription(Activity activity,
         String defaultDesc)
   {
      return getDescription(activity, defaultDesc);
   }
   
   private static String getActivityLabel(Activity activity, String defaultLabel, int mode)
   {
      return getLabel(activity, defaultLabel, mode);
   }
   
   public static String getDataPathName(DataPath dataPath)
   {
      return getDataPathLabel(dataPath, ModelElementLocalizerKey.KEY_NAME);
   }
   
   public static String getDataPathDescription(DataPath dataPath)
   {
      return getDataPathLabel(dataPath, ModelElementLocalizerKey.KEY_DESC);
   }
   
   private static String getDataPathLabel(DataPath dataPath, int mode)
   {
      return getLabel(dataPath, "<Unknown DataPath>", ModelElementLocalizerKey.KEY_NAME);
   }
   
   public static String getDataName(Data data)
   {
      return getLabel(data, "<Unknown Data>", ModelElementLocalizerKey.KEY_NAME);
   }
   
   public static String getLabel(ModelElement element, String defaultLabel, int mode)
   {
      String label = null;

      if (null != element)
      {
         if(element instanceof User)
         {
            label = getUserLabel((User)element);
         }
         else if(element instanceof UserGroup)
         {
            label = getUserGroupLabel((UserGroup)element);
         }
         else
         {
            label = getLabel(element, mode);            
         }
         
         if ( StringUtils.isEmpty(label) && !StringUtils.isEmpty(element.getName()))
         {
            label = element.getName();
         }
         else if ( StringUtils.isEmpty(label) && !StringUtils.isEmpty(element.getId()))
         {
            label = element.getId();
         }
      }

      return StringUtils.isEmpty(label) ? defaultLabel : label;
   }

   /**
    * 
    * @param element
    * @param defaultLabel
    * @return
    */
   public static String getLabel(ModelElement element, String defaultLabel)
   {
      return getLabel(element, defaultLabel, ModelElementLocalizerKey.KEY_NAME);
   }

   /**
    * 
    * @param element
    * @param defaultDesc
    * @return
    */
   public static String getDescription(ModelElement element, String defaultDesc)
   {
      String desc = null;

      if (null != element)
      {
         desc = getLabel(element, ModelElementLocalizerKey.KEY_DESC);
      }
      return StringUtils.isEmpty(desc) ? defaultDesc : desc;
   }
   
   /**
    * 
    * @param element
    * @param defaultDesc
    * @return
    */
   public static String getDescriptionAsHtml(ModelElement element, String defaultDesc)
   {
      String desc = getDescription(element, defaultDesc);

      return StringUtils.replace(desc, "\n", "<br/>");
   }
   
   private static String getLabel(ModelElement element, int mode)
   {
      String label = null;
      LocalizerKey key = new ModelElementLocalizerKey(element, mode);
      if (trace.isDebugEnabled())
      {
         trace.debug("use '" + key.getBundleName() + "' for label receivment");
      }
      try
      {
         label = Localizer.getString(key);
         if (null == label && isComputedCaseDataPath(element))
         {
            label = findMatchingCaseDataPathLabel((DataPath) element, mode);
         }
      }
      catch (Exception e)
      {
         // TODO when should logged the error?
      }
      return label;
   }

   /**
    * @param typedXPath
    * @param model
    * @param defaultLabel
    * @param mode
    * @return
    */
   public static String getLabel(TypedXPath typedXPath, Model model, String defaultLabel, int mode)
   {
      String label = null;

      LocalizerKey key = new StructuredTypeLocalizerKey(typedXPath, model, mode);
      if (trace.isDebugEnabled())
      {
         trace.debug("use '" + key.getBundleName() + "' for label receivment");
      }

      try
      {
         label = Localizer.getString(key);
      }
      catch (Exception e)
      {
         // TODO when should logged the error?
      }

      return StringUtils.isEmpty(label) ? defaultLabel : label;
   }

   /**
    * @param typedXPath
    * @param model
    * @param defaultLabel
    * @return
    */
   public static String getLabel(TypedXPath typedXPath, Model model, String defaultLabel)
   {
      return getLabel(typedXPath, model, defaultLabel, StructuredTypeLocalizerKey.KEY_NAME);
   }

   /**
    * @param typedXPath
    * @param model
    * @param defaultLabel
    * @return
    */
   public static String getDescription(TypedXPath typedXPath, Model model, String defaultLabel)
   {
      return getLabel(typedXPath, model, defaultLabel, StructuredTypeLocalizerKey.KEY_DESC);
   }

   /**
    * 
    * @param caseDataPath
    * @param mode
    * @return
    */
   private static String findMatchingCaseDataPathLabel(DataPath caseDataPath, int mode)
   {     
      String label = null;

      // check is key is already present in cache
      if (LocalizerCache.contains(caseDataPath.getId()))
      {
         DataPath cachedDataPath = LocalizerCache.get(caseDataPath.getId());
         if (null != cachedDataPath)
         {
            label = getString(cachedDataPath, mode);
            if (null == label)
            {
               label = cachedDataPath.getName();
            }
         }
         return label;
      }
     
      // if label is still null then search matching DataPath to get label
      List<DataPath> datas = getAllDataPath();
      DataPath matchingDataPath = null;
      
      for (DataPath path : datas)
      {
         if (caseDataPath.getId().equals(path.getId()) && caseDataPath.getMappedType().equals(path.getMappedType())
               && path.isKeyDescriptor())
         {
            label = getString(path, mode);
            if (null != label)
            {
               LocalizerCache.put(caseDataPath.getId(), path);
               break;
            }
            if (null == matchingDataPath)
            {
               matchingDataPath = path;
            }
         }
      }
      //if nothing is find then show name of first matching datapath 
      if (StringUtils.isEmpty(label) && null != matchingDataPath && !StringUtils.isEmpty(matchingDataPath.getName()))
      {
         label = matchingDataPath.getName();
         LocalizerCache.put(caseDataPath.getId(), matchingDataPath);        
      }
      
      if (null == label)
      {
         LocalizerCache.put(caseDataPath.getId(), null);
      }
      return label;
   }

   /**
    * 
    * @return List<DataPath> from all model except PredefinedModel
    */
   private static List<DataPath> getAllDataPath()
   {
      List<DataPath> datas = CollectionUtils.newArrayList();
      Collection<DeployedModel> models = ModelCache.findModelCache().getActiveModels();
      for (DeployedModel model : models)
      {
         if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
         {
            List<ProcessDefinition> pds = model.getAllProcessDefinitions();

            for (ProcessDefinition pd : pds)
            {
               datas.addAll(pd.getAllDataPaths());
            }
         }
      }
      return datas;
   }

   /**
    * 
    * @param path
    * @param mode
    * @return get value from resource bundle
    */
   private static String getString(DataPath path, int mode)
   {
      LocalizerKey key = new ModelElementLocalizerKey(path, mode);
      return Localizer.getString(key);
   }

   /**
    * 
    * @param element
    * @return if DataPath is type DataPath and belongs to Case Process Instance
    */
   private static boolean isComputedCaseDataPath(ModelElement element)
   {
      if (element instanceof DataPath)
      {
         DataPath dataPath = (DataPath) element;
         if (PredefinedConstants.CASE_DATA_ID.equals(dataPath.getData())
               && !PredefinedConstants.CASE_NAME_ELEMENT.equals(dataPath.getId())
               && !PredefinedConstants.CASE_DESCRIPTION_ELEMENT.equals(dataPath.getId()))
         {
            return true;
         }
      }
      return false;
   }
   
   
}
