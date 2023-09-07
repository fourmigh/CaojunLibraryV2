package org.caojun.library.room

import android.content.Context
import org.caojun.library.room.sp.*

object DataStorageUtils {

    fun getSPDatabase(context: Context): SPDatabase {
        return SPDatabase.getDatabase(context)
    }

    fun getBooleanDao(context: Context): BooleanDao {
        return getSPDatabase(context).booleanDao()
    }

    fun getBooleanArrayDao(context: Context): BooleanArrayDao {
        return getSPDatabase(context).booleanArrayDao()
    }

    fun getFloatDao(context: Context): FloatDao {
        return getSPDatabase(context).floatDao()
    }

    fun getFloatArrayDao(context: Context): FloatArrayDao {
        return getSPDatabase(context).floatArrayDao()
    }

    fun getIntDao(context: Context): IntDao {
        return getSPDatabase(context).intDao()
    }

    fun getIntArrayDao(context: Context): IntArrayDao {
        return getSPDatabase(context).intArrayDao()
    }

    fun getLongDao(context: Context): LongDao {
        return getSPDatabase(context).longDao()
    }

    fun getLongArrayDao(context: Context): LongArrayDao {
        return getSPDatabase(context).longArrayDao()
    }

    fun getStringDao(context: Context): StringDao {
        return getSPDatabase(context).stringDao()
    }

    fun getStringArrayDao(context: Context): StringArrayDao {
        return getSPDatabase(context).stringArrayDao()
    }

    fun getByteDao(context: Context): ByteDao {
        return getSPDatabase(context).byteDao()
    }

    fun getByteArrayDao(context: Context): ByteArrayDao {
        return getSPDatabase(context).byteArrayDao()
    }

    fun saveBoolean(context: Context, key: String, value: Boolean): Boolean {
        return try {
            val data = BooleanData()
            data.mKey = key
            data.mValue = value
            getBooleanDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadBoolean(context: Context, key: String, defValue: Boolean?): Boolean? {
        val data = getBooleanDao(context).query(key) ?: return defValue
        return data.mValue
    }

    fun saveBooleanArray(context: Context, key: String, values: Array<Boolean>): Boolean {
        return try {
            val data = BooleanArrayData()
            data.mKey = key
            data.mValue.addAll(values.toList())
            getBooleanArrayDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveBooleanArray(context: Context, key: String, values: BooleanArray): Boolean {
        return saveBooleanArray(context, key, values.toTypedArray())
    }

    fun loadBooleanArray(context: Context, key: String): Array<Boolean>? {
        val data = getBooleanArrayDao(context).query(key) ?: return null
        return data.mValue.toTypedArray()
    }

    fun saveFloat(context: Context, key: String, value: Float): Boolean {
        return try {
            val data = FloatData()
            data.mKey = key
            data.mValue = value
            getFloatDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadFloat(context: Context, key: String, defValue: Float): Float {
        val data = getFloatDao(context).query(key) ?: return defValue
        return data.mValue
    }

    fun saveFloatArray(context: Context, key: String, values: Array<Float>): Boolean {
        return try {
            val data = FloatArrayData()
            data.mKey = key
            data.mValue.addAll(values.toList())
            getFloatArrayDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveFloatArray(context: Context, key: String, values: FloatArray): Boolean {
        return saveFloatArray(context, key, values.toTypedArray())
    }

    fun loadFloatArray(context: Context, key: String): Array<Float>? {
        val data = getFloatArrayDao(context).query(key) ?: return null
        return data.mValue.toTypedArray()
    }

    fun saveInt(context: Context, key: String, value: Int): Boolean {
        return try {
            val data = IntData()
            data.mKey = key
            data.mValue = value
            getIntDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadInt(context: Context, key: String, defValue: Int): Int {
        val data = getIntDao(context).query(key) ?: return defValue
        return data.mValue
    }

    fun saveIntArray(context: Context, key: String, values: Array<Int>): Boolean {
        return try {
            val data = IntArrayData()
            data.mKey = key
            data.mValue.addAll(values.toList())
            getIntArrayDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveIntArray(context: Context, key: String, values: IntArray): Boolean {
        return saveIntArray(context, key, values.toTypedArray())
    }

    fun loadIntArray(context: Context, key: String): Array<Int>? {
        val data = getIntArrayDao(context).query(key) ?: return null
        return data.mValue.toTypedArray()
    }

    fun saveLong(context: Context, key: String, value: Long): Boolean {
        return try {
            val data = LongData()
            data.mKey = key
            data.mValue = value
            getLongDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadLong(context: Context, key: String, defValue: Long): Long {
        val data = getLongDao(context).query(key) ?: return defValue
        return data.mValue
    }

    fun saveLongArray(context: Context, key: String, values: Array<Long>): Boolean {
        return try {
            val data = LongArrayData()
            data.mKey = key
            data.mValue.addAll(values.toList())
            getLongArrayDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveLongArray(context: Context, key: String, values: LongArray): Boolean {
        return saveLongArray(context, key, values.toTypedArray())
    }

    fun loadLongArray(context: Context, key: String): Array<Long>? {
        val data = getLongArrayDao(context).query(key) ?: return null
        return data.mValue.toTypedArray()
    }

    fun saveString(context: Context, key: String, value: String): Boolean {
        return try {
            val data = StringData()
            data.mKey = key
            data.mValue = value
            getStringDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadString(context: Context, key: String, defValue: String): String {
        val data = getStringDao(context).query(key) ?: return defValue
        return data.mValue
    }

    fun saveStringArray(context: Context, key: String, values: Array<String>): Boolean {
        return try {
            val data = StringArrayData()
            data.mKey = key
            data.mValue.addAll(values.toList())
            getStringArrayDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadStringArray(context: Context, key: String): Array<String>? {
        val data = getStringArrayDao(context).query(key) ?: return null
        return data.mValue.toTypedArray()
    }

    fun saveByte(context: Context, key: String, value: Byte): Boolean {
        return try {
            val data = ByteData()
            data.mKey = key
            data.mValue = value
            getByteDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadByte(context: Context, key: String, defValue: Byte): Byte {
        val data = getByteDao(context).query(key) ?: return defValue
        return data.mValue
    }

    fun saveByteArray(context: Context, key: String, values: Array<Byte>): Boolean {
        return try {
            val data = ByteArrayData()
            data.mKey = key
            data.mValue.addAll(values.toList())
            getByteArrayDao(context).insert(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveByteArray(context: Context, key: String, values: ByteArray): Boolean {
        return saveByteArray(context, key, values.toTypedArray())
    }

    fun loadByteArray(context: Context, key: String): Array<Byte>? {
        val data = getByteArrayDao(context).query(key) ?: return null
        return data.mValue.toTypedArray()
    }

    fun loadBytes(context: Context, key: String): ByteArray? {
        val data = getByteArrayDao(context).query(key) ?: return null
        return data.mValue.toByteArray()
    }
}