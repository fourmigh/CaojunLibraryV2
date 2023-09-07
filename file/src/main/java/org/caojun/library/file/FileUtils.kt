package org.caojun.library.file

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.text.TextUtils
import java.io.*
import java.nio.ByteBuffer

object FileUtils {

    fun openFileExplorer(context: Context) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "file/*"
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSaveFile(path: String): File {
        val file = File(path)
        if (!file.exists()) {
            val folder = file.parentFile
            if (!folder.exists()) {
                folder.mkdirs()
            }
            file.createNewFile()
        }
        return file
    }

    /**
     * 创建文件
     * @param type Environment.DIRECTORY_MOVIES,DIRECTORY_DOWNLOADS,DIRECTORY_DOCUMENTS等
     * @param folderName 文件夹名
     * @param fileName 文件名（包含扩展名）
     */
    @JvmStatic
    fun getSaveFile(type: String, folderName: String?, fileName: String?): File {
        val file = getFile(type, folderName, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }
    fun getFile(type: String, folderName: String?, fileName: String?): File {
        val storePath = getFolder(type, folderName)
        return File(storePath, fileName)
    }

    fun getFolderType(type: String): File? {
        val storePath = Environment.getExternalStoragePublicDirectory(type)
        if (!storePath.exists()) {
            if (!storePath.mkdirs()) {
                return null
            }
        }
        return storePath
    }

    fun getFolder(type: String, folderName: String?): File? {
        val storePath = File(
            Environment.getExternalStoragePublicDirectory(type),
            folderName
        )
        if (!storePath.exists()) {
            if (!storePath.mkdirs()) {
                return null
            }
        }
        return storePath
    }

    fun findFile(fileName: String, folderFile: File? = Environment.getExternalStorageDirectory()): File? {
        val folder = folderFile ?: Environment.getExternalStorageDirectory()
        if (folder.isFile) {
            if (folder.name == fileName) {
                return folder
            }
            return null
        }
        val files = folder.listFiles()
        if (files == null || files.isEmpty()) {
            return null
        }
        for (file in files) {
            if (file.isFile && file.name == fileName) {
                return file
            }
        }
        for (file in files) {
            if (file.isDirectory) {
                val f = findFile(fileName, file)
                if (f != null) {
                    return f
                }
            }
        }
        return null
    }

    fun getString(inputStream: InputStream?, addN: Boolean = true): String {
        val sb = StringBuffer()
        try {
            val inputStreamReader = InputStreamReader(inputStream)
            val reader = BufferedReader(inputStreamReader)
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (addN && sb.isNotEmpty()) {
                    sb.append("\n")
                }
                sb.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    fun getString(file: File, addN: Boolean = true): String {
        return try {
            val fileInputStream = FileInputStream(file)
            getString(fileInputStream, addN)
        } catch (e: Exception) {
            ""
        }
    }

    fun searchFiles(files: ArrayList<File>, expandedNames: Array<String>) {
        searchFiles(Environment.getExternalStorageDirectory(), files, expandedNames)
    }

    fun searchFiles(folder: File, files: ArrayList<File>, expandedNames: Array<String>) {
        val list = folder.listFiles()
        if (list == null || list.isEmpty()) {
            return
        }
        for (file in list) {
            if (file.isFile) {
                for (ename in expandedNames) {
                    val filename = file.name.lowercase()
                    if (filename.endsWith(ename.lowercase())) {
                        files.add(file)
                    }
                }
            }
        }
        for (file in list) {
            if (file.isDirectory) {
                searchFiles(file, files, expandedNames)
            }
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            deleteFile(File(path))
        }catch (e: Exception){
            false
        }
    }

    fun deleteFile(file: File): Boolean {
        return file.delete()
    }

    fun deleteFolder(path: String): Boolean {
        return deleteFolder(File(path))
    }

    fun deleteFolder(folder: File?): Boolean {
        if (folder == null) {
            return false
        }
        if (!folder.exists()) {
            return false
        }
        if (!folder.isDirectory) {
            return false
        }
        return folder.deleteRecursively()
    }

    fun findOldestFolder(folder: File): File? {
        if (!folder.exists()) {
            return null
        }
        if (!folder.isDirectory) {
            return null
        }
        val list = folder.listFiles() ?: return null
        var f : File? = null
        for (file in list) {
            if (!file.isDirectory) {
                continue
            }
            if (f == null) {
                f = file
            } else if (file.lastModified() < f.lastModified()) {
                f = file
            }
        }
        return f
    }

    fun findLatestFile(folderPath: String, expandedNames: Array<String>): File? {
        return findLatestFile(File(folderPath), expandedNames)
    }

    fun findLatestFile(folder: File, expandedNames: Array<String>): File? {
        if (!folder.exists()) {
            return null
        }
        if (!folder.isDirectory) {
            return null
        }
        val list = folder.listFiles()
        var f : File? = null
        for (file in list) {
            if (!file.isFile) {
                continue
            }
            for (ename in expandedNames) {
                val filename = file.name.lowercase()
                if (filename.endsWith(ename.lowercase())) {
                    if (f == null) {
                        f = file
                    } else if (file.lastModified() > f.lastModified()) {
                        f = file
                    }
                }
            }
        }
        return f
    }

    /**
     * 写指定大小文件
     */
    fun writeFile(prefix: String, size: Int): File? {
        try {
            val file = File.createTempFile("${prefix}_", ".tmp") ?: return null
            val fileWriter = FileWriter(file)
            val bytes = CharArray(size)
            fileWriter.write(bytes)
            fileWriter.close()
            return file
        } catch (e: Exception) {
            return null
        }
    }

    fun writePath(path: String, content: String): Boolean {
        var result = false
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(
                OutputStreamWriter(
                    FileOutputStream(File(path), true)
                )
            )
            out.write("\r\n")
            out.write(content)
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun writeFile(file: File, content: String, append: Boolean = false) : Boolean{
        var result = false
        var out: BufferedWriter? = null
        val fos = FileOutputStream(file, append)
        try {
            out = BufferedWriter(
                OutputStreamWriter(
                    fos
                )
            )
            out.write(content)
            out.flush()
            fos.fd.sync()
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun getFileSize(file: File): Int {
        var size = 0
        if (file.exists()) {
            val fis = FileInputStream(file)
            size = fis.available()
            fis.close()
        }
        return size
    }

    fun file2MD5(path: String?): String {
        if (path == null || TextUtils.isEmpty(path)) {
            return ""
        }
        return file2MD5(File(path))
    }

    fun file2MD5(file: File?): String {
        return File2MD5.md5(file)
    }

    fun write(bytes: ByteArray, path: String): Boolean {
        return try {
            val out = FileOutputStream(path)
            val fileChannel = out.channel
            fileChannel.write(ByteBuffer.wrap(bytes))
            fileChannel.force(true)
            fileChannel.close()
            true
        } catch (e: Exception) {
            true
        }
    }

    fun readBytes(path: String): ByteArray? {
        return try {
            val fis = FileInputStream(path)
            val size = fis.channel.size()
            if (size <= 0) {
                return null
            }
            val buffer = ByteArray(fis.available())
            fis.read(buffer)
            fis.close()
            return buffer
        } catch (e: Exception) {
            null
        }
    }

    fun copyDirectory(sourceDir: File, targetDir: File){
        sourceDir.listFiles()?.forEach { file ->
            val targetFile = File(targetDir.absolutePath + File.separator + file.name)
            if (file.isDirectory) {
                targetFile.mkdir()
                copyDirectory(file, targetFile)
            } else {
                try {
                    file.copyTo(targetFile, true)
                } catch (e: Exception) {
                    throw IOException("Failed to copy ${file.name}", e)
                }
            }
        }
    }

    fun getInputStream(filePath: String): InputStream? {
        if (TextUtils.isEmpty(filePath)) {
            return null
        }
        val file = File(filePath)
        if (!file.exists()) {
            return null
        }
        if (file.isDirectory) {
            return null
        }
        return FileInputStream(file)
    }
}
