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
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.XADataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.alibaba.druid.pool.DruidDataSource;
import com.jiovan.plexus.spring.condition.annotation.ConditionalOnAnyProperties;
import com.jiovan.spring.starter.multipledatasource.druid.DruidFilterConfiguration;
import com.jiovan.spring.starter.multipledatasource.druid.DruidSpringAopConfiguration;
import com.jiovan.spring.starter.multipledatasource.druid.DruidStatProperties;
import com.jiovan.spring.starter.multipledatasource.druid.DruidStatViewServletConfiguration;
import com.jiovan.spring.starter.multipledatasource.druid.DruidWebStatFilterConfiguration;


/**
 * Pre-Configuration for druid datasource
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = "jiovan.multiple-datasource", name = "enabled", matchIfMissing = true)
@ConditionalOnAnyProperties(value = {
    @ConditionalOnProperty(prefix = "jiovan.multiple-datasource.primary", name = "type", havingValue = "com.alibaba.druid.pool.DruidDataSource", matchIfMissing = true),
    @ConditionalOnProperty(prefix = "jiovan.multiple-datasource.secondary", name = "type", havingValue = "com.alibaba.druid.pool.DruidDataSource", matchIfMissing = true),
    @ConditionalOnProperty(prefix = "jiovan.multiple-datasource.tertiary", name = "type", havingValue = "com.alibaba.druid.pool.DruidDataSource", matchIfMissing = true)
})
@ConditionalOnClass(value = {DataSource.class, DruidDataSource.class})
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@EnableConfigurationProperties(value = DruidStatProperties.class)
@Import(value = {DruidSpringAopConfiguration.class, DruidStatViewServletConfiguration.class, DruidWebStatFilterConfiguration.class, DruidFilterConfiguration.class})
public class DruidDataSourcePreConfiguration {
}
