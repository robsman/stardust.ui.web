package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.Iterator;

import javax.annotation.Resource;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailService
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   public void recoverWorkflowEngine()
   {
      try
      {
         QueryService queryService = serviceFactoryUtils.getQueryService();
         AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
         ProcessInstanceQuery query = ProcessInstanceQuery.findInState(new ProcessInstanceState[] {
               ProcessInstanceState.Active, ProcessInstanceState.Interrupted, ProcessInstanceState.Aborting});
         ProcessInstances pi = queryService.getAllProcessInstances(query);
         Iterator<ProcessInstance> itr = pi != null ? pi.iterator() : null;
         while (itr != null && itr.hasNext())
         {
            ProcessInstance details = (ProcessInstance) itr.next();
            try
            {  
               adminService.recoverProcessInstance(details.getOID());
            }
            catch (AccessForbiddenException e)
            {
               throw e;
            }
            catch (Exception e)
            {
               String errorMsg = restCommonClientMessages
                     .getString("launchPanels.ippAdmAdministrativeActions.auditTrail.processInstanceRecoveringFailed")
                     + " " + details.getOID();
               throw new I18NException(errorMsg);
            }

         }

         SessionContext.findSessionContext().resetSession();
      }
      catch (AccessForbiddenException e)
      {
         throw e;
      }
      catch (I18NException e)
      {     
         throw e;
      }
      catch (Exception ex)
      {
         String errorMsg = restCommonClientMessages
               .getString("launchPanels.ippAdmAdministrativeActions.auditTrail.runtimeRecoveryFailed");
         throw new I18NException(errorMsg);
      }
   }

   /**
    * 
    * @param retainUsersAndDepts
    * @return
    */
   public Boolean cleanupATD(boolean retainUsersAndDepts)
   {
      AdministrationService service = serviceFactoryUtils.getServiceFactory().getAdministrationService();
      if (service != null)
      {
         service.cleanupRuntime(retainUsersAndDepts);
         SessionContext.findSessionContext().resetSession();
         return true;
      }
      return false;
   }
   /**
    * 
    * @return
    */
   public Boolean cleanupATMD()
   {
      AdministrationService service = serviceFactoryUtils.getServiceFactory().getAdministrationService();
      if (service != null)
      {
         service.cleanupRuntimeAndModels();
         ModelCache.findModelCache().reset();
         SessionContext.findSessionContext().resetSession();
         return true;
      }
      return false;
   }
}
