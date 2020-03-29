package UI;

import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import Model.Move;
import Model.MoveAndStatistik;

public class OpeningBookTableModel extends AbstractTableModel{

	private List<MoveAndStatistik> openingMoves = new LinkedList<MoveAndStatistik>();
	
	@Override
	public int getRowCount() {
		return openingMoves.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public String getColumnName(int column){
		switch(column){
		case 0: return "Move";
		case 1: return "Score";
		default: return null;	
		}
		
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		MoveAndStatistik moveAndStatistiks = openingMoves.get(rowIndex);
		
		switch(columnIndex){
		case 0: return moveAndStatistiks.getMove();
		case 1: return moveAndStatistiks.getWeight();
		default: return 0;
		
		}
	}
	
	public OpeningBookTableModel(){

	}
	
	public void setOpeningMoves(List<MoveAndStatistik> list){
		this.openingMoves = list;
		super.fireTableDataChanged();
	}
	
	public Move getMoveAt(int rowIndex){
		return openingMoves.get(rowIndex).getMove();
	}
	
	public List<MoveAndStatistik> getMoveAndStatistiks(){
		return openingMoves;
	}

	

}
