package com.example.image_to_text.ui.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "ImageToTextDB"
        private const val TABLE_NAME = "ImageTextTable"
        private const val COLUMN_ID = "id"
        private const val COLUMN_IMAGE_DATA = "image_data"
        private const val COLUMN_TEXT = "text"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_IMAGE_DATA BLOB, $COLUMN_TEXT TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData( text: String): Long {
        val contentValues = ContentValues()
        //contentValues.put(COLUMN_IMAGE_DATA, imageData)
        contentValues.put(COLUMN_TEXT, text)
        val db = this.writableDatabase
        return db.insert(TABLE_NAME, null, contentValues)
    }
    @SuppressLint("Range")
    fun getAllData(): List<Map<String, Any>> {
        val dataList = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val dataMap = mutableMapOf<String, Any>()
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                //val imageData = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE_DATA))
                val text = cursor.getString(cursor.getColumnIndex(COLUMN_TEXT))
                dataMap["id"] = id
                //dataMap["image_data"] = imageData
                dataMap["text"] = text
                dataList.add(dataMap)
            }
        }
        return dataList
    }
    fun deleteData(text: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_TEXT = ?", arrayOf(text))
    }
}

