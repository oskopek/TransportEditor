package com.oskopek.transporteditor.planners;

import com.oskopek.transporteditor.model.domain.Domain;
import com.oskopek.transporteditor.model.plan.Plan;
import com.oskopek.transporteditor.model.planner.Planner;
import com.oskopek.transporteditor.model.problem.Problem;
import com.oskopek.transporteditor.persistence.DefaultProblemIO;
import com.oskopek.transporteditor.persistence.IOUtils;
import com.oskopek.transporteditor.persistence.SequentialPlanIO;
import com.oskopek.transporteditor.persistence.VariableDomainIO;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PlannerMain {

//    private static final Planner planner = new FastDownwardExternalPlanner("--alias seq-sat-lama-2011 {0} {1}");
//    private static final Planner planner = new FastDownwardExternalPlanner("{0} {1}  --heuristic hff=ff() --heuristic hcea=cea() --search lazy_greedy([hff,hcea],preferred=[hff,hcea])");
//    private static final Planner planner = new FastDownwardExternalPlanner("{0} {1} --search astar(ff())");
//    private static final Planner planner = new SequentialForwardBFSPlanner();
    private static final Planner planner = new SequentialForwardAstarPlanner();

    private static final Logger logger = LoggerFactory.getLogger(PlannerMain.class);

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Try.run(() -> Thread.sleep(200));
            logger.debug("Shutting down ...");

            if (planner.isPlanning().getValue()) {
                planner.cancel();
                Try.run(() -> Thread.sleep(5000));
            }
            logger.debug("Shut down.");
        }));


        Domain domain;
        try (InputStream inputStream = Files.newInputStream(Paths.get(args[0]))) {
            domain = new VariableDomainIO().parse(IOUtils.concatReadAllLines(inputStream));
        }
        Problem problem;
        try (InputStream inputStream = Files.newInputStream(Paths.get(args[1]))) {
            problem = new DefaultProblemIO(domain).parse(IOUtils.concatReadAllLines(inputStream));
        }

        Plan plan = planner.startAndWait(domain, problem);
        if (plan != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("out.val"))) {
                writer.write(new SequentialPlanIO(domain, problem).serialize(plan));
            }
        } else {
            logger.debug("Planner returned null plan.");
        }
        logger.debug("Written results, exiting.");
    }

}
