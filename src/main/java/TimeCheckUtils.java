
import java.lang.management.*;
import java.util.Random;

public class TimeCheckUtils {

	long totalTime = -1L;
	String timeInfo = "-1";
	long totalCpuTime = -1L;
	long totalUserTime = -1L;

	
	/********Get current thread time info*************/

	public long getCurrentThreadCpuTime() {						//Total CPU time usage
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime() : -1L;
	}

	public long getCurrentThreadKernelTime() {					//Kernel mode thread CPU time usage? maybe include context switch
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime() : -1L;
	}
	
	public long getCurrentThreadUserTime() {						//User level thread CPU time usage
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadUserTime() : -1L;
	}

	public String getCurrentThreadTimeInfo() {						//Get time info for current thread
		
		StringBuffer sb = new StringBuffer();
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		if(!bean.isCurrentThreadCpuTimeSupported())
			return timeInfo;
		long userTime = bean.getCurrentThreadUserTime();
		long cpuTime = bean.getCurrentThreadCpuTime();

		sb.append(userTime).append("#").append(cpuTime);
		timeInfo = sb.toString();
//		System.out.println("lib: " + timeInfo);
		return timeInfo;
	}

	/********Get specified thread time info*************/

	public long getThreadCpuTime(long tid) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		if(!bean.isCurrentThreadCpuTimeSupported())
			System.out.println("current thread doesn't support cpu time");
		return bean.isThreadCpuTimeSupported() ? bean.getThreadCpuTime(tid) : -1L;
	}

	public long getThreadUserTime(long tid) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isThreadCpuTimeSupported() ? bean.getThreadUserTime(tid) : -1L;
	}


	public long getThreadKernelTime(long tid) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isThreadCpuTimeSupported() ? bean.getThreadUserTime(tid) : -1L;
	}

	public String getThreadTimeInfo(long tid) {						//Get time info for the specified thread 
		
		StringBuffer sb = new StringBuffer();
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		if(!bean.isThreadCpuTimeSupported()) 
			return "-1";

		long userTime = bean.getThreadUserTime(tid);
		long cpuTime = bean.getThreadCpuTime(tid);

		sb.append(userTime).append("#").append(cpuTime);
		timeInfo = sb.toString();
		return timeInfo;
	}

	/********Get number of threads time info*************/

	public long getTotalThreadCpuTime(long[] tids) {				//get multiple threads cpu time usage to prevent overhead for multiple 
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();		//method invocations of getThreadCpuTime(long tid)
		if(!bean.isThreadCpuTimeSupported()) 							//If JVM supports for CPU time measurement for any thread, it returns true
			return -1L;
		for(long i : tids) {
			long currentTime = bean.getThreadCpuTime(i);
			if(currentTime != -1) {										//Check if the thread CPU measurement is enableed.
				totalTime += currentTime;
			}
		}
		return totalTime;
	}

	public long getTotalThreadUserTime(long[] tids) {			//Get multiple threads user time usage			 
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();		
		if(!bean.isThreadCpuTimeSupported()) 							
			return -1L;
		
		for(long i : tids) {
			long currentTime = bean.getThreadUserTime(i);
			if(currentTime != -1) {										
				totalTime += currentTime;
			}
		}
		return totalTime;
	}

	public long getTotalThreadKernelTime(long[] tids) {			//Get multiple threads Kernel time usage	 
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();		
		if(!bean.isThreadCpuTimeSupported()) 							
			return -1L;
		
		for(long i : tids) {
			long currentCpuTime = bean.getThreadCpuTime(i);
			long currentUserTime = bean.getThreadUserTime(i);
			if(currentCpuTime != -1 && currentUserTime != -1) {										
				totalTime += (currentCpuTime - currentUserTime);
			}
		}
		return totalTime;
	}

	public String getTotalThreadTimeInfo(long[] tids) {			//Get time info  for multiple threads timea information 

		StringBuffer sb = new StringBuffer();
	
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();		
		if(!bean.isThreadCpuTimeSupported()) 							
			return timeInfo;
		for(long i : tids) {
			long currentUserTime = bean.getThreadUserTime(i);
			long currentCpuTime = bean.getThreadCpuTime(i);
			if(currentCpuTime != -1) {										
				totalCpuTime += currentCpuTime;
				totalUserTime += currentUserTime;
			}
		
		}
		sb.append(totalUserTime).append("#").append(totalCpuTime);
		timeInfo = sb.toString();
		return timeInfo;

	}

	public void main(String[] args) {
		String start, end;
		String[] preamble = null;
		String[] epilogue = null;
		long cpuTime, userTime, kernelTime;
		long wallClockStart, wallClockEnd, wallClockTimeUse;
		Random rand = new Random();
		int randNum;
		int option = 2;
		wallClockStart = System.currentTimeMillis();	
		start = getCurrentThreadTimeInfo();
		
		if(option == 1) {
			for(int i = 0; i < 10000000; i++) {
				for(int j = 0; j < 100; j++) {
					randNum = rand.nextInt()%1000;
				}
			}
		} else if(option == 2) {
			for(int i = 0; i < 10000000; i++) {
				//for(int j = 0; j < 10; j++) {
					System.out.println("++++");
				//}
			}
		}
		end = getCurrentThreadTimeInfo();
		wallClockEnd = System.currentTimeMillis();	
		if(!start.equals("-1")) {
			
			preamble = start.split("#");
			epilogue = end.split("#");
			userTime = Long.parseLong(epilogue[0]) - Long.parseLong(preamble[0]);
			cpuTime = Long.parseLong(epilogue[1]) - Long.parseLong(preamble[1]);
			kernelTime = cpuTime - userTime;
			System.out.println("CPU time usage: " + cpuTime/1000000);
			System.out.println("user time usage: " + userTime/1000000);
			System.out.println("kernel time usage: " + kernelTime/1000000);
		}
		wallClockTimeUse = wallClockEnd - wallClockStart;
		System.out.println("Wall clock time usage: " + wallClockTimeUse);
	}
}
