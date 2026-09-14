package com.example.ui.components

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.curriculum.oer.OerMediaResource
import com.example.data.curriculum.oer.OerMediaType
import com.example.data.curriculum.oer.OerPlaybackCheckpoint
import com.example.speech.SpeechManager
import com.example.ui.NeuroPathViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OerMultimediaPlayerBottomSheet(
    resource: OerMediaResource,
    onDismiss: () -> Unit,
    viewModel: NeuroPathViewModel? = null
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        OerMultimediaPlayerContent(resource, onDismiss, viewModel)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OerMultimediaPlayerContent(
    resource: OerMediaResource,
    onClose: () -> Unit,
    viewModel: NeuroPathViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val speechManager = remember { SpeechManager(context) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableIntStateOf(0) }
    var captions by remember { mutableStateOf(true) }
    var speed by remember { mutableFloatStateOf(1f) }
    var checkpoint by remember { mutableStateOf<OerPlaybackCheckpoint?>(null) }
    var answered by remember { mutableStateOf(false) }
    var answerCorrect by remember { mutableStateOf(false) }
    var mediaError by remember { mutableStateOf<String?>(null) }
    var audioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val total = resource.durationSeconds.coerceAtLeast(1)
    val transcriptLine = remember(position, resource.transcript) {
        resource.transcript.lastOrNull { it.timestampSeconds <= position }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer?.release()
            speechManager.stop()
        }
    }

    LaunchedEffect(isPlaying, speed, resource.id) {
        while (isPlaying && position < total) {
            delay((1000L / speed).toLong().coerceAtLeast(100L))
            position++
            val cp = resource.checkpoints.firstOrNull { it.timestampSeconds == position }
            if (cp != null) {
                checkpoint = cp
                answered = false
                isPlaying = false
            }
            if (resource.mediaType != OerMediaType.VIDEO_LESSON && transcriptLine != null && resource.audioUrl.isNullOrBlank()) {
                speechManager.speak(transcriptLine.text)
            }
        }
        if (position >= total) isPlaying = false
    }

    Column(modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(if (resource.mediaType == OerMediaType.VIDEO_LESSON) Icons.Default.Videocam else Icons.Default.Audiotrack, null)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(resource.title, fontWeight = FontWeight.Bold, maxLines = 2)
                    Text(resource.creatorOrSource, fontSize = 11.sp)
                }
            }
            TextButton(onClick = onClose) { Text("Close") }
        }

        Spacer(Modifier.height(10.dp))

        if (resource.mediaType == OerMediaType.VIDEO_LESSON || resource.mediaType == OerMediaType.SCIENCE_SIMULATION) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                factory = {
                    WebView(it).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        webChromeClient = WebChromeClient()
                        webViewClient = WebViewClient()
                        val url = resource.videoUrl ?: resource.sourceUrl
                        if (url.isBlank()) mediaError = "This lesson does not have an online video source yet."
                        else loadUrl(url)
                    }
                },
                update = { webView ->
                    val url = resource.videoUrl ?: resource.sourceUrl
                    if (url.isNotBlank() && webView.url != url) webView.loadUrl(url)
                }
            )
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Audiotrack, null, modifier = Modifier.size(48.dp))
                    Text("Audio lesson", fontWeight = FontWeight.Bold)
                    Text(transcriptLine?.text ?: resource.description, fontSize = 13.sp)
                    Text(
                        if (resource.audioUrl.isNullOrBlank()) "Using accessible on-device narration from the lesson transcript."
                        else "Streaming audio source",
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (mediaError != null) Text(mediaError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

        Spacer(Modifier.height(8.dp))
        Slider(value = position.toFloat(), onValueChange = { position = it.toInt() }, valueRange = 0f..total.toFloat())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(position), fontSize = 11.sp)
            Text(formatTime(total), fontSize = 11.sp)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { position = 0; isPlaying = false; speechManager.stop(); audioPlayer?.seekTo(0) }) { Icon(Icons.Default.Replay, "Restart") }
            IconButton(onClick = {
                isPlaying = !isPlaying
                if (!isPlaying) speechManager.stop()
                if (isPlaying && resource.audioUrl != null && audioPlayer == null) {
                    scope.launch {
                        try {
                            audioPlayer = MediaPlayer().apply {
                                setDataSource(resource.audioUrl)
                                prepare()
                                start()
                            }
                        } catch (e: Exception) {
                            mediaError = "Unable to stream this audio source: ${e.message ?: "unknown error"}"
                            audioPlayer?.release()
                            audioPlayer = null
                        }
                    }
                } else if (resource.audioUrl != null) {
                    if (isPlaying) audioPlayer?.start() else audioPlayer?.pause()
                }
            }) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, if (isPlaying) "Pause" else "Play") }
            IconButton(onClick = { speed = when (speed) { 0.75f -> 1f; 1f -> 1.25f; 1.25f -> 1.5f; 1.5f -> 2f; else -> 0.75f } }) { Icon(Icons.Default.Speed, "Playback speed") }
            IconButton(onClick = { captions = !captions }) { Icon(Icons.Default.ClosedCaption, "Captions") }
        }

        if (captions && transcriptLine != null) {
            Card(Modifier.fillMaxWidth()) { Text("${transcriptLine.speaker}: ${transcriptLine.text}", Modifier.padding(10.dp), fontSize = 12.sp) }
        }

        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f, fill = false).fillMaxWidth()) {
            items(resource.transcript) { line ->
                TextButton(onClick = { position = line.timestampSeconds; isPlaying = true }) {
                    Text("${formatTime(line.timestampSeconds)}  ${line.speaker}: ${line.text}", fontSize = 11.sp)
                }
            }
        }

        if (checkpoint != null) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(checkpoint!!.title) },
                text = {
                    Column {
                        Text(checkpoint!!.questionPrompt)
                        checkpoint!!.options.forEach { option ->
                            TextButton(onClick = {
                                answered = true
                                answerCorrect = option == checkpoint!!.correctAnswer
                                if (answerCorrect) viewModel?.addPoints(15)
                            }) { Text(option) }
                        }
                        if (answered) Text(if (answerCorrect) "Correct! ${checkpoint!!.explanation}" else checkpoint!!.explanation)
                    }
                },
                confirmButton = { TextButton(enabled = answered, onClick = { checkpoint = null; isPlaying = true }) { Text("Continue") } }
            )
        }
    }
}

private fun formatTime(seconds: Int): String = String.format("%02d:%02d", seconds / 60, seconds % 60)
