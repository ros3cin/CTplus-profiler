
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jsr166e.ConcurrentHashMapV8;

public class HashCollisionTest {
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
		Map<String, Integer> synchronizedHashMap = Collections
				.synchronizedMap(new HashMap<String, Integer>(capacity, loadFactor));
		Map<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>(capacity, loadFactor);
		Map<String, Integer> concurrentSkipListMap = new ConcurrentSkipListMap<String, Integer>();

		List<Hash> maps = new ArrayList<>();
		
		maps.add(new Hash("LinkedHashMap", linkedHashMap));
		maps.add(new Hash("concurrentHashMapV8", concurrentHashMapV8));
		maps.add(new Hash("concurrentHashMap", concurrentHashMap));
		maps.add(new Hash("hashtable", hashtable));
		maps.add(new Hash("synchronizedMap", synchronizedHashMap));
		maps.add(new Hash("concurrentSkipListMap", concurrentSkipListMap));
		
		for (final Hash map : maps) {
			
			//Kenan: Initializing data printer for write, traversalIterator and Get
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			write(map, THREADS, N, ITERATIONS);
			//Kenan: Reinitializing data printer for remove. No warmup.
//			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
//			TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
//			DataPrinter ener = new DataPrinter("mapClear", MAINTHREAD);
//			ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
//			ener.wallClockTimeStart = System.currentTimeMillis() / 1000.0;
//			ener.preEnergy = EnergyCheckUtils.EnergyStatCheck();
			// Kenan
			map.getMap().clear();
			   /**
		       * @editStart: kenan
		       */
//		      	ener.timeEpilogue= mainTimeHelper.getCurrentThreadTimeInfo();
//				ener.wallClockTimeEnd  = System.currentTimeMillis()/1000.0;
//				ener.postEnergy= EnergyCheckUtils.EnergyStatCheck();
//				ener.dataReport();

		      /**
		       * @editEnd: kenan
		       */
		}
	}

	private static void write(final Hash map, int threads, final int total,
			int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(map.name, MAINTHREAD,"put(constant-key,value)");
		//Kenan
		for (int i = 0; i < iterations; i++) {
			//Kenan
			ener.timePreamble = mainTimeHelper.getCurrentThreadTimeInfo();
			ener.wallClockTimeStart = System.currentTimeMillis()/1000.0;
			ener.preEnergy= EnergyCheckUtils.EnergyStatCheck();
			//Kenan
			ExecutorService executors = Executors.newFixedThreadPool(threads);
			for (int j = 0; j < threads; j++) {
				executors.execute(new Writer(map, total));
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
		private int total;
		
		public Writer(Hash map, int total) {
			this.map = map;
			this.total = total;
		}
		
		@Override
		public void run() {
			for (int k = 0; k < total; k++) {
				String key = String.valueOf(1) + "-"+ "1";
				map.getMap().put(key, k);
			}
		}
	}
	
}