package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.FileConfigSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

class FileChangeReloadStrategyTest {

    @Test
    public void changeContentsOfFile() throws GestaltException, IOException, InterruptedException {
        Path path;
        path = Files.createTempFile("gestalt", "test.properties");
        path.toFile().deleteOnExit();
        Files.write(path, "user=userA".getBytes(UTF_8));

        FileConfigSource source = new FileConfigSource(path);
        ConfigReloadStrategy strategy = new FileChangeReloadStrategy(source);

        ConfigListener listener = new ConfigListener();
        strategy.registerListener(listener);

        Files.write(path, "user=userB".getBytes(UTF_8));

        for (int i = 0; i < 5; i++) {
            if (listener.count > 1) {
                break;
            } else {
                Thread.sleep(10);
            }
        }

        Assertions.assertTrue(listener.count >= 1);
        strategy.removeListener(listener);

        Files.write(path, "user=userC".getBytes(UTF_8));

        Thread.sleep(100);

        int previousCount = listener.count;
        Assertions.assertEquals(previousCount, listener.count);
    }

    @Test
    public void changeContentsOfFileWithSubDir() throws GestaltException, IOException, InterruptedException {
        Path folder = Files.createTempDirectory("gestalt");
        Path path = folder.resolve("reloadedfile.properties");

        folder.toFile().mkdirs();
        folder.toFile().deleteOnExit();

        Files.write(path, "user=userA".getBytes(UTF_8));

        FileConfigSource source = new FileConfigSource(path);
        ConfigReloadStrategy strategy = new FileChangeReloadStrategy(source);

        ConfigListener listener = new ConfigListener();
        strategy.registerListener(listener);

        Thread.sleep(100);
        Files.write(path, "user=userB".getBytes(UTF_8));

        for (int i = 0; i < 5; i++) {
            if (listener.count > 1) {
                break;
            } else {
                Thread.sleep(10);
            }
        }

        Assertions.assertTrue(listener.count >= 1);

        strategy.removeListener(listener);

        Files.write(path, "user=userC".getBytes(UTF_8));

        Thread.sleep(100);

        int previousCount = listener.count;
        Assertions.assertEquals(previousCount, listener.count);
    }

    //to run this test it must be run as an administrator.
    @Test
    @Disabled
    public void changeContentsOfFileWithSymlinkChain() throws GestaltException, IOException, InterruptedException {
        Path folder = Files.createTempDirectory("gestalt");

        folder.toFile().mkdirs();
        folder.toFile().deleteOnExit();

        Path numbered1 = Files.createDirectory(folder.resolve("..10001"));
        Path numbered2 = Files.createDirectory(folder.resolve("..10002"));
        Path dataLn = Files.createSymbolicLink(folder.resolve("..data"), folder.relativize(numbered1));

        Path file1 = numbered1.resolve("reloadedfile.properties");
        Files.write(file1, "user=userA".getBytes(UTF_8));

        Path file2 = numbered2.resolve("reloadedfile.properties");
        Files.write(file2, "user=userB".getBytes(UTF_8));

        Path configFileLn = Files.createSymbolicLink(folder.resolve("reloadedfile.properties"),
            folder.relativize(dataLn).resolve("reloadedfile.properties"));

        FileConfigSource source = new FileConfigSource(configFileLn);
        ConfigReloadStrategy strategy = new FileChangeReloadStrategy(source);

        ConfigListener listener = new ConfigListener();
        strategy.registerListener(listener);

        Files.delete(dataLn);
        Files.createSymbolicLink(folder.resolve("..data"), folder.relativize(numbered2));
        for (int i = 0; i < 5; i++) {
            if (listener.count >= 1) {
                break;
            } else {
                Thread.sleep(100);
            }
        }

        Assertions.assertTrue(listener.count >= 1);

        strategy.removeListener(listener);
        // change the ..data link like Kubernetes does
        Files.delete(dataLn);
        Files.createSymbolicLink(folder.resolve("..data"), folder.relativize(numbered1));

        int previousCount = listener.count;
        Thread.sleep(100);

        Assertions.assertEquals(previousCount, listener.count);
    }

    private static class ConfigListener implements ConfigReloadListener {

        public int count = 0;

        @Override
        public void reload(ConfigSource source) {
            count++;
        }
    }
}
