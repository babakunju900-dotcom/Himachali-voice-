package com.example.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class StarKingVoiceEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _isMicEnabled = MutableStateFlow(false)
    val isMicEnabled: StateFlow<Boolean> = _isMicEnabled.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _micDecibelLevel = MutableStateFlow(0f)
    val micDecibelLevel: StateFlow<Float> = _micDecibelLevel.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioWaveLevels = MutableStateFlow(List(7) { 0.1f })
    val audioWaveLevels: StateFlow<List<Float>> = _audioWaveLevels.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var onSpeakingStateChanged: ((Boolean) -> Unit)? = null

    init {
        // Default to speakerphone on entry to party room
        setSpeakerphone(true)
    }

    @SuppressLint("MissingPermission")
    fun startMicrophone(): Boolean {
        if (_isMicEnabled.value) return true

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (bufferSize <= 0) return false

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                channelConfig,
                audioFormat,
                maxOf(bufferSize, 2048)
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return false
            }

            audioRecord?.startRecording()
            _isMicEnabled.value = true
            _isMuted.value = false

            startAudioLevelProcessing(maxOf(bufferSize / 2, 1024))
            return true
        } catch (e: SecurityException) {
            _isMicEnabled.value = false
            return false
        } catch (e: Exception) {
            _isMicEnabled.value = false
            return false
        }
    }

    private fun startAudioLevelProcessing(bufferSize: Int) {
        recordJob?.cancel()
        recordJob = scope.launch {
            val audioBuffer = ShortArray(bufferSize)
            var lastSpeakingState = false

            while (isActive && _isMicEnabled.value) {
                val record = audioRecord ?: break
                if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) break

                val readCount = record.read(audioBuffer, 0, audioBuffer.size)
                if (readCount > 0 && !_isMuted.value) {
                    var sum = 0.0
                    for (i in 0 until readCount) {
                        val sample = audioBuffer[i].toDouble()
                        sum += sample * sample
                    }
                    val rms = sqrt(sum / readCount)
                    // Normalize RMS (typical speech is ~200 - 3000)
                    val normalized = (rms / 32767.0).toFloat().coerceIn(0f, 1f)
                    val amplified = (normalized * 8f).coerceIn(0f, 1f)
                    _micDecibelLevel.value = amplified

                    val currentlySpeaking = amplified > 0.06f
                    if (currentlySpeaking != lastSpeakingState) {
                        lastSpeakingState = currentlySpeaking
                        _isSpeaking.value = currentlySpeaking
                        withContext(Dispatchers.Main) {
                            onSpeakingStateChanged?.invoke(currentlySpeaking)
                        }
                    }

                    // Update waveform visualizer bars
                    val waves = List(7) { idx ->
                        val base = (amplified * (0.6f + (idx % 3) * 0.2f)).coerceIn(0.08f, 1f)
                        base
                    }
                    _audioWaveLevels.value = waves
                } else {
                    _micDecibelLevel.value = 0f
                    if (lastSpeakingState) {
                        lastSpeakingState = false
                        _isSpeaking.value = false
                        withContext(Dispatchers.Main) {
                            onSpeakingStateChanged?.invoke(false)
                        }
                    }
                    _audioWaveLevels.value = List(7) { 0.08f }
                }

                delay(60)
            }
        }
    }

    fun toggleMute() {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        if (newMute) {
            _isSpeaking.value = false
            onSpeakingStateChanged?.invoke(false)
        }
    }

    fun stopMicrophone() {
        _isMicEnabled.value = false
        _isSpeaking.value = false
        _micDecibelLevel.value = 0f
        onSpeakingStateChanged?.invoke(false)
        recordJob?.cancel()
        recordJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    fun setSpeakerphone(on: Boolean) {
        _isSpeakerOn.value = on
        try {
            audioManager?.isSpeakerphoneOn = on
            if (on) {
                audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            } else {
                audioManager?.mode = AudioManager.MODE_NORMAL
            }
        } catch (_: Exception) {}
    }

    fun toggleSpeakerphone() {
        setSpeakerphone(!_isSpeakerOn.value)
    }

    fun release() {
        stopMicrophone()
        scope.cancel()
    }
}
