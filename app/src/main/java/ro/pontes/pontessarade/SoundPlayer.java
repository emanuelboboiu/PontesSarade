package ro.pontes.pontessarade;

import android.content.Context;
import android.media.MediaPlayer;

/*
 * This is a class which contains only static methods to play sound in different ways.
 * This class is created by Manu, rewritten on 15 January 2015, early in the morning.
 */

public class SoundPlayer {

    public static long durationForWait = 0; // the duration for thread.sleep in
    // playWaitFinal.

    // A method to play sound, a static one:
    public static void playSimple(Context context, String fileName) {
        if (MainActivity.isSound) {
            MediaPlayer mp = new MediaPlayer();

            int resID;
            resID = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
            mp = MediaPlayer.create(context, resID);

            mp.start();

            mp.setOnCompletionListener(MediaPlayer::release);
        } // end if is sound activated.
    } // end static method playSimple.

    // A method to play wait final, a static one:
    public static void playWaitFinal(final Context context, final String fileName) {
        if (MainActivity.isSound) {
            // Play in another thread, this way it is possible to be better the
            // playWait method of the SoundPlayer class:
            new Thread(() -> {

                MediaPlayer mp = new MediaPlayer();

                int resID = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
                mp = MediaPlayer.create(context, resID);

                mp.start();
                // Determine the duration of the sound:
                durationForWait = mp.getDuration();
                mp.setOnCompletionListener(MediaPlayer::release);
            }).start();
            // Try to make sleep until the sound is played:
            try {
                Thread.sleep(durationForWait + 15);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        } // end if is sound activated.
    } // end static method playWaitFinal.

} // end sound player class.
