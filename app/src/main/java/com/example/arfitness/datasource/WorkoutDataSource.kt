package com.example.arfitness.datasource

import com.example.arfitness.data.User
import com.example.arfitness.data.UserDao
import com.example.arfitness.data.WorkoutDao
import com.example.arfitness.data.WorkoutSession
import kotlinx.coroutines.flow.Flow


// veritabanındaki işlemleri yöneten sınıf.
class WorkoutDataSource(private val workoutDao: WorkoutDao,
                        private val userDao: UserDao ) {


    fun getAllSessions(): Flow<List<WorkoutSession>> {
        return workoutDao.getAllSessions()
    }

    suspend fun insertSession(session: WorkoutSession) {
        workoutDao.insertSession(session)
    }

    suspend fun clearHistory() {
        workoutDao.clearHistory()
    }

    fun getUser(): Flow<User?> {
        return userDao.getUser()
    }
    suspend fun insertOrUpdateUser(user: User) {
        userDao.insertOrUpdateUser(user)
    }
}