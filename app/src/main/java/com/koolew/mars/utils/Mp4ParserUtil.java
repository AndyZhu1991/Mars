package com.koolew.mars.utils;

import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jinchangzhu on 6/13/15.
 */
public class Mp4ParserUtil {

    private static final String TAG = "koolew-Mp4ParserUtil";
    private static final boolean DEBUG = true;

    private static final String VIDEO_TRACK_HANDLER_KEY = "vide";
    private static final String SOUND_TRACK_HANDLER_KEY = "soun";


    public static void clip(String inMoviePath, double startTime, double endTime,
                            String outMoviePath) throws IOException {
        Movie movie = MovieCreator.build(inMoviePath);

        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        // remove all tracks we will create new tracks from the old
        boolean timeCorrected = false;

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)

                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
                startTime = correctTimeToSyncSample(track, startTime, false);
                endTime = correctTimeToSyncSample(track, endTime, true);
                timeCorrected = true;
            }
        }

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = -1;
            long startSample1 = -1;
            long endSample1 = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                long delta = track.getSampleDurations()[i];


                if (currentTime > lastTime && currentTime <= startTime) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample;
                }
                if (currentTime > lastTime && currentTime <= endTime) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample;
                }
                lastTime = currentTime;
                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
        }
        Container out = new DefaultMp4Builder().build(movie);
        FileOutputStream fos = new FileOutputStream(outMoviePath);
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fc.close();
        fos.close();
    }


    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

    public static void mp4Cat(List<String> inMoviesPath, String outMoviePath) throws IOException {

        List<Movie> inMovies = new LinkedList<Movie>();
        for (String movie: inMoviesPath) {
            inMovies.add(MovieCreator.build(movie));
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals(SOUND_TRACK_HANDLER_KEY)) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals(VIDEO_TRACK_HANDLER_KEY)) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }
        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        FileChannel fc = new RandomAccessFile(String.format(outMoviePath), "rw").getChannel();
        out.writeContainer(fc);
        fc.close();
    }


    /**
     *
     * @param filePath the absolute path of movie file
     * @return movie time length in second
     * @throws IOException
     */
    public static final double getDuration(String filePath) throws IOException {
        IsoFile isoFile = new IsoFile(filePath);
        return ((double) isoFile.getMovieBox().getMovieHeaderBox().getDuration())
                / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
    }

    public static void setVideoBgm(String videoPath, String bgmPath, String outMoviePath) throws IOException {
        String clipedBgm = new File(bgmPath).getParent() + "clipedBgm.mp4";
        double videoLen = getDuration(videoPath);
        Log.d("stdzhu", "origin: " + videoLen);
        clip(bgmPath, 0, videoLen, clipedBgm);

        Movie result = new Movie();

        for (Track track: MovieCreator.build(videoPath).getTracks()) {
            if (track.getHandler().equals(VIDEO_TRACK_HANDLER_KEY)) {
                result.addTrack(track);
            }
        }

        for (Track track: MovieCreator.build(clipedBgm).getTracks()) {
            if (track.getHandler().equals(SOUND_TRACK_HANDLER_KEY)) {
                result.addTrack(track);
            }
        }

        Container out = new DefaultMp4Builder().build(result);

        FileChannel fc = new RandomAccessFile(String.format(outMoviePath), "rw").getChannel();
        out.writeContainer(fc);
        fc.close();

        Log.d("stdzhu", "bgmed: " + getDuration(outMoviePath));
    }
}
