@file:JvmName("File2")
package com.github.balloonupdate.util

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.zip.CRC32
import kotlin.io.path.pathString

class File2 : Iterable<File2>
{
    var file: File

    constructor(file: String): this(File(file))

    constructor(file: File)
    {
        this.file = file.absoluteFile
    }

    @get:JvmName("getRawFile")
    val _file: File get() = file

    @get:JvmName("getName")
    val name: String get() = file.name

    @get:JvmName("isDirectory")
    val isDirectory: Boolean get() = file.isDirectory

    @get:JvmName("isFile")
    val isFile: Boolean get() = file.isFile

    @get:JvmName("exists")
    val exists: Boolean get() = file.exists()

    val parent: File2 get() = File2(file.parent)

    fun mkdirs() = file.mkdirs()

    fun makeParentDirs() = parent.mkdirs()

    fun rename(newName: String) = file.renameTo(File(newName))

    fun touch(fileContent: String? =null)
    {
        content = fileContent ?: ""
    }

    @get:JvmName("getContent")
    var content: String
        get() {
            if(!exists)
                throw FileNotFoundException(path)
            FileInputStream(file).use {
                return it.readBytes().decodeToString()
            }
        }
        set(value) {
            if(!exists)
                file.createNewFile()
            FileOutputStream(file).use {
                it.write(value.encodeToByteArray())
            }
        }

    fun append(content: String)
    {
        if(!exists)
            file.createNewFile()
        FileOutputStream(file, true).use {
            it.write(content.encodeToByteArray())
        }
    }

    val length: Long
        get() {
            if(!exists)
                throw FileNotFoundException(path)
            if(isDirectory)
                throw FileNotFoundException("is not a file: $path")
            return file.length()
        }

    val modified: Long
        get() {
            if(!exists)
                throw FileNotFoundException(path)
            if(isDirectory)
                throw FileNotFoundException("is not a file: $path")
            return file.lastModified()
        }

    val files: List<File2> get() = file.listFiles().map { File2(it) }

    @get:JvmName("isDirty")
    val isDirty: Boolean
        get() {
            if(!exists)
                throw FileNotFoundException(path)
            return if(isFile) length==0L else files.isEmpty()
        }

    fun clear()
    {
        if(!exists)
            return
        if(isDirectory)
            for (f in files)
                f.delete()
        else
            content = ""
    }

    fun delete()
    {
        if(!exists)
            return
        if(isDirectory)
            for (f in files)
                f.delete()
        file.delete()
    }

    fun copy(target: File2)
    {
        file.copyRecursively(target.file, overwrite = true)
    }

    fun move(target: File2)
    {
        copy(target)
        target.delete()
    }

    val path: String get() = platformPath.replace("\\", "/")

    val platformPath: String get() = file.absolutePath

    fun relativize(target: File2, platformize: Boolean = false): String {
        return Paths.get(path).relativize(Paths.get(target.path)).pathString.run {
            if(!platformize) replace("\\", "/") else this
        }
    }

    fun relativizedBy(base: File2, platformize: Boolean = false): String {
        return Paths.get(base.path).relativize(Paths.get(path)).pathString.run {
            if(!platformize) replace("\\", "/") else this
        }
    }

    override fun iterator(): Iterator<File2>
    {
        return files.iterator()
    }

    operator fun plus(value: String): File2
    {
        return File2(path + File.separator + value)
    }

    operator fun invoke(value: String): File2
    {
        return this + value
    }

    operator fun get(value: String): File2
    {
        return this + value
    }

    operator fun contains(value: String): Boolean
    {
        return (this + value).exists
    }
}