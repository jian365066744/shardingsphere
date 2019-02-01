/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import org.apache.shardingsphere.core.merger.MergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowDatabasesMergedResult;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.FieldCountPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryResponsePackets;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Show databases backend handler.
 *
 * @author chenqingyang
 * @author zhaojun
 */
public final class ShowDatabasesBackendHandler implements TextProtocolBackendHandler {
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    private int columnCount;
    
    private final List<Integer> columnTypes = new LinkedList<>();
    
    @Override
    public CommandResponsePackets execute() {
        mergedResult = new ShowDatabasesMergedResult(GlobalRegistry.getInstance().getSchemaNames());
        int sequenceId = 0;
        FieldCountPacket fieldCountPacket = new FieldCountPacket(++sequenceId, 1);
        Collection<ColumnDefinition41Packet> columnDefinition41Packets = new ArrayList<>(1);
        columnDefinition41Packets.add(new ColumnDefinition41Packet(++sequenceId, "", "", "", "Database", "", 100, ColumnType.MYSQL_TYPE_VARCHAR, 0));
        QueryResponsePackets result = new QueryResponsePackets(Collections.singletonList(Types.VARCHAR), fieldCountPacket, columnDefinition41Packets, ++sequenceId);
        currentSequenceId = result.getPackets().size();
        columnCount = result.getColumnCount();
        columnTypes.addAll(result.getColumnTypes());
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        List<Object> data = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            data.add(mergedResult.getValue(columnIndex, Object.class));
        }
        return new ResultPacket(++currentSequenceId, data, columnCount, columnTypes);
    }
}
