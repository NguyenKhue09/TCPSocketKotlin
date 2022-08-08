package com.khue.tcpsocket.tcp

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.khue.tcpsocket.constants.SocketDataType.TYPE_AES_IV
import com.khue.tcpsocket.constants.SocketDataType.TYPE_AES_KEY
import com.khue.tcpsocket.constants.SocketDataType.TYPE_CLOSE
import com.khue.tcpsocket.constants.SocketDataType.TYPE_ERROR
import com.khue.tcpsocket.constants.SocketDataType.TYPE_FILE_DATA
import com.khue.tcpsocket.constants.SocketDataType.TYPE_HELLO
import com.khue.tcpsocket.constants.SocketDataType.TYPE_MD5
import com.khue.tcpsocket.constants.SocketDataType.TYPE_RSA_KEY
import com.khue.tcpsocket.constants.SocketDataType.TYPE_START
import com.khue.tcpsocket.constants.SocketDataType.TYPE_SUCCESS
import com.khue.tcpsocket.md5.MD5
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors


class Server : Thread() {

    lateinit var serverSocket: ServerSocket
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream
    lateinit var socket: Socket
    lateinit var dIn: DataInputStream
    lateinit var dOut: DataOutputStream
    private var prevSocketDataType: Int = -2

    override fun run() {
        try {
            serverSocket = ServerSocket()
            serverSocket.bind(InetSocketAddress(8888))
            serverSocket.reuseAddress = true
            socket = serverSocket.accept()
            inputStream = socket.getInputStream()
            dIn = DataInputStream(inputStream)
            outputStream = socket.getOutputStream()
            dOut = DataOutputStream(outputStream)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        handlerMessage()
    }

    private fun handlerMessage() {
        val executors = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executors.execute(Runnable {
            kotlin.run {
                while (!socket.isClosed) {
                    when (dIn.readInt()) {
                        TYPE_START -> {
                            prevSocketDataType = TYPE_START

                            // 2
                            // receive start message from client
                            handler.post(Runnable {
                                kotlin.run {
                                    val sentMsg = "2 receive start message from client"
                                    Log.i("Server class", sentMsg)
                                }
                            })
                            dOut.writeInt(TYPE_START)
                            dOut.writeUTF("Start")

                        }
                        TYPE_HELLO -> {
                            prevSocketDataType = TYPE_HELLO

                            // 6
                            dOut.writeInt(TYPE_SUCCESS)
                        }
                        TYPE_RSA_KEY -> {
                            prevSocketDataType = TYPE_RSA_KEY

                            // 8
                            // receive rsa key from client
                            handler.post(Runnable {
                                kotlin.run {
                                    val sentMsg = "receive rsa key from client"
                                    Log.i("Server class", sentMsg)
                                }
                            })
                            dOut.writeInt(TYPE_RSA_KEY)
                            dOut.writeUTF("Send rsa key to client")
                            handler.post(Runnable {
                                kotlin.run {
                                    val sentMsg = "Send rsa key to client"
                                    Log.i("Server class", sentMsg)
                                }
                            })
                        }
                        TYPE_AES_KEY -> {
                            prevSocketDataType = TYPE_AES_KEY

                            // 12
                            dOut.writeInt(TYPE_SUCCESS)

                        }
                        TYPE_AES_IV -> {
                            prevSocketDataType = TYPE_AES_IV

                            // 14
                            dOut.writeInt(TYPE_AES_IV)
                            dOut.writeUTF("Send iv key to client")
                            handler.post(Runnable {
                                kotlin.run {
                                    val sentMsg = "Send iv key to client"
                                    Log.i("Server class", sentMsg)
                                }
                            })
                        }
                        TYPE_MD5 -> {
                            prevSocketDataType = TYPE_MD5

                            // 19
                            dOut.writeInt(TYPE_FILE_DATA)
                        }
                        TYPE_FILE_DATA -> {

                            // 21
                            prevSocketDataType = TYPE_FILE_DATA
                            fileTransfer(handler)
                        }
                        TYPE_CLOSE -> {
                            prevSocketDataType = TYPE_CLOSE
                        }
                        TYPE_SUCCESS -> {
                            handlerSuccess(prevSocketDataType, handler)
                        }
                        TYPE_ERROR -> {

                        }
                    }
                }
            }
        })
    }

    private fun handlerSuccess(type: Int, handler: Handler) {
        when (type) {
            TYPE_START -> {
                // 4
                dOut.writeInt(TYPE_HELLO)
                dOut.writeUTF("Hello Client")
                handler.post(Runnable {
                    kotlin.run {
                        val sentMsg = "Hello Client"
                        Log.i("Server class", sentMsg)
                    }
                })
            }
            TYPE_HELLO -> {

            }
            TYPE_RSA_KEY -> {
                // 10
                dOut.writeInt(TYPE_AES_KEY)
                dOut.writeUTF("Send aes key to client")
                handler.post(Runnable {
                    kotlin.run {
                        val sentMsg = "Send aes key to client"
                        Log.i("Server class", sentMsg)
                    }
                })
            }
            TYPE_AES_KEY -> {
                dOut.writeInt(TYPE_SUCCESS)
            }
            TYPE_AES_IV -> {
                dOut.writeInt(TYPE_SUCCESS)

                // 16
                dOut.writeInt(TYPE_FILE_DATA)
            }
            TYPE_MD5 -> {
                // 17
                dOut.writeInt(TYPE_MD5)
                dOut.writeUTF("Send md5 to client")
                handler.post(Runnable {
                    kotlin.run {
                        val sentMsg = "Send md5 to client"
                        Log.i("Server class", sentMsg)
                    }
                })
            }
            TYPE_FILE_DATA -> {
                dOut.writeInt(4)
                fileTransfer(handler)
            }
            TYPE_CLOSE -> {
            }
        }
    }

    fun write(byteArray: ByteArray) {
        try {
            Log.i("Server write", "$byteArray sending")
            outputStream.write(byteArray)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun fileTransfer(handler: Handler) {
        val file =
            File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/meta_world.zip")
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
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var totalReal = 0L
            while (true) {
                val bytesRead = bis.read(buffer)
                if (bytesRead != -1 && bytesRead != 0) {
                    totalReal += bytesRead.toLong()
                    dOut.write(buffer, 0, bytesRead)
                    dOut.flush()
                    handler.post(Runnable {
                        kotlin.run {
                            val sentMsg =
                                "File sent to: " + socket.inetAddress + ": " + totalReal + "/" + file.length()
                            Log.i("Server class", sentMsg)
                        }
                    })
                } else {
                    break
                }
            }

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

    fun closeSocket() {
        socket.close()
        serverSocket.close()
    }
}


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