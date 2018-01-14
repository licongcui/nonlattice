package nl.pre;
/**
 * @author Guangming Xing and Licong Cui
 * @date July 13, 2016
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import nl.model.Concept;
import nl.model.ConceptPair;
import nl.model.ConceptRef;
import nl.model.RelationRowData;

public class NonLatticeProcessorBottomUp {
	String DELIMITER = "\\t|\\|";
	Map<String, Concept> conceptMap;

	Set<String> allRelations;
	
	private NonLatticeProcessorBottomUp() {
		super();

		this.conceptMap = new LinkedHashMap<String, Concept>();
		this.allRelations = new LinkedHashSet<String>();

	}

	/**
	 * in case we want to use in a web app
	 */
	private static NonLatticeProcessorBottomUp instance;

	public static NonLatticeProcessorBottomUp getInstance(String conceptFileName,
			String relationFileName, String relType) {
		if (instance == null) {
			instance = new NonLatticeProcessorBottomUp();
			instance.init(conceptFileName, relationFileName, relType);
		}

		return instance;
	}
	
	public void init(String conceptFileName, String relationFileName, String relType) {

		this.load(conceptFileName);
		this.buildConceptRelation(relationFileName, relType);

		Iterator<String> it = allRelations.iterator();
	}
	
	public void load(String conceptFileName) {
		Scanner input = null;
		try {
			input = new Scanner(new FileInputStream(conceptFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// skip the first line
		input.nextLine();

		while (input.hasNextLine()) {
			String line = input.nextLine();
			String[] lineData = line.split(DELIMITER);
			String cid = lineData[0];
			String clabel = lineData[1];
			Concept concept = conceptMap.get(cid);

			if (concept == null) {
				concept = new Concept();
				conceptMap.put(cid, concept);
				concept.setId(cid);
				concept.setLabel(clabel);
			}

		}

		System.out.println("Loading concepts done with " + conceptMap.size() + " entries");
	}

	public void buildConceptRelation(String relationFileName, String rel) {
		Scanner input = null;
		try {
			input = new Scanner(new FileInputStream(relationFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Set<String> allConcepts = new HashSet<String>(){};
		
		//Assume there is no header information. Use "input.nextLine();" if we want to skip the header information in the first line
	
		int cnt = 0;
		while (input.hasNextLine()) {
			String line = input.nextLine();
			RelationRowData data = new RelationRowData(line);
			
			if (!data.type().equals(rel))
				continue;

			Concept src = conceptMap.get(data.src());
			Concept dest = conceptMap.get(data.dest());
			allConcepts.add(data.src());
			allConcepts.add(data.dest());
			
			String type = data.type();
			allRelations.add(type);

			if (src == null || dest == null)
				continue;

			src.addToRef(new ConceptRef(dest, type));
			dest.addFromRef(new ConceptRef(src, type));
			cnt++;

		}
		System.out.println(allConcepts.size() + " unique concepts");
		System.out.println(cnt + " relations");
	}
	
	private Concept getOther(Concept me, ConceptPair pair) {
		if (pair.a.equals(me))
			return pair.b;
		else
			return pair.a;
	}
	
	public long nonLatticeBottomUpCnt() throws FileNotFoundException {
		long computeCnt = 0;
		int cnt = 0;
		Concept[] concepts = new Concept[this.conceptMap.size()];

		Concept[] sorted = new Concept[this.conceptMap.size()];

		Iterator<Entry<String, Concept>> it = this.conceptMap.entrySet()
				.iterator();

		while (it.hasNext()) {
			concepts[cnt++] = it.next().getValue();
		}

		//bottom-up topological sort begin as well as computing ancestors
		Queue<Concept> conQueue = new LinkedList<Concept>(); //Set of all nodes with no incoming edges

		for (int i = 0; i < concepts.length; i++) {
			concepts[i].ancestor = new LinkedHashSet<Concept>();
			if (concepts[i].getFromRefs() == null
					|| concepts[i].getFromRefs().size() == 0) {
				conQueue.add(concepts[i]);
				concepts[i].visited = true;
			}
		}
		long startTime = System.currentTimeMillis();

		while (!conQueue.isEmpty()) {
			Concept con = conQueue.remove();

			sorted[(int) computeCnt] = con;
			computeCnt++;

			if (con.getToRefs() == null)
				continue;

			for (ConceptRef conRef : con.getToRefs()) {
				conRef.getConcept().toProcessed++;
				conRef.getConcept().ancestor.addAll(con.ancestor);
				conRef.getConcept().ancestor.add(con);
				if (conRef.getConcept().toProcessed == conRef.getConcept()
						.getFromRefs().size()) {
					if (!conRef.getConcept().visited)
						conQueue.add(conRef.getConcept());
				}
			}

		}//bottom-up topological sort end
		
		//System.out.println("start processing with " + sorted.length);
		//System.out.println("" + computeCnt + " has been computed in " + (System.currentTimeMillis() - startTime) / 1000);
		
		//up-down topological sort begin as well as computing descendants
		Concept[] sortedRev = new Concept[this.conceptMap.size()];
		computeCnt = 0;
		Queue<Concept> conQueueRev = new LinkedList<Concept>(); //Set of all nodes with no incoming edges

		for (int i = 0; i < concepts.length; i++) {
			concepts[i].descendant = new LinkedHashSet<Concept>();
			if (concepts[i].getToRefs() == null
					|| concepts[i].getToRefs().size() == 0) {
				conQueueRev.add(concepts[i]);
				concepts[i].visitedRev = true;
			}
		}
		startTime = System.currentTimeMillis();

		while (!conQueueRev.isEmpty()) {
			Concept con = conQueueRev.remove();

			sortedRev[(int) computeCnt] = con;
			computeCnt++;

			if (con.getFromRefs() == null)
				continue;

			for (ConceptRef conRef : con.getFromRefs()) {
				conRef.getConcept().toProcessedRev++;
				conRef.getConcept().descendant.addAll(con.descendant);
				conRef.getConcept().descendant.add(con);
				if (conRef.getConcept().toProcessedRev == conRef.getConcept()
						.getToRefs().size()) {
					if (!conRef.getConcept().visitedRev)
						conQueueRev.add(conRef.getConcept());
				}
			}

		}//up-down topological sort end

		cnt = 0;

		Map<ConceptPair, ConceptPair> pairMap = new LinkedHashMap<ConceptPair, ConceptPair>();

		Map<Concept, List<ConceptPair>> conceptToPairMap = new LinkedHashMap<Concept, List<ConceptPair>>();

		// go through all edges
		// in the topological order of the concepts that are visited
		for (int i = 0; i < sorted.length; i++) {
			Concept con = sorted[i];
//			System.out.println(i);
//			System.out.println(con.getClass());
//			System.out.println(con.toString());
			if (con == null){
				System.out.println("N");
				continue;
			}
				
			List<ConceptRef> refs = con.getToRefs();
			if (refs != null)
				for (int j = 0; j < refs.size(); j++) {
					for (int k = j + 1; k < refs.size(); k++) {

						if (refs.get(j).getConcept().ancestor.contains(refs
								.get(k).getConcept())
								|| refs.get(k).getConcept().ancestor
										.contains(refs.get(j).getConcept()))
							continue;

						ConceptPair pair = new ConceptPair(refs.get(j)
								.getConcept(), refs.get(k).getConcept());

						/**
						 * Weird situation for identical pairs, print sth out
						 */
						if (pair.a.equals(pair.b)) {
							System.out.println("**************************"
									+ pair.a);
							System.out.println("##########################"
									+ con);
						}

						ConceptPair realPair = pairMap.get(pair);

						if (realPair == null) {
							pairMap.put(pair, pair);
							pair.addLca(con);

							List<ConceptPair> jList = conceptToPairMap.get(refs
									.get(j).getConcept());
							if (jList == null) {
								jList = new ArrayList<ConceptPair>();
								conceptToPairMap.put(refs.get(j).getConcept(),
										jList);
							}

							jList.add(pair);

							List<ConceptPair> kList = conceptToPairMap.get(refs
									.get(k).getConcept());
							if (kList == null) {
								kList = new ArrayList<ConceptPair>();
								conceptToPairMap.put(refs.get(k).getConcept(),
										kList);
							}

							kList.add(pair);
						} else {
							Set<Concept> toAdd = new LinkedHashSet<Concept>();
							toAdd.add(con);
							realPair.addLca(toAdd);
						}

					}

				}
		}

		System.out.println("pair map (initial pariable pair) is of " + pairMap.size());

		Iterator<Entry<ConceptPair, ConceptPair>> pIt = pairMap.entrySet()
				.iterator();
		while (pIt.hasNext()) {
			ConceptPair pair = pIt.next().getValue();
			// System.out.println(pair);

			if (pair.lca.size() > 1)
				cnt++;

		}

		//System.out.println("********************************non-lattices of " + cnt);
		
		for (int i = 0; i < sorted.length; i++) {
			Concept con = sorted[i];
			if (con == null){
				System.out.println("N");
				continue;
			}
			List<ConceptRef> refs = con.getToRefs();
			List<ConceptPair> conPairs = conceptToPairMap.get(con);

			if (refs != null && conPairs != null)
				for (int j = 0; j < refs.size(); j++) {
					for (int k = 0; k < conPairs.size(); k++) {

						ConceptPair old = conPairs.get(k);

						Concept other = this.getOther(con, old);

						if (refs.get(j).getConcept().ancestor
								.contains(other)
								|| other.ancestor.contains(refs.get(j)
										.getConcept()))
							continue;

						ConceptPair pair = new ConceptPair(refs.get(j)
								.getConcept(), other);

						/**
						 * Weird situation, print sth out
						 */
						if (pair.a.equals(pair.b)) {
							System.out.println("**************************"
									+ pair.a);
							System.out.println("##########################"
									+ con);

							List<ConceptRef> trefs = con.getToRefs();
							List<ConceptPair> tconPairs = conceptToPairMap
									.get(con);

							for (int tj = 0; tj < trefs.size(); tj++) {
								System.out.println(trefs.get(tj)
										.getConcept());
							}

							for (int tk = 0; tk < tconPairs.size(); tk++) {
								System.out.println(this.getOther(con,
										tconPairs.get(tk)));
							}
						}

						ConceptPair realPair = pairMap.get(pair);

						if (realPair == null) {
							pair.addLca(old.lca);
							pairMap.put(pair, pair);

							List<ConceptPair> oList = conceptToPairMap
									.get(other);
							if (oList == null) {
								oList = new ArrayList<ConceptPair>();
								conceptToPairMap.put(other, oList);
							}

							oList.add(pair);

							List<ConceptPair> jList = conceptToPairMap
									.get(refs.get(j).getConcept());
							if (jList == null) {
								jList = new ArrayList<ConceptPair>();
								conceptToPairMap.put(refs.get(j)
										.getConcept(), jList);
							}

							jList.add(pair);

						} else {
							// make sure that this new one is good by checking no existing ones is a descendant
							realPair.addLca(old.lca);
							pair = realPair;

						}

					}

				}

		}

		//System.out.println("pairmap size: " + pairMap.size());

		cnt = 0;

		pIt = pairMap.entrySet().iterator();
		while (pIt.hasNext()) {
			ConceptPair pair = pIt.next().getValue();
			// System.out.println(pair);

			if (pair.lca.size() > 1)
				cnt++;

		}

		System.out.println("pair map (pariable pairs) is of " + pairMap.size());
		
		PrintWriter pw = new PrintWriter(new File("nonlattice_pairs.txt"));

		cnt = 0;
		pIt = pairMap.entrySet().iterator();
		while (pIt.hasNext()) {
			ConceptPair p = pIt.next().getValue();
			if (p.lca.size() > 1 && !p.a.equals(p.b)) {
				cnt++;
				pw.println(p);
			}
		}
		pw.close();
		System.out.println("non-lattice pairs of " + cnt);
		return cnt;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		long startTime = System.currentTimeMillis();
		
//		NonLatticeProcessorBottomUpClosure cp = getInstance(
//		"concepts.txt",
//		"relations.txt",
//		"is-a");
		
		NonLatticeProcessorBottomUp cp = getInstance(
		"labels.txt",
		"hierarchy.txt",
		"");

		cp.nonLatticeBottomUpCnt();

		System.out.println((System.currentTimeMillis() - startTime) / 1000 + " seconds");

	}

}
