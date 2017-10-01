
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jsr166e.ConcurrentHashMapV8;

public class SetTest {
	
	  /**
	   * @editStart: kenan
	   */
	  public static TimeCheckUtils preRenderTimeHelper = new TimeCheckUtils();
	  private static final int MAINTHREAD = 1;
	  private static final int ENABLE = 1;
	  private static final int DISABLE = 0;
	  /**
	   * @editEnd: kenan
	   */

	static class Lists {
		private String name;
		private Set<String> set;

		public Lists(String name, Set<String> set) {
			this.name = name;
			this.set = set;
		}

		public String getName() { return this.name; }
		public Set<String> getSet() { return this.set; }
	}

	public static void main(String... args) throws Exception {

		final int ITERATIONS = 10;
		final int THREADS = Integer.parseInt(args[0]);
		final int N = Integer.parseInt(args[1]);
		final int capacity = Integer.parseInt(args[2]);


		/**
		 * @editStart: kenan
		 */
		final int RMITERATION = 1;
		final int WARMUP = 3;
		final int NOWARMUP = 0;
		String socketNumCheck = EnergyCheckUtils.EnergyStatCheck();
		int sockNum = 0;
		int powerLimitEnable = 0;
		sockNum = socketNumCheck.contains("@") ? socketNumCheck.split("@").length : 1;

	    DataPrinter.printTitle(sockNum);
		/**
		 * @editEnd: kenan
		 */

		System.out.format("Conf: Iterations=%s, threads=%s, N=%s, capacity=%s\n", ITERATIONS, THREADS, N, capacity);
		
		Set<String> hashSet = new LinkedHashSet<>();
		Set<String> syncSet = Collections.synchronizedSet(new HashSet<String>());
		Set<String> copyOnWrite = new CopyOnWriteArraySet<>();
		Set<String> skipListSet = new ConcurrentSkipListSet<>();
		Set<String> concurrentHashSet = Collections.newSetFromMap((new ConcurrentHashMap<String, Boolean>()));
		Set<String> concurrentHashSetV8 = Collections.newSetFromMap((new ConcurrentHashMapV8<String, Boolean>()));

		List<Lists> lists = new ArrayList<>();
		lists.add(new Lists("hashSet", hashSet));
		lists.add(new Lists("syncSet", syncSet));
		lists.add(new Lists("skipListSet", skipListSet));
		lists.add(new Lists("concurrentHashSet", concurrentHashSet));
		lists.add(new Lists("concurrentHashSetV8", concurrentHashSetV8));
		lists.add(new Lists("copyOnWrite", copyOnWrite));

		for (final Lists list : lists) {
			//Kenan: Initializing data printer for write, traversalIterator and Get
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			write(list, THREADS, N, ITERATIONS);
			traversalIterator(list, THREADS, N, ITERATIONS);
			//Kenan: Reinitializing data printer for remove. No warmup.
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
			traversalRemove(list, THREADS, N);
			

			list.getSet().clear();
		}
	}
	static class Run implements Runnable {
		
		private Lists list;
		private int total, thread;
		
		public Run(Lists list, int thread, int total) {
			this.list = list;
			this.total = total;
			this.thread = thread;
		}
		
		@Override
		public void run() {
			for (int k = 0; k < total; k++) {
				list.getSet().add(k + "-" + thread);
			}
		}
	}
	
	static class Remover implements Runnable {
		private Lists list;
		private int total, current;
		
		public Remover(Lists list, int current, int total) {
			this.list = list;
			this.total = total;
			this.current = current;
		}
		
		@Override
		public void run() {
			for (int k = 0; k < total; k++) {
				String key = String.valueOf(k) + "-"+ current;
				list.getSet().remove(key);
			}
		}
	}

	private static void write(final Lists list, int threads, final int total,
			int iterations) throws InterruptedException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD);
		//Kenan
		for (int i = 0; i < iterations; i++) {
			//Kenan
			ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
			ener.wallClockTimeStart = System.currentTimeMillis()/1000.0;
			ener.preEnergy= EnergyCheckUtils.EnergyStatCheck();
			//Kenan
			
			ExecutorService executors = Executors.newFixedThreadPool(threads);
			for (int j = 0; j < threads; j++) {
				executors.execute(new Run(list, j, total));
			}
			executors.shutdown();
			executors.awaitTermination(1, TimeUnit.DAYS);

			/**
			 * @editStart: kenan
			 */
			ener.timeEpilogue = mainTimeHelper.getCurrentThreadTimeInfo();
			ener.wallClockTimeEnd = System.currentTimeMillis() / 1000.0;
			ener.postEnergy = EnergyCheckUtils.EnergyStatCheck();
			ener.dataReport();

			/**
			 * @editEnd: kenan
			 */
			
			if (list.getSet() instanceof CopyOnWriteArraySet) {
				break;
			}
		}

	}

	static void traversalIterator(final Lists list, final int threads, final int total,
			int iterations) throws InterruptedException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD);
		//Kenan
		
		for (int i = 0; i < iterations; i++) {
			//Kenan
			ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
			ener.wallClockTimeStart = System.currentTimeMillis()/1000.0;
			ener.preEnergy= EnergyCheckUtils.EnergyStatCheck();
			//Kenan
			
			ExecutorService executors = Executors.newFixedThreadPool(threads);
			for (int j = 0; j < threads; j++) {
				executors.execute(new Runnable() {
					@Override
					public void run() {
						for (String key : list.getSet()) {
							String e = key;
						}
					}
				});
			}
			executors.shutdown();
			executors.awaitTermination(1, TimeUnit.DAYS);

			/**
			 * @editStart: kenan
			 */
			ener.timeEpilogue = mainTimeHelper.getCurrentThreadTimeInfo();
			ener.wallClockTimeEnd = System.currentTimeMillis() / 1000.0;
			ener.postEnergy = EnergyCheckUtils.EnergyStatCheck();
			ener.dataReport();

			/**
			 * @editEnd: kenan
			 */
		}

	}
	
	static void traversalRemove(final Lists list, final int threads, final int total) throws InterruptedException {
		List<String> lastThree = new ArrayList<>();

		//Kenan

		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD);
		ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
		ener.wallClockTimeStart = System.currentTimeMillis()/1000.0;
		ener.preEnergy= EnergyCheckUtils.EnergyStatCheck();
		//Kenan
		
		ExecutorService executors = Executors.newFixedThreadPool(threads);
		for (int j = 0; j < threads; j++) {
			executors.execute(new Remover(list, j, total));
		}
		executors.shutdown();
		executors.awaitTermination(1, TimeUnit.DAYS);

		   /**
	       * @editStart: kenan
	       */
	      	ener.timeEpilogue= mainTimeHelper.getCurrentThreadTimeInfo();
			ener.wallClockTimeEnd  = System.currentTimeMillis()/1000.0;
			ener.postEnergy= EnergyCheckUtils.EnergyStatCheck();
			ener.dataReport();

	      /**
	       * @editEnd: kenan
	       */
	}
}
