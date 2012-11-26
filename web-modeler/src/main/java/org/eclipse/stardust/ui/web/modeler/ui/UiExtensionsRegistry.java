package org.eclipse.stardust.ui.web.modeler.ui;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.ui.extension.ExtensionDiscoveryUtils;

@Component
@Scope("singleton")
public class UiExtensionsRegistry
{
   private static final Logger trace = LogManager.getLogger(UiExtensionsRegistry.class);

   @Resource
   private ApplicationContext context;

   private Map<String, List<String>> extensionDescriptors = emptyMap();

   @PostConstruct
   public void discoverExtensionDescriptors()
   {
      this.extensionDescriptors = unmodifiableMap(ExtensionDiscoveryUtils.findExtensions(
            context, asList("viewManager", "diagramToolbar", "propertiesPage", "metaModel", "integrationOverlay", "modelDecoration", "view", "ruleSetProvider")));
   }

   public List<String> getViewManagerExtensions()
   {
      return unmodifiableList(extensionDescriptors.get("viewManager"));
   }

   public List<String> getDiagramToolbarExtensions()
   {
      return unmodifiableList(extensionDescriptors.get("diagramToolbar"));
   }

   public List<String> getPropertiesPageExtensions()
   {
      return unmodifiableList(extensionDescriptors.get("propertiesPage"));
   }

   public List<String> getIntegrationOverlayExtensions()
   {
      return unmodifiableList(extensionDescriptors.get("integrationOverlay"));
   }

   public List<String> getMetaModelExtensions()
   {
      return unmodifiableList(extensionDescriptors.get("metaModel"));
   }

   public List<String> getModelDecorations()
   {
      return unmodifiableList(extensionDescriptors.get("modelDecoration"));
   }

   public List<String> getViewExtensions()
   {
      return unmodifiableList(extensionDescriptors.get("view"));
   }

   public List<String> getRuleSetProviderExtensions()
   {
      return unmodifiableList(extensionDescriptors.get("ruleSetProvider"));
   }
}
