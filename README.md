#Spring Torque Tx

This library provides lightweight integration of Apache Torque 4.x mapper with Spring Transaction Management. Using it you can take advantage of powerfull transaction support in Spring framework with Torque as an underlying persistent technology. Currently library supports transaction handling only over one data source at a time. 

## Basic principle
Apache Torque 4.0 introduced new `TransactionManager` interface which allows to delegate transaction handling to
external framework like Spring. `SpringTransactionManagerAdapter` is such an implementation. Once it detects running spring transaction upon managed data source, it automatically suppres Torque's own transaction handling. That way transaction
boundaries are strictly controlled by Spring. Only rollback raised through Torque API will mark spring transaction for rollback as well.

![Overview][overview]

In order to create transaction, transaction manager in Spring must be able to obtain JDBC connection first. Using Torque, database settings like server, database name, credentials etc. are commonly specified in `Torque.properties`. For that reason `TorqueDelegatingDataSource` has been invented. It allows to retrieve database connections through Torque API.

## Code examples

Following beans needs to registered in Spring container in order to use Spring Tx management with Torque:

* `TorqueDelegatingDataSource`
* `SpringTransactionManagerAdapter`
* `DataSourceTransactionManager`

Once you have registered them, you can simply initialize Torque by:

```java
Torque.init(torqueProperties);
Transaction.setTransactionManager(torqueTxManager);
```

Afterwards you can use declarative transaction management or Spring Transaction API programmaticaly to designate code, that needs to be invoked within transaction. For instance (declarative approach):

```java
@Transactional
public void create()
{  
}
```

Below, you can find examples of standard Spring XML configuration or newer approach with JavaConfig (since Spring 3).

### XML configuration

```xml
<bean id="dataSource" class="org.exitcode.spring.torque.TorqueDelegatingDataSource">
	<property name="databaseName">
		<bean class="org.apache.torque.Torque" factory-method="getDefaultDB" />
	</property>
</bean>

<bean id="torqueTxManager" class="org.exitcode.spring.torque.tx.SpringTransactionManagerAdapter">
	<property name="springDataSource" ref="dataSource" />
</bean>

<bean id="springTxManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
	<property name="dataSource" ref="dataSource" />
</bean>

<tx:annotation-driven transaction-manager="springTxManager" />
```

### JavaConfig

```java
@Configuration
@EnableTransactionManagement
public class Config
{
    @Bean
    public DataSource dataSource()
    {
        return new TorqueDelegatingDataSource(Torque.getDefaultDB());
    }

    @Bean
    public TransactionManager torqueTxManager()
    {
        return new SpringTransactionManagerAdapter(dataSource());
    }

    @Bean
    public PlatformTransactionManager springTxManager()
    {
        return new DataSourceTransactionManager(dataSource());
    }
}
```

## License
The Spring Torque Tx libray is released under version 2.0 of the [Apache License][].

[overview]: src/doc/overview.png
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0




  




