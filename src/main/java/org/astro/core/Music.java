package org.astro.core;

import org.newdawn.slick.SlickException;

import java.util.ArrayList;
import java.util.List;

public class Music {
    public static final List<Utils.Pair<org.newdawn.slick.Music, Float>> tracks = new ArrayList<>();

    public static org.newdawn.slick.Music track;

    private static final int timeBetweenTracks = ClassSettings.loadInt("music/time between tracks", 5000);
    private static int time = 0;
    private static boolean betweenTracks = true;

    static {
        try {
            tracks.add(new Utils.Pair<>(new org.newdawn.slick.Music("music/track1.ogg"), 1f));
            tracks.add(new Utils.Pair<>(new org.newdawn.slick.Music("music/track2.ogg"), 1f));
            tracks.add(new Utils.Pair<>(new org.newdawn.slick.Music("music/track3.ogg"), 1f));
            tracks.add(new Utils.Pair<>(new org.newdawn.slick.Music("music/track4.ogg"), 1f));
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
    }

    public static void update() {
        if (null == track) {
            betweenTracks = true;
        } else if (!track.playing()) {
            betweenTracks = true;
        }

        if (betweenTracks) {
            time += Astro.delta;
            if (time >= timeBetweenTracks) {
                betweenTracks = false;
                time = 0;
                if (track == null) playRandom();
                else playRandom(track);
            }
        }
    }

    public static void playRandom() {
        int trackNum = Utils.randomRange(0, tracks.size() - 1);
        track = tracks.get(trackNum).left;
        track.play();
        track.setVolume(0);
        track.fade(ClassSettings.loadInt("music/fade duration", 1500), tracks.get(trackNum).right, false);
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).left == track) System.out.println("Playing track " + (i + 1));
        }
    }

    public static void playRandom(org.newdawn.slick.Music ignoreTrack) {
        int trackNum = Utils.randomRange(0, tracks.size() - 1);
        do track = tracks.get(trackNum).left;
        while (track == ignoreTrack);
        track.play();
        track.setVolume(0);
        track.fade(ClassSettings.loadInt("music/fade duration", 1500), tracks.get(trackNum).right, false);
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).left == track) System.out.println("Playing track " + (i + 1));
        }
    }
}
