package org.optaplanner.examples.app;

import static java.util.stream.Collectors.toConcurrentMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AllExamplesSolutionImporterTest {

    private static SortedMap<Path, String> readUnsolvedDataDirectory() throws IOException {
        return Files.walk(Paths.get("data/"))
                .parallel()
                .map(Path::toAbsolutePath)
                .filter(p -> p.toFile().isFile())
                .filter(p -> p.toString().contains("unsolved"))
                .collect(Collectors.collectingAndThen(
                        toConcurrentMap(Function.identity(),
                                path -> {
                                    try {
                                        return DigestUtils.md5Hex(Files.readAllBytes(path));
                                    } catch (IOException ex) {
                                        throw new IllegalStateException("Failed reading " + path);
                                    }
                                }),
                        TreeMap::new));
    }

    private SortedMap<Path, String> fileChecksums;

    @BeforeEach
    public void loadData() throws IOException {
        fileChecksums = readUnsolvedDataDirectory();
    }

    @Test
    public void ensureReproducibleImporters() throws IOException {
        AllExamplesSolutionImporter.main(new String[0]);
        Map<Path, String> newFileChecksums = readUnsolvedDataDirectory();
        // Compares checksums of all unsolved data sets to the ones before this test started.
        Assertions.assertThat(newFileChecksums).containsExactlyEntriesOf(fileChecksums);
    }

}
