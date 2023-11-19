package com.example.baikiemtraappmusic

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var mediaPlayer: MediaPlayer? = null
    private var currentSongPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        mediaPlayer = MediaPlayer()

        // Khởi tạo danh sách bài hát (lấy danh sách từ bộ nhớ)
        val songs = listOf(
            Song("Song 1", "Artist 1", R.raw.binz),
            Song("Song 2", "Artist 2", R.raw.anhkhongthathu),
            Song("Song 3", "Artist 2", R.raw.ongbagiataolohet),
            Song("Song 4", "Artist 2", R.raw.thiswaycara),
            Song("Song 5", "Artist 2", R.raw.emdathuongnguoitahonanh),
        )

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
            val rawResId = song.rawResourceId
            val uri = Uri.parse("android.resource://$packageName/$rawResId")
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
}