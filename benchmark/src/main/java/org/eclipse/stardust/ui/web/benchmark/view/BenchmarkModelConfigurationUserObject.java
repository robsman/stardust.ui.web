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

import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.util.StringUtils;

/**
 * @author Aditya.Gaikwad NodeUserObject for Benchmark Configuration table
 */
public class BenchmarkModelConfigurationUserObject extends NodeUserObject
{
   private static final String MODEL_IMAGE_PATH = "/plugins/views-common/images/icons/model.gif";
   private static final String PROCESS_IMAGE_PATH = "/plugins/views-common/images/icons/process.png";
   private static final long serialVersionUID = 1L;
   private BenchmarkModelConfigurationTreeItem treeItem = null;

   public BenchmarkModelConfigurationUserObject(TreeTable treeTable, TreeTableNode node, TreeTableBean treeBeanPointer,
         BenchmarkModelConfigurationTreeItem treeItem, int componenttype)
   {
      super(treeTable, node, treeBeanPointer, componenttype);
      this.treeItem = treeItem;

      String imagePath = null;
      this.treeItem = treeItem;

      if (StringUtils.isNotEmpty(treeItem.getModelName()))
      {
         if (treeItem.isModelArtifact())
         {
            setLeaf(false);
            imagePath = MODEL_IMAGE_PATH;
         }
         else
         {
            setLeaf(true);
            imagePath = PROCESS_IMAGE_PATH;
         }
      }

      setExpanded(false);
      setBranchContractedIcon(imagePath);
      setBranchExpandedIcon(imagePath);
      setLeafIcon(imagePath);
   }

   @Override
   public String getLine1Text()
   {
      return treeItem.getModelName();
   }

   @Override
   public String getLine2Text()
   {
      return null;
   }

   public String getModelName()
   {
      return treeItem.getModelName();
   }

   public String getName()
   {
      return treeItem.getName();
   }

   public Object getParent()
   {
      return treeItem.getParent();
   }

   public Object getSource()
   {
      return treeItem.getSource();
   }

   public String getDefaultBenchmarkId()
   {
      return treeItem.getDefaultBenchmarkId();
   }

   public void setDefaultBenchmarkId(String defaultBenchmarkId)
   {
      treeItem.setDefaultBenchmarkId(defaultBenchmarkId);
      if (getSource() instanceof BenchmarkConfiguration)
      {
         BenchmarkConfiguration var = (BenchmarkConfiguration) getSource();
         var.setDefaultBenchmarkId(defaultBenchmarkId);
      }
      else if (getSource() instanceof BenchmarkConfigurations)
      {
         BenchmarkConfigurations var = (BenchmarkConfigurations) getSource();
         var.setDefaultBenchmarkId(defaultBenchmarkId);
      }
   }

   public boolean isModelArtifact()
   {
      return treeItem.isModelArtifact();
   }

   public void setModelArtifact(boolean modelArtifact)
   {
      treeItem.setModelArtifact(modelArtifact);
   }

   @Override
   public boolean isFilterOut(TableDataFilters dataFilters)
   {
      return false;
   }
}
