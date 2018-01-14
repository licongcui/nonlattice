package nl.model;

/**
 * @author Guangming Xing and Licong Cui
 * @date July 13, 2016
 */

public class RelationRowData {
	String[] fields;
	public RelationRowData(String data) {
		fields = data.split("\\t|\\|");
	}

	public String src() {
		return fields[0];
	}
	
	public String dest() {
		return fields[1];
	}

	public String type() {
		if (fields.length <= 2){
			return "";
		}
		return fields[2];
	}
}

