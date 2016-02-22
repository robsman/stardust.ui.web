/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.benchmark.view;

import java.util.List;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
public class BenchmarkConfigurations
{
   private String modelId;
   private String defaultBenchmarkId;
   private List<BenchmarkConfiguration> benchmarkConfiguraions;
   
   public String getModelId()
   {
      return modelId;
   }
   public void setModelId(String modelId)
   {
      this.modelId = modelId;
   }
   public String getDefaultBenchmarkId()
   {
      return defaultBenchmarkId;
   }
   public void setDefaultBenchmarkId(String defaultBenchmarkId)
   {
      this.defaultBenchmarkId = defaultBenchmarkId;
   }
   public List<BenchmarkConfiguration> getBenchmarkConfiguraions()
   {
      return benchmarkConfiguraions;
   }
   public void setBenchmarkConfiguraions(List<BenchmarkConfiguration> benchmarkConfiguraions)
   {
      this.benchmarkConfiguraions = benchmarkConfiguraions;
   }
   /**
    * @param modelId
    * @param defaultBenchmarkId
    * @param benchmarkConfiguraions
    */
   public BenchmarkConfigurations(String modelId, String defaultBenchmarkId,
         List<BenchmarkConfiguration> benchmarkConfiguraions)
   {
      super();
      this.modelId = modelId;
      this.defaultBenchmarkId = defaultBenchmarkId;
      this.benchmarkConfiguraions = benchmarkConfiguraions;
   }
}
