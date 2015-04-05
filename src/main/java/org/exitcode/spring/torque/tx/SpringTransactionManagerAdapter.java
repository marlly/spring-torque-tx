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
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Custom implementation of {@link TransactionManager} introduced in Torque 4.0. Once transaction above managed data
 * source is opened using Spring, all operations like begin or commit performed by Torque API are suppresed. Only
 * rollback will mark for rollback also running spring transaction. In that way, transaction boundaries are strictly
 * controlled by Spring. When no running spring transaction is detected, all calls are just forwarded to the original
 * {@code TransactionManager} {@link TransactionManagerImpl implementation}.
 * 
 * @author Marek Holly
 */
public class SpringTransactionManagerAdapter implements TransactionManager, InitializingBean {

    private static final Log LOG = LogFactory.getLog(SpringTransactionManagerAdapter.class);

    // original Torque transactional manager
    private final TransactionManager torqueTxManager = new TransactionManagerImpl();

    private TorqueDelegatingDataSource springDataSource;

    /**
     * Create new empty instance of {@code SpringTransactionManagerAdapter}.
     */
    public SpringTransactionManagerAdapter() {
    }

    /**
     * Create new instance of {@code SpringTransactionManagerAdapter} which will detect any spring transactions created
     * above given data source.
     * 
     * @param springDataSource spring managed data source
     */
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

    /**
     * Return data source scanned for running spring transactions.
     * 
     * @return managed spring data source
     */
    public TorqueDelegatingDataSource getSpringDataSource() {
        return springDataSource;
    }

    /**
     * Set managed spring data source which should be scanned for running spring transactions.
     * 
     * @param springDataSource managed spring data source
     */
    public void setSpringDataSource(TorqueDelegatingDataSource springDataSource) {
        this.springDataSource = springDataSource;
    }

    private boolean isSpringTxOpened() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    private Connection getSpringTxConnection() {
        Connection conn = ((ConnectionHolder) TransactionSynchronizationManager.getResource(springDataSource)).getConnection();
        if (conn == null) {
            throw new IllegalStateException("No connection associated with running transaction!");
        }

        return conn;
    }

    private void markTxForRollback() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
}
