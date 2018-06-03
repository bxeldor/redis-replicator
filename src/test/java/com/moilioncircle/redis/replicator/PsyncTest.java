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

package com.moilioncircle.redis.replicator;

import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.cmd.CommandListener;
import com.moilioncircle.redis.replicator.cmd.CommandName;
import com.moilioncircle.redis.replicator.cmd.impl.SetCommand;
import com.moilioncircle.redis.replicator.rdb.AuxFieldListener;
import com.moilioncircle.redis.replicator.rdb.RdbListener;
import com.moilioncircle.redis.replicator.rdb.datatype.AuxField;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class PsyncTest {

    @Test
    public void psync() throws IOException {

        final Configuration configuration = Configuration.defaultSetting().
                setAuthPassword("test").
                setConnectionTimeout(3000).
                setReadTimeout(3000).
                setBufferSize(64).
                setAsyncCachedBytes(0).
                setHeartBeatPeriod(200).
                setReceiveBufferSize(0).
                setSendBufferSize(0).
                setRetryTimeInterval(1000).
                setUseDefaultExceptionListener(false);
        System.out.println(configuration);
        @SuppressWarnings("resource")
        Replicator replicator = new TestRedisSocketReplicator("127.0.0.1", 6380, configuration);
        final AtomicBoolean flag = new AtomicBoolean(false);
        final Set<AuxField> set = new LinkedHashSet<>();
        replicator.addAuxFieldListener(new AuxFieldListener() {
            @Override
            public void handle(Replicator replicator, AuxField auxField) {
                set.add(auxField);
            }
        });
        replicator.addRdbListener(new RdbListener() {
            @Override
            public void preFullSync(Replicator replicator) {
        
            }
    
            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
        
            }
    
            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                if (flag.compareAndSet(false, true)) {
                    Thread thread = new Thread(new JRun());
                    thread.setDaemon(true);
                    thread.start();
                    replicator.removeCommandParser(CommandName.name("PING"));
                }
            }
        });
        final AtomicInteger acc = new AtomicInteger();
        replicator.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                if (command instanceof SetCommand && ((SetCommand) command).getKey().startsWith("psync")) {
                    SetCommand setCommand = (SetCommand) command;
                    Integer.parseInt(setCommand.getKey().split(" ")[1]); // num
                    acc.incrementAndGet();
                    if (acc.get() == 200) {
                        System.out.println("close for psync");
                        //close current process port;
                        //that will auto trigger psync command
                        close(replicator);
                    }
                    if (acc.get() == 980) {
                        configuration.setVerbose(true);
                    }
                    if (acc.get() == 1000) {
                        try {
                            replicator.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        });
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                System.out.println("psync closed");
            }
        });
        replicator.open();
        assertEquals(1000, acc.get());
        for (AuxField auxField : set) {
            System.out.println(auxField.getAuxKey() + "=" + auxField.getAuxValue());
        }
    }

    private static void close(Replicator replicator) {
        try {
            ((TestRedisSocketReplicator) replicator).getOutputStream().close();
        } catch (IOException e) {
        }
        try {
            ((TestRedisSocketReplicator) replicator).getInputStream().close();
        } catch (IOException e) {
        }
        try {
            ((TestRedisSocketReplicator) replicator).getSocket().close();
        } catch (IOException e) {
        }
    }

    private static class JRun implements Runnable {

        @Override
        public void run() {
            System.out.println("start jedis insert");
            Jedis jedis = new Jedis("127.0.0.1", 6380);
            jedis.auth("test");
            for (int i = 0; i < 1000; i++) {
                jedis.set("psync " + i, "psync" + i);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
            jedis.close();
            System.out.println("stop jedis insert");
        }
    }

    private static class TestRedisSocketReplicator extends RedisSocketReplicator {

        public TestRedisSocketReplicator(String host, int port, Configuration configuration) {
            super(host, port, configuration);
        }

        public Socket getSocket() {
            return super.socket;
        }

        public InputStream getInputStream() {
            return super.inputStream;
        }

        public OutputStream getOutputStream() {
            return super.outputStream;
        }
    }

}
