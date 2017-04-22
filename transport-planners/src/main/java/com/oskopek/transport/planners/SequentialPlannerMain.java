package com.oskopek.transport.planners;

import com.oskopek.transport.model.domain.Domain;
import com.oskopek.transport.model.plan.Plan;
import com.oskopek.transport.model.planner.Planner;
import com.oskopek.transport.model.problem.Problem;
import com.oskopek.transport.persistence.DefaultProblemIO;
import com.oskopek.transport.persistence.IOUtils;
import com.oskopek.transport.persistence.SequentialPlanIO;
import com.oskopek.transport.persistence.VariableDomainIO;
import com.oskopek.transport.planners.sequential.*;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main class for running sequential planners.
 */
public final class SequentialPlannerMain {

//    private static final Planner planner = new FastDownwardExternalPlanner("--alias seq-sat-lama-2011 {0} {1}");
//    private static final Planner planner = new FastDownwardExternalPlanner("{0} {1}"
// + " --heuristic hff=ff() --heuristic hcea=cea() --search lazy_greedy([hff,hcea],preferred=[hff,hcea])");
//    private static final Planner planner = new FastDownwardExternalPlanner("{0} {1} --search astar(ff())");
//    private static final Planner planner = new ForwardAstarPlanner();
//    private static final Planner planner = new RandomizedRestartFromLocationPlanner();
//    private static final Planner planner = new RandomizedRestartOnPathPlanner();
//    private static final Planner planner = new RandomizedRestartOnPathNearbyPlanner();
    private static final Planner planner = new RandomizedRestartAroundPathNearbyPlanner();
//    private static final Planner planner = new RandomizedBacktrackRestartAroundPathNearbyPlanner();
//    private static final Planner planner = new RandomizedRestartAroundPathDistributionPlanner();

    private static final Logger logger = LoggerFactory.getLogger(SequentialPlannerMain.class);

    /**
     * Private empty constructor.
     */
    private SequentialPlannerMain() {
        // intentionally empty
    }

    /**
     * The main method. Takes two args, the domain and the problem file in PDDL. The domain
     * has to be a sequential transport domain.
     *
     * @param args the command-line arguments
     * @throws IOException if an error during loading the problem or saving the plan occurs
     */
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
