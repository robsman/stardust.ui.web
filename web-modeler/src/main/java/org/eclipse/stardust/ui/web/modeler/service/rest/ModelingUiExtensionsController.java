package org.eclipse.stardust.ui.web.modeler.service.rest;

import static org.eclipse.stardust.ui.web.modeler.service.rest.RestControllerUtils.resolveSpringBean;

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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
   @Path("/plugins/modeler-plugins.js")
   @Produces("application/x-javascript")
   public String listModelerPlugins()
   {
      UiExtensionsRegistry registry = resolveUiExtensionsRegistry();

      StringBuilder buffer = new StringBuilder();
      buffer.append("define([ 'm_extensionManager',\n");

      buffer.append("    // View Managers\n");
      listExtensionDependencies(buffer, registry.getViewManagerExtensions());

      buffer.append("    // Diagram Toolbar\n");
      listExtensionDependencies(buffer, registry.getDiagramToolbarExtensions());

      buffer.append("    // Properties Pages\n");
      listExtensionDependencies(buffer, registry.getPropertiesPageExtensions());

      buffer.append("    // Meta-types\n");
      listExtensionDependencies(buffer, registry.getMetaModelExtensions());

      buffer.append("    // Decorations\n");
      listExtensionDependencies(buffer, registry.getModelDecorations());

      buffer.append("    // Integration Overlays\n");
      listExtensionDependencies(buffer, registry.getIntegrationOverlayExtensions());

      buffer.append("], function(m_extensionManager) {\n")
            .append("\n");

      listExtensionInitialization(buffer, "registerViewManager", registry.getViewManagerExtensions());
      listExtensionInitialization(buffer, "registerToolbarExtensions", registry.getDiagramToolbarExtensions());
      listExtensionInitialization(buffer, "registerPropertyPageExtensions", registry.getPropertiesPageExtensions());
      listExtensionInitialization(buffer, "registerMetaModelExtensions", registry.getMetaModelExtensions());
      listExtensionInitialization(buffer, "registerModelDecorationExtensions", registry.getModelDecorations());
      listExtensionInitialization(buffer, "registerIntegrationOverlayExtensions", registry.getIntegrationOverlayExtensions());

      buffer.append("\n")
            .append("    return {};\n")
            .append("});\n");

      return buffer.toString();
   }

   @GET
   @Path("/plugins/outline-plugins.js")
   @Produces("application/x-javascript")
   public String listOutlinePlugins()
   {
      UiExtensionsRegistry registry = resolveUiExtensionsRegistry();

      StringBuilder buffer = new StringBuilder();
      buffer.append("define([ 'm_extensionManager',\n");

      buffer.append("    // View Manager\n");
      listExtensionDependencies(buffer, registry.getViewManagerExtensions());

      buffer.append("    // Meta Model\n");
      listExtensionDependencies(buffer, registry.getMetaModelExtensions());

      buffer.append("    // Views\n");
      listExtensionDependencies(buffer, registry.getViewExtensions());

      buffer.append("], function(m_extensionManager) {\n")
            .append("\n");

      listExtensionInitialization(buffer, "registerViewManager", registry.getViewManagerExtensions());
      listExtensionInitialization(buffer, "registerMetaModelExtensions", registry.getMetaModelExtensions());
      listExtensionInitialization(buffer, "registerViewExtensions", registry.getViewExtensions());

      buffer.append("\n")
            .append("    return {};\n")
            .append("});\n");

      return buffer.toString();
   }

   @GET
   @Path("/plugins/common-plugins.js")
   @Produces("application/x-javascript")
   public String listCommonPlugins()
   {
      UiExtensionsRegistry registry = resolveUiExtensionsRegistry();

      StringBuilder buffer = new StringBuilder();
      buffer.append("define([ 'm_extensionManager',\n");

      buffer.append("    // View Managers\n");
      listExtensionDependencies(buffer, registry.getViewManagerExtensions());
      listExtensionDependencies(buffer, registry.getMetaModelExtensions());

      buffer.append("    // Properties Pages\n");
      listExtensionDependencies(buffer, registry.getPropertiesPageExtensions());

      buffer.append("    // Integration Overlays\n");
      listExtensionDependencies(buffer, registry.getIntegrationOverlayExtensions());

      buffer.append("    // Views\n");
      listExtensionDependencies(buffer, registry.getViewExtensions());

      buffer.append("], function(m_extensionManager) {\n")
            .append("\n");

      listExtensionInitialization(buffer, "registerViewManager", registry.getViewManagerExtensions());
      listExtensionInitialization(buffer, "registerMetaModelExtensions", registry.getMetaModelExtensions());
      listExtensionInitialization(buffer, "registerPropertyPageExtensions", registry.getPropertiesPageExtensions());
      listExtensionInitialization(buffer, "registerIntegrationOverlayExtensions", registry.getIntegrationOverlayExtensions());
      listExtensionInitialization(buffer, "registerViewExtensions", registry.getViewExtensions());

      buffer.append("\n")
            .append("    return {};\n")
            .append("});\n");

      return buffer.toString();
   }

   private void listExtensionDependencies(StringBuilder buffer,
         List<String> extensionUris)
   {
      for (String extensionUri : extensionUris)
      {
         buffer.append("    '").append(toModuleUri(extensionUri)).append("',\n");
      }
   }

   private void listExtensionInitialization(StringBuilder buffer, String initFunction,
         List<String> extensionUris)
   {
      for (String extensionUri : extensionUris)
      {
         buffer.append("    m_extensionManager.").append(initFunction).append("(require('").append(toModuleUri(extensionUri)).append("'));\n");
      }
   }

   private String toModuleUri(String extensionUri)
   {
      String moduleUri = "../../../" + extensionUri;
      if (moduleUri.endsWith(".js"))
      {
         moduleUri = moduleUri.substring(0, moduleUri.length() - ".js".length());
      }

      return moduleUri;
   }

   private UiExtensionsRegistry resolveUiExtensionsRegistry()
   {
      return resolveSpringBean(UiExtensionsRegistry.class, servletContext);
   }
}
