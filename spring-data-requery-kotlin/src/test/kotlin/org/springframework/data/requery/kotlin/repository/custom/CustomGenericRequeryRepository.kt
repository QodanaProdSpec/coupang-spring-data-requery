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

package org.springframework.data.requery.kotlin.repository.custom

import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.repository.support.RequeryEntityInformation
import org.springframework.data.requery.kotlin.repository.support.SimpleRequeryRepository

/**
 * org.springframework.data.requery.repository.custom.CustomGenericRequeryRepository
 *
 * @author debop
 */
class CustomGenericRequeryRepository<E : Any, ID : Any>(entityInformation: RequeryEntityInformation<E, ID>,
                                                        operations: RequeryOperations)
    : SimpleRequeryRepository<E, ID>(entityInformation, operations), CustomGenericRepository<E, ID> {

    override fun customMethod(id: ID): E {
        throw UnsupportedOperationException("Forced exception for testing purposes")
    }
}