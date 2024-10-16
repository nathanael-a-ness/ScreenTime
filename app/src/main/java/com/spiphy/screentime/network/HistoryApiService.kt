package com.spiphy.screentime.network

import com.spiphy.screentime.model.Ticket
import retrofit2.http.GET

interface HistoryApiService {
    @GET("Ticket/GetAllTickets")
    suspend fun getAllTickets(): List<Ticket>
}