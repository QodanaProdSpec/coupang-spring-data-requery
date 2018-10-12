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

package org.springframework.data.requery.kotlin.configs

import io.requery.meta.EntityModel
import io.requery.sql.TableCreationMode
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.requery.kotlin.domain.Models
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * RequeryTestConfiguration
 *
 * @author debop
 */
@Configuration
@EnableTransactionManagement
class RequeryTestConfiguration : AbstractRequeryConfiguration() {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun getEntityModel(): EntityModel = Models.DEFAULT

    override fun getTableCreationMode(): TableCreationMode = TableCreationMode.CREATE_NOT_EXISTS

    @Bean
    fun dataSource(): DataSource {
        log.info { "Create Datasource for Embedded H2 Database..." }

        return EmbeddedDatabaseBuilder()
            .setName("data")
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .build()
    }
}