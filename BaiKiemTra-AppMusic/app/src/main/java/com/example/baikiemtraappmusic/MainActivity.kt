package com.example.baikiemtraappmusic

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var localSongAdapter: SongLoader
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongPosition: Int = -1
    private val READ_EXTERNAL_STORAGE_REQUEST = 123

//    (lấy danh sách từ bộ nhớ)
    val songLoader = SongLoader(this)
    val localSongs = songLoader.loadLocalSongs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST
            )
        } else {
            // Permission is already granted, proceed with your app logic
            initializeApp()
        }
    }

    private fun initializeApp() {
        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        mediaPlayer = MediaPlayer()

        // Khởi tạo danh sách bài hát
//        val songs = listOf(
//            Song("Song 1", "Artist 1", R.raw.binz),
//            Song("Song 2", "Artist 2", R.raw.anhkhongthathu),
//            Song("Song 3", "Artist 2", R.raw.ongbagiataolohet),
//            Song("Song 4", "Artist 2", R.raw.thiswaycara),
//            Song("Song 5", "Artist 2", R.raw.emdathuongnguoitahonanh),
//        )
//        songAdapter = SongAdapter(songs) { song -> onSongClicked(song) }
//        recyclerView.adapter = songAdapter

//      (lấy danh sách từ bộ nhớ)
        val songs = localSongs.map { song ->
            Song(song.title, song.artist, song.path)
        }

        // Khởi tạo Adapter và gán vào RecyclerView
        songAdapter = SongAdapter(songs) { song -> onSongClicked(song) }
        recyclerView.adapter = songAdapter
    }

    private fun onSongClicked(song: Song) {
        val position = songAdapter.songs.indexOf(song)

        if (currentSongPosition == -1) {
            // Nếu chưa có bài hát nào được phát, phát bài hiện tại
            startSong(song)
            currentSongPosition = position
        } else {
            if (currentSongPosition == position) {
                // Nếu bài hát hiện tại đang được phát, kiểm tra trạng thái và xử lý tương ứng
                if (mediaPlayer?.isPlaying == true) {
                    // Nếu đang phát, tạm dừng
                    pauseSong()
                } else {
                    // Nếu đang tạm dừng, tiếp tục phát
                    resumeSong()
                }
            } else {
                // Nếu click vào bài hát khác, dừng bài hiện tại và phát bài mới
                stopSong()
                startSong(song)
                currentSongPosition = position
            }
        }
    }

    private fun startSong(song: Song) {
        stopSong()

        try {
            mediaPlayer?.reset()
//            // Khởi tạo danh sách bài hát
//            val rawResId = song.rawResourceId
//            val uri = Uri.parse("android.resource://$packageName/$rawResId")

//          Lấy danh sách từ bộ nhớ
            val uri = Uri.parse(song.path)

            mediaPlayer?.setDataSource(this, uri)

            mediaPlayer?.prepare()
            mediaPlayer?.start()

            currentSongPosition = songAdapter.songs.indexOf(song)

            songAdapter.notifyDataSetChanged()

            mediaPlayer?.setOnCompletionListener {
                handleSongCompletion()
            }
        } catch (e: IOException) {
            Log.e("MediaPlayer", "Error setting data source", e)
        }
    }

    private fun stopSong() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        }

        songAdapter.notifyDataSetChanged()
    }

    private fun pauseSong() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            songAdapter.notifyDataSetChanged()
        }
    }

    private fun resumeSong() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            songAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    private fun handleSongCompletion() {
        val nextSongPosition = currentSongPosition + 1

        if (nextSongPosition < songAdapter.itemCount) {
            val nextSong = songAdapter.songs[nextSongPosition]
            stopSong()
            startSong(nextSong)
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your app logic
                initializeApp()
            } else {
                // Permission denied, handle accordingly (show a message, disable features, etc.)
                // For simplicity, you can finish the activity in this example
                finish()
            }
        }
    }
}