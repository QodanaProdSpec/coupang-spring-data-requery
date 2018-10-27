/*
 * Copyright 2018 Coupang Ltd.
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

package org.springframework.data.requery.core;

import io.requery.TransactionIsolation;
import io.requery.meta.Attribute;
import io.requery.meta.EntityModel;
import io.requery.meta.QueryAttribute;
import io.requery.query.Deletion;
import io.requery.query.Expression;
import io.requery.query.InsertInto;
import io.requery.query.Insertion;
import io.requery.query.Result;
import io.requery.query.Scalar;
import io.requery.query.Selection;
import io.requery.query.Tuple;
import io.requery.query.Update;
import io.requery.query.element.QueryElement;
import io.requery.query.function.Count;
import io.requery.sql.EntityContext;
import io.requery.sql.EntityDataStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.requery.mapping.RequeryMappingContext;
import org.springframework.data.requery.utils.Iterables;
import org.springframework.data.requery.utils.RequeryUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.springframework.data.requery.utils.RequeryUtils.unwrap;

/**
 * Java용 RequeryOperations
 *
 * @author debop
 * @since 18. 6. 4
 */
@ParametersAreNonnullByDefault
@Transactional(readOnly = true)
public interface RequeryOperations {

    @NotNull
    EntityDataStore<Object> getDataStore();

    @NotNull
    RequeryMappingContext getMappingContext();

    default EntityModel getEntityModel() {
        return RequeryUtils.getEntityModel(getDataStore());
    }

    @SuppressWarnings("unchecked")
    default <E> EntityContext<E> getEntityContext() {
        return RequeryUtils.getEntityContext(getDataStore());
    }

    default <E> Selection<? extends Result<E>> select(@NotNull final Class<E> entityType) {
        return getDataStore().select(entityType);
    }

    default <E> Selection<? extends Result<E>> select(@NotNull final Class<E> entityType,
                                                      @NotNull final QueryAttribute<?, ?>... attributes) {
        return getDataStore().select(entityType, attributes);
    }

    default Selection<? extends Result<Tuple>> select(@NotNull final Expression<?>... expressions) {
        return getDataStore().select(expressions);
    }

    default <E, K> E findById(@NotNull final Class<E> entityType, @NotNull final K id) {
        return getDataStore().findByKey(entityType, id);
    }

    default <E> List<E> findAll(@NotNull final Class<E> entityType) {
        return getDataStore().select(entityType).get().toList();
    }

    default <E> E refresh(@NotNull final E entity) {
        return getDataStore().refresh(entity);
    }

    @SuppressWarnings("UnusedReturnValue")
    default <E> E refresh(@NotNull final E entity, @Nullable final Attribute<?, ?>... attributes) {
        return getDataStore().refresh(entity, attributes);
    }

    default <E> List<E> refresh(@NotNull final Iterable<E> entities, final Attribute<?, ?>... attributes) {
        return Iterables.toList(getDataStore().refresh(entities, attributes));
    }

    default <E> E refreshAllProperties(@NotNull final E entity) {
        return getDataStore().refreshAll(entity);
    }

    @Transactional
    default <E> E upsert(@NotNull final E entity) {
        return getDataStore().upsert(entity);
    }

    @Transactional
    default <E> List<E> upsertAll(@NotNull final Iterable<E> entities) {
        return Iterables.toList(getDataStore().upsert(entities));
    }

    @Transactional
    default <E> E insert(@NotNull final E entity) {
        return getDataStore().insert(entity);
    }

    @Transactional
    default <E, K> K insert(@NotNull final E entity, @NotNull final Class<K> keyClass) {
        return getDataStore().insert(entity, keyClass);
    }

    default <E> Insertion<? extends Result<Tuple>> insert(@NotNull final Class<E> entityType) {
        return getDataStore().insert(entityType);
    }

    default <E> InsertInto<? extends Result<Tuple>> insert(@NotNull final Class<E> entityType, QueryAttribute<E, ?>... attributes) {
        return getDataStore().insert(entityType, attributes);
    }

    @Transactional
    default <E> List<E> insertAll(@NotNull final Iterable<E> entities) {
        return Iterables.toList(getDataStore().insert(entities));
    }

    @Transactional
    default <E, K> List<K> insertAll(@NotNull final Iterable<E> entities, @NotNull final Class<K> keyClass) {
        return Iterables.toList(getDataStore().insert(entities, keyClass));
    }

    @NotNull
    default Update<? extends Scalar<Integer>> update() {
        return getDataStore().update();
    }

    @Transactional
    default <E> E update(@NotNull final E entity) {
        return getDataStore().update(entity);
    }

    @Transactional
    default <E> E update(@NotNull final E entity, final Attribute<?, ?>... attributes) {
        return getDataStore().update(entity, attributes);
    }

    @Transactional
    default <E> Update<? extends Scalar<Integer>> update(@NotNull final Class<E> entityType) {
        return getDataStore().update(entityType);
    }

    @Transactional
    default <E> List<E> updateAll(@NotNull final Iterable<E> entities) {
        return Iterables.toList(getDataStore().update(entities));
    }

    default Deletion<? extends Scalar<Integer>> delete() {
        return getDataStore().delete();
    }

    @Transactional
    default <E> Deletion<? extends Scalar<Integer>> delete(@NotNull final Class<E> entityType) {
        return getDataStore().delete(entityType);
    }

    @Transactional
    default <E> void delete(@NotNull final E entity) {
        getDataStore().delete(entity);
    }

    @Transactional
    default <E> void deleteAll(@NotNull final Iterable<E> entities) {
        getDataStore().delete(entities);
    }

    @Transactional
    default <E> Integer deleteAll(@NotNull final Class<E> entityType) {
        return getDataStore().delete(entityType).get().value();
    }

    default <E> Selection<? extends Scalar<Integer>> count(@NotNull final Class<E> entityType) {
        return getDataStore().count(entityType);
    }

    default Selection<? extends Scalar<Integer>> count(final QueryAttribute<?, ?>... attributes) {
        return getDataStore().count(attributes);
    }

    @SuppressWarnings("unchecked")
    default <E> int count(@NotNull final Class<E> entityType,
                          @NotNull final QueryElement<? extends Result<E>> whereClause) {
        QueryElement<?> query = RequeryUtils.applyWhereClause(unwrap(select(Count.count(entityType))), whereClause.getWhereElements());
        Tuple tuple = ((QueryElement<? extends Result<Tuple>>) query).get().first();
        return tuple.<Integer>get(0);
    }

    default <E> boolean exists(@NotNull final Class<E> entityType,
                               @NotNull final QueryElement<? extends Result<E>> whereClause) {
        return whereClause.limit(1).get().firstOrNull() != null;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    default Result<Tuple> raw(@NotNull final String query, final Object... parameters) {
        return getDataStore().raw(query, parameters);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    default <E> Result<E> raw(@NotNull final Class<E> entityType,
                              @NotNull final String query,
                              final Object... parameters) {
        return getDataStore().raw(entityType, query, parameters);
    }

    default <V> V runInTransaction(@NotNull final Callable<V> callable) {
        return runInTransaction(callable, null);
    }

    <V> V runInTransaction(@NotNull final Callable<V> callable, @Nullable final TransactionIsolation isolation);

    default <V> V withTransaction(@NotNull final Function<EntityDataStore<Object>, V> block) {
        return withTransaction(block, null);
    }

    <V> V withTransaction(@NotNull final Function<EntityDataStore<Object>, V> block, @Nullable final TransactionIsolation isolation);
}
