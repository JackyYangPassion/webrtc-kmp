import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import com.shepeliev.webrtckmp.AudioStreamTrack
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.VideoStreamTrack
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    LaunchedEffect(Unit) {
        Logger.setLogWriters(platformLogWriter())
    }

    MaterialTheme {
        val scope = rememberCoroutineScope()
        val (localStream, setLocalStream) = remember { mutableStateOf<MediaStream?>(null) }
        val (remoteVideoTrack, setRemoteVideoTrack) = remember {
            mutableStateOf<VideoStreamTrack?>(
                null
            )
        }
        val (remoteAudioTrack, setRemoteAudioTrack) = remember {
            mutableStateOf<AudioStreamTrack?>(
                null
            )
        }
        val (peerConnections, setPeerConnections) = remember {
            mutableStateOf<Pair<PeerConnection, PeerConnection>?>(null)
        }

        LaunchedEffect(localStream, peerConnections) {
            if (peerConnections == null || localStream == null) return@LaunchedEffect
            makeCall(peerConnections, localStream, setRemoteVideoTrack, setRemoteAudioTrack)
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val localVideoTrack = localStream?.videoTracks?.firstOrNull()


            Box(
                modifier = Modifier.fillMaxWidth().weight(2f).background(MaterialTheme.colors.onSurface)
            ) {
                localVideoTrack?.let {
                    Video(
                        videoTrack = it,
                        modifier = Modifier.fillMaxSize()// 通过查字典，看这个含义
                    )
                } ?: Box(
                    //modifier = Modifier.weight(1f).fillMaxWidth(),
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Local video")
                }
            }

            //增加一个分割线
            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onSurface)
            )

            remoteVideoTrack?.let {
                Video(
                    videoTrack = it,
                    audioTrack = remoteAudioTrack,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            } ?: Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Remote video")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (localStream == null) {
                    StartButton(onClick = {
                        scope.launch {
                            val stream = MediaDevices.getUserMedia(audio = true, video = true)
                            setLocalStream(stream)
                        }
                    })
                } else {
                    StopButton(
                        onClick = {
                            hangup(peerConnections)
                            localStream.release()
                            setLocalStream(null)
                            setPeerConnections(null)
                            setRemoteVideoTrack(null)
                            setRemoteAudioTrack(null)
                        }
                    )

                    SwitchCameraButton(
                        onClick = {
                            scope.launch { localStream.videoTracks.firstOrNull()?.switchCamera() }
                        }
                    )
                }
                if (peerConnections == null) {
                    CallButton(
                        onClick = { setPeerConnections(Pair(PeerConnection(), PeerConnection())) },
                    )
                } else {
                    HangupButton(onClick = {
                        hangup(peerConnections)
                        setPeerConnections(null)
                        setRemoteVideoTrack(null)
                        setRemoteAudioTrack(null)
                    })
                }
            }
        }
    }
}

@Composable
private fun CallButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier = modifier) {
        Text("Call")
    }
}

@Composable
private fun HangupButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier = modifier) {
        Text("Hangup")
    }
}

@Composable
private fun SwitchCameraButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Text("Switch Camera")
    }
}

@Composable
private fun StopButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Text("Stop")
    }
}
