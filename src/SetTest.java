
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
		
		Set<String> synchronizedLinkedHashSet = Collections.synchronizedSet(new LinkedHashSet<>());
		Set<String> synchronizedHashSet = Collections.synchronizedSet(new HashSet<String>());
		Set<String> copyOnWriteArraySet = new CopyOnWriteArraySet<>();
		Set<String> concurrentSkipListSet = new ConcurrentSkipListSet<>();
		Set<String> setFromConcurrentHashMap = Collections.newSetFromMap((new ConcurrentHashMap<String, Boolean>()));
		Set<String> setFromConcurrentHashMapV8 = Collections.newSetFromMap((new ConcurrentHashMapV8<String, Boolean>()));
		Set<String> synchronizedTreeSet = Collections.synchronizedSortedSet(new TreeSet<String>());
		
		Set<String> hashSet = new HashSet<String>();
		Set<String> linkedHashSet = new LinkedHashSet<String>();
		Set<String> treeSet = new TreeSet<String>();

		List<Lists> synchronizedSets = new ArrayList<>();
		synchronizedSets.add(new Lists("synchronizedLinkedHashSet", synchronizedLinkedHashSet));
		synchronizedSets.add(new Lists("synchronizedHashSet", synchronizedHashSet));
		synchronizedSets.add(new Lists("concurrentSkipListSet", concurrentSkipListSet));
		synchronizedSets.add(new Lists("setFromConcurrentHashMap", setFromConcurrentHashMap));
		synchronizedSets.add(new Lists("setFromConcurrentHashMapV8", setFromConcurrentHashMapV8));
		synchronizedSets.add(new Lists("copyOnWriteArraySet", copyOnWriteArraySet));
		synchronizedSets.add(new Lists("synchronizedTreeSet", synchronizedTreeSet));
		
		List<Lists> nonSynchronizedSets = new ArrayList<>();
		nonSynchronizedSets.add(new Lists("hashSet",hashSet));
		nonSynchronizedSets.add(new Lists("linkedHashSet",linkedHashSet));
		nonSynchronizedSets.add(new Lists("treeSet",treeSet));

		for (final Lists list : synchronizedSets) {
			//Kenan: Initializing data printer for write, traversalIterator and Get
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			write(list, THREADS, N, ITERATIONS);
			traversalIterator(list, THREADS, N, ITERATIONS);
			//Kenan: Reinitializing data printer for remove. No warmup.
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
			traversalRemove(list, THREADS, N);
			

			list.getSet().clear();
		}
		
		final int ZERO_THREADS=0;
		for (final Lists list : nonSynchronizedSets) {
			//Kenan: Initializing data printer for write, traversalIterator and Get
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			write(list, ZERO_THREADS, N, ITERATIONS);
			traversalIterator(list, ZERO_THREADS, N, ITERATIONS);
			//Kenan: Reinitializing data printer for remove. No warmup.
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
			traversalRemove(list, ZERO_THREADS, N);
			

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
			int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"add(value)",MainTest.printForAnalyzer);

		//Kenan
		for (int i = 0; i < iterations; i++) {
			//Kenan
			ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
			ener.wallClockTimeStart = System.currentTimeMillis()/1000.0;
			ener.preEnergy= EnergyCheckUtils.EnergyStatCheck();
			//Kenan
			
			if(threads>0) {
				ExecutorService executors = Executors.newFixedThreadPool(threads);
				for (int j = 0; j < threads; j++) {
					executors.execute(new Run(list, j, total));
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				for (int k = 0; k < total; k++) {
					list.getSet().add(k + "-" + "1");
				}
			}

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
			
			/*if (list.getSet() instanceof CopyOnWriteArraySet) {
				break;
			}*/
		}

	}

	static void traversalIterator(final Lists list, final int threads, final int total,
			int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"iterator",MainTest.printForAnalyzer);
		//Kenan
		
		for (int i = 0; i < iterations; i++) {
			//Kenan
			ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
			ener.wallClockTimeStart = System.currentTimeMillis()/1000.0;
			ener.preEnergy= EnergyCheckUtils.EnergyStatCheck();
			//Kenan
			
			if(threads>0) {
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
			} else {
				for (String key : list.getSet()) {
					String e = key;
				}
			}

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
	
	static void traversalRemove(final Lists list, final int threads, final int total) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();

		//Kenan

		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"remove(key)",MainTest.printForAnalyzer);
		ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
		ener.wallClockTimeStart = System.currentTimeMillis()/1000.0;
		ener.preEnergy= EnergyCheckUtils.EnergyStatCheck();
		//Kenan
		
		if(threads>0) {
			ExecutorService executors = Executors.newFixedThreadPool(threads);
			for (int j = 0; j < threads; j++) {
				executors.execute(new Remover(list, j, total));
			}
			executors.shutdown();
			executors.awaitTermination(1, TimeUnit.DAYS);
		} else {
			for (int k = 0; k < total; k++) {
				String key = String.valueOf(k) + "-"+ "1";
				list.getSet().remove(key);
			}
		}

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
