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

package org.springframework.data.requery.kotlin.coroutines

import io.requery.TransactionIsolation
import io.requery.sql.KotlinEntityDataStore
import mu.KotlinLogging

/**
 * [CoroutineRequeryOperations]의 구현체 입니다.
 *
 * @author debop
 * @since 18. 6. 2
 */
class CoroutineRequeryTemplate(override val dataStore: KotlinEntityDataStore<Any>) : CoroutineRequeryOperations {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    /**
     * Transaction 환경 하에서 지정된 block을 실행합니다.
     * @param T
     * @param isolation transaction isolation level
     * @param block data operation
     */
    override suspend fun <T : Any> withTransaction(isolation: TransactionIsolation?,
                                                   block: CoroutineRequeryOperations.() -> T): T {
        //        return isolation?.let {
        //            dataStore.withTransaction(isolation) {
        //                block.invoke(this@CoroutineRequeryTemplate)
        //            }
        //        } ?: dataStore.withTransaction { block.invoke(this@CoroutineRequeryTemplate) }
        val operations = this@CoroutineRequeryTemplate

        log.trace { "Execution in transaction. isolation=$isolation" }
        return if(isolation == null) {
            dataStore.withTransaction { block.invoke(operations) }
        } else {
            dataStore.withTransaction(isolation) {
                block.invoke(operations)
            }
        }
    }
}