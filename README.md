#JCR exmaple (API v.2.0)

**Apache Jackrabbit v.2.10.1**

**JBOSS EAP v.6.4**

###configuration

  1. Download JBOSS EAP 6.4: http://www.jboss.org/products/eap/download/
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
    
  4. Download jcr-2.0.jar: http://jackrabbit.apache.org/jcr/downloads.html#v2.10 into $JBOSS_HOME/modules/system/layers/base/javax/jcr/main/
  5. Update JBOSS standalone configuration: $JBOSS_HOME/standalone/configuration/standalone.xml with next content:

    ```xml:
    <!-- see jcr/web/src/etc/standalone.xml, but all important thinks is here: -->
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
                                    /path/to/repository <!-- for windows should be like: /d:/path/to/repository -->
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
    Important things here is: 1. global module javax.jcr, 2 turn off validation for jca, 3. resourse adapter configuration for JCR: HomeDir and ConfigFile properties. Make sure that HomeDir is exists and JBOSS user has all needed rights. We will use shared Repository resource through JNDI name: java:/jca/app/repository
    
  6. Add repository configuration
    
    ```xml:
    <!-- see jcr/web/src/etc/repository.xml -->
    ```
    and save it as ConfigFile.
    
  7. Download jackrabbit-jca-2.10.1.rar from apache jackrabbit homesite into $JBOSS_HOME/standalone/deployments/
  8. Start JBOSS

###links

http://jackrabbit.apache.org/jcr/articles.html

http://wiki.apache.org/jackrabbit/FrontPage

https://developer.jboss.org/wiki/JackrabbitDeploymentInAS6AndAS7
