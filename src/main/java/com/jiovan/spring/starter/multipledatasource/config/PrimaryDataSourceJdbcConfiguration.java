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


import java.util.List;
import javax.sql.DataSource;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.jdbc.autoconfigure.XADataSourceAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizers;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.boot.jdbc.metadata.CommonsDbcp2DataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.OracleUcpDataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.TomcatDataSourcePoolMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionManager;
import com.alibaba.druid.filter.Filter;
import com.jiovan.plexus.spring.condition.annotation.ConditionalOnAnyProperties;
import com.jiovan.plexus.spring.condition.annotation.ConditionalOnMissingProperty;
import com.jiovan.spring.starter.datasourcebuilder.composer.DataSourceBuilder;
import com.jiovan.spring.starter.datasourcebuilder.config.DataSourceBuilderConfiguration;
import com.jiovan.spring.starter.datasourcebuilder.constant.DataSourcePoolConst;
import com.jiovan.spring.starter.datasourcebuilder.enumeration.DataSourcePoolType;


/**
 * Primary datasource configuration for JDBC
 *
 * @author David Hsing
 *
 * @see org.springframework.boot.jdbc.autoconfigure.DataSourcePoolMetadataProvidersConfiguration
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = "jiovan.multiple-datasource", name = "enabled", matchIfMissing = true)
@ConditionalOnAnyProperties(value = {
    @ConditionalOnProperty(prefix = PrimaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX, name = "jndi-name"),
    @ConditionalOnProperty(prefix = PrimaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX, name = "url")
})
@ConditionalOnClass(value = {DataSource.class, JdbcOperations.class})
@AutoConfigureAfter(value = {DataSourceBuilderConfiguration.class, DruidDataSourcePreConfiguration.class})
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import(value = {DataSourceBuilderConfiguration.class, PrimaryDataSourceJdbcConfiguration.Entry.class, PrimaryDataSourceJdbcConfiguration.Xa.class, PrimaryDataSourceJdbcConfiguration.Jndi.class, PrimaryDataSourceJdbcConfiguration.C3p0.class, PrimaryDataSourceJdbcConfiguration.Dbcp2.class, PrimaryDataSourceJdbcConfiguration.Druid.class, PrimaryDataSourceJdbcConfiguration.Hikari.class, PrimaryDataSourceJdbcConfiguration.OracleUcp.class, PrimaryDataSourceJdbcConfiguration.OracleUcpXa.class, PrimaryDataSourceJdbcConfiguration.Tomcat.class, PrimaryDataSourceJdbcConfiguration.Generic.class, PrimaryDataSourceJdbcConfiguration.Stage.class})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class PrimaryDataSourceJdbcConfiguration {
    public static final String PROPERTIES_PREFIX = "jiovan.multiple-datasource.primary";    // $NON-NLS-1$
    public static final String DATA_SOURCE_PROPERTIES = "primaryDataSourceProperties";    // $NON-NLS-1$
    public static final String DATA_SOURCE = "primaryDataSource";    // $NON-NLS-1$
    public static final String JDBC_TEMPLATE = "primaryDataSourceJdbcTemplate";    // $NON-NLS-1$
    public static final String TRANSACTION_MANAGER = "primaryDataSourceTransactionManager";    // $NON-NLS-1$
    public static final String METADATA_PROVIDER = "primaryDataSourceMetadataProvider";    // $NON-NLS-1$


    @Order(value = 0)
    static class Entry {
        /**
         * Returns {@link org.springframework.boot.jdbc.autoconfigure.DataSourceProperties} bound from the {@code PROPERTIES_PREFIX} configuration prefix
         *
         * @param builder the data source builder reading the properties
         * @param environment the environment supplying the configuration properties
         *
         * @return the {@link org.springframework.boot.jdbc.autoconfigure.DataSourceProperties}
         */
        @Primary
        @Bean(name = DATA_SOURCE_PROPERTIES)
        @ConditionalOnBean(value = DataSourceBuilder.class)
        @ConditionalOnMissingBean(name = DATA_SOURCE_PROPERTIES)
        public DataSourceProperties dataSourceProperties(@Nonnull DataSourceBuilder builder, @Nonnull Environment environment) {
            return builder.dataSourceProperties(environment, PROPERTIES_PREFIX);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "xa")
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = {DataSourceBuilder.class, XADataSourceWrapper.class})
    @Order(value = 1)
    static class Xa {
        /**
         * Returns an XA {@link javax.sql.DataSource} built from the given properties by the XA data source wrapper
         *
         * @param builder the data source builder creating the data source
         * @param wrapper the XA data source wrapper adapting the XA data source
         * @param properties the qualified data source properties
         *
         * @return an XA {@link javax.sql.DataSource}
         *
         * @throws Exception if the XA data source cannot be built
         */
        @Primary
        @Bean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public DataSource dataSource(@Nonnull DataSourceBuilder builder, @Nonnull XADataSourceWrapper wrapper, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) throws Exception {
            return builder.xaDataSource(wrapper, properties, null);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 2)
    static class Jndi {
        /**
         * Returns a {@link javax.sql.DataSource} resolved from the JNDI name in the given properties
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties containing the JNDI name
         *
         * @return the {@link javax.sql.DataSource} resolved from the JNDI configuration
         */
        @Primary
        @Bean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public DataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return builder.dataSource(properties);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.C3P0, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = com.mchange.v2.c3p0.ComboPooledDataSource.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 3)
    static class C3p0 {
        /**
         * Returns a {@link com.mchange.v2.c3p0.ComboPooledDataSource} built from the given properties
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties
         *
         * @return the {@link com.mchange.v2.c3p0.ComboPooledDataSource}
         */
        @Primary
        @Bean(name = DATA_SOURCE, destroyMethod = "close")
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public com.mchange.v2.c3p0.ComboPooledDataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (com.mchange.v2.c3p0.ComboPooledDataSource) builder.dataSource(properties, DataSourcePoolType.C3P0);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.DBCP2, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = org.apache.commons.dbcp2.BasicDataSource.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 4)
    static class Dbcp2 {
        /**
         * Returns a {@link org.apache.commons.dbcp2.BasicDataSource} built from the given properties
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties
         *
         * @return the {@link org.apache.commons.dbcp2.BasicDataSource}
         */
        @Primary
        @Bean(name = DATA_SOURCE, destroyMethod = "close")
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public org.apache.commons.dbcp2.BasicDataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (org.apache.commons.dbcp2.BasicDataSource) builder.dataSource(properties, DataSourcePoolType.DBCP2);
        }

        /**
         * Returns a {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} exposing DBCP2 pool metadata of the given data source
         *
         * @param dataSource the qualified DBCP2 basic data source
         *
         * @return the {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} for the DBCP2 pool
         */
        @Primary
        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull org.apache.commons.dbcp2.BasicDataSource dataSource) {
            return sqlDataSource -> new CommonsDbcp2DataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.DRUID, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = com.alibaba.druid.pool.DruidDataSource.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 5)
    static class Druid {
        /**
         * Returns a {@link com.alibaba.druid.pool.DruidDataSource} built from the given properties with the supplied filters applied
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties
         * @param filters the optional Druid filters to add or replace on the data source
         *
         * @return the configured {@link com.alibaba.druid.pool.DruidDataSource}
         */
        @Primary
        @Bean(name = DATA_SOURCE, initMethod = "init", destroyMethod = "close")
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public com.alibaba.druid.pool.DruidDataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties, @Nullable List<Filter> filters) {
            com.alibaba.druid.pool.DruidDataSource dataSource = (com.alibaba.druid.pool.DruidDataSource) builder.dataSource(properties, DataSourcePoolType.DRUID);
            DruidDataSourceBuilder.addOrReplaceFilters(dataSource, filters);
            return dataSource;
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.HIKARI, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = com.zaxxer.hikari.HikariDataSource.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 6)
    static class Hikari {
        /**
         * Returns a {@link com.zaxxer.hikari.HikariDataSource} built from the given properties
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties
         *
         * @return the {@link com.zaxxer.hikari.HikariDataSource}
         */
        @Primary
        @Bean(name = DATA_SOURCE, destroyMethod = "close")
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public com.zaxxer.hikari.HikariDataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (com.zaxxer.hikari.HikariDataSource) builder.dataSource(properties, DataSourcePoolType.HIKARI);
        }

        /**
         * Returns a {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} exposing Hikari pool metadata of the given data source
         *
         * @param dataSource the qualified Hikari data source
         *
         * @return the {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} for the Hikari pool
         */
        @Primary
        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull com.zaxxer.hikari.HikariDataSource dataSource) {
            return sqlDataSource -> new HikariDataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.ORACLE_UCP, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = oracle.ucp.jdbc.PoolDataSourceImpl.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 7)
    static class OracleUcp {
        /**
         * Returns an {@link oracle.ucp.jdbc.PoolDataSourceImpl} built from the given properties
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties
         *
         * @return the {@link oracle.ucp.jdbc.PoolDataSourceImpl}
         */
        @Primary
        @Bean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public oracle.ucp.jdbc.PoolDataSourceImpl dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (oracle.ucp.jdbc.PoolDataSourceImpl) builder.dataSource(properties, DataSourcePoolType.ORACLE_UCP);
        }

        /**
         * Returns a {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} exposing Oracle UCP pool metadata of the given data source
         *
         * @param dataSource the qualified Oracle UCP pool data source
         *
         * @return the {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} for the Oracle UCP pool
         */
        @Primary
        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull oracle.ucp.jdbc.PoolDataSourceImpl dataSource) {
            return sqlDataSource -> new OracleUcpDataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.ORACLE_UCP_XA, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = oracle.ucp.jdbc.PoolXADataSourceImpl.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 8)
    static class OracleUcpXa {
        /**
         * Returns an {@link oracle.ucp.jdbc.PoolXADataSourceImpl} built from the given properties
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties
         *
         * @return the {@link oracle.ucp.jdbc.PoolXADataSourceImpl}
         */
        @Primary
        @Bean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public oracle.ucp.jdbc.PoolXADataSourceImpl dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (oracle.ucp.jdbc.PoolXADataSourceImpl) builder.dataSource(properties, DataSourcePoolType.ORACLE_UCP_XA);
        }

        /**
         * Returns a {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} exposing Oracle UCP XA pool metadata of the given data source
         *
         * @param dataSource the qualified Oracle UCP XA pool data source
         *
         * @return the {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} for the Oracle UCP XA pool
         */
        @Primary
        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull oracle.ucp.jdbc.PoolXADataSourceImpl dataSource) {
            return sqlDataSource -> new OracleUcpDataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.TOMCAT, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnBean(name = DATA_SOURCE, value = org.apache.tomcat.jdbc.pool.DataSource.class)
    @Order(value = 9)
    static class Tomcat {
        /**
         * Returns a {@link org.apache.tomcat.jdbc.pool.DataSource} built from the given properties
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties
         *
         * @return the {@link org.apache.tomcat.jdbc.pool.DataSource}
         */
        @Primary
        @Bean(name = DATA_SOURCE, destroyMethod = "close")
        @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.TOMCAT, matchIfMissing = true)
        @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
        @ConditionalOnClass(value = org.apache.tomcat.jdbc.pool.DataSource.class)
        @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public org.apache.tomcat.jdbc.pool.DataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (org.apache.tomcat.jdbc.pool.DataSource) builder.dataSource(properties, DataSourcePoolType.TOMCAT);
        }

        /**
         * Returns a {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} exposing Tomcat pool metadata of the given data source
         *
         * @param dataSource the qualified Tomcat jdbc pool data source
         *
         * @return the {@link org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider} for the Tomcat pool
         */
        @Primary
        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull org.apache.tomcat.jdbc.pool.DataSource dataSource) {
            return sqlDataSource -> new TomcatDataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @Order(value = 10)
    static class Generic {
        /**
         * Returns a generic {@link javax.sql.DataSource} built from the given properties when no specific pool type is configured
         *
         * @param builder the data source builder creating the data source
         * @param properties the qualified data source properties
         *
         * @return the generic {@link javax.sql.DataSource}
         */
        @Primary
        @Bean(name = DATA_SOURCE)
        @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public DataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return builder.dataSource(properties);
        }
    }


    @Order(value = 11)
    static class Stage {
        /**
         * Returns a {@link org.springframework.jdbc.core.JdbcTemplate} backed by the qualified data source
         *
         * @param dataSource the qualified data source
         *
         * @return the {@link org.springframework.jdbc.core.JdbcTemplate}
         */
        @Primary
        @Bean(name = JDBC_TEMPLATE)
        @ConditionalOnBean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = JDBC_TEMPLATE)
        public JdbcTemplate jdbcTemplate(@Qualifier(value = DATA_SOURCE) @Nonnull DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        /**
         * Returns a JDBC {@link org.springframework.transaction.TransactionManager} built from the qualified data source with the available transaction manager customizers applied
         *
         * @param builder the data source builder creating the transaction manager
         * @param dataSource the qualified data source managed by the transaction manager
         * @param customizers the provider of transaction manager customizers
         *
         * @return a JDBC {@link org.springframework.transaction.TransactionManager}
         */
        @Bean(name = TRANSACTION_MANAGER)
        @ConditionalOnBooleanProperty(prefix = PROPERTIES_PREFIX, name = "jdbc-transaction", matchIfMissing = true)
        @ConditionalOnBean(name = DATA_SOURCE, value = DataSourceBuilder.class)
        @ConditionalOnMissingBean(name = TRANSACTION_MANAGER)
        public TransactionManager transactionManager(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE) DataSource dataSource, @Nonnull ObjectProvider<TransactionManagerCustomizers> customizers) {
            return builder.jdbcTransactionManager(dataSource, customizers);
        }
    }
}
