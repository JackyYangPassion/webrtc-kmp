`makeCall` 函数实现了一个 WebRTC 的呼叫过程，涉及两个 `PeerConnection` 对象的交互。以下是主要步骤和功能：

1. **初始化**：
   ```kotlin
   val (pc1, pc2) = peerConnections
   localStream.tracks.forEach { pc1.addTrack(it) }
   ```
   将本地媒体流的轨道添加到第一个 `PeerConnection`。

2. **处理 ICE 候选**：
   ```kotlin
   pc1.onIceCandidate.onEach { ... }.launchIn(this)
   pc2.onIceCandidate.onEach { ... }.launchIn(this)
   ```
   监听 ICE 候选事件，并在合适的时机将候选添加到对方的 `PeerConnection`。

3. **处理信令状态变化**：
   ```kotlin
   pc1.onSignalingStateChange.onEach { ... }.launchIn(this)
   pc2.onSignalingStateChange.onEach { ... }.launchIn(this)
   ```
   监听信令状态变化，当状态为 `HaveRemoteOffer` 时，处理缓存的 ICE 候选。

4. **处理连接状态变化**：
   ```kotlin
   pc1.onIceConnectionStateChange.onEach { ... }.launchIn(this)
   pc2.onIceConnectionStateChange.onEach { ... }.launchIn(this)
   pc1.onConnectionStateChange.onEach { ... }.launchIn(this)
   pc2.onConnectionStateChange.onEach { ... }.launchIn(this)
   ```
   记录连接状态变化日志。

5. **处理轨道事件**：
   ```kotlin
   pc1.onTrack.onEach { ... }.launchIn(this)
   pc2.onTrack.onEach { ... }
       .map { it.track }
       .filterNotNull()
       .onEach { ... }
       .launchIn(this)
   ```
   监听轨道事件，并根据轨道类型调用相应的回调函数。

6. **创建和交换 SDP**：
   ```kotlin
   val offer = pc1.createOffer(OfferAnswerOptions(offerToReceiveVideo = true, offerToReceiveAudio = true))
   pc1.setLocalDescription(offer)
   pc2.setRemoteDescription(offer)
   val answer = pc2.createAnswer(options = OfferAnswerOptions())
   pc2.setLocalDescription(answer)
   pc1.setRemoteDescription(answer)
   ```
   创建和交换 SDP offer 和 answer。

7. **保持协程运行**：
   ```kotlin
   awaitCancellation()
   ```
   保持协程运行，直到被取消。