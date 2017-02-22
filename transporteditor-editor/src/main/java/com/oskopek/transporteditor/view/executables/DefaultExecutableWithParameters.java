package com.oskopek.transporteditor.view.executables;

import javaslang.collection.Stream;
import javaslang.control.Try;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simple {@link java.nio} based implementation of {@link ExecutableWithParameters}.
 */
public final class DefaultExecutableWithParameters implements ExecutableWithParameters {

    private static final transient Logger logger = LoggerFactory.getLogger(DefaultExecutableWithParameters.class);

    private final String executable;
    private final String parameters;
    private final transient List<Path> executablesOnPath;

    /**
     * Default constructor.
     *
     * @param executable an executable (either a file, or a command on the current system PATH).
     * @param parameters a parametrized string (containing {0}, {1}, ....)
     */
    public DefaultExecutableWithParameters(String executable, String parameters) {
        this.executable = executable.trim();
        this.parameters = parameters.trim();
        this.executablesOnPath = new ArrayList<>();
    }

    /**
     * A simple greedy regex-based implementation of {@link #findExecutablePath()} that looks at the system
     * {@code PATH} variable.
     *
     * @param executable the executable command
     * @return empty if we couldn't find such an executable, else its path
     */
    public Optional<Path> findExecutablePath(String executable) {
        if (Files.isExecutable(Paths.get(executable))) {
            return Optional.of(executable).map(Paths::get);
        }
        if (executablesOnPath.isEmpty()) {
            populateExecutablePaths();
        }
        return executablesOnPath.stream().filter(p -> p.getFileName().toString().equals(executable)).findFirst();
    }

    /**
     * Populates the {@code executablesOnPath} list serving as a cache from the filesystem and the environment
     * variable PATH.
     */
    private void populateExecutablePaths() {
        String path = System.getenv("PATH");
        String[] pathFolders;
        if (File.separatorChar == '/') {
            pathFolders = path.split(":");
        } else if (File.separatorChar == '\\') {
            pathFolders = path.split(";");
        } else {
            logger.warn("Found unknown PATH env variable format: \"{}\". Cannot populate executables.", path);
            return;
        }

        Stream<Path> pathVariableFiles = Stream.of(pathFolders).map(Paths::get).filter(Files::isRegularFile);
        Stream<Path> pathVariableFolderContents = Stream.of(pathFolders).map(Paths::get).filter(Files::isDirectory).map(
                d -> Try.of(() -> Files.list(d))).filter(Try::isSuccess).map(Try::get).flatMap(
                s -> Stream.ofAll(s.collect(Collectors.toList())));
        executablesOnPath.clear();
        executablesOnPath.addAll(pathVariableFiles.appendAll(pathVariableFolderContents).filter(Files::isExecutable)
                .toJavaList());
    }

    @Override
    public String getExecutable() {
        return executable;
    }

    @Override
    public Optional<Path> findExecutablePath() {
        return findExecutablePath(executable);
    }

    @Override
    public String getParameters() {
        return parameters;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getExecutable())
                .append(parameters)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultExecutableWithParameters)) {
            return false;
        }
        DefaultExecutableWithParameters that = (DefaultExecutableWithParameters) o;
        return new EqualsBuilder().append(getExecutable(), that.getExecutable())
                .append(parameters, that.parameters)
                .isEquals();
    }
}
