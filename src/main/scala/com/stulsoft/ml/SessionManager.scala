/*
 * Copyright (c) 2023. StulSoft
 */

package com.stulsoft.ml

import org.neo4j.driver.Config.ConfigBuilder
import org.neo4j.driver.async.AsyncSession
import org.neo4j.driver.*

import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success, Try}

object SessionManager:
  private lazy val driverConfig = Config.builder()
    .withConnectionTimeout(500, TimeUnit.MILLISECONDS)
    .withMaxConnectionPoolSize(50)
    .withConnectionAcquisitionTimeout(500, TimeUnit.MILLISECONDS)
    .build()
  private lazy val driver = GraphDatabase
    .driver(Configuration.uri, AuthTokens.basic(Configuration.username, Configuration.password), driverConfig)

  def session(): Session =
    driver.session()

  def asyncSession(): AsyncSession =
    driver.session(classOf[AsyncSession])

  def closeDriver(): Unit =
    driver.close()
