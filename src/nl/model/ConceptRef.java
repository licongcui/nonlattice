package nl.model;


/**
 * @author Guangming Xing and Licong Cui
 * @date July 13, 2016
 */

public class ConceptRef {
	Concept concept;
	String relation;
	
	
	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public ConceptRef(Concept concept, String relation){
		this.concept = concept;
		this.relation = relation;
	}


	@Override
	public int hashCode() {
		return 31 * concept.hashCode() + relation.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		ConceptRef other = (ConceptRef) obj;
		if(concept.equals(other.concept) && other.relation.equals(relation))
			return true;
		
		return false;
	}




	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConceptRef [concept:");
		builder.append(" \n id:"+concept.getId());
		builder.append(" \n relation:");
		builder.append("]");
		return builder.toString();
	}

	
	
	
}
