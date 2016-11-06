package ch.ethz.globis.tinspin;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TestLogging {

	private final ArrayList<TestStats> stats = new ArrayList<>();
	private final ArrayList<TestStats> avgStats = new ArrayList<>();
	
	private Path logFolder = createLogFolder();
	private final ArrayList<TestStats> logBufferStats = new ArrayList<>();
	private final ArrayList<TestStats> logBufferAvgStats = new ArrayList<>();

	/**
	 * Prints all results to console.
	 */
	public void printSummary() {
		System.out.println("Summary-Avg:");
		System.out.println("============");
		for (TestStats s: avgStats) {
			System.out.println(s);
		}
		System.out.println("Summary:");
		System.out.println("========");
		for (TestStats s: stats) {
			System.out.println(s);
		}
	}

	/**
	 * Resets the internal result buffer.
	 */
	public void reset() {
		stats.clear();
		avgStats.clear();
		logBufferStats.clear();
		logBufferAvgStats.clear();
	}
	
	/**
	 * Add a result.
	 * @param ts
	 */
	public void logResult(TestStats ts) {
		stats.add(ts);
		logBufferStats.add(ts);
	}
	
	/**
	 * Calculates the average of the last n results.
	 * @param nStats
	 * @return a result instance with the average results
	 */
	public TestStats logResultAverage(int nStats) {
		TestStats avg = average(nStats);
		avgStats.add(avg);
		logBufferAvgStats.add(avg);
		return avg;
	}
	
	private TestStats average(int nStats) {
		int n = stats.size();
		List<TestStats> toAggregate = stats.subList(n-nStats, n); 
		TestStats avg = TestStats.aggregate(toAggregate);
		return avg;
	}

	/**
	 * Write all results to a file.
	 * This should be called after every test series, i.e. whenever
	 * switching to a new index or new dataset.
	 * 
	 * This methods writes results that accumulated after the
	 * previous call to this method. The results are written to the
	 * specified subfolder.
	 * 
	 * Also, this methods writes ALL results to a file called full.txt.
	 * 
	 * Results are stored in prjHome/target/logs.
	 * @param subDir 
	 */
	public void writeLogFileForTestSeries(String subDir) {
		writeLogFileFull();
		writeLogFileForSeries(subDir);
	}
	
	private void writeLogFileForSeries(String subDir) {
		TestStats avg = logBufferAvgStats.get(0);
		String name = avg.testDescription1() + "-" + avg.testDescription2();
		
		Path sub = Paths.get(logFolder.toString(), subDir);
		Path file = Paths.get(sub.toString(), name + ".txt");
		try {
			if (!Files.exists(sub)) {
				Files.createDirectory(sub);
			}
			
			int i = 1;
			while (Files.exists(file)) {
				file = Paths.get(sub.toString(), name + "(" + i + ").txt");
				i++;
			}
			Files.createFile(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        try (PrintWriter out = new PrintWriter(file.toFile())) {
        	for (String header: TestStats.testHeader()) {
        		out.println(header);
        	}
        	out.println("% Averages");
        	out.println("% ========");
        	for (TestStats ts: logBufferAvgStats) {
        		out.println(ts);
        	}
        	out.println("% Measurements");
        	out.println("% ============");
        	for (TestStats ts: logBufferStats) {
        		out.println(ts);
        	}
        } catch (IOException e) {
			throw new RuntimeException(e);
        }
        logBufferAvgStats.clear();
        logBufferStats.clear();
	}

	private void writeLogFileFull() {
		Path file = Paths.get(logFolder.toString(), "full.txt");
		Path fileBak = Paths.get(logFolder.toString(), "full.bak");
		try {
			if (Files.exists(fileBak)) {
				//just in case...
				Files.delete(fileBak);
			}
			if (Files.exists(file)) {
				Files.move(file, fileBak);
			}
			Files.createFile(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        try (PrintWriter out = new PrintWriter(file.toFile())) {
        	for (String header: TestStats.testHeader()) {
        		out.println(header);
        	}
        	out.println("% Averages");
        	out.println("% ========");
        	for (TestStats ts: avgStats) {
        		out.println(ts);
        	}
        	out.println("% Measurements");
        	out.println("% ============");
        	for (TestStats ts: stats) {
        		out.println(ts);
        	}
        } catch (IOException e) {
			throw new RuntimeException(e);
        }

		try {
			if (Files.exists(fileBak)) {
				Files.delete(fileBak);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Path createLogFolder() {
		SimpleDateFormat FT = new SimpleDateFormat ("yyyyMMdd-HHmmss");
		Path dir = Paths.get("target", "logs", FT.format(new Date()));
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return dir;
	}

	public List<TestStats> getAvgStats() {
		return Collections.unmodifiableList(avgStats);
	}
	
}
