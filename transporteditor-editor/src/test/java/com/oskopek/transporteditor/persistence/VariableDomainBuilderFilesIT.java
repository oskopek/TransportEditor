package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.model.problem.DefaultProblem;
import com.oskopek.transporteditor.test.TestUtils;
import static org.junit.Assert.*;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class VariableDomainBuilderFilesIT {

    private String fileName;
    private VariableDomainIO domainIO;

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
        for (String domainType : Arrays.asList("sequential", "temporal")) {
            for (javaslang.collection.List<Integer> chosen : javaslang.collection.List.ofAll(indices).combinations()) {
                String fileName = "domain-variants" + File.separator + domainType + File.separator + "%s"
                        + File.separator + "%s-";
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
        Assume.assumeTrue("Numeric tests are ignored for now.", fileName.contains("NoNum")); // TODO: Num
        String domainFile = String.format(fileName, "domain", "domain");
        String problemFile = String.format(fileName, "problem", "p01");

        VariableDomain domain = domainIO
                .parse(TestUtils.readAllConcatenatedLines(getClass().getResourceAsStream(domainFile)));
        assertNotNull(domain);

        DefaultProblemIO problemIO = new DefaultProblemIO(domain);
        DefaultProblem problem = problemIO
                .parse(TestUtils.readAllConcatenatedLines(getClass().getResourceAsStream(problemFile)));
        assertNotNull(problem);
    }

}
