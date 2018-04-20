import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class music {
	public static void main(String[] args) {
		File bgm = new File("backgroundMusic.wav");
		System.out.println("Music");
		PlaySound(bgm);
	}

	public static void PlaySound(File bgm) {
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(bgm));
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			Thread.sleep(clip.getMicrosecondLength() / 1000);
		} catch (Exception e) {
		}
	}
}