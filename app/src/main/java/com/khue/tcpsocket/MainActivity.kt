package com.khue.tcpsocket

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.khue.tcpsocket.databinding.ActivityMainBinding
import com.khue.tcpsocket.tcp.Client
import com.khue.tcpsocket.tcp.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val server = Server()
    private val client = Client(hostAddress = InetAddress.getByName("10.0.0.152"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnServer.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                server.run()
            }
        }

        binding.btnClient.setOnClickListener {
            if(
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    client.run()
                }
            } else {
                requestWriteFilePermission()
            }

        }

        binding.btnServerSendMsg.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                server.write("Hello from server".toByteArray())
            }

        }
    }

    private fun requestWriteFilePermission() {
        getWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private var getWritePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                CoroutineScope(Dispatchers.IO).launch {
                    client.run()
                }
            } else {
            }
        }
}