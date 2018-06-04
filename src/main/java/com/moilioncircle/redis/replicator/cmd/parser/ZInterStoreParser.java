/*
 * Copyright 2016-2018 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moilioncircle.redis.replicator.cmd.parser;

import com.moilioncircle.redis.replicator.cmd.CommandParser;
import com.moilioncircle.redis.replicator.cmd.impl.AggregateType;
import com.moilioncircle.redis.replicator.cmd.impl.ZInterStoreCommand;

import static com.moilioncircle.redis.replicator.cmd.parser.CommandParsers.eq;
import static com.moilioncircle.redis.replicator.cmd.parser.CommandParsers.toBytes;
import static com.moilioncircle.redis.replicator.cmd.parser.CommandParsers.toDouble;
import static com.moilioncircle.redis.replicator.cmd.parser.CommandParsers.toInt;
import static com.moilioncircle.redis.replicator.cmd.parser.CommandParsers.toRune;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class ZInterStoreParser implements CommandParser<ZInterStoreCommand> {
    @Override
    public ZInterStoreCommand parse(Object[] command) {
        int idx = 1;
        AggregateType aggregateType = null;
        String destination = toRune(command[idx]);
        byte[] rawDestination = toBytes(command[idx]);
        idx++;
        int numkeys = toInt(command[idx++]);
        String[] keys = new String[numkeys];
        byte[][] rawKeys = new byte[numkeys][];
        for (int i = 0; i < numkeys; i++) {
            keys[i] = toRune(command[idx]);
            rawKeys[i] = toBytes(command[idx]);
            idx++;
        }
        double[] weights = null;
        while (idx < command.length) {
            String param = toRune(command[idx]);
            if (eq(param, "WEIGHTS")) {
                idx++;
                weights = new double[numkeys];
                for (int i = 0; i < numkeys; i++) {
                    weights[i] = toDouble(command[idx++]);
                }
            }
            if (eq(param, "AGGREGATE")) {
                idx++;
                String next = toRune(command[idx++]);
                if (eq(next, "SUM")) {
                    aggregateType = AggregateType.SUM;
                } else if (eq(next, "MIN")) {
                    aggregateType = AggregateType.MIN;
                } else if (eq(next, "MAX")) {
                    aggregateType = AggregateType.MAX;
                }
            }
        }
        return new ZInterStoreCommand(destination, numkeys, keys, weights, aggregateType, rawDestination, rawKeys);
    }

}
