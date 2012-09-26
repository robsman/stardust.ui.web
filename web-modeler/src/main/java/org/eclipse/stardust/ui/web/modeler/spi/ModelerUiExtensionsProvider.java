package org.eclipse.stardust.ui.web.modeler.spi;

import java.util.List;

public interface ModelerUiExtensionsProvider
{
   List<ViewManagerConfiguration> getViewManagerConfigs();

   List<PropertyPageConfiguration> getPropertyPageConfigs();

   List<ToolbarSectionConfiguration> getToolbarSectionConfigs();

   List<ToolbarEntryConfiguration> getToolbarEntryConfigs();

   List<ApplicationTypeConfiguration> getApplicationTypeConfigs();

   List<DataTypeConfiguration> getDataTypeConfigs();

   class ViewManagerConfiguration
   {
      public final String id;

      public final String moduleUri;

      public ViewManagerConfiguration(String id, String moduleUri)
      {
         this.id = id;
         this.moduleUri = moduleUri;
      }
   }

   class PropertyPageConfiguration
   {
      public final String pageId;

      public final String panelId;

      public final String pageUri;

      public final String moduleUri;

      public final String visibility;

      public PropertyPageConfiguration(String panelId, String pageId, String moduleUri, String visibility)
      {
         this.pageId = pageId;
         this.panelId = panelId;
         this.pageUri = null;
         this.moduleUri = moduleUri;
         this.visibility = visibility;
      }

      public PropertyPageConfiguration(String panelId, String pageId, String pageUri, String moduleUri, String visibility)
      {
         this.pageId = pageId;
         this.panelId = panelId;
         this.pageUri = pageUri;
         this.moduleUri = moduleUri;
         this.visibility = visibility;
      }
   }

   class ToolbarSectionConfiguration
   {
      public final String id;

      public final String title;

      public final String pageUri;

      public final String moduleUri;

      public final String visibility;

      public ToolbarSectionConfiguration(String id, String title, String visibility)
      {
         this.id = id;
         this.title = title;
         this.pageUri = null;
         this.moduleUri = null;
         this.visibility = visibility;
      }

      public ToolbarSectionConfiguration(String id, String title, String pageUri, String moduleUri, String visibility)
      {
         this.id = id;
         this.title = title;
         this.pageUri = pageUri;
         this.moduleUri = moduleUri;
         this.visibility = visibility;
      }
   }

   class ToolbarEntryConfiguration
   {
      public final String id;

      public final String sectionId;

      public final String title;

      public final String iconUri;

      public final String moduleUri;

      public final String handlerMethod;

      public final String visibility;

      public ToolbarEntryConfiguration(String id, String sectionId, String title, String iconUri, String moduleUri, String handlerMethod, String visibility)
      {
         this.id = id;
         this.sectionId = sectionId;
         this.title = title;
         this.iconUri = iconUri;
         this.moduleUri = moduleUri;
         this.handlerMethod = handlerMethod;
         this.visibility = visibility;
      }
   }

   class ApplicationTypeConfiguration
   {
      public final String id;

      public final String title;

      public final String iconUri;

      public final String viewId;

      public ApplicationTypeConfiguration(String id, String title, String iconUri, String viewId)
      {
         this.id = id;
         this.title = title;
         this.iconUri = iconUri;
         this.viewId = viewId;
      }
   }

   class DataTypeConfiguration
   {
      public final String id;

      public final String title;

      public final String iconUri;

      public DataTypeConfiguration(String id, String title, String iconUri)
      {
         this.id = id;
         this.title = title;
         this.iconUri = iconUri;
      }
   }
}
