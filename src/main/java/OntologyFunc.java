import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.checkerframework.checker.nullness.qual.NonNull;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.search.EntitySearcher;


import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyModel;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
/*
class Ontology{
	String owl_path = "D:\\Nodejs\\CBSAO\\21\\CB_PDO_V1.owl";
	OWLOntology ontology;
	SQWRLQueryEngine queryEngine;
	
	boolean LoadOntology() {
		try {
			Optional<String> owlFilename = Optional.of(owl_path);
		    Optional<File> owlFile = (owlFilename != null && owlFilename.isPresent()) ? Optional.of(new File(owlFilename.get())) : Optional.<File>empty();

	        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
	        //System.out.println("Load Ontology...");
	        ontology = owlFile.isPresent()?
	      		  ontologyManager.loadOntologyFromOntologyDocument(owlFile.get()):ontologyManager.createOntology();
	        CreateSWRLAPIEngine();
		}
        catch (OWLOntologyCreationException e) {
            System.err.println("Error creating OWL ontology: " + e.getMessage());
            //System.exit(-1);
            return false;	
        } 
		catch (RuntimeException e) {
            System.err.println("Error starting application: " + e.getMessage());
            System.exit(-1);
        }
		return true;
	}
	
	public void testfunc() {
		System.out.println(ontology.getClass());
	}
	
		
	private void CreateSWRLAPIEngine() {
		queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);
	}
	

}
*/
public class OntologyFunc
{
	OWLOntologyManager manager = OntManagers.createONT();
	OWLDataFactory factory = manager.getOWLDataFactory();
	File file = new File("owlfile/CB_PDO_V4.owl");
	OWLOntology ontology;
	OntologyModel o;
	
	public boolean LoadOntology() {
		try {
			ontology = manager.loadOntologyFromOntologyDocument(file);
			o = (OntologyModel)ontology;
			return true;	
		}
		catch (OWLOntologyCreationException e) {
			System.err.println("Error creating OWL ontology: " + e.getMessage());
			//	System.exit(-1);
			return false;
		} 	
	}		
	
	// attacker가 사용한 sw와 내용, 플랫폼 load
	public ArrayList LoadGroupSW(String group) {
		ArrayList<String[]> listvector = new ArrayList<String[]>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?a_l ?a_c ?b WHERE { PDO:"+ group +" PDO:use_software ?a. ?a rdfs:label ?a_l."
				+ " ?a rdfs:comment ?a_c. ?a PDO:work_in_platforms ?b.}";
		
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("a_l").toString();
			String s2 = qs.get("a_c").toString();
			String s3 = qs.get("b").toString().split("#")[1];
			
			listvector.add(new String[]{s1, s2, s3});
		}	
		
		return listvector;
	}
	
	//"Reconnaissance", "Resource Development", "Initial Access", "Execution", "Persistence", "Privilege Escalation",
	//"Defense Evasion", "Credential Access", "Discovery", "Lateral Movement", "Collection", "Command and Control", "Exfiltration",
	//"Impact"
	// 전술에 따른 technique load
	public ArrayList LoadTechniquesByTactic(String tactic) {
		ArrayList<String> listvector = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?tech_label \n"
				+ "WHERE { \n"
				+ "  ?tactic rdfs:label \"" + tactic + "\".\n"
				+ "  ?tactic PDO:consist_of_techniques ?tech.\n"
				+ "  ?tech rdfs:label ?tech_label.\n"
				+ "}\n"
				+ "ORDER BY ASC(?tech_label)";
		//System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("tech_label").toString();
			
			listvector.add(s1);
		}		
		
		
		return listvector;
	}
	
	// attacker가 사용한 technique와 내용, 사용사례 load
	public ArrayList<String[]> LoadGroupTechnique(String group) {
		ArrayList<String[]> listvector = new ArrayList<String[]>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?a_l ?a_c ?s_l WHERE { PDO:"+ group +" PDO:use_techniques ?a. ?a rdfs:label ?a_l. "
				+ "?a rdfs:comment ?a_c. ?sample rdf:type PDO:"+ group +". "
				+ "?sample PDO:sample_of_use ?a. "
				+ "?sample rdfs:label ?s_l.}";
		
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("a_l").toString();
			String s2 = qs.get("a_c").toString();
			String s3 = qs.get("s_l").toString();
			
			listvector.add(new String[]{s1, s2, s3});
		}	
		
		return listvector;
	}
	
	
	// attacker가 사용한 technique에 대한 mitigation load
	public ArrayList LoadSR(String group) {
		ArrayList<String[]> listvector = new ArrayList<String[]>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?b_l ?b_c (count(?b_l) as ?count) WHERE { PDO:"+ group +" PDO:use_techniques ?a. ?a PDO:prevented_by ?b. "
				+ "?b rdfs:label ?b_l. ?b rdfs:comment ?b_c.} GROUP BY ?b_l ?b_c ORDER BY DESC(?count)";
		
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("b_l").toString();
			String s2 = qs.get("b_c").toString();
			String s3 = qs.get("count").toString().split("\\^")[0];
			
			listvector.add(new String[] {s1, s2, s3});
		}	
		
		return listvector;
	}
		
	// attacker가 사용한 sw에서 수행할 수 있는 technqiue들에 대한 mitigation 추가 load
	public ArrayList LoadAdditionalSR(String group) {
		ArrayList<String[]> listvector = new ArrayList<String[]>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?m_l ?m_c (count(?m_l) as ?count) WHERE { PDO:"+ group +" PDO:use_software ?s. ?s PDO:perform_techniques ?t. "
				+ "?t PDO:prevented_by ?m. ?m rdfs:label ?m_l. "
				+ "?m rdfs:comment ?m_c. FILTER NOT EXISTS{ PDO:"+ group +" PDO:use_techniques ?t. "
				+ "}} GROUP BY ?m_l ?m_c ORDER BY DESC(?count)";

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("m_l").toString();
			String s2 = qs.get("m_c").toString();
			String s3 = qs.get("count").toString().split("\\^")[0];
			
			listvector.add(new String[]{s1, s2, s3});
		}	
		
		return listvector;
	}
	
	public ArrayList LoadTacticsfromTechList(String group) {
		ArrayList<String[]> listvector = new ArrayList<String[]>();

		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?t_l ?ta_l\n"
				+ "WHERE { \n"
				+ "  PDO:"+ group +" PDO:use_techniques ?t.\n"
				+ "  ?t PDO:constitute_tactics ?ta.\n"
				+ "  ?t rdfs:label ?t_l.\n"
				+ "  ?ta rdfs:label ?ta_l.\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("t_l").toString();
			String s2 = qs.get("ta_l").toString();
			
			listvector.add(new String[]{s1, s2});
		}	
		return listvector;
		
	}
	//하나의 sw에 대한 work in platform load
	public List<String> LoadWorkingPlatformfromTech(String tech){
		List<String> platforms = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?p_l\n"
				+ "WHERE { \n"
				+ "  ?t rdfs:label \"" + tech + "\".\n"
				+ "  ?t PDO:work_in_platforms ?p.\n"
				+ "  ?p rdfs:label ?p_l.\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();	
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("p_l").toString();
			//System.out.println(s1);
			platforms.add(s1);
		}
		
		return platforms;
	}
	
	//하나의 technique에 대한 tactic load
	public List<String> LoadTacticfromOneTech(String tech) {
		List<String> tactics = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?ta_l\n"
				+ "WHERE { \n"
				+ "  ?t rdfs:label \"" + tech + "\".\n"
				+ "  ?t PDO:constitute_tactics ?ta.\n"
				+ "  ?ta rdfs:label ?ta_l.\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();	
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("ta_l").toString();
			//System.out.println(s1);
			tactics.add(s1);
		}
		return tactics;
	}

	//하나의 technique에 대한 attack pattern load
	public List<String> LoadAttackPatternfromOneTech(String tech) {
		List<String> attack_patterns = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?ap_l\n"
				+ "WHERE { \n"
				+ "  ?t rdfs:label \"" + tech + "\".\r\n"
				+ "  ?t PDO:have_attack_patterns ?ap.\r\n"
				+ "  ?ap rdfs:label ?ap_l.\r\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();	
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("ap_l").toString();
			attack_patterns.add(s1);
		}
		return attack_patterns;
	}	
	
	//하나의 attack pattern에 대한 weakness load
	public List<String> LoadWeaknessfromOneAttackPattern(String attackpattern) {
		List<String> weaknesses = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.oLoadSpecificGrouprg/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?w_l\n"
				+ "WHERE { \n"
				+ "  ?ap rdfs:label \"" + attackpattern + "\".\n"
				+ "  ?ap PDO:have_weaknesses ?w.\n"
				+ "  ?w rdfs:label ?w_l.\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();	
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("w_l").toString();
			weaknesses.add(s1);
		}
		return weaknesses;
	}	
	
	//하나의 weakness에 대한 vulnerability load
	public List<String> LoadVulnerabilityfromOneWeakness(String weakness) {
		List<String> vulnerabilities = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?v_l\n"
				+ "WHERE { \n"
				+ "  ?w rdfs:label \"" + weakness + "\".\n"
				+ "  ?w PDO:have_vulnerabilities ?v.\n"
				+ "  ?v rdfs:label ?v_l.\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();	
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("v_l").toString();
			vulnerabilities.add(s1);
		}
		return vulnerabilities;
	}	
	
	// attacker 리스트 load
	public List<String> LoadAttackGroups() {
		List<String> groups = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?b WHERE { ?ag rdf:type PDO:AC-Attack_Groups.\n ?ag rdfs:label ?b.}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("b").toString();
			//System.out.println(s1);
			groups.add(s1);
		}
		Collections.sort(groups);
		//System.out.println(groups);
		return groups;
	}
	
	//특정 attacker의 sw, tech load
	public List<List> LoadSpecificGroup(String group) {
		List<String> groups = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n";
		//if(group.contains("@en")) {
			//group = group.substring(0, group.indexOf("@en"));
			
		//}
		String software_query = "SELECT ?b WHERE { PDO:"+ group + " PDO:use_software ?a.\n ?a rdfs:label ?b.}";
		String technique_query = "SELECT ?b WHERE { PDO:"+ group + " PDO:use_techniques ?a.\n ?a rdfs:label ?b.}";
		
		String[] query_vec = {software_query, technique_query};
		
		List<List> list = new ArrayList();
		List<String> swlist = new ArrayList();
		List<String> techlist = new ArrayList();	
		
		list.add(swlist);
		list.add(techlist);		
		for(int i =0; i < list.size(); i++) {
			//System.out.println(queryString + query_vec[i]);
			List ml = list.get(i);
			//System.out.println(group);
			Query query = QueryFactory.create(queryString + query_vec[i]);
			QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
			ResultSet res = qe.execSelect();
			while (res.hasNext()) {
				QuerySolution qs = res.next();
				String s1 = qs.get("b").toString();
				ml.add(s1);
			}
			Collections.sort(ml);			
		}
		
		return list;
	}
	
	public List<String> LoadVectors(String vectorType) {
		List<String> vector = new ArrayList<String>();
		try {
			String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
					+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
					+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
					+ "SELECT ?b WHERE { ?a rdf:type "+ vectorType +".\n ?a rdfs:label ?b.}";
			Query query = QueryFactory.create(queryString);
			QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
			ResultSet res = qe.execSelect();
			while (res.hasNext()) {
				QuerySolution qs = res.next();
				String s1 = qs.get("b").toString();
				vector.add(s1);
			}
			Collections.sort(vector);
		}
		catch (RuntimeException e) {
			System.err.println("Error starting application: " + e.getMessage());
		}
		return vector;
	}
	
	public List<String> LoadTechniquesFromSW(String SWvector){
		List<String> techlist = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n"
				+ "SELECT ?t_l\n"
				+ "WHERE { \n"
				+ "  ?s rdfs:label \""+ SWvector +"\".\n"
				+ "  ?s PDO:perform_techniques ?t.\n"
				+ "  ?t rdfs:label ?t_l.\n"
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("t_l").toString();
			techlist.add(s1);
		}
		
		return techlist;
	}
	//공격 그룹이 사용한 techniques load
	public List<String> LoadTechniquesFromGroup(String group){
		List<String> groups = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\n";
		String technique_query = "SELECT ?b WHERE { PDO:"+ group + " PDO:use_techniques ?a.\n ?a rdfs:label ?b.}";

		List<String> techlist = new ArrayList();	

		Query query = QueryFactory.create(queryString + technique_query);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("b").toString();
			techlist.add(s1);
		}
		return techlist;	
	}
	
	public List<String> Rule1(String input_technique, String group) {
		List<String> groups = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\r\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\r\n"
				+ "SELECT DISTINCT ?t_l\r\n"
				+ "WHERE { \r\n"
				+ "  PDO:"+ group +" PDO:use_techniques ?t.\r\n"
				+ "  ?i_t rdfs:label \""+ input_technique +"\".\r\n"
				+ "  ?t PDO:is_child_of|PDO:is_sibling_with|PDO:is_parent_of ?i_t.\r\n"
				+ "  ?ta rdfs:label ?ta_l.\r\n"
				+ "  ?t rdfs:label ?t_l.\r\n"
				+ "} ";
		List<String> techlist = new ArrayList();	

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("t_l").toString();
			techlist.add(s1);
		}
		return techlist;	
	}

	public List<String> Rule2(String input_technique, String group) {
		List<String> groups = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\r\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\r\n"
				+ "SELECT DISTINCT ?t_l\r\n"
				+ "WHERE { \r\n"
				+ "  PDO:"+ group +" PDO:use_techniques ?t.\r\n"
				+ "  ?i_t rdfs:label \""+ input_technique +"\".\r\n"
				+ "  ?i_t PDO:work_in_platforms ?p.\r\n"
				+ "  ?t PDO:work_in_platforms ?p.\r\n"
				+ "  ?t rdfs:label ?t_l.\r\n"
				+ "  FILTER ( ?i_t != ?t )\r\n"
				+ "} ";
		List<String> techlist = new ArrayList();	
		//System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("t_l").toString();
			techlist.add(s1);
		}
		return techlist;	
	}

	public List<String> Rule3(String input_technique, String group) {
		List<String> groups = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\r\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\r\n"
				+ "SELECT DISTINCT ?t_l\r\n"
				+ "WHERE { \r\n"
				+ "  PDO:"+ group +" PDO:use_techniques ?t.\r\n"
				+ "  ?i_t rdfs:label \""+ input_technique +"\".\r\n"
				+ "  ?i_t PDO:constitute_tactics ?ta.\r\n"
				+ "  ?t PDO:constitute_tactics ?ta.\r\n"
				+ "  ?t rdfs:label ?t_l.\r\n"
				+ "  FILTER ( ?i_t != ?t )\r\n"
				+ "} ";
		List<String> techlist = new ArrayList();	

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("t_l").toString();
			techlist.add(s1);
		}
		return techlist;	
	}

	public List<String> Rule4(String input_technique, String group) {
		List<String> groups = new ArrayList<String>();
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX PDO: <http://www.semanticweb.org/PDO#>\r\n"
				+ "PREFIX CBSAO: <http://www.semanticweb.org/CBSAO#>\r\n"
				+ "SELECT DISTINCT ?t_l\r\n"
				+ "WHERE { \r\n"
				+ "  PDO:"+ group +" PDO:use_techniques ?t.\r\n"
				+ "  ?i_t rdfs:label \""+ input_technique +"\".\r\n"
				+ "  ?i_t PDO:have_attack_patterns ?ap.\r\n"
				+ "  ?t PDO:have_attack_patterns ?ap.\r\n"
				+ "  ?t rdfs:label ?t_l.\r\n"
				+ "  FILTER ( ?i_t != ?t )\r\n"
				+ "} ";
		List<String> techlist = new ArrayList();	

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, o.asGraphModel());
		ResultSet res = qe.execSelect();
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String s1 = qs.get("t_l").toString();
			techlist.add(s1);
		}
		return techlist;	
	}
	
	private static void Usage()
	{
		System.err.println("Usage: " + OntologyFunc.class.getName() + " [ <owlFileName> ]");
		System.exit(1);
	}
  
}
