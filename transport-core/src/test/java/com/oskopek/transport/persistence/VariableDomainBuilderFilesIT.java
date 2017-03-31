package com.oskopek.transport.persistence;

import com.oskopek.transport.model.domain.DomainType;
import com.oskopek.transport.model.domain.VariableDomain;
import com.oskopek.transport.model.problem.DefaultProblem;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.oskopek.transport.persistence.IOUtils.concatReadAllLines;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class VariableDomainBuilderFilesIT {

    private String fileName;
    private VariableDomainIO domainIO;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public VariableDomainBuilderFilesIT(String fileName) {
        this.fileName = fileName;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<String> fileNameCombinations() {
        List<String> output = new ArrayList<>();
        List<String> features = Arrays.asList("Cap", "Fuel", "Num");
        int[] indices = new int[features.size()];
        for (int i = 0; i < features.size(); i++) {
            indices[i] = i;
        }
        char separator = '/';
        for (DomainType domainType : DomainType.values()) {
            for (javaslang.collection.List<Integer> chosen : javaslang.collection.List.ofAll(indices).combinations()) {
                String fileName = "domain-variants" + separator + domainType.toString().toLowerCase()
                        + separator + "%s" + separator + "%s-";
                for (int i = 0; i < features.size(); i++) {
                    if (!chosen.contains(i)) {
                        fileName += "No";
                    }
                    fileName += features.get(i) + '-';
                }
                fileName = fileName.substring(0, fileName.length() - 1) + ".pddl";
                output.add(fileName);
            }
        }
        return output;
    }

    @Before
    public void setUp() throws Exception {
        domainIO = new VariableDomainIO();
    }

    @Test
    public void toDomain() throws Exception {
        Assume.assumeTrue("Numeric tests are ignored for now.", fileName.contains("NoNum")); // TODO: Numeric
        String domainFile = String.format(fileName, "domain", "domain");
        String problemFile = String.format(fileName, "problem", "p01");

        logger.debug("Parsing domain from {}", domainFile);
        VariableDomain domain = domainIO
                .parse(concatReadAllLines(getClass().getResourceAsStream(domainFile)));
        assertNotNull(domain);

        DefaultProblemIO problemIO = new DefaultProblemIO(domain);
        logger.debug("Parsing problem from {}", problemFile);
        DefaultProblem problem = problemIO
                .parseDefault(concatReadAllLines(getClass().getResourceAsStream(problemFile)));
        assertNotNull(problem);
    }

}
