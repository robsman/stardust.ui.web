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

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
public class BenchmarkConfiguration
{
   private String modelId;
   private String processId;
   private String name;
   private String defaultBenchmarkId;
   
   /**
    * @param modelId
    * @param processId
    * @param name
    * @param defaultBenchmark
    */
   public BenchmarkConfiguration(String modelId, String processId, String name, String defaultBenchmarkId)
   {
      super();
      this.modelId = modelId;
      this.processId = processId;
      this.name = name;
      this.defaultBenchmarkId = defaultBenchmarkId;
   }

   public String getModelId()
   {
      return modelId;
   }

   public void setModelId(String modelId)
   {
      this.modelId = modelId;
   }

   public String getProcessId()
   {
      return processId;
   }

   public void setProcessId(String processId)
   {
      this.processId = processId;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDefaultBenchmarkId()
   {
      return defaultBenchmarkId;
   }

   public void setDefaultBenchmarkId(String defaultBenchmarkId)
   {
      this.defaultBenchmarkId = defaultBenchmarkId;
   }

}
