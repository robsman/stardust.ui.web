package org.eclipse.stardust.ui.web.modeler.service.rest;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.portal.spi.UiExtensionsRegistry;

@Path("/config/ui")
public class ModelingUiExtensionsController
{
   private static final Logger trace = LogManager.getLogger(ModelingUiExtensionsController.class);

   @Resource
   private UiExtensionsRegistry uiExtensionsRegistry;

   @GET
   @Path("/plugins/modeler-plugins.js")
   @Produces("application/x-javascript")
   public String listModelerPlugins()
   {
      UiExtensionsRegistry registry = uiExtensionsRegistry;

      StringBuilder buffer = new StringBuilder();
      buffer.append("define([ 'bpm-modeler/js/m_extensionManager',\n");

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

      buffer.append("    // Rule Set Providers\n");
      listExtensionDependencies(buffer, registry.getRuleSetProviderExtensions());

      buffer.append("], function(m_extensionManager) {\n")
            .append("\n");

      listExtensionInitialization(buffer, "registerViewManager", registry.getViewManagerExtensions());
      listExtensionInitialization(buffer, "registerToolbarExtensions", registry.getDiagramToolbarExtensions());
      listExtensionInitialization(buffer, "registerPropertyPageExtensions", registry.getPropertiesPageExtensions());
      listExtensionInitialization(buffer, "registerMetaModelExtensions", registry.getMetaModelExtensions());
      listExtensionInitialization(buffer, "registerModelDecorationExtensions", registry.getModelDecorations());
      listExtensionInitialization(buffer, "registerIntegrationOverlayExtensions", registry.getIntegrationOverlayExtensions());
      listExtensionInitialization(buffer, "registerRuleSetProviderExtensions", registry.getRuleSetProviderExtensions());

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
      UiExtensionsRegistry registry = uiExtensionsRegistry;

      StringBuilder buffer = new StringBuilder();
      buffer.append("define([ 'bpm-modeler/js/m_extensionManager',\n");

      buffer.append("    // View Manager\n");
      listExtensionDependencies(buffer, registry.getViewManagerExtensions());

      buffer.append("    // Outline\n");
      listExtensionDependencies(buffer, registry.getOutlineExtensions());

      buffer.append("    // Meta Model\n");
      listExtensionDependencies(buffer, registry.getMetaModelExtensions());

      buffer.append("    // Views\n");
      listExtensionDependencies(buffer, registry.getViewExtensions());

      buffer.append("    // Integration Overlays\n");
      listExtensionDependencies(buffer, registry.getIntegrationOverlayExtensions());

      buffer.append("], function(m_extensionManager) {\n")
            .append("\n");

      listExtensionInitialization(buffer, "registerViewManager", registry.getViewManagerExtensions());
      listExtensionInitialization(buffer, "registerOutlineExtensions", registry.getOutlineExtensions());
      listExtensionInitialization(buffer, "registerMetaModelExtensions", registry.getMetaModelExtensions());
      listExtensionInitialization(buffer, "registerViewExtensions", registry.getViewExtensions());
      listExtensionInitialization(buffer, "registerIntegrationOverlayExtensions", registry.getIntegrationOverlayExtensions());

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
      UiExtensionsRegistry registry = uiExtensionsRegistry;

      StringBuilder buffer = new StringBuilder();
      buffer.append("define([ 'bpm-modeler/js/m_extensionManager',\n");

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
      String moduleUri = extensionUri;
      if (moduleUri.endsWith(".js"))
      {
         moduleUri = moduleUri.substring(0, moduleUri.length() - ".js".length());
      }

      return moduleUri;
   }
}
