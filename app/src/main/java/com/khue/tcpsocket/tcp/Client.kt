package com.khue.tcpsocket.tcp

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.khue.tcpsocket.md5.MD5
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class Client(hostAddress: InetAddress) : Thread() {

    var hostAddress: String = hostAddress.hostAddress
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream
    lateinit var socket: Socket

    fun write(byteArray: ByteArray) {
        try {
            outputStream.write(byteArray)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    override fun run() {
        try {
            socket = Socket()
            socket.connect(InetSocketAddress(hostAddress, 8888), 500)
            inputStream = socket.getInputStream()
            outputStream = socket.getOutputStream()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                receiveFile(handler)
            }
        })
    }

    private fun receiveFile(handler: Handler) {
        try {
            val file =  File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/meta_world.zip")

            val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
            val inputStream = socket.getInputStream()
            val fos = FileOutputStream(file)
            val bos = BufferedOutputStream(fos)
            while (true) {
                val bytesRead = inputStream.read(bytes, 0, bytes.size)
                if(bytesRead != -1 && bytesRead != 0) {
                    bos.write(bytes, 0, bytesRead)
                    handler.post(Runnable {
                        kotlin.run {
                            Log.i("client class", "File received")
                        }
                    })
                } else {
                    val md5 = MD5.calculateMD5(file)
                    handler.post(Runnable {
                        kotlin.run {
                            val sentMsg = "MD5: $md5"
                            Log.i("client class", sentMsg)
                        }
                    })
                    break
                }
            }

            handler.post(Runnable {
                kotlin.run {
                    val sentMsg = "File receive completed"
                    Log.i("client class", sentMsg)
                }
            })

            bos.close()
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
            handler.post(Runnable {
                kotlin.run {
                    val eMsg = "Something wrong: " + e.message
                    Log.i("client class", eMsg)
                }
            })
        } finally {
            socket.close()
        }
    }

}