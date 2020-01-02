Introduction
--------------------------------------------------------------
This is a profiler for the collections that will be analyzed by CECOTool

Pre-requisites
--------------------------------------------------------------
It needs processors with RAPL support

Don't forget to initialize the msr module with:

```
sudo modprobe msr
```
When using Eclipse, you should run it as sudo, otherwise the application will fail to use the MSR module.

Instructions
--------------------------------------------------------------
To run the profiler:
1. Go to the MainTest class
2. Right click and go to 'Run As' - 'Run Configurations'
3. At the 'Arguments' tab type: [hash|list|set|collision] number_of_threads amount_of_operations capacity loadfactor
4. Parameters up to capacity are used by lists and sets, maps also use the fourth parameter, which is a float between 0 and 1
