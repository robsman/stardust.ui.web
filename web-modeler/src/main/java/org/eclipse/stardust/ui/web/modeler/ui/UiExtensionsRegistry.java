package org.eclipse.stardust.ui.web.modeler.ui;

import static java.util.Collections.unmodifiableList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.spi.ModelerUiExtensionsProvider;

@Component
@Scope("singleton")
public class UiExtensionsRegistry
{
   private static final Logger trace = LogManager.getLogger(UiExtensionsRegistry.class);

   @Resource
   private ApplicationContext context;

   private List<ModelerUiExtensionsProvider.ViewManagerConfiguration> viewManagers = newArrayList();

   private List<ModelerUiExtensionsProvider.PropertyPageConfiguration> propertyPages = newArrayList();

   private List<ModelerUiExtensionsProvider.ToolbarSectionConfiguration> toolbarSections = newArrayList();

   private List<ModelerUiExtensionsProvider.ToolbarEntryConfiguration> toolbarEntries = newArrayList();

   private List<ModelerUiExtensionsProvider.ApplicationTypeConfiguration> applicationTypes = newArrayList();

   private List<ModelerUiExtensionsProvider.DataTypeConfiguration> dataTypes = newArrayList();

   @PostConstruct
   protected void loadExtensions()
   {
      Map<String, ModelerUiExtensionsProvider> providers = context.getBeansOfType(ModelerUiExtensionsProvider.class);
      for (Map.Entry<String, ModelerUiExtensionsProvider> provider : providers.entrySet())
      {
         trace.info("Registering UI extensions from provider " + provider.getKey());

         viewManagers.addAll(provider.getValue().getViewManagerConfigs());
         propertyPages.addAll(provider.getValue().getPropertyPageConfigs());
         toolbarSections.addAll(provider.getValue().getToolbarSectionConfigs());
         toolbarEntries.addAll(provider.getValue().getToolbarEntryConfigs());
         applicationTypes.addAll(provider.getValue().getApplicationTypeConfigs());
         dataTypes.addAll(provider.getValue().getDataTypeConfigs());
      }
   }

   public List<ModelerUiExtensionsProvider.ViewManagerConfiguration> getViewManagers()
   {
      return unmodifiableList(viewManagers);
   }

   public List<ModelerUiExtensionsProvider.PropertyPageConfiguration> getPropertyPages()
   {
      return unmodifiableList(propertyPages);
   }

   public List<ModelerUiExtensionsProvider.ToolbarSectionConfiguration> getToolbarSections()
   {
      return unmodifiableList(toolbarSections);
   }

   public List<ModelerUiExtensionsProvider.ToolbarEntryConfiguration> getToolbarEntries()
   {
      return unmodifiableList(toolbarEntries);
   }

   public List<ModelerUiExtensionsProvider.ApplicationTypeConfiguration> getApplicationTypes()
   {
      return unmodifiableList(applicationTypes);
   }

   public List<ModelerUiExtensionsProvider.DataTypeConfiguration> getDataTypes()
   {
      return unmodifiableList(dataTypes);
   }

}
