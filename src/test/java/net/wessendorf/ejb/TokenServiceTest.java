/**
 * Copyright (C) Matthias Weßendorf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wessendorf.ejb;

import net.wessendorf.ejb.entity.Token;
import net.wessendorf.ejb.service.TokenService;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

@RunWith(CdiTestRunner.class)
public class TokenServiceTest {

    @Inject
    private TokenService service;

    @Test
    public void addToken() throws Exception {
        Assert.assertTrue(service.addToken(new Token()).get());
    }

    @Test
    public void testLowNumberOfThreads() throws Exception {
        final int threads = 16;
        final int startItems = service.findAll().size();
        addConcurrentTokens(threads);
        assertEquals(threads + startItems, service.findAll().size());
    }

    @Test
    public void testHeighNumberOfThreads() throws Exception {
        final int threads = 64;
        final int startItems = service.findAll().size();
        addConcurrentTokens(threads);
        assertEquals(threads + startItems, service.findAll().size());
    }




    // =======================================================
    //                  Helper methods...
    // =======================================================


    private void addConcurrentTokens(final int threads) throws InterruptedException, ExecutionException {

        final AtomicBoolean outcome = new AtomicBoolean(true);
        List<Future<Boolean>> results = new ArrayList<Future<Boolean>>(threads);

        for (int i = 0; i < threads; i++) {
            final Token token = new Token();
            token.setValue(generateToken());

            results.add(service.addToken(token));
        }

        for (Future<Boolean> result : results) {
            if (!result.get()) {
                Assert.fail("test failed. Please check stacktrace(s)");
            }
        }
    }

    private String generateToken() {

        String raw = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        raw = raw.replaceAll("-","");

        return raw;
    }
}
