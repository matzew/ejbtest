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
package net.wessendorf.ejb.dao;

import net.wessendorf.ejb.entity.Token;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class TokenDao {

    @PersistenceContext(unitName = "TestPU")
    private EntityManager entityManager;

    public void create(Token token) {
        entityManager.persist(token);
    }

    public List<Token> findAll() {
        return entityManager.createQuery("select t from "+Token.class.getSimpleName()+" t").getResultList();
    }
}
