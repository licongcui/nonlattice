package nl.model;

import nl.model.Concept;
import nl.model.ConceptRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Guangming Xing and Licong Cui
 * @date July 13, 2016
 */

public class Concept {
	private String id;
	private String label;
	
	private List<ConceptRef> toRefs;
	private List<ConceptRef> fromRefs;
	
	public Set<Concept> ancestor;
	public Set<Concept> descendant;
	
	// only used in find multiple path with direct link
	public Set<Concept> directAncestor;
	
	
	public boolean visited = false;
	public boolean visitedRev = false;
	public boolean isObsolete = false;
	public int toProcessed = 0;
	public int toProcessedRev = 0;
	
	public void addToRef(ConceptRef ref) {
		if(toRefs == null)
			toRefs = new ArrayList<ConceptRef>();
	
		toRefs.add(ref);
	}
	
	public void addFromRef(ConceptRef ref) {
		if(fromRefs == null)
			fromRefs = new ArrayList<ConceptRef>();
	
		fromRefs.add(ref);
	}
	
	public void removeToRef(ConceptRef ref){
		if (toRefs != null)
			toRefs.remove(ref);
	}
	
	public void removeFromRef(ConceptRef ref){
		if (fromRefs != null)
			fromRefs.remove(ref);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getAncestorLabel(){
		StringBuilder builder = new StringBuilder();
		for (Concept con : this.directAncestor ) {
			builder.append(con.getId());
			builder.append("|");
		}
		builder.setLength(builder.length() - 1);
		return builder.toString();
	}
	
	public List<ConceptRef> getToRefs() {
		return toRefs;
	}

	public List<ConceptRef> getFromRefs() {
		return fromRefs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Concept [id:");
		builder.append(id);
		builder.append(" \n text:");
		builder.append(" \n toRefs:");
		builder.append(toRefs);
		builder.append(" \n fromRefs:");
		builder.append(fromRefs);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		Concept other = (Concept) obj;
		if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}