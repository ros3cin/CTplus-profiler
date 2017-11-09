
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.util.Date;
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

		//synchronized lists
		List<Integer> vector = new Vector<>(capacity);
		List<Integer> synchronizedArrayList = Collections.synchronizedList(new ArrayList<Integer>(capacity));
		List<Integer> synchronizedLinkedList = Collections.synchronizedList(new LinkedList<Integer>());
		List<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();

		
		//non synchronized lists
		List<Integer> arrayList = new ArrayList<Integer>(capacity);
		List<Integer> linkedList = new LinkedList<Integer>();

		List<Lists> synchronizedLists = new ArrayList<>();
		synchronizedLists.add(new Lists("synchronizedLinkedList", synchronizedLinkedList));
		synchronizedLists.add(new Lists("vector", vector));
		synchronizedLists.add(new Lists("synchronizedArrayList", synchronizedArrayList));
		synchronizedLists.add(new Lists("copyOnWriteArrayList", copyOnWriteArrayList));
		
		List<Lists> nonSynchronizedLists = new ArrayList<>();
		nonSynchronizedLists.add(new Lists("arrayList",arrayList));
		nonSynchronizedLists.add(new Lists("linkedList",linkedList));

		for (final Lists list : synchronizedLists) {
			//Kenan: Initializing data printer for write, traversalIterator and Get
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			
			// we should write before read or remove ops
			write(list, THREADS, N, ITERATIONS);
			traversalIterator(list, THREADS, N, ITERATIONS);
			sequencialGet(list, THREADS, N, ITERATIONS);
			randomGet(list, THREADS, N, ITERATIONS);
			
			//Kenan: Reinitializing data printer for remove. No warmup.
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
			removeFromBeginning(list, THREADS, N);

			list.getList().clear();
			
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			writeAtBeginning(list, THREADS, N, ITERATIONS);
			list.getList().clear();
			
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			writeAtMiddle(list, THREADS, N, ITERATIONS);
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP);
			removeFromMiddle(list, THREADS, N);
			list.getList().clear();
			
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			writeAtEnding(list, THREADS, N, ITERATIONS);
			EnergyCalc.preInit(0, THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP);
			removeFromEnding(list, THREADS, N);
			list.getList().clear();
		}
		
		final int ZERO_THREADS=0;
		for (final Lists list : nonSynchronizedLists) {
			//Kenan: Initializing data printer for write, traversalIterator and Get
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			// we should write before read or remove ops
			write(list, ZERO_THREADS, N, ITERATIONS);
			traversalIterator(list, ZERO_THREADS, N, ITERATIONS);
			sequencialGet(list, ZERO_THREADS, N, ITERATIONS);
			randomGet(list, ZERO_THREADS, N, ITERATIONS);
			
			//Kenan: Reinitializing data printer for remove. No warmup.
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP); //change iteration to be one for remove operation
			removeFromBeginning(list, ZERO_THREADS, N);

			list.getList().clear();
			
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			writeAtBeginning(list, ZERO_THREADS, N, ITERATIONS);
			list.getList().clear();
			
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			writeAtMiddle(list, ZERO_THREADS, N, ITERATIONS);
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP);
			removeFromMiddle(list, ZERO_THREADS, N);
			list.getList().clear();
			
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, ITERATIONS, WARMUP);
			writeAtEnding(list, ZERO_THREADS, N, ITERATIONS);
			EnergyCalc.preInit(0, ZERO_THREADS, 0, 0, 0, 0, 0, 0, RMITERATION, NOWARMUP);
			removeFromEnding(list, ZERO_THREADS, N);
			list.getList().clear();
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
					executors.execute(new Runnable() {
						@Override
						public void run() {
							try{ //used only in the non-thread-safe case
							for (int j = 0; j < (total/threads); j++) {
								list.getList().add(j);
								//System.out.println(list.getList().size());
							}
							}catch (ArrayIndexOutOfBoundsException e){}
						}
					});
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				try{ 
					for (int j = 0; j < total; j++) {
						list.getList().add(j);
					}
				}catch (ArrayIndexOutOfBoundsException e){}
			}
			
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
	
	private static void writeAtBeginning(final Lists list, int threads, final int total,
			int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"add(starting-index,value)",MainTest.printForAnalyzer);
		//Kenan

		for (int i = 0; i < iterations; i++) {
			//Kenan
			ener.timePreDate = new Date();
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
							try{ //used only in the non-thread-safe case
							for (int j = 0; j < (total/threads); j++) {
								list.getList().add(0,j);
								//System.out.println(list.getList().size());
							}
							}catch (ArrayIndexOutOfBoundsException e){}
						}
					});
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				try{ 
					for (int j = 0; j < total; j++) {
						list.getList().add(j);
					}
				}catch (ArrayIndexOutOfBoundsException e){}
			}
			
	  	  /**
		   * @editStart: kenan
		   */
			ener.timePosDate = new Date();
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
	
	private static void writeAtMiddle(final Lists list, int threads, final int total,
			int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"add(middle-index,value)",MainTest.printForAnalyzer);
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
							try{ //used only in the non-thread-safe case
							for (int j = 0; j < (total/threads); j++) {
								list.getList().add(list.getList().size()/2,j);
								//System.out.println(list.getList().size());
							}
							}catch (ArrayIndexOutOfBoundsException e){}
						}
					});
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				try{ 
					for (int j = 0; j < total; j++) {
						list.getList().add(j);
					}
				}catch (ArrayIndexOutOfBoundsException e){}
			}
			
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
	
	private static void writeAtEnding(final Lists list, int threads, final int total,
			int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"add(ending-index,value)",MainTest.printForAnalyzer);
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
							try{ 
								for (int j = 0; j < (total/threads); j++) {
									list.getList().add(list.getList().size(),j);
								}
							}catch (Exception e){
								e.printStackTrace();
							}
						}
					});
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				try{ 
					for (int j = 0; j < total; j++) {
						list.getList().add(list.getList().size(),j);
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			
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
							int i = 0;
							try {
								//System.out.println(list.getList().size());
								for (Integer key : list.getList()) {
									Integer e = key;
									i++;
									if(i >= (total/threads)) break;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				int z = 0;
				try {
					for (Integer key : list.getList()) {
						Integer e = key;
						z++;
						if(z >= total) break;
					}
				} catch (Exception e) {
					e.printStackTrace();
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

	static void sequencialGet(final Lists list, final int threads, final int total,
			int iterations) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();

		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"sequencialGet",MainTest.printForAnalyzer);
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
							List<Integer> l = list.getList();
							for (int j = 0; j < (total/threads); j++) {
								try {
									Integer e = l.get(j);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				List<Integer> l = list.getList();
				for (int j = 0; j < total; j++) {
					try {
						Integer e = l.get(j);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
	
	static void randomGet(final Lists list, final int threads, final int total,
			int iterations) throws InterruptedException, ParseException {

		//Kenan
		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"randomGet",MainTest.printForAnalyzer);
		//Kenan

		Random rng = new Random((new Date()).getTime());
		int[] randomPositions = new int[total];
		for(int z = 0;z<total;z++) {
			randomPositions[z]=rng.nextInt(total);
		}

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
							List<Integer> l = list.getList();
							
							for (int j = 0; j < (total/threads); j++) {
								try {
									Integer e = l.get(randomPositions[j]);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
				executors.shutdown();
				executors.awaitTermination(1, TimeUnit.DAYS);
			} else {
				List<Integer> l = list.getList();
				for (int j = 0; j < total; j++) {
					try {
						Integer e = l.get(randomPositions[j]);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
	
	static void removeFromBeginning(final Lists list, final int threads, final int total) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan


		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"remove(starting-index)",MainTest.printForAnalyzer);
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
						List<Integer> l = list.getList();
						try {
							for (int j = 0; j < (total/threads); j++) {
								Integer e = l.remove(0);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
	
			executors.shutdown();
			executors.awaitTermination(1, TimeUnit.DAYS);
		} else {
			List<Integer> l = list.getList();
			try {
				for (int j = 0; j < total; j++) {
					Integer e = l.remove(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
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
	
	static void removeFromEnding(final Lists list, final int threads, final int total) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan


		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"remove(ending-index)",MainTest.printForAnalyzer);
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
						List<Integer> l = list.getList();
						try {
							for (int j = (total-1)/threads; j >= 0; j--) {
								Integer e = l.remove(j);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
	
			executors.shutdown();
			executors.awaitTermination(1, TimeUnit.DAYS);
		} else {
			List<Integer> l = list.getList();
			try {
				for (int j = total-1; j >= 0; j--) {
					Integer e = l.remove(j);
				}
			} catch (Exception e) {
				e.printStackTrace();
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
	
	static void removeFromMiddle(final Lists list, final int threads, final int total) throws InterruptedException, ParseException {
		List<String> lastThree = new ArrayList<>();
		//Kenan


		TimeCheckUtils mainTimeHelper = new TimeCheckUtils();
		DataPrinter ener = new DataPrinter(list.name, MAINTHREAD,"remove(middle-index)",MainTest.printForAnalyzer);
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
						List<Integer> l = list.getList();
						try {
							for (int j = (total-1)/threads; j >= 0; j--) {
								Integer e = l.remove(j/2);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
	
			executors.shutdown();
			executors.awaitTermination(1, TimeUnit.DAYS);
		} else {
			List<Integer> l = list.getList();
			try {
				for (int j = total-1; j >= 0; j--) {
					Integer e = l.remove(j/2);
				}
			} catch (Exception e) {
				e.printStackTrace();
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
