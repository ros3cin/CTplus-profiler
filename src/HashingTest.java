
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jsr166e.ConcurrentHashMapV8;


public class HashingTest {
	

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


	static class Hash {
		private String name;
		private Map<String, Integer> map;

		public Hash(String name, Map<String, Integer> map) {
			this.name = name;
			this.map = map;
		}

		public String getName() { return this.name; }
		public Map<String, Integer> getMap() { return this.map; }
	}

	public static void main(String... args) throws Exception {

		final int ITERATIONS = 10;
		final int THREADS = Integer.parseInt(args[0]);
		final int N = Integer.parseInt(args[1]);
		final int capacity = Integer.parseInt(args[2]);
		final float loadFactor = Float.parseFloat(args[3]);
		
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

//	    DataPrinter.printTitle(sockNum);
		/**
		 * @editEnd: kenan
		 */

		System.out.format("Conf: Iterations=%s, threads=%s, N=%s, capacity=%s, load_factor=%s\n", ITERATIONS, THREADS, N, capacity, loadFactor);
		
		Map<String, Integer> concurrentHashMapV8 = new ConcurrentHashMapV8<>(capacity, loadFactor);
		Map<String, Integer> concurrentHashMap = new ConcurrentHashMap<>(capacity, loadFactor);
		Map<String, Integer> hashtable = new Hashtable<>(capacity, loadFactor);
		Map<String, Integer> synchronizedHashMap = Collections.synchronizedMap(new HashMap<String, Integer>(capacity, loadFactor));
		Map<String, Integer> synchronizedLinkedHashMap = Collections.synchronizedMap(new LinkedHashMap<String, Integer>(capacity, loadFactor));
		Map<String, Integer> concurrentSkipListMap = new ConcurrentSkipListMap<String, Integer>();
		Map<String, Integer> synchronizedTreeMap = Collections.synchronizedSortedMap(new TreeMap<String, Integer>());
		Map<String, Integer> synchronizedWeakHashMap = Collections.synchronizedMap(new WeakHashMap<String, Integer>());
		
		Map<String, Integer> hashMap = new HashMap<String, Integer>(capacity,loadFactor);
		Map<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>(capacity, loadFactor);
		Map<String, Integer> treeMap = new TreeMap<String, Integer>();
		Map<String, Integer> weakHashMap = new WeakHashMap<String, Integer>(capacity,loadFactor);
		

		List<Hash> synchronizedMaps = new ArrayList<>();
		synchronizedMaps.add(new Hash("linkedHashMap", synchronizedLinkedHashMap));
		synchronizedMaps.add(new Hash("concurrentHashMapV8", concurrentHashMapV8));
		synchronizedMaps.add(new Hash("concurrentHashMap", concurrentHashMap));
		synchronizedMaps.add(new Hash("hashtable", hashtable));
		synchronizedMaps.add(new Hash("synchronizedHashMap", synchronizedHashMap));
		synchronizedMaps.add(new Hash("concurrentSkipListMap", concurrentSkipListMap));
		synchronizedMaps.add(new Hash("synchronizedTreeMap", synchronizedTreeMap));
		synchronizedMaps.add(new Hash("synchronizedWeakHashMap", synchronizedWeakHashMap));
		
		List<Hash> nonSynchronizedMaps = new ArrayList<>();
		nonSynchronizedMaps.add(new Hash("hashMap", hashMap));
		nonSynchronizedMaps.add(new Hash("linkedHashMap", linkedHashMap));
		nonSynchronizedMaps.add(new Hash("treeMap", treeMap));
		nonSynchronizedMaps.add(new Hash("weakHashMap", weakHashMap));
		
		for (final Hash map : synchronizedMaps) {
			//Kenan: Initializing data printer for write, traversalIterator and Get
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			write(map, THREADS, N, ITERATIONS);
			traversal(map, THREADS, N, ITERATIONS);
			//Kenan: Reinitializing data printer for remove. No warmup.
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
			remove(map, THREADS, N);
			
			map.getMap().clear();
		}
		
		final int ZERO_THREADS=0;
		for (final Hash map : nonSynchronizedMaps) {
			//Kenan: Initializing data printer for write, traversalIterator and Get
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			write(map, ZERO_THREADS, N, ITERATIONS);
			traversal(map, ZERO_THREADS, N, ITERATIONS);
			//Kenan: Reinitializing data printer for remove. No warmup.
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
			remove(map, ZERO_THREADS, N);
			
			map.getMap().clear();
		}
	}

	private static void write(final Hash map, int threads, final int total,
			int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(map.name, MAINTHREAD,"put(key;value)",MainTest.printForAnalyzer);
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
					executors.execute(new Writer(map, j, total/threads));
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				for (int k = 0; k < total; k++) {
					String key = String.valueOf(k) + "-"+ "1";
					map.getMap().put(key, k);
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

	static class Remover implements Runnable {
		private Hash map;
		private int total, current;
		
		public Remover(Hash map, int current, int total) {
			this.map = map;
			this.total = total;
			this.current = current;
		}
		
		@Override
		public void run() {
			for (int k = 0; k < total; k++) {
				String key = String.valueOf(k) + "-"+ current;
				map.getMap().remove(key);
			}
		}
	}
	
	static class Writer implements Runnable {
		
		private Hash map;
		private int total, current;
		
		public Writer(Hash map, int current, int total) {
			this.map = map;
			this.total = total;
			this.current = current;
		}
		
		@Override
		public void run() {
			for (int k = 0; k < total; k++) {
				String key = String.valueOf(k) + "-"+ current;
				map.getMap().put(key, k);
			}
		}
	}
	
	static void traversal(final Hash map, final int threads, final int total, int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(map.name, MAINTHREAD,"iterator",MainTest.printForAnalyzer);
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
							Set<String> keys = map.getMap().keySet();
							int i=0;
							for (String key : keys) {
								Integer e = map.getMap().get(key);
								i++;
								if(i>= (total/threads)) break;
							}
						}
					});
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				Set<String> keys = map.getMap().keySet();
				int z=0;
				for (String key : keys) {
					Integer e = map.getMap().get(key);
					z++;
					if(z>= total) break;
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
	
	static void remove(final Hash map, final int threads, final int total) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();

		// Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(map.name, MAINTHREAD,"remove(key)",MainTest.printForAnalyzer);
		ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
		ener.wallClockTimeStart = System.currentTimeMillis() / 1000.0;
		ener.preEnergy = EnergyCheckUtils.EnergyStatCheck();
		// Kenan

		if(threads>0) {
			ExecutorService executors = Executors.newFixedThreadPool(threads);
			for (int j = 0; j < threads; j++) {
				executors.execute(new Remover(map, j, total/threads));
			}
			executors.shutdown();
			executors.awaitTermination(1, TimeUnit.DAYS);
		} else {
			for (int k = 0; k < total; k++) {
				String key = String.valueOf(k) + "-"+ "1";
				map.getMap().remove(key);
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