package com.example.possible.repo.local.database

import android.content.Context
import com.example.possible.model.Child
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalRepoImp(context: Context):LocalRepo {
    private val db = AppDatabase.getDatabase(context)
    override suspend fun insertChild(child: Child) {
        withContext(Dispatchers.IO){
            db.childDao().insertChild(child)

        }
    }

    override suspend fun updateChild(child: Child) {
        withContext(Dispatchers.IO){
            db.childDao().editChild(child)
        }
    }

    override suspend fun getAllChildren(): List<Child> =
        withContext(Dispatchers.IO){
            db.childDao().getAllChildren()

        }


    override suspend fun getChildById(id: Int): Child =
        withContext(Dispatchers.IO) {
            db.childDao().getChildById(id)!!
        }


    override suspend fun deleteChild(child: Child) {
        withContext(Dispatchers.IO){
            db.childDao().deleteChild(child)

        }
    }

    override suspend fun updateReadingRate(childId: Int, readingRate: Int) {
        withContext(Dispatchers.IO){
            db.childDao().updateReadingRate(childId,readingRate)
        }
    }

    override suspend fun updateWritingRate(childId: Int, writingRate: Int) {
        withContext(Dispatchers.IO){
            db.childDao().updateWritingRate(childId,writingRate)
        }
    }

    override suspend fun updateReadingDays(childId: Int, readingDays: Int) {
        withContext(Dispatchers.IO){
            db.childDao().updateReadingDays(childId,readingDays)
        }
    }
    override suspend fun updateWritingDays(childId: Int, writingDays: Int) {
        withContext(Dispatchers.IO){
            db.childDao().updateWritingDays(childId,writingDays)
        }
    }


    override suspend fun updateLatestReadingDay(childId: Int, latestReadingDay: String) {
        withContext(Dispatchers.IO){
            db.childDao().updateLatestReadingDay(childId,latestReadingDay)
        }
    }

    override suspend fun updateLatestWritingDay(childId: Int, latestWritingDay: String) {
        withContext(Dispatchers.IO){
            db.childDao().updateLatestWritingDay(childId,latestWritingDay)
        }
    }
}
