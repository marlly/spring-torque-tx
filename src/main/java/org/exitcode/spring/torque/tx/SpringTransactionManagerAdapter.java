/*
 * Copyright 2015 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exitcode.spring.torque.tx;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.util.TransactionManager;
import org.apache.torque.util.TransactionManagerImpl;
import org.exitcode.spring.torque.TorqueDelegatingDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

public class SpringTransactionManagerAdapter implements TransactionManager, InitializingBean {

    private static final Log LOG = LogFactory.getLog(SpringTransactionManagerAdapter.class);

    // original Torque transactional manager
    private final TransactionManager torqueTxManager = new TransactionManagerImpl();

    private TorqueDelegatingDataSource springDataSource;

    public SpringTransactionManagerAdapter() {
    }

    public SpringTransactionManagerAdapter(TorqueDelegatingDataSource springDataSource) {
        this.springDataSource = springDataSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(springDataSource, "No spring managed data source has been specified!");
    }

    @Override
    public Connection begin() throws TorqueException {
        return begin(Torque.getDefaultDB());
    }

    @Override
    public Connection begin(String dbName) throws TorqueException {
        LOG.debug("Begin tx on database: " + dbName);
        if (isSpringTxOpened()) {
            LOG.info("Running spring transaction detected. Returning connection participating in transaction...");
            return getSpringTxConnection();
        }

        return torqueTxManager.begin(dbName);
    }

    @Override
    public void commit(Connection con) throws TorqueException {
        LOG.debug("Commit tx");
        if (isSpringTxOpened()) {
            LOG.info("Running Spring transaction detected. Commit is ignored since tx boundaries are controlled by Spring itself.");
            // ignore commit when spring transcation is opened, spring should do it
            return;
        }

        torqueTxManager.commit(con);
    }

    @Override
    public void rollback(Connection con) throws TorqueException {
        LOG.debug("Rollback tx");
        if (isSpringTxOpened()) {
            LOG.info("Running Spring transaction detected. Transaction is marked for rollback.");
            markTxForRollback();
            return;
        }

        torqueTxManager.rollback(con);
    }

    @Override
    public void safeRollback(Connection con) {
        LOG.debug("SafeRollback tx");
        if (isSpringTxOpened()) {
            LOG.info("Running Spring transaction detected. Transaction is marked for rollback.");
            markTxForRollback();
            return;
        }

        torqueTxManager.safeRollback(con);
    }

    public TorqueDelegatingDataSource getSpringDataSource() {
        return springDataSource;
    }

    public void setSpringDataSource(TorqueDelegatingDataSource springDataSource) {
        this.springDataSource = springDataSource;
    }

    private boolean isSpringTxOpened() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    private Connection getSpringTxConnection() {
        return DataSourceUtils.getConnection(springDataSource);
    }

    private void markTxForRollback() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
}
