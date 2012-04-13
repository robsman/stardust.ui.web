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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.processportal.common.DefaultAssemblyLineActivityProvider;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.provider.IAssemblyLineActivityProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.SpiConstants;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.springframework.beans.factory.InitializingBean;

import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.SlideDown;

/**
 * @author roland.stamm
 * 
 */
public class WorklistsBean extends AbstractLaunchPanel implements InitializingBean
{
   private static final long serialVersionUID = 1L;

   public static final String BEAN_NAME = "worklistsBean";

   public static Logger trace = LogManager.getLogger(WorklistsBean.class);

   private WorklistsTreeModel treeModel;

   private Effect effect = new SlideDown();

   private boolean showEmptyWorklist = false;

   private Set<String> assemblyLineParticipants;
   private IAssemblyLineActivityProvider assemblyLineActivityProvider;
   private boolean assemblyLineMode;

   /**
    * 
    */
   public WorklistsBean()
   {
      super("worklists");
      buildAssemblyLineParticipants();
   }

   /**
    * @return
    */
   public static WorklistsBean getInstance()
   {
      return (WorklistsBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      init();
   }

   /**
    * 
    */
   public void init()
   {
      getEffect().setFired(false);
      setExpanded(true);

      treeModel = new WorklistsTreeModel(new WorklistsTreeRoot(), false, "showhideEmptyWorklist", this);

      update(false);
   }

   /**
    * Updates tree as per boolean value
    * 
    * @param event
    */
   public void showEmptyWorklist(ValueChangeEvent event)
   {
      showEmptyWorklist = ((Boolean) event.getNewValue()).booleanValue();
      treeModel.setShowEmptyWorklist(showEmptyWorklist);
      treeModel.update(false);
   }

   /**
    * 
    */
   public void clear()
   {
      setExpanded(false);
      getTreeModel().clear();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel#update()
    */
   public void update()
   {
      update(true);
   }

   /**
    * @param reload
    */
   public void update(boolean reload)
   {
      getTreeModel().update(reload);
   }

   /**
    * @param params
    * @return
    */
   public ActivityInstance openNextAssemblyLineActivity(Map<String, Object> params)
   {
      if (null != getAssemblyLineActivityProvider())
      {
         try
         {
            ActivityInstance ai = getAssemblyLineActivityProvider().getNextAssemblyLineActivity(
                  ServiceFactoryUtils.getProcessExecutionPortal(), getAssemblyLineParticipants());

            if (null == params)
            {
               params = CollectionUtils.newTreeMap();
            }

            params.put("assemblyLineActivity", true);
            params.put("worklistsBean", this);

            ActivityInstanceUtils.openActivity(ai, params);

            return ai;
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }

      return null;
   }

   /**
    * @return
    */
   public ActivityInstance openNextAssemblyLineActivity()
   {
      return openNextAssemblyLineActivity(null);
   }

   /**
    * @param activity
    * @return
    */
   public boolean isAssemblyLineActivity(Activity activity)
   {
      if (null != activity)
      {
         return getAssemblyLineParticipants().contains(activity.getDefaultPerformer().getId());
      }

      return false;
   }

   /**
    * @return
    */
   private void buildAssemblyLineParticipants()
   {
      assemblyLineParticipants = ParticipantUtils.categorizeParticipants(SessionContext.findSessionContext().getUser())
            .getAssemblyLineParticipants();

      assemblyLineMode = CollectionUtils.isNotEmpty(assemblyLineParticipants) ? true : false;

      if (assemblyLineMode)
      {
         try
         {
            String assemblyLineProvider = (String) Parameters.instance().get(
                  SpiConstants.ASSEMBLY_LINE_ACTIVITY_PROVIDER);
            if (StringUtils.isNotEmpty(assemblyLineProvider))
            {
               Object instance = ReflectionUtils.createInstance(assemblyLineProvider);
               if (instance instanceof IAssemblyLineActivityProvider)
               {
                  assemblyLineActivityProvider = ((IAssemblyLineActivityProvider) instance);
               }
               else
               {
                  MessageDialog
                        .addErrorMessage("Assembly Line Provider is not an instance of org.eclipse.stardust.ui.web.processportal.spi.IAssemblyLineActivityProvider");
                  FacesUtils.refreshPage(); // Sometimes this is required otherwise dialog
                                            // appears as blank
               }
            }
            else
            {
               assemblyLineActivityProvider = new DefaultAssemblyLineActivityProvider();
               trace.info("Using DefaultAssemblyLineActivityProvider...");
            }
         }
         catch (Exception e)
         {
            // This may not be internationalized. Since this is 'dev only' scenario
            MessageDialog.addErrorMessage("Cannot instantiate Assembly Line Provider", e);
            FacesUtils.refreshPage(); // Sometimes this is required otherwise dialog
                                      // appears as blank
         }
      }
   }

   public WorklistsTreeModel getTreeModel()
   {
      return treeModel;
   }

   public void setTreeModel(WorklistsTreeModel treeModel)
   {
      this.treeModel = treeModel;
   }

   public Effect getEffect()
   {
      return effect;
   }

   public boolean isShowEmptyWorklist()
   {
      return showEmptyWorklist;
   }

   public void setShowEmptyWorklist(boolean showEmptyWorklist)
   {
      this.showEmptyWorklist = showEmptyWorklist;
   }

   public IAssemblyLineActivityProvider getAssemblyLineActivityProvider()
   {
      return assemblyLineActivityProvider;
   }

   public Set<String> getAssemblyLineParticipants()
   {
      return assemblyLineParticipants;
   }

   public boolean isAssemblyLineMode()
   {
      return assemblyLineMode;
   }
}
