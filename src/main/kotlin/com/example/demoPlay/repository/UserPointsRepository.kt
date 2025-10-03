package com.example.demoPlay.repository

import com.example.demoPlay.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserPointsRepository : JpaRepository<UserPoints, Long> {
    fun findByUserId(userId: Long): Optional<UserPoints>
}