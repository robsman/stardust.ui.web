package org.eclipse.stardust.ui.web.modeler.rest;

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.rest.RestControllerUtils.resolveSpringBean;

import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.spi.ModelerUiExtensionsProvider;
import org.eclipse.stardust.ui.web.modeler.ui.UiExtensionsRegistry;

@Path("/bpm-modeler/config/ui")
public class ModelingUiExtensionsController
{
   private static final Logger trace = LogManager.getLogger(ModelingUiExtensionsController.class);

   @Context
   private ServletContext servletContext;

   @Context
   private UriInfo uriInfo;

   @GET
   @Path("/extensions.js")
   @Produces("application/x-javascript")
   public String listUiExtensions(@QueryParam("p") String callbackVar)
   {
      UiExtensionsRegistry registry = resolveUiExtensionsRegistry();

      StringBuilder response = new StringBuilder();
      if ( !isEmpty(callbackVar))
      {
         response.append("var ").append(callbackVar).append(" = ");
      }
      response.append("{\n");

      listViewManagers(registry.getViewManagers(), response);
      listPropertyPages(registry.getPropertyPages(), response);
      listToolbarSections(registry.getToolbarSections(), response);
      listToolbarEntries(registry.getToolbarEntries(), response);
      listApplicationTypes(registry.getApplicationTypes(), response);
      listDataTypes(registry.getDataTypes(), response);

      response.append("};\n");

      return response.toString();
   }

   @GET
   @Path("/plugins/modeler-plugins.js")
   @Produces("application/x-javascript")
   public String listModelerPluginIncludes()
   {
      UiExtensionsRegistry registry = resolveUiExtensionsRegistry();

      StringBuilder buffer = new StringBuilder();
      buffer.append("define([ 'require', 'm_extensionManager',\n");

      listViewManagerImports(registry.getViewManagers(), buffer);
      listPropertyPageImports(registry.getPropertyPages(), buffer);
      listToolbarSectionImports(registry.getToolbarSections(), buffer);
      listToolbarEntryImports(registry.getToolbarEntries(), buffer);

      buffer.append("], function(require, m_extensionManager) {\n")
            .append("    // inject plugins module loader to extensions manager\n")
            .append("    m_extensionManager.initialize(require);\n")
            .append("    return {};\n")
            .append("});\n");

      return buffer.toString();
   }

   @GET
   @Path("/plugins/outline-plugins.js")
   @Produces("application/x-javascript")
   public String listOutlinePluginIncludes()
   {
      UiExtensionsRegistry registry = resolveUiExtensionsRegistry();

      StringBuilder buffer = new StringBuilder();
      buffer.append("define([ 'require', 'm_extensionManager',\n");

      listViewManagerImports(registry.getViewManagers(), buffer);

      buffer.append("], function(require, m_extensionManager) {\n")
            .append("    // inject plugins module loader to extensions manager\n")
            .append("    m_extensionManager.initialize(require);\n")
            .append("    return {};\n")
            .append("});\n");

      return buffer.toString();
   }

   private void listViewManagerImports(
         List<ModelerUiExtensionsProvider.ViewManagerConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("    // view managers\n");

      Set<String> imports = newHashSet();
      for (ModelerUiExtensionsProvider.ViewManagerConfiguration config : extensions)
      {
         if ( !isEmpty(config.moduleUri) && !imports.contains(config.moduleUri))
         {
            buffer.append("    '").append(config.moduleUri).append("',\n");
            imports.add(config.moduleUri);
         }
      }
   }

   private void listViewManagers(
         List<ModelerUiExtensionsProvider.ViewManagerConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("  viewManager: [\n");
      for (ModelerUiExtensionsProvider.ViewManagerConfiguration config : extensions)
      {
         buffer.append("    { moduleUrl: '").append(config.moduleUri).append("' },\n");
      }
      buffer.append("  ],\n");
   }

   private void listPropertyPageImports(
         List<ModelerUiExtensionsProvider.PropertyPageConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("    // property pages\n");

      Set<String> imports = newHashSet();
      for (ModelerUiExtensionsProvider.PropertyPageConfiguration config : extensions)
      {
         if ( !isEmpty(config.moduleUri) && !imports.contains(config.moduleUri))
         {
            buffer.append("    '").append(config.moduleUri).append("',\n");
            imports.add(config.moduleUri);
         }
      }
   }

   private void listPropertyPages(
         List<ModelerUiExtensionsProvider.PropertyPageConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("  propertiesPage: [\n");
      for (ModelerUiExtensionsProvider.PropertyPageConfiguration config : extensions)
      {
         buffer.append("    {")
               .append(" panelId: '").append(config.panelId).append("',")
               .append(" pageId: '").append(config.pageId).append("',");

         if ( !isEmpty(config.pageUri))
         {
            buffer.append(" pageHtmlUrl: '").append(config.pageUri).append("',");
         }

         buffer.append(" pageJavaScriptUrl: '").append(config.moduleUri).append("',")
               .append(" visibility: '").append(config.visibility).append("'")
               .append(" },\n");;
      }
      buffer.append("  ],\n");
   }

   private void listToolbarSectionImports(
         List<ModelerUiExtensionsProvider.ToolbarSectionConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("    // toolbar sections\n");

      Set<String> imports = newHashSet();
      for (ModelerUiExtensionsProvider.ToolbarSectionConfiguration config : extensions)
      {
         if ( !isEmpty(config.moduleUri) && !imports.contains(config.moduleUri))
         {
            buffer.append("    '").append(config.moduleUri).append("',\n");
            imports.add(config.moduleUri);
         }
      }
   }

   private void listToolbarSections(
         List<ModelerUiExtensionsProvider.ToolbarSectionConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("  diagramToolbarPalette: [\n");
      for (ModelerUiExtensionsProvider.ToolbarSectionConfiguration config : extensions)
      {
         buffer.append("    {")
               .append(" id: '").append(config.id).append("',")
               .append(" title: '").append(config.title).append("',");

         if ( !isEmpty(config.pageUri))
         {
            buffer.append(" contentHtmlUrl: '").append(config.pageUri).append("',");
         }
         if ( !isEmpty(config.moduleUri))
         {
            buffer.append(" controllerJavaScriptUrl: '").append(config.moduleUri).append("',");
         }
         if ( !isEmpty(config.visibility))
         {
            buffer.append(" visibility: '").append(config.visibility).append("'");
         }
         buffer.append(" },\n");;
      }
      buffer.append("  ],\n");
   }

   private void listToolbarEntryImports(
         List<ModelerUiExtensionsProvider.ToolbarEntryConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("    // toolbar entries\n");

      Set<String> imports = newHashSet();
      for (ModelerUiExtensionsProvider.ToolbarEntryConfiguration config : extensions)
      {
         if ( !isEmpty(config.moduleUri) && !imports.contains(config.moduleUri))
         {
            buffer.append("    '").append(config.moduleUri).append("',\n");
            imports.add(config.moduleUri);
         }
      }
   }

   private void listToolbarEntries(
         List<ModelerUiExtensionsProvider.ToolbarEntryConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("  diagramToolbarPaletteEntry: [\n");
      for (ModelerUiExtensionsProvider.ToolbarEntryConfiguration config : extensions)
      {
         buffer.append("    {")
               .append(" id: '").append(config.id).append("',")
               .append(" paletteId: '").append(config.sectionId).append("',")
               .append(" title: '").append(config.title).append("',")
               .append(" iconUrl: '").append(config.iconUri).append("',")
               .append(" handler: '").append(config.moduleUri).append("',")
               .append(" handlerMethod: '").append(config.handlerMethod).append("',")
               .append(" visibility: '").append(config.visibility).append("'");
         buffer.append(" },\n");;
      }
      buffer.append("  ],\n");
   }

   private void listApplicationTypes(
         List<ModelerUiExtensionsProvider.ApplicationTypeConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("  applicationType: [\n");
      for (ModelerUiExtensionsProvider.ApplicationTypeConfiguration config : extensions)
      {
         buffer.append("    {")
               .append(" id: '").append(config.id).append("',")
               .append(" readableName: '").append(config.title).append("',")
               .append(" iconPath: '").append(config.iconUri).append("',")
               .append(" viewId: '").append(config.viewId).append("'");
         buffer.append(" },\n");;
      }
      buffer.append("  ],\n");
   }

   private void listDataTypes(
         List<ModelerUiExtensionsProvider.DataTypeConfiguration> extensions,
         StringBuilder buffer)
   {
      buffer.append("  dataType: [\n");
      for (ModelerUiExtensionsProvider.DataTypeConfiguration config : extensions)
      {
         buffer.append("    {")
               .append(" id: '").append(config.id).append("',")
               .append(" readableName: '").append(config.title).append("',")
               .append(" iconPath: '").append(config.iconUri).append("'");
         buffer.append(" },\n");;
      }
      buffer.append("  ],\n");
   }

   private UiExtensionsRegistry resolveUiExtensionsRegistry()
   {
      return resolveSpringBean(UiExtensionsRegistry.class, servletContext);
   }
}
