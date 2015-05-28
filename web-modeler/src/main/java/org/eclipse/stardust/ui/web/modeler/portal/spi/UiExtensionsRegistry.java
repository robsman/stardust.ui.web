package org.eclipse.stardust.ui.web.modeler.portal.spi;

import static java.util.Collections.unmodifiableList;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class UiExtensionsRegistry
{
   @Resource
   private ApplicationContext context;

   public List<String> findExtensions(String category)
   {
      return unmodifiableList(ExtensionDiscoveryUtils.findExtensions(context, category));
   }

   public List<String> getViewManagerExtensions()
   {
      return findExtensions("viewManager");
   }

   public List<String> getOutlineExtensions()
   {
      return findExtensions("outline");
   }

   public List<String> getDiagramToolbarExtensions()
   {
      return findExtensions("diagramToolbar");
   }

   public List<String> getPropertiesPageExtensions()
   {
      return findExtensions("propertiesPage");
   }

   public List<String> getIntegrationOverlayExtensions()
   {
      return findExtensions("integrationOverlay");
   }

   public List<String> getMetaModelExtensions()
   {
      return findExtensions("metaModel");
   }

   public List<String> getModelDecorations()
   {
      return findExtensions("modelDecoration");
   }

   public List<String> getViewExtensions()
   {
      return findExtensions("view");
   }

   public List<String> getRuleSetProviderExtensions()
   {
      return findExtensions("ruleSetProvider");
   }

   public List<String> getUiMashupGeneratorExtensions()
   {
      return findExtensions("uiMashupGenerator");
   }
}
