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
package org.eclipse.stardust.ui.web.benchmark.view;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.engine.core.monitoring.ActivityInstanceStateChangeMonitor;
import org.eclipse.stardust.ui.web.benchmark.portal.messages.Messages;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Aditya.Gaikwad
 * 
 */
public class BenchmarkConfigurationBean
      implements InitializingBean, ConfirmationDialogHandler, PortalConfigurationListener
{
   public static final String BEAN_NAME = "benchmarkConfigurationBean";
   private static final Boolean DEFAULT_ACTIVITY_CREATION = true;
   private static final Boolean DEFAULT_ACTIVITY_SUSPEND_AND_SAVE = true;

   private static final long serialVersionUID = 1L;
   private ConfirmationDialog benchmarkConfirmationDialog;

   private boolean activityCreation;
   private boolean activitySuspendAndSave;

   private BenchmarkConfigurationTableBean benchmarkConfigurationTableBean;

   public BenchmarkConfigurationBean()
   {}

   /**
    * @return
    */
   public static BenchmarkConfigurationBean getInstance()
   {
      return (BenchmarkConfigurationBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      initialize();
   }

   /**
    * 
    */
   public void initialize()
   {
      BenchmarkConfigurationTableBean current = BenchmarkConfigurationTableBean.getCurrent();
      Map<String, Serializable> benchmarkPreferencesMap = current.getBenchmarkPreferences().getPreferences();

      Serializable serializable = benchmarkPreferencesMap
            .get(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONCREATE);
      activityCreation = serializable != null ? Boolean.valueOf(serializable.toString()) : DEFAULT_ACTIVITY_CREATION;
      Serializable value = benchmarkPreferencesMap
            .get(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONSUSPEND);
      activitySuspendAndSave = value != null ? Boolean.valueOf(value.toString()) : DEFAULT_ACTIVITY_SUSPEND_AND_SAVE;
   }

   /**
    * 
    */
   public void save()
   {
      BenchmarkConfigurationTableBean current = BenchmarkConfigurationTableBean.getCurrent();
      Map<String, Serializable> benchmarkPreferencesMap = current.getBenchmarkPreferences().getPreferences();
      benchmarkPreferencesMap.put(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONCREATE, activityCreation);
      benchmarkPreferencesMap.put(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONSUSPEND,
            activitySuspendAndSave);

      current.saveBenchmarkPreferences();

      current.saveConfigurationValue();
   }

   /**
    *
    */
   public void reset()
   {
      BenchmarkConfigurationTableBean current = BenchmarkConfigurationTableBean.getCurrent();
      Map<String, Serializable> benchmarkPreferencesMap = current.getBenchmarkPreferences().getPreferences();
      benchmarkPreferencesMap.put(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONCREATE,
            DEFAULT_ACTIVITY_CREATION);
      benchmarkPreferencesMap.put(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONSUSPEND,
            DEFAULT_ACTIVITY_SUSPEND_AND_SAVE);

      current.saveBenchmarkPreferences();
      initialize();

      FacesUtils.clearFacesTreeValues();
      current.reset();
   }

   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      benchmarkConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      Messages propsBean = Messages.getInstance();
      benchmarkConfirmationDialog.setTitle(propsBean.getString("views.benchmarkPanelConfiguration.confirmResetTitle"));
      benchmarkConfirmationDialog.setMessage(propsBean.getParamString("views.benchmarkPanelConfiguration.confirmReset",
            propsBean.getParamString("views.benchmarkPanelConfiguration.confirmResetBenchmarkView.label")));
      benchmarkConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      reset();
      benchmarkConfirmationDialog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      benchmarkConfirmationDialog = null;
      return true;
   }

   public boolean preferencesScopeChanging(PreferenceScope pScope)
   {
      return true;
   }

   public void preferencesScopeChanged(PreferenceScope pScope)
   {
      activityCreation = false;
      activitySuspendAndSave = false;
   }

   public boolean isActivityCreation()
   {
      return activityCreation;
   }

   public void setActivityCreation(boolean activityCreation)
   {
      this.activityCreation = activityCreation;
   }

   public boolean isActivitySuspendAndSave()
   {
      return activitySuspendAndSave;
   }

   public void setActivitySuspendAndSave(boolean activitySuspendAndSave)
   {
      this.activitySuspendAndSave = activitySuspendAndSave;
   }

   public BenchmarkConfigurationTableBean getBenchmarkConfigurationTableBean()
   {
      return benchmarkConfigurationTableBean;
   }

   public void setBenchmarkConfigurationTableBean(BenchmarkConfigurationTableBean benchmarkConfigurationTableBean)
   {
      this.benchmarkConfigurationTableBean = benchmarkConfigurationTableBean;
   }

   public ConfirmationDialog getBenchmarkConfirmationDialog()
   {
      return benchmarkConfirmationDialog;
   }

   public void setBenchmarkConfirmationDialog(ConfirmationDialog benchmarkConfirmationDialog)
   {
      this.benchmarkConfirmationDialog = benchmarkConfirmationDialog;
   }

}
