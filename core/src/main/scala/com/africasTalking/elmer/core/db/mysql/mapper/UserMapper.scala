package com.africasTalking.elmer.core
package db.mysql.mapper

import scala.concurrent.Future

import com.github.mauricio.async.db.{ RowData, Connection }
import com.github.mauricio.async.db.mysql.MySQLQueryResult
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext

import com.africasTalking._

import elmer.core.db.mysql.ElmerMysqlDb
import elmer.core.db.mysql.service.ElmerMysqlDbService.UserDbEntry

private[mysql] object UserMapper extends ElmerMysqlDb {

  private val FetchAllSql = "SELECT * FROM user"
  
  def fetchAll(): Future[List[UserDbEntry]] = {

    pool.sendPreparedStatement(FetchAllSql).map { queryResult =>
      queryResult.rows match {
        case Some(rows) => rows.toList map (x => rowToModel(x))
        case None => List()
      }
    }
  }

  private def rowToModel(row: RowData) = UserDbEntry(
    id       = row("id").asInstanceOf[Int],
    username = row("username").asInstanceOf[String],
    apikey   = row("api_key").asInstanceOf[String]
  )

}
