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

import net.wessendorf.ejb.dao.TokenDao;
import net.wessendorf.ejb.entity.Token;
import net.wessendorf.ejb.service.TokenService;
import net.wessendorf.ejb.service.TokenServiceImpl;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class TokenServiceTest {

    @Inject
    private TokenService service;

    @Test
    public void addToken() {
        service.addToken(new Token());
    }

    @Test
    public void testLowNumberOfThreads() throws InterruptedException {
        final int threads = 16;
        addConcurrentTokens(threads);
        assertEquals(threads, service.findAll().size());
    }

    @Test
    public void testHeighNumberOfThreads() throws InterruptedException {
        final int threads = 64;
        addConcurrentTokens(threads);
        assertEquals(threads, service.findAll().size());
    }

    // =======================================================
    //                  OpenEJB bootstrap code
    // =======================================================

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(TokenDao.class);
        beans.addManagedClass(TokenServiceImpl.class);

        return beans;
    }

    @Module
    public Class<?>[] produceTestEntityManager() throws Exception {
        return new Class<?>[] { EntityManagerProducer.class};
    }

    /**
     * Static class to have OpenEJB produce/lookup a test EntityManager.
     */
    @RequestScoped
    @Stateful
    public static class EntityManagerProducer implements Serializable {

        {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("TestPU");
            entityManager = emf.createEntityManager();
        }

        private static EntityManager entityManager;

        @Produces
        public EntityManager produceEm() {

            if (! entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            return entityManager;
        }

        @PreDestroy
        public void closeEntityManager() {
            if (entityManager.isOpen()) {
                entityManager.getTransaction().commit();
                entityManager.close();
            }
        }
    }


    // =======================================================
    //                  Helper methods...
    // =======================================================


    private void addConcurrentTokens(final int threads) throws InterruptedException {

        final AtomicBoolean outcome = new AtomicBoolean(true);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();

                        try {

                            final Token token = new Token();
                            token.setValue(generateToken());

                            service.addToken(token);

                        } catch (final Exception e) {
                            e.printStackTrace();
                            outcome.compareAndSet(true, false);
                        } finally {
                            endLatch.countDown();
                        }
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }


                }
            }).start();
        }
        startLatch.countDown();
        endLatch.await();
        if (!outcome.get()) {
            Assert.fail("test failed. Please check stacktrace(s)");
        }
    }

    private String generateToken() {

        String raw = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        raw = raw.replaceAll("-","");

        return raw;
    }
}
