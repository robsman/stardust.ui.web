package org.eclipse.stardust.engine.extensions.velocity.tool;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.getServiceFactory;
import static org.eclipse.stardust.engine.extensions.templating.core.Util.getDocumentManagementService;
import static org.eclipse.stardust.ui.web.viewscommon.utils.MySignaturePreferenceUtils.SIGNATURE_IMAGE_FILE_NAME;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.ui.web.viewscommon.utils.MySignaturePreferenceUtils;
import org.springframework.util.StringUtils;

@DefaultKey("user")
@InvalidScope({Scope.APPLICATION, Scope.SESSION})
public class UserServiceTool extends SafeConfig
{

   private static final String SIGNATURE = "signature";

   private String accountId;

   private User user;

   private ServiceFactory sf;

   public UserServiceTool()
   {}

   /**
    * Can used to set user Null; this will allow to change user setting in the same
    * template.
    * 
    * @param user
    */
   public void setUser(User user)
   {
      this.user = user;
   }

   public void setAccountId(String accountId)
   {
      this.accountId = accountId;
   }

   private void checkUserInitialized()
   {
      loadUserDetails();
   }

   private void loadUserDetails()
   {
      if (user == null)
      {
         sf = getServiceFactory();
         if (StringUtils.isEmpty(this.accountId))
         {
            user = sf.getUserService().getUser();
         }
         else
         {
            user = sf.getUserService().getUser(this.accountId);
         }
      }
   }

   public void configure(ValueParser parser)
   {

   }

   public String getFirstName()
   {
      checkUserInitialized();
      return user.getFirstName();
   }

   public String getLastName()
   {
      checkUserInitialized();
      return user.getLastName();
   }

   public String getAccount()
   {
      checkUserInitialized();
      return user.getAccount();
   }

   public String getDescription()
   {
      checkUserInitialized();
      return user.getDescription();
   }

   public String getEmail()
   {
      checkUserInitialized();
      return user.getEMail();
   }

   public String getId()
   {
      checkUserInitialized();
      return user.getId();
   }

   public String getName()
   {
      checkUserInitialized();
      return user.getName();
   }

   public long getOid()
   {
      checkUserInitialized();
      return user.getOID();
   }

   public String getPartitionId()
   {
      checkUserInitialized();
      return user.getPartitionId();
   }

   public short getPartitionOid()
   {
      checkUserInitialized();
      return user.getPartitionOID();
   }

   public Date getPreviousLoginTime()
   {
      checkUserInitialized();
      return user.getPreviousLoginTime();
   }

   public String getQualifiedId()
   {
      checkUserInitialized();
      return user.getQualifiedId();
   }

   public int getQualityAssuranceProbability()
   {
      checkUserInitialized();
      return (user.getQualityAssuranceProbability() != null)
            ? user.getQualityAssuranceProbability()
            : 0;
   }

   public Date getValidFrom()
   {
      checkUserInitialized();
      return user.getValidFrom();
   }

   public Date getValidTo()
   {
      checkUserInitialized();
      return user.getValidTo();
   }

   public UserRealm getRealm()
   {
      checkUserInitialized();
      return user.getRealm();
   }

   public Map<String, Object> getAllProperties()
   {
      checkUserInitialized();
      return user.getAllProperties();
   }

   @SuppressWarnings("unchecked")
   public Map<String, Object> getAllAttributes()
   {
      checkUserInitialized();
      return user.getAllAttributes();
   }

   public List<Grant> getAllGrants()
   {
      checkUserInitialized();
      return user.getAllGrants();
   }

   public List<UserGroup> getAllGroups()
   {
      checkUserInitialized();
      return user.getAllGroups();
   }

   public UserDetailsLevel getDetailsLevel()
   {
      checkUserInitialized();
      return user.getDetailsLevel();
   }

   public boolean getIsAdministrator()
   {
      checkUserInitialized();
      return user.isAdministrator();
   }

   public boolean getIsPasswordExpired()
   {
      checkUserInitialized();
      return user.isPasswordExpired();
   }

   public String getSignatureKey()
   {
      return SIGNATURE;
   }

   public byte[] getSignature()
   {
      checkUserInitialized();
      return getDocumentManagementService(sf).retrieveDocumentContent(
            MySignaturePreferenceUtils.getUserSignatureImageFolderpath(user) + "/"
                  + SIGNATURE_IMAGE_FILE_NAME);
   }
}
