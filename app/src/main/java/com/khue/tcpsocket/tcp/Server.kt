package com.khue.tcpsocket.tcp

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.khue.tcpsocket.md5.MD5
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors


class Server: Thread() {

    lateinit var serverSocket: ServerSocket
    lateinit var inputStream: InputStream
    lateinit var  outputStream: OutputStream
    lateinit var socket: Socket

    override fun run() {
        try {
            serverSocket = ServerSocket()
            serverSocket.bind(InetSocketAddress(8888))
            serverSocket.reuseAddress = true
            socket = serverSocket.accept()
            inputStream = socket.getInputStream()
            outputStream = socket.getOutputStream()
        }catch (ex: IOException){
            ex.printStackTrace()
        }

        val executors = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executors.execute(Runnable{
            kotlin.run {
                fileTransfer(handler)
//                val buffer = ByteArray(1024)
//                var byte:Int
//                while (!socket.isClosed){
//                    try {
//                        byte =  inputStream.read(buffer)
//                        if(byte > 0){
//                            val finalByte = byte
//                            handler.post(Runnable{
//                                kotlin.run {
//                                    val tmpMeassage = String(buffer,0,finalByte)
//
//                                    Log.i("Server class", tmpMeassage)
//                                }
//                            })
//                        }
//                    }catch (ex:IOException){
//                        ex.printStackTrace()
//                    }
//                }
            }
        })
    }

    fun write(byteArray: ByteArray){
        try {
            Log.i("Server write","$byteArray sending")
            outputStream.write(byteArray)
        }catch (ex:IOException){
            ex.printStackTrace()
        }
    }

    private fun fileTransfer(handler: Handler) {
        val file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/meta_world.zip")

        val bis: BufferedInputStream

        val md5 = MD5.calculateMD5(file)
        handler.post(Runnable {
            kotlin.run {
                val sentMsg = "MD5: $md5"
                Log.i("Server class", sentMsg)
            }
        })

        try {
            bis = BufferedInputStream(FileInputStream(file))
            val os = socket.getOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

            while (true) {
                val bytesRead = bis.read(buffer)
                if(bytesRead != -1 && bytesRead != 0){
                    os.write(buffer, 0, bytesRead)
                    os.flush()
                    handler.post(Runnable {
                        kotlin.run {
                            val sentMsg = "File sent to: " + socket.inetAddress + ": " + bytesRead
                            Log.i("Server class", sentMsg)
                        }
                    })
                } else {
                    break
                }
            }

            socket.close()
            serverSocket.close()

        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } finally {
            socket.close()
        }
    }
}