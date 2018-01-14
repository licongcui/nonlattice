package nl.model;

import nl.util.SetUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Guangming Xin and Licong Cui
 * @date July 13, 2016
 */

public class ConceptPair {
	public Concept a;
	public Concept b;
	public Set<Concept> lca;
	
	
	
	public boolean filter(String [] ids) {
		for(String id: ids) {
			if(a.getId().equals(id) || b.getId().equals(id))
				return true;
		}
		
		return false;
	}
	
	/**
	 * @param i
	 * @param j
	 * @return
	 */
	public boolean filter(String i, String j) {
		if(a.getId().equals(i) && b.getId().equals(j))
			return true;
		
		return false;
	}
	
	
	
	public ConceptPair(Concept a, Concept b) {
		super();
		if(a.getId().compareTo(b.getId()) > 0) {
			this.a = b;
			this.b = a;
		} else {
			this.a = a;
			this.b = b;
		}
	}
	
	
	public ConceptPair(Concept a, Concept b, Concept lca) {
		super();
		if(a.getId().compareTo(b.getId()) > 0) {
			this.a = b;
			this.b = a;
		} else {
			this.a = a;
			this.b = b;
		}
		this.lca = new LinkedHashSet<Concept>();
		this.lca.add(lca);
	}
	
	
	
	public ConceptPair(Concept a, Concept b, Set<Concept> lca) {
		super();
		if(a.getId().compareTo(b.getId()) > 0) {
			this.a = b;
			this.b = a;
		} else {
			this.a = a;
			this.b = b;
		}
		this.lca = new LinkedHashSet<Concept>();
		this.lca.addAll(lca);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if(a.hashCode() < b.hashCode()) {
		result = ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		} else {
			result = ((b == null) ? 0 : b.hashCode());
			result = prime * result + ((a == null) ? 0 : a.hashCode());
		}
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		ConceptPair other = (ConceptPair) obj;
		
		if((other.a.equals(this.a) && other.b.equals(this.b)) ||
				(other.a.equals(this.b) && other.b.equals(this.a)))
			return true;
		
		return false;
	}


	/**
	 * @param lca2
	 */
	public void addLca(Set<Concept> lca2) {
		if(lca2 == null)
			return;
		
		
		
		if(this.lca == null)
			this.lca = new LinkedHashSet<Concept>();
		
		
		ArrayList<Concept> toAdd = new ArrayList<Concept>();
		
		for(Concept candidate : lca2) {
			boolean good = true;
			for(Concept old : lca) {
				if(old.ancestor.contains(candidate)) {
					good = false;
					break;
				}
				
			}
			
			if(good)
				toAdd.add(candidate);
		}
		List<Concept> toRemove = new ArrayList<Concept>();
		for(Concept old : lca) {
		
			boolean good = true;
			for(Concept mtoadd : toAdd) {
				if(mtoadd.ancestor.contains(old)) {
					good = false;
					break;
				}
				
			}
			
			if(!good)
				toRemove.add(old);
		}
		
		this.lca.removeAll(toRemove);
		this.lca.addAll(toAdd);
	}


	/**
	 * @param con
	 */
	public void addLca(Concept con) {
		if(this.lca == null)
			this.lca = new LinkedHashSet<Concept>();
		
		
		this.lca.add(con);
	}
	
	/**
	 * Get all nodes in a fragment determined by seed pair and lca 
	 * @param closure
	 * @return
	 */
	public Set<Concept> getFragment(){
		Set<Concept> result = new LinkedHashSet<Concept>();
		result.add(this.a);
		result.add(this.b);
		result.addAll(this.lca);
		for (Concept l : this.lca){
			result.addAll(SetUtil.intersection(a.ancestor, l.descendant));
			result.addAll(SetUtil.intersection(b.ancestor, l.descendant));
		}
		return result;
	}
	
	/**
	 * Get fragment edges
	 * @param fragment
	 * @return
	 */
	public Set<ConceptOrderedPair> getFragmentEdges(Set<Concept> fragment){
		Set<ConceptOrderedPair> result = new LinkedHashSet<ConceptOrderedPair>();
		for (Concept c : fragment){
			List<ConceptRef> toRefs = c.getToRefs();
			for (ConceptRef cr : toRefs){
				if (fragment.contains(cr.getConcept())){
					result.add(new ConceptOrderedPair(c, cr.getConcept()));
				}
			}
		}
		return result;
	}
	
	/**
	 * Output fragment to a string consisting of four parts: 
	 * seed pair, lca nodes, fragment nodes, edges
	 * @return
	 */
	public String toFragmentString(){
		StringBuilder sb = new StringBuilder();
		if (lca == null) {
			sb.append("########");
			return sb.toString();
			
		}
		
		sb.append(this.a.getId());
		sb.append(",");
		sb.append(this.b.getId());
		
		sb.append("\t");
		Iterator<Concept> it = this.lca.iterator();
		sb.append(it.next().getId());
		while(it.hasNext()) {
			sb.append("/");
			sb.append(it.next().getId());
		}
		
		sb.append("\t");
		Set<Concept> fragment = getFragment();
		if (fragment.isEmpty())
			return sb.toString();
		for (Concept c : fragment){
			sb.append(c.getId());
			sb.append(",");
		}
		sb.setLength(sb.length() - 1);
		
		sb.append("\t");
		Set<ConceptOrderedPair> edgePairs = getFragmentEdges(fragment);
		if (edgePairs.isEmpty())
			return sb.toString();
		for (ConceptOrderedPair p : edgePairs){
			sb.append(p.a.getId());
			sb.append("|");
			sb.append(p.b.getId());
			sb.append(",");
		}

		sb.setLength(sb.length() - 1);

		return sb.toString();
	}
	
	/**
	 * Get reverse closure for the pair's lca
	 * @return
	 */
	public Set<Concept> getClosure(){
		Set<Concept> result = new LinkedHashSet<Concept>();
		Set<Concept> common = new LinkedHashSet<Concept>();
		for (Concept con : this.lca){
			common.addAll(con.descendant);
			break;
		}
		for (Concept con : this.lca){
			common.retainAll(con.descendant);
		}
		result.addAll(common);
		for (Concept con : common){
			result.removeAll(con.descendant);
		}
		return result;
	}
	
	/**
	 * Get all nodes in a fragment determined by lca and reverse closure
	 * @param closure
	 * @return
	 */
	public Set<Concept> getClosureFragment(Set<Concept> closure){
		Set<Concept> result = new LinkedHashSet<Concept>();
		result.addAll(closure);
		result.addAll(this.lca);
		for (Concept c : closure){
			for (Concept l : this.lca){
				result.addAll(SetUtil.intersection(c.ancestor, l.descendant));
			}
		}
		return result;
	}
	
	/**
	 * Output closure fragment to a string consisting of five parts: 
	 * lca nodes (lower boundary), reverse closure nodes (upper boundary), fragment nodes, edges, fragment size
	 * @return
	 */
	public String toClosureString(){
		StringBuilder sb = new StringBuilder();
		if (lca == null) {
			sb.append("########");
			return sb.toString();
			
		}
		
		//lower boundary concepts
		Iterator<Concept> it = this.lca.iterator();
		sb.append(it.next().getId());
		while(it.hasNext()) {
			sb.append("|");
			sb.append(it.next().getId());
		}
		
		//upper boundary concepts
		sb.append(";");
		Set<Concept> closure = this.getClosure();
		if (closure.isEmpty())
			return sb.toString();
		for (Concept c : closure){
			sb.append(c.getId());
			sb.append("|");
		}
		sb.setLength(sb.length() - 1);
		
		//fragment concepts
		sb.append(";");
		Set<Concept> fragment = getClosureFragment(closure);
		if (fragment.isEmpty())
			return sb.toString();
		for (Concept c : fragment){
			sb.append(c.getId());
			sb.append("|");
		}
		sb.setLength(sb.length() - 1);
		
		//fragment edges
		sb.append(";");
		Set<ConceptOrderedPair> edgePairs = getFragmentEdges(fragment);
		if (edgePairs.isEmpty())
			return sb.toString();
		for (ConceptOrderedPair p : edgePairs){
			sb.append(p.a.getId());
			sb.append("|");
			sb.append(p.b.getId());
			sb.append(",");
		}
		sb.setLength(sb.length() - 1);
		
		//fragment size
		sb.append(";");
		sb.append(fragment.size());
		
		return sb.toString();
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(a.getId());
		builder.append("/");
		builder.append(b.getId());
		
		if(lca == null) {
			builder.append("########");
			return builder.toString();
			
		}
		
		Iterator<Concept> it = lca.iterator();
		builder.append("\t");
		builder.append(it.next().getId());
		while(it.hasNext()) {
			builder.append(",");
			builder.append(it.next().getId());
		}
		return builder.toString();
	}


	
}