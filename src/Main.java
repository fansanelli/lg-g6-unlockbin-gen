import java.io.IOException;

class Main {
	public static void main(String[] args) throws Exception {
		if (args.length == 3) {
			GenerationWorker worker;
			try {
				worker = new GenerationWorker(args[0], args[1], args[2], true);
		        worker.execute();
		        while (true) {
					// Trap
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {			
			new MyGUI().show();
		}

	}	
}
