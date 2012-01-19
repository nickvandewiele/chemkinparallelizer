package readers;

import java.io.File;

public class ExperimentalDatabaseInput{
	
		public static final String YIELDS = "YIELDS";
		public static final String IGNITION_DELAY = "IGNITION_DELAY";
		public static final String FLAME_SPEED = "FLAME_SPEED";
		
		public String type;
		
		public File location;
		
		public void setType(String type) {
			this.type = type;
		}
		public void setLocation(File location) {
			this.location = location;
		}
	}