[English](README.md) | [简体中文](README.zh-CN.md) | [繁體中文](README.zh-TW.md)

# 多数据源 Spring Boot Starter

让 Spring Boot 应用快速集成多个 `DataSource`。

## 快速开始

- 导入依赖

```xml
    <dependency>
        <groupId>com.jiovan.spring.starter</groupId>
        <artifactId>multiple-datasource-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> 默认情况下，此 starter 会自动生效，你可以通过 `jiovan.multiple-datasource.enabled = false` 关闭它。

- 使用前缀 `jiovan.multiple-datasource` 配置 Spring Boot 的 `application.yml`

```yml
jiovan:
    multiple-datasource:
        primary:
            url: 'jdbc:mysql://127.0.0.1:3306/test_db1'
            driver-class-name: com.mysql.cj.jdbc.Driver
            type: com.zaxxer.hikari.HikariDataSource
            jpa-enabled: true
            repository-enabled: true
            mybatis:
            xa:
        secondary:
            url: 'jdbc:mysql://127.0.0.1:3306/test_db2'
            driver-class-name: com.mysql.cj.jdbc.Driver
            type: com.mchange.v2.c3p0.ComboPooledDataSource
            jpa-enabled: true
            repository-enabled: true
            mybatis:
            xa:
        tertiary:
            url: 'jdbc:mysql://127.0.0.1:3306/test_db3'
            driver-class-name: com.mysql.cj.jdbc.Driver
            type: org.apache.commons.dbcp2.BasicDataSource
            jpa-enabled: true
            repository-enabled: true
            mybatis:
            xa:
```

> 此 starter 最多支持 3 个 `DataSource`。（三振出局）

- 将实体类与仓储类放在以下包中（以 `primary` 为例）

    - 实体类：`**.domain.primary.rdbms`
    - 仓储类：`**.repository.primary.rdbms`

- 通过 `@Autowired`/`@Resource` 注解并配合 `@Qualifier` 注解注入以下 Bean（以 `primary` 为例）

| Bean 类型 | 限定符 |
|------------------------|------------------------------------------------------------|
| DataSource | PrimaryDataSourceJdbcConfiguration.DATA_SOURCE |
| JdbcTemplate | PrimaryDataSourceJdbcConfiguration.JDBC_TEMPLATE |
| TransactionManager | PrimaryDataSourceJdbcConfiguration.TRANSACTION_MANAGER |
| PersistenceUnitManager | PrimaryDataSourceJpaConfiguration.PERSISTENCE_UNIT |
| EntityManager | PrimaryDataSourceJpaConfiguration.ENTITY_MANAGER |
| SqlSessionFactory | PrimaryDataSourceMybatisConfiguration.SQL_SESSION_FACTORY |
| SqlSessionTemplate | PrimaryDataSourceMybatisConfiguration.SQL_SESSION_TEMPLATE |

- 此 starter 支持全球主流数据源连接池，包括
  - c3p0
  - dbcp2
  - druid
  - hikari
  - oracle ucp
  - tomcat
  - vibur

## 文档

- GitHub：https://github.com/jiovan-labs/multiple-datasource-spring-boot-starter

## 环境要求

- jdk 17+

## 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

请参阅 `NOTICE.txt` 文件了解所需的声明与归属信息。

## 捐赠

喜欢这个工具包吗？欢迎[向我们捐赠](https://jiovan.com/donation)以支持开发。

## 版权

北京玖漫科技有限公司

## 网站

- 玖漫：https://jiovan.com
