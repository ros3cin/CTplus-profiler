

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

public class DataPrinter extends EnergyCalc{
	
	private int count = 0;
	private final int WARMUP = 2;
	String methodName = null;
	public boolean hasTimeCalc = false;
	int threadsNeedCalcInTime = 0;
	private String operationName;
	private boolean printForAnalyzer;
	
	public DataPrinter(String methodName, int threadsNeedCalcInTime, String operationName, boolean printForAnalyzer) {
		super();
		this.threadsNeedCalcInTime = threadsNeedCalcInTime;
    	this.methodName = methodName;
    	this.operationName=operationName;
    	this.printForAnalyzer=printForAnalyzer;
	}
	
    public DataPrinter(String methodName, String preEnergy, double wallClockTimeStart, String timePreamble, 
			String timeEpilogue, double wallClockTimeEnd, String postEnergy, int threadsNeedCalcInTime) {
    	super(preEnergy, wallClockTimeStart, timePreamble, timeEpilogue, wallClockTimeEnd, postEnergy);
    	
    	this.threadsNeedCalcInTime = threadsNeedCalcInTime;
    	
    	this.methodName = methodName;
    }
    
	public void printResult(String signal, int loopNum) throws ParseException {

		DecimalFormat df = new DecimalFormat("#.##");
		DecimalFormat frq = new DecimalFormat("#.#");
		
		for (int k = 0; k < sockNum; k++) {
			gpuEnerPowerSum[k] = NumberFormat.getInstance().parse(df.format(gpuEnerPowerSum[k]/ loopNum)).doubleValue();
			cpuEnerPowerSum[k] = NumberFormat.getInstance().parse(df.format(cpuEnerPowerSum[k]/ loopNum)).doubleValue();
			pkgEnerPowerSum[k] = NumberFormat.getInstance().parse(df.format(pkgEnerPowerSum[k]/ loopNum)).doubleValue();
			
			gpuEnergySum[k] = NumberFormat.getInstance().parse(df.format(gpuEnergySum[k] / loopNum)).doubleValue();
			cpuEnergySum[k] = NumberFormat.getInstance().parse(df.format(cpuEnergySum[k] / loopNum)).doubleValue();
			pkgEnergySum[k] = NumberFormat.getInstance().parse(df.format(pkgEnergySum[k] / loopNum)).doubleValue();
		}
//		System.out.println("====================================================");
		/**** Time and Energy information ****/
		if(!printForAnalyzer) {
			if(NumThread != 0)
				System.out.print(NumThread + "," + NumberFormat.getInstance().parse(frq.format(frequency/1000000.0)).doubleValue() + ",");
			if(powerOption == 0 || powerOption == 1 || (powerOption == 2 && pkgPower != 0))
				System.out.print(pkgPower + "-" + dramPower + "," + pkgTime + "-" + dramTime + ",");
			else 
				System.out.print("power_limit_disable,power_limit_disable,");
		}
		
		if(!printForAnalyzer) {
			System.out.print(signal + "," + operationName + ","
					+ NumberFormat.getInstance().parse(df.format(wallClockTime / loopNum)).doubleValue() + ","
					+ NumberFormat.getInstance().parse(df.format(cpuTime / loopNum)).doubleValue() + ","
					+ NumberFormat.getInstance().parse(df.format(userModeTime / loopNum)).doubleValue() + ","
					+ NumberFormat.getInstance().parse(df.format(kernelModeTime / loopNum)).doubleValue());
		}

		if(!printForAnalyzer) {
			for (int i = 0; i < sockNum; i++) {
				System.out.print(","
						+ gpuEnergySum[i] 
						+ ","
						+ cpuEnergySum[i] 
						+ ","
						+ pkgEnergySum[i] 
						+ ","); 
				// Power information
				if (wallClockTime != 0.0) {
					System.out.print(gpuEnerPowerSum[i] + "," + cpuEnerPowerSum[i] + ","
							+ pkgEnerPowerSum[i]);
				} else
					System.out.print("0.00," + "0.00," + "0.00");
				
				System.out.print("," + gpuEnerSD[i] + "," + cpuEnerSD[i] + "," + pkgEnerSD[i]);
				
			}
			System.out.print("," + wallClockTimeSD);
		}
		
		if(printForAnalyzer) {
			long timeDif = this.timePosDate.getTime()-this.timePreDate.getTime();
			System.out.print(signal+","+operationName+","+gpuEnergySum[0]+","+cpuEnergySum[0]+","+pkgEnergySum[0]+","+(timeDif/1000.0f)/loopNum);
		}
		System.out.println();
	}
    
	public static void printTitle(int sockNum) {

		System.out.print("NumberOfThread,Frequency,power_limit(pkg-dram),time_window(pkg-dram),MethodName,WallClockTime," +
				"CpuTime,UserModeTime,KernelModeTime");
		for(int i = 0; i < sockNum; i++) {
			//String str = String.format("socket%d", i);
			System.out.print(",DramEnergy" + i + "," + "CPUEnergy" + i + "," + "PackageEnergy" + i + "," 
								+ "DramPower" + i + "," + "CPUPower" + i + "," + "PackagePower" + i + "," 
								+ "DramEnergySD" + i + "," + "CPUEnergySD" + i + "," + "PackageEnergySD" + i ); 
		}
		System.out.print(",WallClockTimeSD");
		System.out.println();
		
	}

	public void reset() {
		for(int i = 0; i < sockNum; i++) {
			super.gpuEnergySum[i] = 0.0;
			super.cpuEnergySum[i] = 0.0;
			super.pkgEnergySum[i] = 0.0;
			
			super.gpuEnerPowerSum[i] = 0.0;
			super.cpuEnerPowerSum[i] = 0.0;
			super.pkgEnerPowerSum[i] = 0.0;
			
			super.gpuEnerSD[i] = 0.0;
			super.cpuEnerSD[i] = 0.0;
			super.pkgEnerSD[i] = 0.0;
			
			for(int j = 0; j < warmup; j++) {
				super.gpuEnerPerLoop[i][j] = 0.0;
				super.cpuEnerPerLoop[i][j] = 0.0;
				super.pkgEnerPerLoop[i][j] = 0.0;

			}
		}
		for(int i = 0; i < warmup; i++) {
			super.wallClockTimePerLoop[i] = 0.0;
		}
		super.userModeTime = 0.0;
		super.cpuTime = 0.0;
		super.kernelModeTime = 0.0;
		super.wallClockTime = 0.0;
		super.wallClockTimeSD = 0.0;
		
//	    pkgPower = 0.0;
//		dramPower = 0.0; 
//		pkgTime = 0.0; 
//		dramTime = 0.0; 
//		powerOption = 0;
//		timeOption = 0;
	}
	
	public void setTimeCalc() {
		if(!hasTimeCalc)
			super.timeCalc();
		
		userModeTime /= threadsNeedCalcInTime;
		cpuTime /= threadsNeedCalcInTime;
		kernelModeTime /= threadsNeedCalcInTime;
		
	}

	public void dataReport() throws ParseException {
		this.count++;
		//warmup iterations
		if(count <= warmup) {
//			super.energyCalc(count);
//			if(count == WARMUP) 
//				printResult(methodName + "Warmup", WARMUP);

		} else {

			if(count == warmup + 1 && warmup != 0)
				reset();
			super.energyCalc(count - warmup - 1);
			setTimeCalc();

			if(count == loopNum) {
				getStandDev(loopNum - warmup);
				printResult(methodName, loopNum - warmup);
				
			}

		}
	
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public boolean isPrintForAnalyzer() {
		return printForAnalyzer;
	}

	public void setPrintForAnalyzer(boolean printForAnalyzer) {
		this.printForAnalyzer = printForAnalyzer;
	}

}
