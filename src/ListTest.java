
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ListTest {

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
			private List<Integer> list;

			public Lists(String name, List<Integer> list) {
				this.name = name;
				this.list = list;
			}

			public String getName() { return this.name; }
			public List<Integer> getList() { return this.list; }
		}

		public static void main(String... args) throws Exception {

		final int RMITERATION = 1;
		final int ITERATIONS = 10;
		final int THREADS = Integer.parseInt(args[0]);
		final int N = Integer.parseInt(args[1]);
		final int capacity = Integer.parseInt(args[2]);
		final int WARMUP = 7;
		final int NOWARMUP = 0;


		/**
		 * @editStart: kenan
		 */

		String socketNumCheck = EnergyCheckUtils.EnergyStatCheck();
		int sockNum = 0;
		int powerLimitEnable = 0;
		sockNum = socketNumCheck.contains("@") ? socketNumCheck.split("@").length : 1;

	    DataPrinter.printTitle(sockNum);
		/**
		 * @editEnd: kenan
		 */

		System.out.format("Conf: Iterations=%s, threads=%s, N=%s, capacity=%s\n", ITERATIONS, THREADS, N, capacity);

		List<Integer> vector = new Vector<>(capacity);
		List<Integer> syncList = Collections.synchronizedList(new ArrayList<Integer>(capacity));
		List<Integer> arrayList = new LinkedList<Integer>();
		List<Integer> copyOnWrite = new CopyOnWriteArrayList<>();

		List<Lists> lists = new ArrayList<>();
		lists.add(new Lists("arrayList", arrayList));
		lists.add(new Lists("vector", vector));
		lists.add(new Lists("syncList", syncList));
		lists.add(new Lists("copyOnWrite", copyOnWrite));

		for (final Lists list : lists) {
		//Kenan: Initializing data printer for write, traversalIterator and Get
		EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			// we should write before read or remove ops
			write(list, THREADS, N, ITERATIONS);
			traversalIterator(list, THREADS, N, ITERATIONS);
			traversalGet(list, THREADS, N, ITERATIONS);
		//Kenan: Reinitializing data printer for remove. No warmup.
		EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
			traversalRemove(list, THREADS, N);

			list.getList().clear();
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
				executors.execute(new Runnable() {
					@Override
					public void run() {
						try{ //used only in the non-thread-safe case
						for (int j = 0; j < total; j++) {
							list.getList().add(j);
							//System.out.println(list.getList().size());
						}
						}catch (ArrayIndexOutOfBoundsException e){}
					}
				});
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
			if(/*list.getList() instanceof CopyOnWriteArrayList && */i+1!=iterations){
				list.getList().clear();
			}

		  /**
		   * @editEnd: kenan
		   */

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
						int i = 0;
						try {
							//System.out.println(list.getList().size());
						for (Integer key : list.getList()) {
							Integer e = key;
							i++;
							if(i >= total) break;
						}
					} catch (Exception e) {}
					}
				});
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

	static void traversalGet(final Lists list, final int threads, final int total,
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
						List<Integer> l = list.getList();
						for (int j = 0; j < total; j++) {
							try {
							Integer e = l.get(j);
						} catch (Exception e) {}
						}
					}
				});
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
			executors.execute(new Runnable() {
				@Override
				public void run() {
					List<Integer> l = list.getList();
					try {
						for (int j = 0; j < total; j++) {
							Integer e = l.remove(j);
						}
					} catch (Exception e) {
					}
				}
			});
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
