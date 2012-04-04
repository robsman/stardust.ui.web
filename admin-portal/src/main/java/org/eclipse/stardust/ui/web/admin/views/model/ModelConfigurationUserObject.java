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
package org.eclipse.stardust.ui.web.admin.views.model;

import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



/**
 * @author Vikas.Mishra
 *  NodeUserObject for  ModelConfiguration table
 */
public class ModelConfigurationUserObject extends NodeUserObject
{
    private static final String MODEL_IMAGE_PATH = "/plugins/views-common/images/icons/model.gif";
    private static final String BLANK_IMAGE_PATH = "/plugins/views-common/images/spacer.png";
    private static final long serialVersionUID = 1L;
    private ModelConfigurationTreeItem treeItem = null;
    private boolean activeModelVersion;

    // private String value;
    private boolean edited = false;

    public ModelConfigurationUserObject(TreeTable treeTable, TreeTableNode node, TreeTableBean treeBeanPointer,
        ModelConfigurationTreeItem treeItem, int componenttype)
    {
        super(treeTable, node, treeBeanPointer, componenttype);
        this.treeItem = treeItem;

        String imagePath = null;
        this.treeItem = treeItem;

        if (StringUtils.isNotEmpty(treeItem.getModelName()))
        {
            setLeaf(false);
            imagePath = MODEL_IMAGE_PATH;
        }
        else
        {
            setLeaf(true);
            imagePath = BLANK_IMAGE_PATH;
        }

        setExpanded(false);
        setBranchContractedIcon(imagePath);
        setBranchExpandedIcon(imagePath);
        setLeafIcon(imagePath);
        setTooltip(treeItem.getDescription());
    }

    public String getDefaultValue()
    {
        return treeItem.getDefaultValue();
    }

    public String getDescription()
    {
        return treeItem.getDescription();
    }

    public String getFilterType()
    {
        return "";
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

    public String getValue()
    {
        return treeItem.getValue();
    }

    public boolean isEdited()
    {
        return edited;
    }

    @Override
    public boolean isFilterOut(TableDataFilters dataFilters)
    {
       for (ITableDataFilter tableDataFilters : dataFilters.getList())
       {
          ITableDataFilterOnOff onOffFilter = ((ITableDataFilterOnOff) tableDataFilters);

          if (onOffFilter.isOn())
          {
             if (getModelName()==null && isActiveModelVersion())
             {
                return true;
             }
          }
       }

       return false;
    }

    public void setEdited(boolean edited)
    {
        this.edited = edited;
    }

    public void setValue(String value)
    {
        treeItem.setValue(value);

        if (getSource() instanceof ConfigurationVariable)
        {
            ConfigurationVariable var = (ConfigurationVariable) getSource();
            var.setValue(value);
        }
    }

   public boolean isActiveModelVersion()
   {
      return activeModelVersion;
   }

   public void setActiveModelVersion(boolean activeModelVersion)
   {
      this.activeModelVersion = activeModelVersion;
   }
    
}
