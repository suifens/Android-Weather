package com.chunjing.tq.db

import android.util.Log
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.chunjing.tq.bean.CalendarBgBean
import com.chunjing.tq.bean.WeatherBgBean
import com.chunjing.tq.db.entity.CalendarBgEntity
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.utils.ContentUtil
import com.goodtech.weatherlib.net.HttpUtils
import com.goodtech.weatherlib.utils.DateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 天气/日历背景图远程拉取、落库与内存缓存。
 */
class WeatherBgRepository private constructor(
    private val appRepo: AppRepo = AppRepo.getInstance()
) {
    /// key: tempType-timeType
    private val memoryCache = HashMap<String, WeatherBgEntity>()

    suspend fun getWeatherBg(tempType: String, timeType: String): WeatherBgEntity? {
        val key = "$tempType-$timeType"
        synchronized(memoryCache) {
            memoryCache[key]?.let { return it }
        }
        val entity = appRepo.getWeatherBg(tempType, timeType)
        if (entity != null) {
            cacheEntity(entity)
        }
        return entity
    }

    /**
     * 按天气信息解析背景；本地缺失时会尝试同步远端。
     */
    suspend fun resolveWeatherBg(tempType: String, timeType: String): WeatherBgEntity? {
        getWeatherBg(tempType, timeType)?.let { return it }
        fetchAndSyncWeatherBg()
        getWeatherBg(tempType, timeType)?.let { return it }
        fetchAndSyncWeatherBg(force = true)
        getWeatherBg(tempType, timeType)?.let { return it }
        // 无精确匹配时回退到「晴」
        if (tempType != "晴") {
            getWeatherBg("晴", timeType)?.let { return it }
        }
        return getWeatherBg("晴", "白天")
    }

    /**
     * 从远端拉取天气背景列表；若比本地缓存新则落库。
     * @param force true 时忽略版本标记强制拉取（本地表为空时也会强制）
     * @return true 表示有更新并写入，false 表示无需更新或请求失败
     */
    suspend fun fetchAndSyncWeatherBg(force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val result = HttpUtils.get<WeatherBgBean>(WEATHER_BG_URL) ?: return@withContext false
        val lastTime = appRepo.getCache<String?>(CACHE_WEATHER_BG_UPDATE)
        val updateTime = TimeUtils.string2Millis(result.updateTime, "yyyy-MM-dd")
        val lastMillis = lastTime?.let { TimeUtils.string2Millis(it, "yyyy-MM-dd") } ?: 0L
        val localEmpty = appRepo.getAllBgWeathers().isEmpty()
        if (!force && lastTime != null && updateTime <= lastMillis && !localEmpty) {
            warmMemoryFromDb()
            return@withContext false
        }
        for (entity in result.imgList) {
            appRepo.addWeatherBg(entity)
            cacheEntity(entity)
        }
        appRepo.saveCache(CACHE_WEATHER_BG_UPDATE, result.updateTime)
        true
    }

    private suspend fun warmMemoryFromDb() {
        for (entity in appRepo.getAllBgWeathers()) {
            cacheEntity(entity)
        }
    }

    private fun cacheEntity(entity: WeatherBgEntity) {
        val key = "${entity.tempType}-${entity.timeType}"
        synchronized(memoryCache) {
            memoryCache[key] = entity
        }
    }

    suspend fun saveWeatherBg(entity: WeatherBgEntity) {
        appRepo.addWeatherBg(entity)
        cacheEntity(entity)
    }

    /**
     * 下载背景视频到本地目录并更新实体路径。
     */
    fun downloadBgVideo(info: WeatherBgEntity, cover: Boolean, onDone: ((WeatherBgEntity) -> Unit)? = null) {
        val url = info.videoPath
        val fileName = url.split("/").lastOrNull() ?: return
        if (!fileName.endsWith("mp4")) return

        val filePath = File("${ContentUtil.getVideoDir()}/$fileName")
        if (FileUtils.isFileExists(filePath) && !cover) {
            info.videoPath = filePath.absolutePath
            onDone?.invoke(info)
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder().get().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "downloadBgVideo onFailure: ${e.message}")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val inputFile = File("${ContentUtil.getVideoDir()}/$fileName")
                val input = response.body!!.byteStream()
                val fos = FileOutputStream(inputFile)
                val buffer = ByteArray(2048)
                var len: Int
                while (input.read(buffer).also { len = it } != -1) {
                    fos.write(buffer, 0, len)
                }
                fos.flush()
                fos.close()
                input.close()
                if (inputFile.length() > 0) {
                    info.videoPath = inputFile.absolutePath
                    onDone?.invoke(info)
                }
            }
        })
    }

    suspend fun getCalendarBg(holidayTime: String): CalendarBgEntity? =
        appRepo.getCalendarBg(holidayTime)

    /**
     * 从远端拉取日历背景；有更新则扩日并落库。
     */
    suspend fun fetchAndSyncCalendarBg(): Boolean = withContext(Dispatchers.IO) {
        val result = HttpUtils.get<CalendarBgBean>(CALENDAR_BG_URL) ?: return@withContext false
        val lastTime = appRepo.getCache<String?>(CACHE_CALENDAR_BG_UPDATE)
        val updateTime = TimeUtils.string2Millis(result.updateTime, "yyyy-MM-dd")
        val lastMillis = lastTime?.let { TimeUtils.string2Millis(it, "yyyy-MM-dd") } ?: 0L
        if (lastTime != null && updateTime <= lastMillis) {
            return@withContext false
        }
        appRepo.saveCache(CACHE_CALENDAR_BG_UPDATE, result.updateTime)
        for (entity in result.holidayList) {
            if (entity.duration.toInt() in 2..9) {
                val time = TimeUtils.string2Millis(entity.holidayTime, "yyyy-MM-dd")
                for (i in 0 until entity.duration.toInt()) {
                    val nextTime = time + DateUtil.dayMillis() * i
                    val newEntity = CalendarBgEntity()
                    newEntity.holiday = entity.holiday
                    newEntity.holidayTime = TimeUtils.millis2String(nextTime, "yyyy-MM-dd")
                    newEntity.duration = entity.duration
                    newEntity.imgPath = entity.imgPath
                    appRepo.addCalendarBg(newEntity)
                }
            } else {
                appRepo.addCalendarBg(entity)
            }
        }
        true
    }

    companion object {
        private const val TAG = "WeatherBgRepository"
        private const val WEATHER_BG_URL = "https://app.yiguxm.com/chunjing/beijing.json"
        private const val CALENDAR_BG_URL = "https://app.yiguxm.com/chunjing/rili.json"
        private const val CACHE_WEATHER_BG_UPDATE = "Weather_Bg_Update"
        private const val CACHE_CALENDAR_BG_UPDATE = "Calendar_Bg_Update"

        @Volatile
        private var instance: WeatherBgRepository? = null

        @JvmStatic
        fun getInstance(): WeatherBgRepository =
            instance ?: synchronized(this) {
                instance ?: WeatherBgRepository().also { instance = it }
            }
    }
}
