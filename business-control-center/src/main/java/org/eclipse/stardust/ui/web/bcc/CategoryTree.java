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
package org.eclipse.stardust.ui.web.bcc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategory;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTree;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject;
import org.eclipse.stardust.ui.web.common.categorytree.GenericItem;
import org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.UIViewComponentBean;





/**
 */
public class CategoryTree extends UIViewComponentBean
      implements ResourcePaths, ViewEventHandler, IGenericCategoryTreeUserObjectCallback
{
   private static final long serialVersionUID = 1L;
   private static final String PRE_NODE = "views.categoryTree.tree.";
   private static final String PRE_LEAF = "views.";
   private static final String POST_LEAF = ".label";
   public static final String IMAGE_BASE_PATH = "/plugins/views-common/images/icons/";
   private static final Map<String, String> iconMap;
   
   private GenericCategory userArchivedReportsCategory;
   private GenericCategory userReportDesignsCategory;
   private GenericCategoryTree tree;
   private boolean canUploadReport;

   static
   {
      iconMap = new LinkedHashMap<String, String>();
      iconMap.put("processOverviewView", "process_manager.png");
      iconMap.put("processSearchView", "cog_search.png");
      iconMap.put("trafficLightView", "traffic_light.png");
      iconMap.put("activityCriticalityManagerView", "criticality_manager.png");
      iconMap.put("pendingActivities", "pending_activities.png");
      iconMap.put("completedActivities", "/plugins/views-common/images/icons/process-history/activity_completed.png");
      iconMap.put("postponedActivities", "activity_postponed.png");
      iconMap.put("strandedActivities", "activity_stranded.png");
      iconMap.put("resourceAvailabilityView", "group.png");
      iconMap.put("roleAssignmentView", "role.png");
      iconMap.put("resourceLoginView", "group_key.png");
      iconMap.put("resourcePerformance", "chart-up.png");
      iconMap.put("performanceTeamleader", "chart-up-color.png");
      iconMap.put("costs", "money-coin.png");
      iconMap.put("myReportsView", "chart-pie.png");
     
   }
   
   public CategoryTree()
   {
      super("categoryTree");
   }

   public void collapsed(GenericCategoryTreeUserObject treeUserobject)
   {}

   public void expanded(GenericCategoryTreeUserObject treeUserobject)
   {}

   public GenericCategoryTree getTree()
   {
      return tree;
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         canUploadReport = Parameters.instance().getBoolean(
               IPreferenceStorageManager.PRP_USE_DOCUMENT_REPOSITORY, false);

         MessagesBCCBean messageBean = MessagesBCCBean.getInstance();

         tree = new GenericCategoryTree("categoriesRoot", "", this);

         // ***** Process Node *****
         GenericCategory processNode = tree.getRootCategory().addSubCategory("processes",
               messageBean.getString(PRE_NODE + "processes"), "portalConfig");

         processNode.addItem("processOverviewView",
               messageBean.getString("views." + "processOverviewView" + POST_LEAF),
               getItem("processOverviewView", null), getIconPath("processOverviewView"));
         processNode.addItem("processSearchView", messageBean.getString(PRE_LEAF + "processSearchView" + POST_LEAF),
               getItem("processSearchView", null), getIconPath("processSearchView"));
         processNode.addItem("trafficLightView", messageBean.getString(PRE_LEAF + "trafficLightView" + POST_LEAF),
               getItem("trafficLightView", null), getIconPath("trafficLightView"));
         processNode.setExpanded(true);

         // ***** Activities Node *****
         GenericCategory activitiesNode = tree.getRootCategory().addSubCategory("activities",
               messageBean.getString(PRE_NODE + "activities"), "portalConfig");

         activitiesNode.addItem("activityCriticalityManagerView",
               messageBean.getString(PRE_LEAF + "activityCriticalityManagerView" + POST_LEAF),
               getItem("activityCriticalityManagerView", null), getIconPath("activityCriticalityManagerView"));
         activitiesNode.addItem("pendingActivities", messageBean.getString(PRE_LEAF + "pendingActivities" + POST_LEAF),
               getItem("pendingActivities", null), getIconPath("pendingActivities"));
         activitiesNode.addItem("completedActivities", messageBean.getString(PRE_LEAF + "completedActivities"
               + POST_LEAF), getItem("completedActivities", null), getIconPath("completedActivities"));
         activitiesNode.addItem("postponedActivities", messageBean.getString(PRE_LEAF + "postponedActivities"
               + POST_LEAF), getItem("postponedActivities", null), getIconPath("postponedActivities"));
         activitiesNode.addItem(ResourcePaths.V_strandedActivitiesView, messageBean.getString(PRE_LEAF
               + ResourcePaths.V_strandedActivitiesView + POST_LEAF), getItem(ResourcePaths.V_strandedActivitiesView,
               null), getIconPath(ResourcePaths.V_strandedActivitiesView));
         activitiesNode.setExpanded(true);

         // ***** Resources Node *****
         GenericCategory resourcesNode = tree.getRootCategory().addSubCategory("resources",
               messageBean.getString(PRE_NODE + "resources"), "portalConfig");

         resourcesNode.addItem("resourceAvailabilityView", messageBean.getString(PRE_LEAF + "resourceAvailabilityView"
               + POST_LEAF), getItem("resourceAvailabilityView", null), getIconPath("resourceAvailabilityView"));
         resourcesNode.addItem("roleAssignmentView",
               messageBean.getString(PRE_LEAF + "roleAssignmentView" + POST_LEAF), getItem("roleAssignmentView", null),
               getIconPath("roleAssignmentView"));
         resourcesNode.addItem("resourceLoginView", messageBean.getString(PRE_LEAF + "resourceLoginView" + POST_LEAF),
               getItem("resourceLoginView", null), getIconPath("resourceLoginView"));
         resourcesNode.addItem("resourcePerformance", messageBean.getString(PRE_LEAF + "resourcePerformance"
               + POST_LEAF), getItem("resourcePerformance", null), getIconPath("resourcePerformance"));
         resourcesNode.addItem("performanceTeamleader", messageBean.getString(PRE_LEAF + "performanceTeamleader"
               + POST_LEAF), getItem("performanceTeamleader", null), getIconPath("performanceTeamleader"));
         resourcesNode.setExpanded(true);

         // ***** Costs Node *****
         GenericCategory costNode = tree.getRootCategory().addSubCategory("costsAndControlling",
               messageBean.getString(PRE_NODE + "costsAndControlling"), "portalConfig");

         costNode.addItem("costs", messageBean.getString(PRE_LEAF + "costs" + POST_LEAF), getItem("costs", null), getIconPath("costs"));

         costNode.setExpanded(true);

         // ***** Reports Node *****
         GenericCategory reportsNode = tree.getRootCategory().addSubCategory("reportDesigns",
               messageBean.getString(PRE_NODE + "reportDesigns"), "reportDesigns");
         reportsNode.setExpanded(true);

         reportsNode.addItem("myReportsView", messageBean.getString("views.myReportsView.reportManagement"), getItem(
               "myReportsView", null), getIconPath("myReportsView"));
         
         tree.refreshTreeModel();

         initialize();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {
   // FW Method. Not used
   }

   public boolean isCanUploadReport()
   {
      return canUploadReport;
   }

   public void itemClicked(GenericCategoryTreeUserObject treeUserobject)
   {
      Object itemObject = treeUserobject.getItem().getItemObject();
      if (itemObject instanceof Map)
      {
         Map map = (Map) itemObject;
         String viewId = (String) map.get("view");
         Map<String, Object> params = new HashMap<String, Object>();
         PortalApplication.getInstance().openViewById(viewId, null, params, null, true);
      }
   }

   public void refreshTreeModel()
   {
      tree.refreshTreeModel();
   }

   public void removeUserArchivedReport(String viewUrl)
   {
      removeView(viewUrl, this.userArchivedReportsCategory);
   }

   public void removeUserReportDesign(String viewUrl)
   {
      removeView(viewUrl, this.userReportDesignsCategory);
   }

   private Map<String, Object> getItem(String view, Object object)
   {
      MessagesBCCBean messageBean = MessagesBCCBean.getInstance();
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("view", view);
      map.put("item", object);
      map.put("description", messageBean.getString(PRE_LEAF + view + ".description"));

      return map;
   }


   private void removeView(String viewUrl, GenericCategory category)
   {
      // TODO: write a Category.removeView() method
      GenericItem viewToRemove = null;

      for (GenericItem item : category.getItems())
      {
         Map map = (Map) item.getItemObject();
         View view = (View) map.get("item");

         if (viewUrl.equals(view.getUrl()))
         {
            viewToRemove = item;

            break;
         }
      }

      if (viewToRemove != null)
      {
         category.getItems().remove(viewToRemove);
      }
   }
   
   /**
    * @param formatType
    * @return
    */
   public static String getIconPath(String formatType)
   {
      String fileName = getIconMap().get(formatType);
      if (!fileName.contains("/"))
      {
         return (IMAGE_BASE_PATH + fileName);
      }
      return fileName;
   }

   public static Map<String, String> getIconMap()
   {
      return iconMap;
   }
   
}
