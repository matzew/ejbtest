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
package net.wessendorf.ejb.service;

import net.wessendorf.ejb.dao.TokenDao;
import net.wessendorf.ejb.entity.Token;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Stateless
public class TokenService
{

    private static Logger logger = Logger.getLogger(TokenService.class.getSimpleName());

    @Inject
    private TokenDao dao;

    @Asynchronous
    public Future<Boolean> addToken(Token token) {

        logger.info("Thread ==> " + Thread.currentThread().getName());

        try  {
            dao.create(token);
        }
        catch (final Exception e) {
            e.printStackTrace();
            return new AsyncResult<Boolean>(Boolean.FALSE);
        }
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }

    public List<Token> findAll() {
        return dao.findAll();
    }
}
