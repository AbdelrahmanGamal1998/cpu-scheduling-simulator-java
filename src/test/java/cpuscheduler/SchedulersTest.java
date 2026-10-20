package cpuscheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Each scheduler is checked against a worked textbook example (or the one in the assignment brief). */
class SchedulersTest {

	/** One row per process: name, arrival time, burst time, priority. */
	private static Input input(int quantum, Object[]... rows) {
		Input in = new Input();
		in.numberOfProcesses = rows.length;
		in.timeQuantum = quantum;
		Input.timeContext = 0;
		for (Object[] row : rows) {
			in.names.add((String) row[0]);
			in.colors.add("blue");
			in.arrivalTimes.add((Integer) row[1]);
			in.burstTimes.add((Integer) row[2]);
			in.priorityNumbers.add((Integer) row[3]);
		}
		return in;
	}

	@Test
	void shortestJobFirstRunsTheShortestArrivedJob() {
		Input in = input(4, new Object[] { "P1", 0, 7, 0 }, new Object[] { "P2", 2, 4, 0 },
				new Object[] { "P3", 4, 1, 0 }, new Object[] { "P4", 5, 4, 0 });

		SJB_NonPre sjf = new SJB_NonPre(in);

		List<String> order = new ArrayList<>();
		sjf.processes.forEach(p -> order.add(p.name));
		assertEquals(List.of("P1", "P3", "P2", "P4"), order);
		assertEquals(4.0, sjf.averageWaitingTime, 1e-9);
		assertEquals(8.0, sjf.averageTurnaroundTime, 1e-9);
	}

	@Test
	void shortestJobFirstWaitsWhenTheCpuIsIdle() {
		// P2 only arrives at time 10, after P1 has finished at time 3.
		Input in = input(4, new Object[] { "P1", 0, 3, 0 }, new Object[] { "P2", 10, 2, 0 });

		SJB_NonPre sjf = new SJB_NonPre(in);

		assertEquals(10, sjf.processes.get(1).beginTime);
		assertEquals(12, sjf.processes.get(1).endTime);
		assertEquals(0.0, sjf.averageWaitingTime, 1e-9);
	}

	@Test
	void shortestRemainingTimeFirstPreemptsForShorterJobs() {
		Input in = input(4, new Object[] { "P1", 0, 7, 0 }, new Object[] { "P2", 2, 4, 0 },
				new Object[] { "P3", 4, 1, 0 }, new Object[] { "P4", 5, 4, 0 });

		SRTF srtf = new SRTF(in);

		assertEquals(3.0, srtf.AWT, 1e-9);
		assertEquals(7.0, srtf.ATAT, 1e-9);
	}

	@Test
	void priorityRunsTheHighestPriorityFirst() {
		// A lower number means a higher priority. All processes arrive together.
		Input in = input(4, new Object[] { "P1", 0, 10, 3 }, new Object[] { "P2", 0, 1, 1 },
				new Object[] { "P3", 0, 2, 4 }, new Object[] { "P4", 0, 1, 5 }, new Object[] { "P5", 0, 5, 2 });

		PriorityScheduling priority = new PriorityScheduling(in);

		List<String> order = new ArrayList<>();
		priority.save.forEach(s -> order.add(s.name));
		assertEquals(List.of("P2", "P5", "P1", "P3", "P4"), order);
		assertEquals(8.2, priority.AvgWT, 1e-9);
		assertEquals(12.0, priority.AvgTAT, 1e-9);
	}

	/** The worked AG example from the assignment brief. */
	@Test
	void agSchedulingMatchesTheExampleInTheAssignment() {
		Input in = input(4, new Object[] { "P1", 0, 17, 4 }, new Object[] { "P2", 3, 6, 9 },
				new Object[] { "P3", 4, 10, 3 }, new Object[] { "P4", 29, 4, 8 });

		AG ag = new AG(in);

		List<String> order = new ArrayList<>();
		List<Integer> boundaries = new ArrayList<>();
		for (AG.save s : ag.outPut) {
			order.add(s.name);
			boundaries.add(s.BT);
		}
		assertEquals(List.of("P1", "P2", "P3", "P1", "P3", "P2", "P3", "P1", "P2", "P1", "P4"), order);
		assertEquals(List.of(3, 5, 9, 12, 17, 20, 21, 25, 26, 33, 37), boundaries);
		assertEquals(11.0, ag.getAwt(), 1e-9);
		assertEquals(20.25, ag.getATAT(), 1e-9);
	}

	/** The quantum rows of the example: the initial quantums, then every update in order. */
	@Test
	void agSchedulingRecordsTheQuantumHistory() {
		Input in = input(4, new Object[] { "P1", 0, 17, 4 }, new Object[] { "P2", 3, 6, 9 },
				new Object[] { "P3", 4, 10, 3 }, new Object[] { "P4", 29, 4, 8 });

		AG ag = new AG(in);

		List<String> history = new ArrayList<>();
		ag.quantumHistory.forEach(h -> history.add(h.name + " " + h.quantumTime));
		assertEquals(List.of("P1 4", "P2 4", "P3 4", "P4 4", "P1 5", "P2 6", "P3 5", "P1 7", "P3 6", "P2 9",
				"P3 0", "P1 10", "P2 0", "P1 0", "P4 0"), history);
	}
}