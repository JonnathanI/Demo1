package com.example.demoPlay.repository

import com.example.demoPlay.entity.ProfileCosmetic
import org.springframework.data.jpa.repository.JpaRepository

interface ProfileCosmeticRepository : JpaRepository<ProfileCosmetic, Long>