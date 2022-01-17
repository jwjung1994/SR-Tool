import java.util.List;

import javax.swing.UIManager;

public class main {

   public static void main(String[] args) {
	   
	   try {
		   UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
	        //UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlackEyeLookAndFeel");

	    }  catch (Exception e) { }

		OntologyFunc owl = new OntologyFunc();
		FrameSetting fs = new FrameSetting();
		
		
		boolean state = owl.LoadOntology();
		if(state) {
			fs.FrameSetting(owl);
			//List<String> aa = owl.LoadVectors("PDO:AC-Software_Used");
			//List<String> bb = owl.LoadVectors("PDO:AC-Techniques_Used");

			fs.ChangeStatus(state);
			//fs.InsertVec(aa, fs.sw_cbo);
			//fs.InsertVec(bb, fs.tech_cbo2);			
		}


   }
}

