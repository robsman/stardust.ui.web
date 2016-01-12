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

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
public class BenchmarkModelConfigurationTreeItem
{
   private final Object parent;
   private final Object source;
   private String modelName;
   private String name;
   private String defaultBenchmarkId;
   private boolean modelArtifact;

   public BenchmarkModelConfigurationTreeItem(Object source, Object parent)
   {
      this.source = source;
      this.parent = parent;

      if ((source != null) && source instanceof BenchmarkConfigurations)
      {
         BenchmarkConfigurations benchmarkConfigurations = (BenchmarkConfigurations) source;
         this.modelName = getModelName(benchmarkConfigurations.getModelId());
         this.modelArtifact = true;
         this.name = "";
         this.defaultBenchmarkId = benchmarkConfigurations.getDefaultBenchmarkId();
      }
      else if ((source != null) && source instanceof BenchmarkConfiguration)
      {
         BenchmarkConfiguration benchmarkConfiguraion = (BenchmarkConfiguration) source;
         this.modelName = benchmarkConfiguraion.getName();
         this.modelArtifact = false;
         this.name = benchmarkConfiguraion.getName();
         this.defaultBenchmarkId = benchmarkConfiguraion.getDefaultBenchmarkId();
      }
   }

   /**
    * @param variables
    * @return
    */
   private String getModelName(String modelId)
   {
      String modelName = (modelId != null) ? modelId : "";
      Model model = null;

      if (modelId != null)
      {
         model = ModelUtils.getModel(modelId);
         if (model != null)
         {
            modelName = I18nUtils.getLabel(model, model.getName());
         }
      }

      return modelName;
   }

   public String getModelName()
   {
      return modelName;
   }

   public String getName()
   {
      return name;
   }

   public Object getParent()
   {
      return parent;
   }

   public Object getSource()
   {
      return source;
   }

   public void setModelName(String modelName)
   {
      this.modelName = modelName;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDefaultBenchmarkId()
   {
      return defaultBenchmarkId;
   }

   public void setDefaultBenchmarkId(String defaultBenchmark)
   {
      this.defaultBenchmarkId = defaultBenchmark;
   }

   public boolean isModelArtifact()
   {
      return modelArtifact;
   }

   public void setModelArtifact(boolean modelArtifact)
   {
      this.modelArtifact = modelArtifact;
   }

}
