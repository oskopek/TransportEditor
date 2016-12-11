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
import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.Collectors;

public final class DefaultExecutableWithParameters implements ExecutableWithParameters {

    private static final transient Logger logger = LoggerFactory.getLogger(DefaultExecutableWithParameters.class);

    private final String executable;
    private final String parameters;

    /**
     * @param executable an executable (either a file, or a command on the current system PATH).
     * @param parameters a parametrized string (containing {0}, {1}, ....)
     */
    public DefaultExecutableWithParameters(String executable, String parameters) {
        this.executable = executable.trim();
        this.parameters = parameters.trim();
    }

    public static Optional<Path> findExecutablePath(String executable) {
        if (Files.isExecutable(Paths.get(executable))) {
            return Optional.of(executable).map(Paths::get);
        }
        String path = System.getenv("PATH");
        String[] pathFolders;
        if (File.separatorChar == '/') {
            pathFolders = path.split(":");
        } else if (File.separatorChar == '\\') {
            pathFolders = path.split(";");
        } else {
            logger.warn("Found unknown PATH env variable format: \"{}\". Cannot verify if executable is valid.", path);
            return Optional.empty();
        }

        Stream<Path> pathVariableFiles = Stream.of(pathFolders).map(Paths::get).filter(Files::isRegularFile);
        Stream<Path> pathVariableFolderContents = Stream.of(pathFolders).map(Paths::get).filter(Files::isDirectory).map(
                d -> Try.of(() -> Files.list(d))).filter(Try::isSuccess).map(Try::get).flatMap(
                s -> Stream.ofAll(s.collect(Collectors.toList())));
        return pathVariableFiles.appendAll(pathVariableFolderContents).filter(Files::isExecutable).filter(
                p -> p.getFileName().toString().equals(executable)).toJavaStream().findFirst();
    }

    @Override
    public String getExecutable() {
        return executable;
    }

    @Override
    public Optional<Path> findExecutablePath() {
        return DefaultExecutableWithParameters.findExecutablePath(executable);
    }

    @Override
    public String getExecutableCommand(Object... params) {
        return MessageFormat.format(executable + " " + parameters, params);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getExecutable())
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
        return new EqualsBuilder()
                .append(getExecutable(), that.getExecutable())
                .append(parameters, that.parameters)
                .isEquals();
    }
}
