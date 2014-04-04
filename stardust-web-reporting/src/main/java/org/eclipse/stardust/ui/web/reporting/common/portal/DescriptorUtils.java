package org.eclipse.stardust.ui.web.reporting.common.portal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.ui.web.reporting.beans.spring.IModelService;
import org.eclipse.stardust.ui.web.reporting.beans.spring.portal.XPathCacheManager;

/**
 * @author Yogesh.Manware
 * 
 *         TODO: methods are copied from CommonDescriptorUtils and other relevant classes
 *         to avoid the dependency on facescontext, needs to be moved to centralized location
 *         later.
 * 
 */
public class DescriptorUtils
{

   private static final Logger trace = LogManager.getLogger(DescriptorUtils.class);

   /**
    * Returns an array with all descriptors which exists in the given processes.
    * Furthermore you have the option to get only descriptors that are filterable.
    * Descriptors are ordered by the order of processes and the order of descriptors in
    * the modeller.
    * 
    * @param processes
    *           List of all processes for which the descriptors should be resolved
    * @param onlyFilterable
    *           Restrict the descriptors to get only filterable items or not
    * @return Descriptors for the given processes
    */
   public static DataPath[] getAllDescriptors(ProcessDefinition process, boolean onlyFilterable,
         IModelService modelService, ServletContext servletContext, XPathCacheManager xPathCacheManager)
   {
      List<DataPath> allDescriptors = new ArrayList<DataPath>();

      for (Iterator descrItr = process.getAllDataPaths().iterator(); descrItr.hasNext();)
      {
         DataPath path = (DataPath) descrItr.next();
         if (Direction.IN.equals(path.getDirection())
               && path.isDescriptor()
               && ((onlyFilterable && isDataFilterable(path, modelService, servletContext, xPathCacheManager)) || !onlyFilterable))
         {
            allDescriptors.add(path);
         }
      }

      DataPath[] descriptors = allDescriptors.toArray(new DataPath[0]);
      return descriptors;
   }

   public static boolean isDataFilterable(DataPath dataPath, IModelService modelService, ServletContext servletContext,
         XPathCacheManager xPathCacheManager)
   {
      return getSortableAndFilterableFlags(dataPath, modelService, servletContext, xPathCacheManager).isFilterable();
   }

   /**
    * Evaluates if a data path is sortable and filterable.
    * 
    * @param dataPath
    *           <code>DataPath</DataPath> that is to be evaluated
    * @param modelService
    * @return Wrapper for the sortable and filterable flag
    */
   public static DescriptorFlags getSortableAndFilterableFlags(DataPath dataPath, IModelService modelService,
         ServletContext servletContext, XPathCacheManager xPathCacheManager)
   {
      Model model = modelService.getModel(dataPath.getModelOID());

      DescriptorFlags flags = new DescriptorFlags();

      if (model != null)
      {
         Data data = model.getData(dataPath.getData());
         if (data instanceof DataDetails)
         {
            DataDetails dataDetails = (DataDetails) data;
            String typeId = dataDetails.getTypeId();
            if (StructuredDataConstants.STRUCTURED_DATA.equals(typeId))
            {
               String myXPath = dataPath.getAccessPath();
               // Pepper models, return null for Access Path, Eclipse model returns "" for
               // Structured Enum
               myXPath = myXPath == null ? "" : myXPath;
               if (null != myXPath && !myXPath.contains("["))
               {
                  // this is important and maybe needs discussion:
                  // since engine does only support queries on simple XPaths (e.g. no
                  // indexes, functions, etc.)
                  // the XPath must be simplified (e.g. indexes will be removed)
                  myXPath = StructuredDataXPathUtils.getXPathWithoutIndexes(myXPath);

                  IXPathMap xPathMap = xPathCacheManager.getXpathMap(model, dataPath);
                  if (null == xPathMap)
                  {
                     trace.warn("Invalid structured data reference. Data path id was '" + dataPath.getId()
                           + "' and has referenced the following access path '" + myXPath + "'");
                  }
                  else
                  {
                     // but, if, for example, indexes are removed, the semantics of the
                     // query is
                     // then different!
                     // my current favourite solution would be not allowing entering
                     // indexes in
                     // the modeller,
                     // in the XPath dialog for process data descriptors
                     TypedXPath typedXPath = xPathMap.getXPath(myXPath);

                     if (typedXPath == null)
                     {
                        trace.warn("Invalid structured data reference. Data path id was '" + dataPath.getId()
                              + "' and has referenced the following access path '" + myXPath + "'");
                     }
                     // test, if the XPath returns a primitive
                     else if (typedXPath.getType() != BigData.NULL)
                     {
                        // it is a list of primitives or a single primitive
                        flags.filterable = true;
                        flags.sortable = !typedXPath.isList();
                     }
                  }
               }
               else
               {
                  trace.warn("Invalid structured data reference. Data path id was '" + dataPath.getId()
                        + "' and has referenced the following access path '" + myXPath + "'");
               }
            }
            else if ("primitive".equals(typeId) && StringUtils.isEmpty(dataPath.getAccessPath()))
            {
               if (!PredefinedConstants.CURRENT_DATE.equals(data.getId())
                     && !PredefinedConstants.ROOT_PROCESS_ID.equals(data.getId()))
               {
                  flags = new DescriptorFlags(true, true);
               }
               Class mappedType = dataPath.getMappedType();
               if (Float.class.equals(mappedType) || Double.class.equals(mappedType))
               {
                  flags.sortable = false;
               }
            }
         }
      }
      return flags;
   }

   public static class DescriptorFlags
   {
      private boolean sortable;

      private boolean filterable;

      public DescriptorFlags()
      {
         sortable = filterable = false;
      }

      public DescriptorFlags(boolean sortable, boolean filterable)
      {
         this.sortable = sortable;
         this.filterable = filterable;
      }

      public boolean isSortable()
      {
         return sortable;
      }

      public boolean isFilterable()
      {
         return filterable;
      }
   }
}
