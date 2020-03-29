package Model;

import javax.swing.text.DefaultEditorKit.BeepAction;

public class EvalNode {

	public static final int PVNODE = 0;
	public static final int CUTNODE = 1;
	public static final int ALLNODE = 2;
	
	public int evaluation;
	public int nodeType;
	
	public EvalNode(int evaluation, int nodeType){
		this.evaluation = evaluation;
		this.nodeType = nodeType;
	}
	
}
