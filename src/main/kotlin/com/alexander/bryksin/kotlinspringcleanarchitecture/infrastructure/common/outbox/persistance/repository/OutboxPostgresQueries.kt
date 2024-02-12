package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.common.outbox.persistance.repository

internal const val GET_OUTBOX_EVENTS_FOR_UPDATE_SKIP_LOCKED_QUERY = """SELECT ot.event_id, ot.event_type, ot.aggregate_id, ot.data, ot.version, ot.timestamp
                |FROM microservices.outbox_table ot 
                |ORDER BY ot.timestamp ASC
                |LIMIT :limit
                |FOR UPDATE SKIP LOCKED """

internal const val GET_OUTBOX_EVENT_BY_ID_FOR_UPDATE_SKIP_LOCKED_QUERY = """SELECT event_id 
                |FROM microservices.outbox_table ot 
                |WHERE ot.event_id = :eventId 
                |FOR UPDATE SKIP LOCKED """

internal const val INSERT_OUTBOX_EVENT_QUERY = """INSERT INTO microservices.outbox_table
                    | (event_id, aggregate_id, event_type, version, data, timestamp) 
                    | VALUES (:event_id, :aggregate_id, :event_type, :version, :data, :timestamp)
                    | RETURNING event_id"""

internal const val DELETE_OUTBOX_EVENT_BY_ID_QUERY = "DELETE FROM microservices.outbox_table WHERE event_id = :eventId"
