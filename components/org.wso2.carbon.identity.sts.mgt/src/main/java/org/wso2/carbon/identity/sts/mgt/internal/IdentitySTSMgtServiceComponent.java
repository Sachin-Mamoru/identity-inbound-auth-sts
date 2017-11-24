/*
 * Copyright (c) 2005, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.sts.mgt.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.provider.IdentityProviderUtil;
import org.wso2.carbon.identity.sts.mgt.STSConfigurationContextObserver;
import org.wso2.carbon.identity.sts.mgt.STSObserver;
import org.wso2.carbon.identity.sts.mgt.admin.STSConfigAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.security.SecurityConstants;
import org.wso2.carbon.security.SecurityScenario;
import org.wso2.carbon.security.SecurityScenarioDatabase;
import org.wso2.carbon.security.config.SecurityConfigAdmin;
import org.wso2.carbon.security.util.XmlConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import java.net.URL;
import java.util.Iterator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "identity.sts.mgt.component", 
         immediate = true)
public class IdentitySTSMgtServiceComponent {

    private static final Log log = LogFactory.getLog(IdentitySTSMgtServiceComponent.class);

    private static RegistryService registryService;

    private static ConfigurationContext configContext;

    private static BundleContext bundleContext;

    private static RealmService realmService;

    public IdentitySTSMgtServiceComponent() {
    }

    public static ConfigurationContext getConfigurationContext() {
        return configContext;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * @param registryService
     */
    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("RegistryService set in Identity STS Mgt bundle");
        }
        try {
            this.registryService = registryService;
        } catch (Throwable e) {
            log.error("Failed to load security scenarios", e);
        }
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * @param ctxt
     */
    @Activate
    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Identity STS Mgt bundle is activated");
        }
        bundleContext = ctxt.getBundleContext();
        try {
            initialize();
        } catch (Throwable e) {
            log.error("Failed to load security scenarios", e);
        }
    }

    /**
     * @param ctxt
     */
    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Identity STS Mgt bundle is deactivated");
        }
    }

    /**
     * @param registryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("RegistryService set in Identity STS Mgt bundle");
        }
        this.registryService = null;
    }

    /**
     * @param contextService
     */
    @Reference(
             name = "config.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("ConfigurationContextService set in Identity STS Mgt bundle");
        }
        this.configContext = contextService.getServerConfigContext();
    }

    /**
     * @param contextService
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("ConfigurationContextService unset in Identity STS Mgt bundle");
        }
        this.configContext = null;
    }

    /**
     * @param providerUtil
     */
    @Reference(
             name = "identity.provider.service", 
             service = org.wso2.carbon.identity.provider.IdentityProviderUtil.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetIdentityProviderAdminUtil")
    protected void setIdentityProviderAdminUtil(IdentityProviderUtil providerUtil) {
        if (log.isDebugEnabled()) {
            log.info("IdentityProviderUtil set in Identity STS Mgt bundle");
        }
    }

    /**
     * @param providerUtil
     */
    protected void unsetIdentityProviderAdminUtil(IdentityProviderUtil providerUtil) {
        if (log.isDebugEnabled()) {
            log.info("IdentityProviderUtil unset in Identity STS Mgt bundle");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        this.realmService = null;
    }

    /**
     * @param securityConfig
     */
    @Reference(
             name = "security.config.service", 
             service = org.wso2.carbon.security.config.SecurityConfigAdmin.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetSecurityConfigAdminService")
    protected void setSecurityConfigAdminService(SecurityConfigAdmin securityConfig) {
        if (log.isDebugEnabled()) {
            log.info("SecurityConfigAdmin set in Identity STS Mgt bundle");
        }
    }

    /**
     * @param securityConfig
     */
    protected void unsetSecurityConfigAdminService(SecurityConfigAdmin securityConfig) {
        if (log.isDebugEnabled()) {
            log.info("SecurityConfigAdmin unset in Identity STS Mgt bundle");
        }
    }

    /**
     * @throws Exception
     */
    private void initialize() throws Exception {
        // Register a Axis2ConfigurationContextObserver to activate on loading tenants.
        bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), new STSConfigurationContextObserver(), null);
        log.debug("Registered STSConfigurationContextObserver to configure STS service for tenants.");
        loadSecurityScenarios();
        STSConfigAdmin.configureService(configContext.getAxisConfiguration(), this.registryService.getConfigSystemRegistry());
        STSConfigAdmin.configureGenericSTS();
        configContext.getAxisConfiguration().addObservers(new STSObserver());
    }

    /**
     * This method is used to load custom security scenarios used inside Identity STS componsnts.
     *
     * @throws Exception
     */
    private void loadSecurityScenarios() throws Exception {
        Registry registry = registryService.getConfigSystemRegistry();
        try {
            // Scenarios are listed in resources/scenario-config.xml
            URL resource = bundleContext.getBundle().getResource("scenario-config.xml");
            if (resource == null) {
                return;
            }
            XmlConfiguration xmlConfiguration = new XmlConfiguration(resource.openStream(), SecurityConstants.SECURITY_NAMESPACE);
            OMElement[] elements = xmlConfiguration.getElements("//ns:Scenario");
            boolean transactionStarted = Transaction.isStarted();
            if (!transactionStarted) {
                registry.beginTransaction();
            }
            for (OMElement scenarioEle : elements) {
                SecurityScenario scenario = new SecurityScenario();
                String scenarioId = scenarioEle.getAttribute(SecurityConstants.ID_QN).getAttributeValue();
                scenario.setScenarioId(scenarioId);
                scenario.setSummary(scenarioEle.getFirstChildWithName(SecurityConstants.SUMMARY_QN).getText());
                scenario.setDescription(scenarioEle.getFirstChildWithName(SecurityConstants.DESCRIPTION_QN).getText());
                scenario.setCategory(scenarioEle.getFirstChildWithName(SecurityConstants.CATEGORY_QN).getText());
                scenario.setWsuId(scenarioEle.getFirstChildWithName(SecurityConstants.WSUID_QN).getText());
                scenario.setType(scenarioEle.getFirstChildWithName(SecurityConstants.TYPE_QN).getText());
                OMElement genPolicyElem = scenarioEle.getFirstChildWithName(SecurityConstants.IS_GEN_POLICY_QN);
                if (genPolicyElem != null && "false".equals(genPolicyElem.getText())) {
                    scenario.setGeneralPolicy(false);
                }
                String resourceUri = SecurityConstants.SECURITY_POLICY + "/" + scenarioId;
                for (Iterator modules = scenarioEle.getFirstChildWithName(SecurityConstants.MODULES_QN).getChildElements(); modules.hasNext(); ) {
                    String module = ((OMElement) modules.next()).getText();
                    scenario.addModule(module);
                }
                // Save it in the DB
                SecurityScenarioDatabase.put(scenarioId, scenario);
                // Store the scenario in the Registry
                if (!scenarioId.equals(SecurityConstants.SCENARIO_DISABLE_SECURITY) && !scenarioId.equals(SecurityConstants.POLICY_FROM_REG_SCENARIO)) {
                    Resource scenarioResource = new ResourceImpl();
                    URL urlResource = bundleContext.getBundle().getResource(scenarioId + "-policy.xml");
                    if (urlResource == null) {
                        return;
                    }
                    scenarioResource.setContentStream(urlResource.openStream());
                    if (!registry.resourceExists(resourceUri)) {
                        registry.put(resourceUri, scenarioResource);
                    }
                }
            }
            if (!transactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            registry.rollbackTransaction();
            throw e;
        }
    }
}

