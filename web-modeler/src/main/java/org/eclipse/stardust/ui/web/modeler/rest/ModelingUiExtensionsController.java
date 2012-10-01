package org.eclipse.stardust.ui.web.modeler.rest;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.rest.RestControllerUtils.resolveSpringBean;

import java.util.List;

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

@Path("/config/ui")
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
