package com.tetris.sound;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javafx.scene.media.AudioClip;

/**
 * Gerencia efeitos sonoros do jogo. Carrega arquivos em /sounds/*.wav dentro de resources.
 * Se os arquivos não estiverem presentes, os métodos são silenciosos (no-ops).
 */
public class SoundManager {

    private static final SoundManager INSTANCE = new SoundManager();

    private AudioClip lockClip;
    private AudioClip rotateClip;
    private AudioClip hardDropClip;
    private AudioClip clear1Clip;
    private AudioClip clear2Clip;
    private AudioClip clear3Clip;
    private AudioClip clear4Clip;
    private AudioClip pauseClip;
    private AudioClip gameOverClip;
    private AudioClip softDropClip;
    private AudioClip scoreClip;
    private AudioClip uiClickClip;

    private double volume = 1.0; // 0.0 - 1.0
    private boolean muted = false;
    private double previousVolume = 1.0;

    private SoundManager() {
        loadClips();
    }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    private AudioClip load(String name, double fallbackFreqHz, int fallbackMs) {
        try {
            URL url = getClass().getResource("/sounds/" + name);
            if (url != null) {
                try {
                    AudioClip ac = new AudioClip(url.toExternalForm());
                    ac.setVolume(volume);
                    return ac;
                } catch (Exception ex) {
                    // se falhar ao carregar, tentar fallback
                }
            }
        } catch (Exception e) {
            // ignore and try fallback
        }

        // Se recurso não existe ou falhou, gera um WAV simples em tempo de execução
        try {
            File tmp = generateToneWav(fallbackFreqHz, fallbackMs);
            if (tmp != null) {
                AudioClip ac = new AudioClip(tmp.toURI().toString());
                ac.setVolume(volume);
                return ac;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private File generateToneWav(double freqHz, int ms) throws IOException {
        final float sampleRate = 44100f;
        int numSamples = (int) ((ms / 1000.0) * sampleRate);
        byte[] data = new byte[2 * numSamples]; // 16-bit PCM

        // ADSR envelope parameters (fractions of total length)
        double attack = 0.02;  // 2%
        double decay = 0.08;   // 8%
        double sustainLevel = 0.7;
        double release = 0.15; // 15%

        int attackSamples = (int) (numSamples * attack);
        int decaySamples = (int) (numSamples * decay);
        int releaseSamples = (int) (numSamples * release);
        int sustainSamples = Math.max(0, numSamples - (attackSamples + decaySamples + releaseSamples));

        // generate waveform with a few harmonics for richer timbre
        for (int i = 0; i < numSamples; i++) {
            double t = i / sampleRate;
            // fundamental + harmonics (amplitudes drop with harmonic number)
            double value = 0.0;
            value += 1.0 * Math.sin(2.0 * Math.PI * freqHz * t); // fundamental
            value += 0.5 * Math.sin(2.0 * Math.PI * 2 * freqHz * t); // 2nd harmonic
            value += 0.25 * Math.sin(2.0 * Math.PI * 3 * freqHz * t); // 3rd
            value += 0.12 * Math.sin(2.0 * Math.PI * 4 * freqHz * t); // 4th

            // normalize harmonic sum
            value = value / (1.0 + 0.5 + 0.25 + 0.12);

            // apply ADSR envelope
            double env = 1.0;
            if (i < attackSamples) {
                env = (double) i / Math.max(1, attackSamples);
            } else if (i < attackSamples + decaySamples) {
                int di = i - attackSamples;
                double frac = (double) di / Math.max(1, decaySamples);
                env = 1.0 + (sustainLevel - 1.0) * frac; // linear decay to sustain level
            } else if (i < attackSamples + decaySamples + sustainSamples) {
                env = sustainLevel;
            } else {
                int ri = i - (attackSamples + decaySamples + sustainSamples);
                double frac = (double) ri / Math.max(1, releaseSamples);
                env = sustainLevel * (1.0 - frac); // release to zero
            }

            short sample = (short) (value * env * Short.MAX_VALUE * 0.6);
            data[2 * i] = (byte) (sample & 0xff);
            data[2 * i + 1] = (byte) ((sample >> 8) & 0xff);
        }

        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, numSamples)) {
            File tmp = File.createTempFile("tetris-tone-", ".wav");
            tmp.deleteOnExit();
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, tmp);
            return tmp;
        } catch (IOException ex) {
            return null;
        }
    }

    private void loadClips() {
    // tente carregar recursos reais; se não existirem, subistitui por tons gerados
    lockClip = load("lock.wav", 440.0, 160);
    rotateClip = load("rotate.wav", 660.0, 120);
    hardDropClip = load("harddrop.wav", 880.0, 220);
    clear1Clip = load("clear1.wav", 740.0, 180);
    clear2Clip = load("clear2.wav", 820.0, 200);
    clear3Clip = load("clear3.wav", 920.0, 220);
    clear4Clip = load("clear4.wav", 1020.0, 300);
    pauseClip = load("pause.wav", 220.0, 140);
    gameOverClip = load("gameover.wav", 120.0, 600);
    softDropClip = load("softdrop.wav", 700.0, 80);
    scoreClip = load("score.wav", 980.0, 120);
    uiClickClip = load("ui_click.wav", 300.0, 80);

    // Aplica volume inicial a todos os clips carregados
    applyVolumeToAll();
    }

    public void playLock() { if (lockClip != null) lockClip.play(); }
    public void playRotate() { if (rotateClip != null) rotateClip.play(); }
    public void playHardDrop() { if (hardDropClip != null) hardDropClip.play(); }
    public void playPause() { if (pauseClip != null) pauseClip.play(); }
    public void playGameOver() { if (gameOverClip != null) gameOverClip.play(); }

    public void playSoftDrop() { if (softDropClip != null) softDropClip.play(); }
    public void playScoreIncrease() { if (scoreClip != null) scoreClip.play(); }
    public void playUIButton() { if (uiClickClip != null) uiClickClip.play(); }

    public void playClear(int lines) {
        if (lines == 1 && clear1Clip != null) clear1Clip.play();
        else if (lines == 2 && clear2Clip != null) clear2Clip.play();
        else if (lines == 3 && clear3Clip != null) clear3Clip.play();
        else if (lines >= 4 && clear4Clip != null) clear4Clip.play();
    }

    // Volume and mute control
    public void setVolume(double vol) {
        if (vol < 0) vol = 0; if (vol > 1) vol = 1;
        this.volume = vol;
        if (!muted) applyVolumeToAll();
    }

    public double getVolume() { return this.volume; }

    public boolean isMuted() { return this.muted; }

    public void setMuted(boolean m) {
        if (this.muted == m) return;
        this.muted = m;
        if (m) {
            // save current volume and silence
            this.previousVolume = this.volume;
            applyVolumeToAll(0.0);
        } else {
            applyVolumeToAll(this.volume);
        }
    }

    public void toggleMute() { setMuted(!this.muted); }

    private void applyVolumeToAll() { applyVolumeToAll(this.volume); }

    private void applyVolumeToAll(double vol) {
        try {
            if (lockClip != null) lockClip.setVolume(vol);
            if (rotateClip != null) rotateClip.setVolume(vol);
            if (hardDropClip != null) hardDropClip.setVolume(vol);
            if (clear1Clip != null) clear1Clip.setVolume(vol);
            if (clear2Clip != null) clear2Clip.setVolume(vol);
            if (clear3Clip != null) clear3Clip.setVolume(vol);
            if (clear4Clip != null) clear4Clip.setVolume(vol);
            if (pauseClip != null) pauseClip.setVolume(vol);
            if (gameOverClip != null) gameOverClip.setVolume(vol);
            if (softDropClip != null) softDropClip.setVolume(vol);
            if (scoreClip != null) scoreClip.setVolume(vol);
            if (uiClickClip != null) uiClickClip.setVolume(vol);
        } catch (Exception e) {
            // Alguns runtimes podem não suportar operações, ignore com segurança
        }
    }
}
