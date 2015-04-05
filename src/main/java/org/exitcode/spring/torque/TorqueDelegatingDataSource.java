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
package org.exitcode.spring.torque;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.springframework.jdbc.datasource.AbstractDataSource;

public class TorqueDelegatingDataSource extends AbstractDataSource {

    private static final Log LOG = LogFactory.getLog(TorqueDelegatingDataSource.class);

    private final String databaseName;

    public TorqueDelegatingDataSource(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public Connection getConnection() throws SQLException {
        LOG.debug("getConnection(), db: " + databaseName);
        try {
            return Torque.getConnection(databaseName);
        } catch (TorqueException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("This operation is not supported!");
    }
}
