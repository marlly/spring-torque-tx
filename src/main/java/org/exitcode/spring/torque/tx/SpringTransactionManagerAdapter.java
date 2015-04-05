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

import org.apache.torque.TorqueException;
import org.apache.torque.util.TransactionManager;

public class SpringTransactionManagerAdapter implements TransactionManager {

    @Override
    public Connection begin() throws TorqueException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Connection begin(String dbName) throws TorqueException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void commit(Connection con) throws TorqueException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rollback(Connection con) throws TorqueException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void safeRollback(Connection con) {
        // TODO Auto-generated method stub
        
    }
}
