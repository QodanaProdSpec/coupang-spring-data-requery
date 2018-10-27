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

package org.springframework.data.requery.repository.query;

import io.requery.query.Result;
import io.requery.query.Scalar;
import io.requery.query.Tuple;
import io.requery.query.element.QueryElement;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.requery.annotation.Query;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * {@link Query} annotation이 정의된 메소드, interface default method, custom defined method를 실행하는 {@link RepositoryQuery}
 *
 * @author debop
 * @since 18. 6. 15
 */
@Slf4j
public class DeclaredRequeryQuery extends AbstractRequeryQuery {

    public DeclaredRequeryQuery(@NotNull RequeryQueryMethod method,
                                @NotNull RequeryOperations operations) {
        super(method, operations);
    }

    @NotNull
    @Override
    protected QueryElement<? extends Result<?>> doCreateQuery(@NotNull final Object[] values) {
        throw new UnsupportedOperationException("Unsupported operation in DeclaredRequeryQuery is defined");
    }

    @NotNull
    @Override
    protected QueryElement<? extends Scalar<Integer>> doCreateCountQuery(@NotNull final Object[] values) {
        throw new UnsupportedOperationException("Unsupported operation in @Query is defined");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object execute(@NotNull final Object[] parameters) {

        Object resultSet = null;

        String query = getRawQuery();

        log.debug("Execute queryMethod={}, return type={}, query={}", getQueryMethod().getName(), getQueryMethod().getReturnType(), query);

        Result<?> result;

        // TODO: Entity 나 Tuple 에 대해 ReturnedType에 맞게 casting 해야 한다.
        // TODO: Paging 에 대해서는 처리했는데, Sort 는 넣지 못했음. 이 부분도 추가해야 함.

        RequeryParametersParameterAccessor accessor = new RequeryParametersParameterAccessor(getQueryMethod().getParameters(), parameters);
        Pageable pageable = accessor.getPageable();

        // FIXME: Spring @Transactional 하에서는 Raw Query에 대해 Count Query, Raw Query 둘 중 먼저 실행된 것만 제대로 값을 가져온다.
        // FIXME: 

        // 참고로 Query By Property 로 PagedExecution 에서는 제대로 수행된다.
        if (pageable.isPaged()) {

            int pageableIndex = accessor.getParameters().getPageableIndex();
            Object[] values = extractValues(pageableIndex, parameters);

            log.trace("values={}", values);

            // Content Query
            long totals = retrieveTotals(query, values);

            // Content query
            Result<?> contentResult = retrieveContents(query, pageable, values);

            if (getQueryMethod().isPageQuery()) {
                List<?> contents = contentResult.toList();
                log.trace("Page results. totals={}, contents={}, values={}", totals, contents, values);

                resultSet = new PageImpl(contents, pageable, totals);
            } else {
                resultSet = castResult(contentResult);
            }

        } else if (getQueryMethod().isQueryForEntity()) {
            log.debug("Query for entity. entity={}", getQueryMethod().getEntityInformation().getJavaType());
            result = operations.raw(getQueryMethod().getEntityInformation().getJavaType(), query, parameters);
            resultSet = castResult(result);
            result.close();
        } else {
            result = operations.raw(query, parameters);
            resultSet = castResult(result);
            result.close();
        }

        return resultSet;
    }

    @NotNull
    private Object[] extractValues(int pageableIndex, @NotNull final Object[] parameters) {

        Object[] values = (pageableIndex >= 0) ? new Object[parameters.length - 1] : parameters;

        int j = 0;
        for (int i = 0; i < parameters.length; i++) {
            if (i == pageableIndex) {
                continue;
            }
            values[j++] = parameters[i];
        }
        return values;
    }

    private Result<?> retrieveContents(final String baseQuery, Pageable pageable, final Object[] values) {
        long offset = pageable.getOffset();
        int limit = pageable.getPageSize();

        String query = baseQuery;
        if (offset > 0L) {
            query = query + " offset " + offset;
        }
        if (limit > 0) {
            query = query + " limit " + limit;
        }

        if (getQueryMethod().isQueryForEntity()) {
            log.trace("query for entity. {}", getQueryMethod().getEntityInformation().getJavaType());
            return operations.raw(getQueryMethod().getEntityInformation().getJavaType(), query, values);
        } else {
            log.trace("raw query for tuple. query={}, values={}", query, values);
            return operations.raw(query, values);
        }
    }

    private long retrieveTotals(final String query, final Object[] values) {
        // Count Query
        String countQuery = queryMethod.getCountQuery();
        if (countQuery == null) {
            countQuery = "select count(cnt_tbl.*) from (" + query + ") as cnt_tbl";
        }

        if (StringUtils.hasText(countQuery)) {
            try {
                Result<Tuple> result = operations.raw(countQuery, values);
                return result.first().get(0);
            } catch (Exception e) {
                log.error("Fail to retrieve count. query={}", query);
                return 0L;
            }
        }
        return 0L;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Object castResult(Result<?> result) {
        return castResult(result, Pageable.unpaged(), null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Object castResult(@NotNull final Result<?> result, @NotNull final Pageable pageable, final Long totals) {
        // TODO: List<Tuple> 인 경우 returned type 으로 변경해야 한다.

        if (getQueryMethod().isCollectionQuery()) {
            return result.toList();
        } else if (getQueryMethod().isStreamQuery()) {
            return result.stream();
        } else if (getQueryMethod().isPageQuery()) {
            List<?> contents = result.toList();
            if (pageable.isPaged()) {
                log.trace("Cast result to Page. totals={}, contents={}, contents size={}", totals, contents, contents.size());
                return new PageImpl<>(contents, pageable, totals);
            } else {
                return new PageImpl<>(result.toList());
            }
        } else {
            return RequeryResultConverter.convertResult(result.firstOrNull());
        }
    }

    @NotNull
    private String getRawQuery() {

        String rawQuery = getQueryMethod().getAnnotatedQuery();
        log.trace("Get raw query = {}", rawQuery);

        if (StringUtils.isEmpty(rawQuery)) {
            throw new IllegalStateException("No @Query query specified on " + queryMethod.getName());
        }

        return rawQuery;
    }

    @NotNull
    private ReturnedType getReturnedType(Object[] parameters) {
        RequeryParametersParameterAccessor accessor = new RequeryParametersParameterAccessor(queryMethod, parameters);
        ResultProcessor processor = getQueryMethod().getResultProcessor();

        return processor.withDynamicProjection(accessor).getReturnedType();
    }
}
