/*
 * Copyright (c) 2020 Jiovan Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiovan.spring.starter.multipledatasource.config;


import javax.sql.DataSource;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.XADataSourceAutoConfiguration;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.jiovan.plexus.spring.condition.annotation.ConditionalOnAllBooleanProperties;
import com.jiovan.plexus.spring.toolkit.util.ClassPathWraps;
import com.jiovan.plexus.spring.toolkit.util.PropertyBinderWraps;
import com.jiovan.spring.starter.datasourcebuilder.composer.DataSourceBuilder;
import com.jiovan.spring.starter.datasourcebuilder.util.JpaConfigurationUtils;


/**
 * Tertiary datasource configuration for Spring Data JPA
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAllBooleanProperties(value = {
    @ConditionalOnBooleanProperty(prefix = "jiovan.multiple-datasource", name = "enabled", matchIfMissing = true),
    @ConditionalOnBooleanProperty(prefix = TertiaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX, name = "jpa-enabled", matchIfMissing = true)
})
@ConditionalOnClass(value = {DataSource.class, JdbcOperations.class, JpaDialect.class, JpaRepository.class})
@ConditionalOnBean(name = TertiaryDataSourceJdbcConfiguration.DATA_SOURCE)
@AutoConfigureAfter(value = {SecondaryDataSourceJpaConfiguration.class, TertiaryDataSourceJdbcConfiguration.class})
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableTransactionManagement(order = 103)
public class TertiaryDataSourceJpaConfiguration {
    public static final String PROPERTIES_PREFIX = TertiaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX + ".jpa";    // $NON-NLS-1$
    public static final String JPA_PROPERTIES = "tertiaryDataSourceJpaProperties";    // $NON-NLS-1$
    public static final String PERSISTENCE_UNIT_MANAGER = "tertiaryDataSourcePersistenceUnitManager";    // $NON-NLS-1$
    public static final String PERSISTENCE_UNIT_POST_PROCESSOR = "tertiaryDataSourcePersistenceUnitPostProcessor";    // $NON-NLS-1$
    public static final String PERSISTENCE_UNIT = "tertiaryDataSourcePersistenceUnit";    // $NON-NLS-1$
    public static final String PERSISTENCE_XML_LOCATION = "classpath*:META-INF/persistence-tertiary.xml";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY_BUILDER_CUSTOMIZER = "tertiaryDataSourceEntityManagerFactoryBuilderCustomizer";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY_BUILDER_EXECUTOR = "tertiaryDataSourceEntityManagerFactoryBuilderExecutor";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY_BUILDER = "tertiaryDataSourceEntityManagerFactoryBuilder";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY_BEAN = "tertiaryDataSourceEntityManagerFactoryBean";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY = "tertiaryDataSourceEntityManagerFactory";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER = "tertiaryDataSourceEntityManager";    // $NON-NLS-1$
    public static final String ENTITY_PACKAGE = "**.domain.tertiary.rdbms";    // $NON-NLS-1$
    private static final String XA_PREFIX = TertiaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX + ".xa";    // $NON-NLS-1$

    /**
     * Returns a {@link org.springframework.boot.jpa.autoconfigure.JpaProperties} bound from the {@code PROPERTIES_PREFIX} configuration prefix
     *
     * @return a new {@link org.springframework.boot.jpa.autoconfigure.JpaProperties}
     */
    @Bean(name = JPA_PROPERTIES)
    @ConditionalOnMissingBean(name = JPA_PROPERTIES)
    @ConfigurationProperties(prefix = PROPERTIES_PREFIX)
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    /**
     * Returns a default {@link org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager} configured with the entity package, optional persistence XML resource, JTA detection and optional post processor
     *
     * @param dataSource the optional qualified data source used by the persistence unit
     * @param processor the optional persistence unit post processor
     * @param loader the resource loader used to locate persistence XML and scan entity classes
     * @param environment the environment used to detect XA configuration
     *
     * @return a default {@link org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager} for the qualified datasource
     */
    @Bean(name = PERSISTENCE_UNIT_MANAGER)
    @ConditionalOnMissingBean(name = PERSISTENCE_UNIT_MANAGER)
    @SuppressWarnings("DuplicatedCode")
    public PersistenceUnitManager persistenceUnitManager(@Autowired(required = false) @Qualifier(value = TertiaryDataSourceJdbcConfiguration.DATA_SOURCE) @Nullable DataSource dataSource,
        @Autowired(required = false) @Qualifier(value = PERSISTENCE_UNIT_POST_PROCESSOR) @Nullable PersistenceUnitPostProcessor processor,
        @Nonnull ResourceLoader loader, @Nonnull Environment environment) {
        String[] packagesToScan = new String[]{ENTITY_PACKAGE};
        String[] xmlLocations = ClassPathWraps.existsResource(PERSISTENCE_XML_LOCATION, loader.getClassLoader()) ? new String[]{PERSISTENCE_XML_LOCATION} : ArrayUtils.EMPTY_STRING_ARRAY;
        boolean jta = PropertyBinderWraps.contains(environment, XA_PREFIX);
        PersistenceUnitPostProcessor[] postProcessors = (processor == null) ? null : new PersistenceUnitPostProcessor[]{processor};
        return JpaConfigurationUtils.defaultPersistenceUnitManager(PERSISTENCE_UNIT, null, dataSource, packagesToScan, null, xmlLocations, jta, loader, SharedCacheMode.ENABLE_SELECTIVE, ValidationMode.AUTO, postProcessors);
    }

    /**
     * Returns an {@link org.springframework.boot.jpa.EntityManagerFactoryBuilder} built from the Hibernate JPA vendor adapter, the persistence unit manager and the JPA properties, with an optional bootstrap executor and customizer applied
     *
     * @param manager the persistence unit manager used by the builder
     * @param executor the optional async task executor used for bootstrap
     * @param customizer the optional customizer applied to the builder
     * @param properties the JPA properties supplying the vendor adapter and property map
     *
     * @return the configured {@link org.springframework.boot.jpa.EntityManagerFactoryBuilder}
     */
    @Bean(name = ENTITY_MANAGER_FACTORY_BUILDER)
    @ConditionalOnBean(name = {PERSISTENCE_UNIT_MANAGER, JPA_PROPERTIES})
    @ConditionalOnMissingBean(name = ENTITY_MANAGER_FACTORY_BUILDER)
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(@Qualifier(value = PERSISTENCE_UNIT_MANAGER) @Nonnull PersistenceUnitManager manager,
        @Autowired(required = false) @Qualifier(value = ENTITY_MANAGER_FACTORY_BUILDER_EXECUTOR) @Nullable AsyncTaskExecutor executor,
        @Autowired(required = false) @Qualifier(value = ENTITY_MANAGER_FACTORY_BUILDER_CUSTOMIZER) @Nullable EntityManagerFactoryBuilderCustomizer customizer,
        @Qualifier(value = JPA_PROPERTIES) @Nonnull JpaProperties properties) {
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(JpaConfigurationUtils.hibernateJpaVendorAdapter(properties), (datasource) -> properties.getProperties(), manager);
        builder.setBootstrapExecutor(executor);
        if (customizer != null) {
            customizer.customize(builder);
        }
        return builder;
    }

    /**
     * Returns a {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean} built from the given builder, qualified data source, JPA properties and JTA setting
     *
     * @param builder the entity manager factory builder
     * @param dataSource the qualified data source for the entity manager factory
     * @param properties the JPA properties supplying the property map
     * @param environment the environment used to detect XA configuration
     *
     * @return the configured {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean}
     */
    @Bean(name = ENTITY_MANAGER_FACTORY_BEAN)
    @ConditionalOnBean(name = {ENTITY_MANAGER_FACTORY_BUILDER, TertiaryDataSourceJdbcConfiguration.DATA_SOURCE, JPA_PROPERTIES})
    @ConditionalOnMissingBean(name = ENTITY_MANAGER_FACTORY_BEAN)
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(@Qualifier(value = ENTITY_MANAGER_FACTORY_BUILDER) @Nonnull EntityManagerFactoryBuilder builder,
        @Qualifier(value = TertiaryDataSourceJdbcConfiguration.DATA_SOURCE) @Nonnull DataSource dataSource,
        @Qualifier(value = JPA_PROPERTIES) @Nonnull JpaProperties properties, @Nonnull Environment environment) {
        return builder.dataSource(dataSource).packages(ENTITY_PACKAGE).persistenceUnit(PERSISTENCE_UNIT).properties(properties.getProperties()).jta(PropertyBinderWraps.contains(environment, XA_PREFIX)).build();
    }

    /**
     * Returns the {@link jakarta.persistence.EntityManagerFactory} produced by the given factory bean
     *
     * @param factoryBean the entity manager factory bean exposing the factory
     *
     * @return the {@link jakarta.persistence.EntityManagerFactory} exposed by the {@code factoryBean}
     */
    @Bean(name = ENTITY_MANAGER_FACTORY)
    @ConditionalOnBean(name = ENTITY_MANAGER_FACTORY_BEAN)
    @ConditionalOnMissingBean(name = ENTITY_MANAGER_FACTORY)
    public EntityManagerFactory entityManagerFactory(@Qualifier(value = ENTITY_MANAGER_FACTORY_BEAN) @Nonnull LocalContainerEntityManagerFactoryBean factoryBean) {
        return factoryBean.getObject();
    }

    /**
     * Returns a new {@link jakarta.persistence.EntityManager} created by the given entity manager factory
     *
     * @param factory the entity manager factory creating the entity manager
     *
     * @return a new {@link jakarta.persistence.EntityManager}
     */
    @Bean(name = ENTITY_MANAGER)
    @ConditionalOnBean(name = ENTITY_MANAGER_FACTORY)
    @ConditionalOnMissingBean(name = ENTITY_MANAGER)
    public EntityManager entityManager(@Qualifier(value = ENTITY_MANAGER_FACTORY) @Nonnull EntityManagerFactory factory) {
        return factory.createEntityManager();
    }

    /**
     * Returns a JPA {@link org.springframework.transaction.TransactionManager} built from the entity manager factory with the available transaction manager customizers applied
     *
     * @param builder the data source builder creating the transaction manager
     * @param factory the entity manager factory managed by the transaction manager
     * @param customizers the provider of transaction manager customizers
     *
     * @return a JPA {@link org.springframework.transaction.TransactionManager}
     */
    @Bean(name = TertiaryDataSourceJdbcConfiguration.TRANSACTION_MANAGER)
    @ConditionalOnBooleanProperty(prefix = TertiaryDataSourceJpaConfiguration.PROPERTIES_PREFIX, name = "jpa-transaction", matchIfMissing = true)
    @ConditionalOnBean(name = ENTITY_MANAGER_FACTORY, value = DataSourceBuilder.class)
    @ConditionalOnMissingBean(name = TertiaryDataSourceJdbcConfiguration.TRANSACTION_MANAGER)
    public TransactionManager transactionManager(@Nonnull DataSourceBuilder builder, @Qualifier(value = ENTITY_MANAGER_FACTORY) @Nonnull EntityManagerFactory factory, @Nonnull ObjectProvider<TransactionManagerCustomizers> customizers) {
        return builder.jpaTransactionManager(factory, customizers);
    }
}
