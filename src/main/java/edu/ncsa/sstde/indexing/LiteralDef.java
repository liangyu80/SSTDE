package edu.ncsa.sstde.indexing;

/**
 * @author liangyu
 * 
 *         A literal definition is composed of the variable name and the type
 *         uri. It is defined for IndexGraph so that the indexer knows how to
 *         create a column with a proper data type for this variable
 */
public class LiteralDef {
	private String var = null;
	private String type = null;

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LiteralDef(String var, String type) {
		this.var = var;
		this.type = type;
	}
}