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

package org.springframework.data.requery.kotlin.converters

import io.requery.Converter

/**
 * [List<Int>] 를 String 으로 저장하는 [Converter] 입니다.
 *
 * @author debop
 * @since 18. 7. 2
 */
class IntArrayListToStringConverter : Converter<ArrayList<Int>, String> {

    override fun getPersistedType(): Class<String> = String::class.java

    @Suppress("UNCHECKED_CAST")
    override fun getMappedType(): Class<ArrayList<Int>> = ArrayList::class.java as Class<ArrayList<Int>>

    override fun getPersistedSize(): Int? = null

    override fun convertToMapped(type: Class<out ArrayList<Int>>?, value: String?): ArrayList<Int>? {
        return value?.let {
            val items = it.split(",")
            items.map { it.toInt() }.toMutableList() as ArrayList<Int>
        }
    }

    override fun convertToPersisted(value: ArrayList<Int>?): String? {
        return value?.joinToString(",") { it.toString() }
    }
}