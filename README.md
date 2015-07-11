#JCR exmaple (API v.2.0)

**Apache Jackrabbit v.2.10.1**

**JBOSS EAP v.6.4**

JBOSS configuration

  1. Download JBOSS EAP 6.4
  2. Create module folder for module JCR API: $JBOSS_HOME/modules/system/layers/base/javax/jcr/main/
  3. Add module configuration $JBOSS_HOME/modules/system/layers/base/javax/jcr/main/module.xml with next content:

    ```xml:
    <!-- module.xml -->
    <?xml version="1.0" encoding="UTF-8"?>
    <module xmlns="urn:jboss:module:1.0" name="javax.jcr">
       <resources>
            <resource-root path="jcr-2.0.jar"/>
        </resources>
       <dependencies>
          <module name="javax.transaction.api" export="true"/>
       </dependencies>
    </module>
    ```
    
  4. Download jcr-2.0.jar into $JBOSS_HOME/modules/system/layers/base/javax/jcr/main/
  5. Update JBOSS standalone configuration: $JBOSS_HOME/standalone/configuration/standalone.xml with next content:

    ```xml:
    <!-- standalone.xml -->
    <?xml version='1.0' encoding='UTF-8'?>
    <server xmlns="urn:jboss:domain:1.7">
    ...
        <profile>
            ...
            <subsystem xmlns="urn:jboss:domain:ee:1.2">
                ...
                <global-modules>
                    <module name="javax.jcr" slot="main"/>
                </global-modules>
            </subsystem>
            ...
            <subsystem xmlns="urn:jboss:domain:jca:1.1">
                <archive-validation enabled="false" fail-on-error="true" fail-on-warn="false"/>
                ...
            </subsystem>
            ...
            <subsystem xmlns="urn:jboss:domain:resource-adapters:1.1">
                <resource-adapters>
                    <resource-adapter id="jackrabbit-jca-2.10.1.rar">
                        <archive>
                            jackrabbit-jca-2.10.1.rar
                        </archive>
                        <transaction-support>XATransaction</transaction-support>
                        <connection-definitions>
                            <connection-definition 
                                    class-name="org.apache.jackrabbit.jca.JCAManagedConnectionFactory" 
                                    jndi-name="java:/jca/app/repository" enabled="true" pool-name="RabbitAdapter">
                                <config-property name="HomeDir">
                                    /path/to/repository
                                </config-property>
                                <config-property name="ConfigFile">
                                    /path/to/repository.xml
                                </config-property>
                                <security>
                                    <application/>
                                </security>
                                <validation>
                                    <background-validation>false</background-validation>
                                </validation>
                            </connection-definition>
                        </connection-definitions>
                    </resource-adapter>
                </resource-adapters>
            </subsystem>
            ...
        </profile>
    </server>
    ```
    Important things here is: HomeDir and ConfigFile properties. Make sure that HomeDir is exists and JBOSS user has all needed rights. We will use shared Repository resource through JNDI name: java:/jca/app/repository
    
  6. Add repository configuration with next content:
    
    ```xml:
    <!-- repository.xml -->
    <?xml version="1.0" encoding="UTF-8"?>
    <!--
       Licensed to the Apache Software Foundation (ASF) under one or more
       contributor license agreements.  See the NOTICE file distributed with
       this work for additional information regarding copyright ownership.
       The ASF licenses this file to You under the Apache License, Version 2.0
       (the "License"); you may not use this file except in compliance with
       the License.  You may obtain a copy of the License at
    
           http://www.apache.org/licenses/LICENSE-2.0
    
       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
    -->
    <!DOCTYPE Repository PUBLIC "-//The Apache Software Foundation//DTD Jackrabbit 1.5//EN"
                                "http://jackrabbit.apache.org/dtd/repository-1.5.dtd">
    <!-- Example Repository Configuration File
         Used by
         - org.apache.jackrabbit.core.config.RepositoryConfigTest.java
         -
    -->
    <Repository>
        <!--
            virtual file system where the repository stores global state
            (e.g. registered namespaces, custom node types, etc.)
        -->
        <FileSystem class="org.apache.jackrabbit.core.fs.local.LocalFileSystem">
            <param name="path" value="${rep.home}/repo"/>
        </FileSystem>
    
        <!--
            security configuration
        -->
        <Security appName="jcr">
            <!--
                security manager:
                class: FQN of class implementing the JackrabbitSecurityManager interface
            -->
            <SecurityManager class="org.apache.jackrabbit.core.security.simple.SimpleSecurityManager" workspaceName="jcr">
                <!--
                workspace access:
                class: FQN of class implementing the WorkspaceAccessManager interface
                -->
                <!-- <WorkspaceAccessManager class="..."/> -->
                <!-- <param name="config" value="${rep.home}/security.xml"/> -->
            </SecurityManager>
            <!--
                access manager:
                class: FQN of class implementing the AccessManager interface
            -->
            <AccessManager class="org.apache.jackrabbit.core.security.simple.SimpleAccessManager">
                <!-- <param name="config" value="${rep.home}/access.xml"/> -->
            </AccessManager>
            <LoginModule class="org.apache.jackrabbit.core.security.simple.SimpleLoginModule">
               <!-- 
                  anonymous user name ('anonymous' is the default value)
               
               <param name="anonymousId" value="anonymous"/>
    		   -->
               <!--
                  administrator user id (default value if param is missing is 'admin')
                -->
               <param name="adminId" value="jcr"/>
            </LoginModule>
        </Security>
    
        <!--
            location of workspaces root directory and name of default workspace
        -->
        <Workspaces rootPath="${rep.home}/ws" defaultWorkspace="jcr"/>
    	
        <!--
            workspace configuration template:
            used to create the initial workspace if there's no workspace yet
        -->
        <Workspace name="${wsp.name}">
            <!--
                virtual file system of the workspace:
                class: FQN of class implementing the FileSystem interface
            -->
            <FileSystem class="org.apache.jackrabbit.core.fs.local.LocalFileSystem">
                <param name="path" value="${wsp.home}"/>
            </FileSystem>
            <!--
                persistence manager of the workspace:
                class: FQN of class implementing the PersistenceManager interface
            -->
            <!--
            <PersistenceManager class="org.apache.jackrabbit.core.persistence.bundle.DerbyPersistenceManager">
              <param name="url" value="jdbc:derby:${wsp.home}/db;create=true"/>
              <param name="schemaObjectPrefix" value="${wsp.name}_"/>
            </PersistenceManager>
    		-->
    		<PersistenceManager class="org.apache.jackrabbit.core.state.xml.XMLPersistenceManager" />
            <!--
                Search index and the file system it uses.
                class: FQN of class implementing the QueryHandler interface
            -->
            <SearchIndex class="org.apache.jackrabbit.core.query.lucene.SearchIndex">
                <param name="path" value="${wsp.home}/idx"/>
                <param name="textFilterClasses" value="org.apache.jackrabbit.extractor.PlainTextExtractor,org.apache.jackrabbit.extractor.MsWordTextExtractor,org.apache.jackrabbit.extractor.MsExcelTextExtractor,org.apache.jackrabbit.extractor.MsPowerPointTextExtractor,org.apache.jackrabbit.extractor.PdfTextExtractor,org.apache.jackrabbit.extractor.OpenOfficeTextExtractor,org.apache.jackrabbit.extractor.RTFTextExtractor,org.apache.jackrabbit.extractor.HTMLTextExtractor,org.apache.jackrabbit.extractor.XMLTextExtractor"/>
                <param name="extractorPoolSize" value="2"/>
                <param name="supportHighlighting" value="true"/>
            </SearchIndex>
        </Workspace>
    
        <!--
            Configures the versioning
        -->
        <Versioning rootPath="${rep.home}/ver">
            <!--
                Configures the filesystem to use for versioning for the respective
                persistence manager
            -->
            <FileSystem class="org.apache.jackrabbit.core.fs.local.LocalFileSystem">
                <param name="path" value="${rep.home}/ver" />
            </FileSystem>
            <!--
                Configures the persistence manager to be used for persisting version state.
                Please note that the current versioning implementation is based on
                a 'normal' persistence manager, but this could change in future
                implementations.
            -->
            <!--
            <PersistenceManager class="org.apache.jackrabbit.core.persistence.bundle.DerbyPersistenceManager">
              <param name="url" value="jdbc:derby:${rep.home}/version/db;create=true"/>
              <param name="schemaObjectPrefix" value="version_"/>
            </PersistenceManager>
    		-->
    		<PersistenceManager class="org.apache.jackrabbit.core.state.xml.XMLPersistenceManager" />
        </Versioning>
    
        <!--
            Search index for content that is shared repository wide
            (/jcr:system tree, contains mainly versions)
        -->
        <SearchIndex class="org.apache.jackrabbit.core.query.lucene.SearchIndex">
            <param name="path" value="${rep.home}/idx"/>
            <param name="textFilterClasses" value="org.apache.jackrabbit.extractor.PlainTextExtractor,org.apache.jackrabbit.extractor.MsWordTextExtractor,org.apache.jackrabbit.extractor.MsExcelTextExtractor,org.apache.jackrabbit.extractor.MsPowerPointTextExtractor,org.apache.jackrabbit.extractor.PdfTextExtractor,org.apache.jackrabbit.extractor.OpenOfficeTextExtractor,org.apache.jackrabbit.extractor.RTFTextExtractor,org.apache.jackrabbit.extractor.HTMLTextExtractor,org.apache.jackrabbit.extractor.XMLTextExtractor"/>
            <param name="extractorPoolSize" value="2"/>
            <param name="supportHighlighting" value="true"/>
        </SearchIndex>
    </Repository>
    ```
    and save it as ConfigFile.
    
  7. Download jackrabbit-jca-2.10.1.rar from apache jackrabbit homesite into $JBOSS_HOME/standalone/deployments/
  8. Start JBOSS

##links
