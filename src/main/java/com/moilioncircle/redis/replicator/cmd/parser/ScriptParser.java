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
import com.moilioncircle.redis.replicator.cmd.impl.ScriptCommand;
import com.moilioncircle.redis.replicator.cmd.impl.ScriptFlushCommand;
import com.moilioncircle.redis.replicator.cmd.impl.ScriptLoadCommand;

import static com.moilioncircle.redis.replicator.cmd.parser.CommandParsers.eq;
import static com.moilioncircle.redis.replicator.cmd.parser.CommandParsers.toBytes;
import static com.moilioncircle.redis.replicator.cmd.parser.CommandParsers.toRune;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class ScriptParser implements CommandParser<ScriptCommand> {
    @Override
    public ScriptCommand parse(Object[] command) {
        int idx = 1;
        String keyword = toRune(command[idx++]);
        if (eq(keyword, "LOAD")) {
            String script = toRune(command[idx]);
            byte[] rawScript = toBytes(command[idx]);
            idx++;
            return new ScriptLoadCommand(script, rawScript);
        } else if (eq(keyword, "FLUSH")) {
            return new ScriptFlushCommand();
        }
        throw new AssertionError("SCRIPT " + keyword);
    }


}
