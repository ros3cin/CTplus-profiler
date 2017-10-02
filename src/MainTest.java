
public class MainTest {
	public static boolean printForAnalyzer = false;
	public static void main(String[] args) throws Exception {

//		if (args.length != 5) {
//			System.out.println("Usage data-structures.jar [data-structure] [threads] [input] [initial_capacity [load_factor]");
//			System.exit(0);
//		}
		
		String construct = args[0];
		System.out.println(construct);
		if(construct.equals("hash")) {
			HashingTest.main(args[1], args[2], args[3], args[4]);
		} else if(construct.equals("list")){
			ListTest.main(args[1], args[2], args[3]);
		} else if(construct.equals("set")){
			SetTest.main(args[1], args[2], args[3]);
		} else if(construct.equals("collision")){
			HashCollisionTest.main(args[1], args[2], args[3], args[4]);
		}
	}
}
