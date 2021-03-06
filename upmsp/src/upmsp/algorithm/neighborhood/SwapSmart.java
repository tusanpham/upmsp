package upmsp.algorithm.neighborhood;

import upmsp.model.*;
import upmsp.model.solution.*;

import java.util.*;

/**
 * This class represents a Swap Move. A neighbor in the Swap Neighborhood is generated by swapping two jobs between two
 * machines. Note that the swapped jobs may be placed in any position on the other machine, i.e. the original positions
 * are not taken into account. This Move first removes the two jobs and then reinserts them in random positions.
 *
 * @author Tulio Toffolo
 */
public class SwapSmart extends Move {

    private Machine machine1, machine2;
    private int pos1M1, pos2M1, pos1M2, pos2M2, job1, job2;
    private boolean useMakespanMachine;

    /**
     * Instantiates a new Swap Move.
     *
     * @param problem            problem.
     * @param random             random number generator.
     * @param priority           the priority of this neighborhood.
     * @param useMakespanMachine true if the makespan machine should be always considered or false otherwise.
     */
    public SwapSmart(Problem problem, Random random, int priority, boolean useMakespanMachine) {
        super(problem, random, "SwapSmart" + (useMakespanMachine ? "(mk)" : ""), priority);
        this.useMakespanMachine = useMakespanMachine;
    }

    public void accept() {
        super.accept();
    }

    public int doMove(Solution solution) {
        super.doMove(solution);

        // selecting machines to involve in operation
        if (useMakespanMachine && solution.makespanMachine.getNJobs() > 0) {
            int m;
            do {
                m = random.nextInt(solution.machines.length);
            }
            while (m == solution.makespanMachine.id || solution.machines[m].getNJobs() == 0);

            machine1 = solution.makespanMachine;
            machine2 = solution.machines[m];
        }
        else {
            int m1, m2;
            do {
                m1 = random.nextInt(solution.machines.length);
                m2 = random.nextInt(solution.machines.length);
            }
            while (m1 == m2 || solution.machines[m1].getNJobs() == 0 || solution.machines[m2].getNJobs() == 0);
            machine1 = solution.machines[m1];
            machine2 = solution.machines[m2];
        }

        // selecting jobs to perform operation
        pos1M1 = random.nextInt(machine1.getNJobs());
        pos1M2 = random.nextInt(machine2.getNJobs());
        job1 = machine1.jobs[pos1M1];
        job2 = machine2.jobs[pos1M2];

        // removing jobs
        machine1.delJob(pos1M1);
        machine2.delJob(pos1M2);

        // selecting position to insert in machine 1
        pos2M1 = 0;
        int cost = Integer.MAX_VALUE;
        for (int p = 0; p <= machine1.getNJobs(); p++) {
            int simulatedCost = machine1.getDeltaCostAddJob(job2, p);
            if (simulatedCost < cost) {
                cost = simulatedCost;
                pos2M1 = p;
            }
        }

        // selecting position to insert in machine2
        pos2M2 = 0;
        cost = Integer.MAX_VALUE;
        for (int p = 0; p <= machine2.getNJobs(); p++) {
            int simulatedCost = machine2.getDeltaCostAddJob(job1, p);
            if (simulatedCost < cost) {
                cost = simulatedCost;
                pos2M2 = p;
            }
        }

        machine1.addJob(job2, pos2M1);
        machine2.addJob(job1, pos2M2);

        solution.updateCost();
        return deltaCost = solution.getCost() - initialCost;
    }

    public boolean hasMove(Solution solution) {
        return solution.getNMachines() > 1;
    }

    public void reject() {
        super.reject();

        machine1.delJob(pos2M1);
        machine2.delJob(pos2M2);
        machine1.addJob(job1, pos1M1);
        machine2.addJob(job2, pos1M2);
        currentSolution.updateCost();
    }
}
