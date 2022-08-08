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
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors

class Client(hostAddress: InetAddress) : Thread() {

    var hostAddress: String = hostAddress.hostAddress
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream
    lateinit var socket: Socket
    lateinit var dIn: DataInputStream
    lateinit var dOut: DataOutputStream
    private var prevSocketDataType: Int = -2

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
            dIn = DataInputStream(inputStream)
            outputStream = socket.getOutputStream()
            dOut = DataOutputStream(outputStream)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        // 1
        dOut.writeInt(TYPE_START)
        dOut.writeUTF("Start")
        dOut.flush()
        handlerMessage()
    }

    private fun handlerMessage() {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute(kotlinx.coroutines.Runnable {
            kotlin.run {
                while (!socket.isClosed) {
                    when (dIn.readInt()) {
                        TYPE_START -> {
                            prevSocketDataType = TYPE_START

                            // 3
                            // receive start message from server
                            handler.post(Runnable {
                                kotlin.run {
                                    Log.i("client class", "3 receive start message from server")
                                }
                            })
                            dOut.writeInt(TYPE_SUCCESS) // start
                        }
                        TYPE_HELLO -> {
                            prevSocketDataType = TYPE_HELLO

                            // 5
                            // receive hello message from server
                            handler.post(Runnable {
                                kotlin.run {
                                    Log.i("client class", "receive hello message from server")
                                }
                            })
                            dOut.writeInt(TYPE_HELLO)
                            dOut.writeUTF("Hello server")
                        }
                        TYPE_RSA_KEY -> {
                            prevSocketDataType = TYPE_RSA_KEY

                            // 9
                            // receive rsa key from server
                            handler.post(Runnable {
                                kotlin.run {
                                    Log.i("client class", "receive rsa key from server")
                                }
                            })
                            dOut.writeInt(TYPE_SUCCESS)
                        }
                        TYPE_AES_KEY -> {
                            prevSocketDataType = TYPE_AES_KEY

                            // 11
                            // receive aes key from server
                            handler.post(Runnable {
                                kotlin.run {
                                    Log.i("client class", "receive aes key from server")
                                }
                            })
                            dOut.writeInt(TYPE_AES_KEY)
                        }
                        TYPE_AES_IV -> {
                            prevSocketDataType = TYPE_AES_IV

                            // 15
                            // receive aes iv from server
                            handler.post(Runnable {
                                kotlin.run {
                                    Log.i("client class", "receive aes iv from server")
                                }
                            })
                            dOut.writeInt(TYPE_SUCCESS)
                        }
                        TYPE_MD5 -> {
                            prevSocketDataType = TYPE_MD5

                            // 18
                            // receive md5 from server
                            handler.post(Runnable {
                                kotlin.run {
                                    Log.i("client class", "receive md5 from server")
                                }
                            })
                            dOut.writeInt(TYPE_MD5)
                        }
                        TYPE_FILE_DATA -> {
                            prevSocketDataType = TYPE_FILE_DATA
                            receiveFile(handler)

                            // 20
                            dOut.writeInt(TYPE_FILE_DATA)
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
            }
            TYPE_HELLO -> {
                // 7
                dOut.writeInt(TYPE_RSA_KEY)
                dOut.writeUTF("send RSA key to server")
                handler.post(Runnable {
                    kotlin.run {
                        Log.i("client class", "send RSA key to server")
                    }
                })
            }
            TYPE_RSA_KEY -> {
            }
            TYPE_AES_KEY -> {
                // 13
                dOut.writeInt(TYPE_AES_KEY)
            }
            TYPE_AES_IV -> {
            }
            TYPE_MD5 -> {
            }
            TYPE_FILE_DATA -> {
            }
            TYPE_CLOSE -> {
                prevSocketDataType = TYPE_CLOSE
            }
        }
    }

    private fun receiveFile(handler: Handler) {
        try {
            val file =
                File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/meta_world.zip")

            val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
            val fos = FileOutputStream(file)
            val bos = BufferedOutputStream(fos)
            var totalRead = 0L
            while (true) {
                val bytesRead = dIn.read(bytes, 0, bytes.size)
                totalRead += bytesRead
                if (bytesRead != -1 && bytesRead != 0) {
                    bos.write(bytes, 0, bytesRead)
                    bos.flush()
                    handler.post(Runnable {
                        kotlin.run {
                            Log.i("client class", "File received: $totalRead")
                        }
                    })
                } else {
                    break
                }
            }

            val md5 = MD5.calculateMD5(file)
            dOut.writeInt(TYPE_SUCCESS)
            handler.post(Runnable {
                kotlin.run {
                    val sentMsg = "MD5: $md5"
                    Log.i("client class", sentMsg)
                }
            })
            handler.post(Runnable {
                kotlin.run {
                    val sentMsg = "File receive completed"
                    Log.i("client class", sentMsg)
                }
            })

            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
            handler.post(Runnable {
                kotlin.run {
                    val eMsg = "Something wrong: " + e.message
                    Log.i("client class", eMsg)
                }
            })
        } finally {
            closeSocket()
        }
    }

    fun closeSocket() {
        socket.close()
    }

}