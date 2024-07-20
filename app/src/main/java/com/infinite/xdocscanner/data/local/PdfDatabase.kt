package com.infinite.xdocscanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.infinite.xdocscanner.data.local.converter.DateTypeConverter
import com.infinite.xdocscanner.data.local.dao.PdfDao
import com.infinite.xdocscanner.data.models.PdfEntity


@Database(
    entities = [PdfEntity::class], version = 1, exportSchema = false
)

@TypeConverters(DateTypeConverter::class)
//abstract class
abstract class PdfDatabase : RoomDatabase() {
//    dao
    abstract val pdfDao : PdfDao

    companion object{

        @Volatile
        private var INSTANCE : PdfDatabase? = null

        fun getInstance(context: Context): PdfDatabase {
            synchronized(this){
                return INSTANCE ?: Room.databaseBuilder(
                  context.applicationContext,
                  PdfDatabase::class.java,
                    "pdf_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}