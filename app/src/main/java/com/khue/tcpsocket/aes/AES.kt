package com.khue.tcpsocket.aes

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.BufferedInputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES {
    private var key: SecretKey? = null
    private val KEY_SIZE = 128
    private val T_LEN = 128
    private lateinit var IV: ByteArray

    @Throws(Exception::class)
    fun init() {
        val generator: KeyGenerator = KeyGenerator.getInstance("AES")
        generator.init(KEY_SIZE)
        key = generator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initFromStrings(secretKey: String, IV: String) {
        key = SecretKeySpec(decode(secretKey), "AES")
        this.IV = decode(IV)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun encryptOld(message: String): String {
        val messageInBytes = message.toByteArray()
        val encryptionCipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key)
        IV = encryptionCipher.iv
        val encryptedBytes: ByteArray = encryptionCipher.doFinal(messageInBytes)
        return encode(encryptedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun encrypt(message: String): String {
        val messageInBytes = message.toByteArray()
        val encryptionCipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(T_LEN, IV)
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key, spec)
        val encryptedBytes: ByteArray = encryptionCipher.doFinal(messageInBytes)
        return encode(encryptedBytes)
    }

    @Throws(Exception::class)
    fun encryptFileContent() {
        val ci = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val spec = GCMParameterSpec(T_LEN, IV)
        ci.init(Cipher.ENCRYPT_MODE, key, spec)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun decrypt(encryptedMessage: String): String {
        val messageInBytes = decode(encryptedMessage)
        val decryptionCipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(T_LEN, IV)
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec)
        val decryptedBytes: ByteArray = decryptionCipher.doFinal(messageInBytes)
        return String(decryptedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encode(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decode(data: String): ByteArray {
        return Base64.getDecoder().decode(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun exportKeys() {
        System.err.println("SecretKey : " + encode(key!!.encoded))
        System.err.println("IV : " + encode(IV))
    }

    @Throws(
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        IOException::class
    )
    fun processFile(ci: Cipher, inputStream: BufferedInputStream, out: OutputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val obuf = ci.update(buffer, 0, bytesRead)
            if (obuf != null) out.write(obuf)
        }
        val obuf = ci.doFinal()
        if (obuf != null) out.write(obuf)
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val aes = AES()
                aes.initFromStrings("CHuO1Fjd8YgJqTyapibFBQ==", "e3IYYJC2hxe24/EO")
                val encryptedMessage = aes.encrypt("TheXCoders_2")
                val decryptedMessage = aes.decrypt(encryptedMessage)
                System.err.println("Encrypted Message : $encryptedMessage")
                System.err.println("Decrypted Message : $decryptedMessage")
                //aes.exportKeys();
            } catch (ignored: Exception) {
            }
        }
    }
}