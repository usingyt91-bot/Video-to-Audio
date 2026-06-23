package com.example.videotoaudio

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var selectVideoButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar

    private val REQUEST_CODE_PICK_VIDEO = 1001
    private val PERMISSION_REQUEST_CODE = 2001
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectVideoButton = findViewById(R.id.selectVideoButton)
        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)

        selectVideoButton.setOnClickListener {
            if (hasPermissions()) {
                openVideoPicker()
            } else {
                requestPermissions()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.file_picker_title)),
            REQUEST_CODE_PICK_VIDEO
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_VIDEO && resultCode == Activity.RESULT_OK) {
            val videoUri = data?.data ?: return
            convertVideoToAudio(videoUri)
        }
    }

    private fun convertVideoToAudio(videoUri: Uri) {
        thread {
            try {
                runOnUiThread {
                    selectVideoButton.isEnabled = false
                    statusText.text = getString(R.string.converting)
                    progressBar.visibility = ProgressBar.VISIBLE
                    progressBar.progress = 0
                }

                val outputFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "converted_audio.mp3"
                )

                contentResolver.openFileDescriptor(videoUri, "r").use { pfd ->
                    if (pfd == null) {
                        throw Exception("Cannot open file descriptor")
                    }

                    val extractor = MediaExtractor()
                    extractor.setDataSource(pfd.fileDescriptor)

                    var audioTrackIndex = -1
                    var audioFormat: MediaFormat? = null

                    for (i in 0 until extractor.trackCount) {
                        val format = extractor.getTrackFormat(i)
                        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                        if (mime.startsWith("audio/")) {
                            audioTrackIndex = i
                            audioFormat = format
                            break
                        }
                    }

                    if (audioTrackIndex == -1 || audioFormat == null) {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.no_audio_track),
                                Toast.LENGTH_SHORT
                            ).show()
                            statusText.text = getString(R.string.status_text)
                            progressBar.visibility = ProgressBar.GONE
                            selectVideoButton.isEnabled = true
                        }
                        extractor.release()
                        return@thread
                    }

                    extractor.selectTrack(audioTrackIndex)

                    val muxer = MediaMuxer(
                        outputFile.absolutePath,
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                    )

                    val writeTrackIndex = muxer.addTrack(audioFormat)
                    muxer.start()

                    val mediaBuffer = java.nio.ByteBuffer.allocate(256 * 1024)
                    val info = android.media.MediaCodec.BufferInfo()
                    var totalFrames = audioFormat.getInteger(MediaFormat.KEY_FRAME_RATE, 0)
                    if (totalFrames == 0) {
                        totalFrames = 1
                    }
                    var frameCount = 0

                    while (true) {
                        info.offset = 0
                        info.size = extractor.readSampleData(mediaBuffer, 0)
                        if (info.size < 0) {
                            info.size = 0
                            break
                        } else {
                            info.presentationTimeUs = extractor.sampleTime
                            info.flags = extractor.sampleFlags
                            muxer.writeSampleData(writeTrackIndex, mediaBuffer, info)
                            extractor.advance()
                            frameCount++
                            val progress = (frameCount * 100) / maxOf(totalFrames, 1)
                            runOnUiThread {
                                progressBar.progress = minOf(progress, 99)
                            }
                        }
                    }

                    muxer.stop()
                    muxer.release()
                    extractor.release()

                    runOnUiThread {
                        progressBar.progress = 100
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.conversion_complete),
                            Toast.LENGTH_SHORT
                        ).show()
                        statusText.text = "Saved: ${outputFile.absolutePath}"
                        progressBar.visibility = ProgressBar.GONE
                        selectVideoButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "${getString(R.string.conversion_failed)}: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    statusText.text = getString(R.string.status_text)
                    progressBar.visibility = ProgressBar.GONE
                    selectVideoButton.isEnabled = true
                }
            }
        }
    }
}
