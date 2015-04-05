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

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.util.TransactionManager;
import org.apache.torque.util.TransactionManagerImpl;
import org.exitcode.spring.torque.TorqueDelegatingDataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class SpringTransactionManagerAdapter implements TransactionManager {

    // original Torque transactional manager
    private final TransactionManager torqueTxManager = new TransactionManagerImpl();

    private final TorqueDelegatingDataSource springDataSource;

    public SpringTransactionManagerAdapter(TorqueDelegatingDataSource springDataSource) {
        this.springDataSource = springDataSource;
    }

    @Override
    public Connection begin() throws TorqueException {
        return begin(Torque.getDefaultDB());
    }

    @Override
    public Connection begin(String dbName) throws TorqueException {
        if (isSpringTxOpened()) {
            return getSpringTxConnection();
        }

        return torqueTxManager.begin(dbName);
    }

    @Override
    public void commit(Connection con) throws TorqueException {
        if (isSpringTxOpened()) {
            // ignore commit when spring transcation is opened, spring should do it
            return;
        }

        torqueTxManager.commit(con);
    }

    @Override
    public void rollback(Connection con) throws TorqueException {
        if (isSpringTxOpened()) {
            markTxForRollback();
            return;
        }

        torqueTxManager.rollback(con);
    }

    @Override
    public void safeRollback(Connection con) {
        if (isSpringTxOpened()) {
            markTxForRollback();
            return;
        }

        torqueTxManager.safeRollback(con);
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
