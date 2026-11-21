[English](README.md) | [簡體中文](README.zh-CN.md) | [繁體中文](README.zh-TW.md)

# 多資料來源 Spring Boot Starter

讓 Spring Boot 應用快速整合多個 `DataSource`。

## 快速開始

- 匯入相依性

```xml
    <dependency>
        <groupId>com.jiovan.spring.starter</groupId>
        <artifactId>multiple-datasource-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> 預設情況下，此 starter 會自動生效，你可以透過 `jiovan.multiple-datasource.enabled = false` 關閉它。

- 使用字首 `jiovan.multiple-datasource` 設定 Spring Boot 的 `application.yml`

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

> 此 starter 最多支援 3 個 `DataSource`。（三振出局）

- 將實體類與倉儲類放在以下包中（以 `primary` 為例）

    - 實體類：`**.domain.primary.rdbms`
    - 倉儲類：`**.repository.primary.rdbms`

- 透過 `@Autowired`/`@Resource` 註解並配合 `@Qualifier` 註解注入以下 Bean（以 `primary` 為例）

| Bean 型別 | 限定符 |
|------------------------|------------------------------------------------------------|
| DataSource | PrimaryDataSourceJdbcConfiguration.DATA_SOURCE |
| JdbcTemplate | PrimaryDataSourceJdbcConfiguration.JDBC_TEMPLATE |
| TransactionManager | PrimaryDataSourceJdbcConfiguration.TRANSACTION_MANAGER |
| PersistenceUnitManager | PrimaryDataSourceJpaConfiguration.PERSISTENCE_UNIT |
| EntityManager | PrimaryDataSourceJpaConfiguration.ENTITY_MANAGER |
| SqlSessionFactory | PrimaryDataSourceMybatisConfiguration.SQL_SESSION_FACTORY |
| SqlSessionTemplate | PrimaryDataSourceMybatisConfiguration.SQL_SESSION_TEMPLATE |

- 此 starter 支援全球主流資料來源連線池，包括
  - c3p0
  - dbcp2
  - druid
  - hikari
  - oracle ucp
  - tomcat
  - vibur

## 文件

- GitHub：https://github.com/jiovan-labs/multiple-datasource-spring-boot-starter

## 環境要求

- jdk 17+

## 許可證

本專案採用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 許可證。

請參閱 `NOTICE.txt` 檔案瞭解所需的宣告與歸屬資訊。

## 捐贈

喜歡這個工具包嗎？歡迎[向我們捐贈](https://jiovan.com/donation)以支援開發。

## 版權

北京玖漫科技有限公司

## 網站

- 玖漫：https://jiovan.com
