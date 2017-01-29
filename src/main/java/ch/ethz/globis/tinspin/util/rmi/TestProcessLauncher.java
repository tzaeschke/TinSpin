/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.util.rmi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestProcessLauncher implements Runnable {

	private final Process process;

	private TestProcessLauncher(Process process) {
		this.process = process;
	}

	public static Process launchProcess(String optionsAsString, Class<?> mainClass, 
			String[] arguments) {
		try {
			ProcessBuilder processBuilder = createProcess(optionsAsString, mainClass, arguments);
			processBuilder.redirectErrorStream(true);
			Process prc = processBuilder.start();
			TestProcessLauncher l = new TestProcessLauncher(prc);

			//output monitor thread
			new Thread(l).start();

			return prc;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static ProcessBuilder createProcess(String optionsAsString, Class<?> cls, 
			String[] arguments) {
		String jvm = 
				System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		String cp = System.getProperty("java.class.path");

		String[] options = optionsAsString.split(" ");
		List<String> command = new ArrayList <>();
		command.add(jvm);
		command.addAll(Arrays.asList(options));
		command.add(cls.getName());
		command.addAll(Arrays.asList(arguments));

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Map< String, String > environment = processBuilder.environment();
		environment.put("CLASSPATH", cp);
		return processBuilder;
	}

	public static void launchRmiRegistry() {
		//check if it exists
		try {
			Registry registry = LocateRegistry.getRegistry();
			registry.lookup("Hello");
		} catch (ConnectException e) {
			//that's fine, that means we need to start it
		} catch (NotBoundException e) {
			//that's fine, that means it's running
			System.out.println("Found RMI process already running");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		
		try {
			ProcessBuilder processBuilder = createRmiProcess();
			processBuilder.redirectErrorStream(true);
			Process prc = processBuilder.start();
			TestProcessLauncher l = new TestProcessLauncher(prc);

			//output monitor thread
			new Thread(l).start();

			System.out.println("Waiting for RMI to start");
			for (int i = 0; i < 5; i++) {
				Thread.sleep(1000);
				//TODO we could try a rebind here an stop sleeping if it succeeds 
//				if (prc.isAlive()) {
//					break;
//				}
//				System.out.println("WPPPP" + i);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}


	private static ProcessBuilder createRmiProcess() {
		String cmd = System.getProperty("java.home") + 
				File.separator + "bin" + File.separator + "rmiregistry";
		String cp = System.getProperty("java.class.path");

		List<String> command = new ArrayList <>();
		command.add(cmd);

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Map< String, String > environment = processBuilder.environment();
		environment.put("CLASSPATH", cp);
		return processBuilder;
	}


	@Override
	public void run() {
		try {
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			System.out.println("Program terminated!");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}