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
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.jiovan.plexus.spring.condition.annotation.ConditionalOnPropertyPrefix;
import com.jiovan.spring.starter.mybatisdelegator.composer.MybatisConfigurationDelegator;
import com.jiovan.spring.starter.mybatisdelegator.config.MybatisDelegatorAutoConfiguration;


/**
 * Tertiary datasource configuration for mybatis
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = "jiovan.multiple-datasource", name = "enabled", matchIfMissing = true)
@ConditionalOnPropertyPrefix(prefix = TertiaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX + ".mybatis")
@ConditionalOnClass(value = {DataSource.class, SqlSession.class, MybatisConfigurationDelegator.class})
@ConditionalOnBean(name = TertiaryDataSourceJdbcConfiguration.DATA_SOURCE)
@AutoConfigureAfter(value = {MybatisDelegatorAutoConfiguration.class, SecondaryDataSourceMybatisConfiguration.class, TertiaryDataSourceRepositoryConfiguration.class})
@Import(value = MybatisDelegatorAutoConfiguration.class)
public class TertiaryDataSourceMybatisConfiguration {
    public static final String MYBATIS_PROPERTIES = "tertiaryMybatisProperties";    // $NON-NLS-1$
    public static final String SQL_SESSION_FACTORY = "tertiarySqlSessionFactory";    // $NON-NLS-1$
    public static final String SQL_SESSION_TEMPLATE = "tertiarySqlSessionTemplate";    // $NON-NLS-1$

    /**
     * Returns a new {@link org.mybatis.spring.boot.autoconfigure.MybatisProperties} bound from the MyBatis properties prefix derived from {@code PROPERTIES_PREFIX}
     *
     * @return a new {@link org.mybatis.spring.boot.autoconfigure.MybatisProperties}
     */
    @Bean(name = MYBATIS_PROPERTIES)
    @ConditionalOnMissingBean(name = MYBATIS_PROPERTIES)
    @ConfigurationProperties(prefix = TertiaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX + ".mybatis")
    public MybatisProperties mybatisProperties() {
        return new MybatisProperties();
    }

    /**
     * Returns a {@link org.apache.ibatis.session.SqlSessionFactory} created by the delegator from the qualified data source and MyBatis properties
     *
     * @param delegator the MyBatis configuration delegator creating the factory
     * @param dataSource the qualified data source used by the factory
     * @param properties the MyBatis properties
     *
     * @return the {@link org.apache.ibatis.session.SqlSessionFactory}
     *
     * @throws Exception if the delegator fails to create the sql session factory
     */
    @Bean(name = SQL_SESSION_FACTORY)
    @ConditionalOnBean(name = MYBATIS_PROPERTIES, value = MybatisConfigurationDelegator.class)
    @ConditionalOnMissingBean(name = SQL_SESSION_FACTORY)
    public SqlSessionFactory sqlSessionFactory(@Nonnull MybatisConfigurationDelegator delegator,
        @Qualifier(value = TertiaryDataSourceJdbcConfiguration.DATA_SOURCE) @Nonnull DataSource dataSource,
        @Qualifier(value = MYBATIS_PROPERTIES) @Nonnull MybatisProperties properties) throws Exception {
        return delegator.sqlSessionFactory(dataSource, properties);
    }

    /**
     * Returns a {@link org.mybatis.spring.SqlSessionTemplate} created by the delegator from the given sql session factory and MyBatis properties
     *
     * @param delegator the MyBatis configuration delegator creating the template
     * @param factory the qualified sql session factory
     * @param properties the MyBatis properties
     *
     * @return the {@link org.mybatis.spring.SqlSessionTemplate}
     */
    @Bean(name = SQL_SESSION_TEMPLATE)
    @ConditionalOnBean(name = {SQL_SESSION_FACTORY, MYBATIS_PROPERTIES}, value = MybatisConfigurationDelegator.class)
    @ConditionalOnMissingBean(name = SQL_SESSION_TEMPLATE)
    public SqlSessionTemplate sqlSessionTemplate(@Nonnull MybatisConfigurationDelegator delegator,
        @Qualifier(value = SQL_SESSION_FACTORY) @Nonnull SqlSessionFactory factory,
        @Qualifier(value = MYBATIS_PROPERTIES) @Nonnull MybatisProperties properties) {
        return delegator.sqlSessionTemplate(factory, properties);
    }
}
