package at.tugraz.oop2.client;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.data.SOMQueryParameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class ClusterHandler {
    private static ClusterHandler instance = null;

    private SOMQueryParameters params;

    ClusterHandler() {
    }

    public static void result(final String cluster, final boolean all) throws CommandHandler.CommandException {
        final File folder = toDoCluster(cluster);
        final File[] results = folder.listFiles();
        if (results == null)
            Logger.info("Could not load files, possibly not a directory");
        final Stream<File> rstream = Arrays.asList(results).parallelStream();
    }

    public static final String TO_CLUST= "clusteringResults";
    public static File toDoCluster(final String cluster) throws CommandHandler.CommandException {
        try {
            final Path make = cluster == null ? Paths.get(TO_CLUST) : Paths.get(TO_CLUST, cluster);
            final File file = make.toFile();
            if (!file.isDirectory())
                throw new IOException("Not a directory");
            // Logger.err ("Not a directory");
            if (!file.exists())
                throw new IOException("Directory doesn't exist");
            //Logger.err("Directory doesn't exist");
            return file;
        } catch (final InvalidPathException | IOException | SecurityException e) {
            Logger.err(String.format("Exception not found: %s", e.getMessage()));
            throw new CommandHandler.CommandException("Could not open directory");
        }
    }
}
