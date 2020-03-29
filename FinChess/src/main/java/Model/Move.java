package Model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a chess move.
 */
public class Move {

	private static Logger logger = LogManager.getLogger();
	// from (6 bits)

	// tosq (6 bits)

	// piec (4 bits)

	// capt (4 bits)

	// prom (4 bits)

	private int moveInt;

	public Move() {
		moveInt = 0;
	}

	public Move(Move move) {
		this.moveInt = move.moveInt;
	}

	public void clear() {
		moveInt = 0;
	}

	public void setFrom(int from) {
		setMoveInt(getMoveInt() & 0xffffffc0);
		setMoveInt(getMoveInt() | (from & 0x0000003f));
	}

	public void setTosq(int tosq) {

		setMoveInt(getMoveInt() & 0xfffff03f);
		setMoveInt(getMoveInt() | (tosq & 0x0000003f) << 6);
	}

	public void setPiec(int piec) {
		setMoveInt(getMoveInt() & 0xffff0fff);
		setMoveInt(getMoveInt() | (piec & 0x0000000f) << 12);

	}

	/*public void setCapt(int capt) {
		setMoveInt(getMoveInt() & 0xfff0ffff);
		setMoveInt(getMoveInt() | (capt & 0x0000000f) << 16);
	}*/

	/*public void setProm(int prom) {
		setMoveInt(getMoveInt() & 0xff0fffff);
		setMoveInt(getMoveInt() | (prom & 0x0000000f) << 20);
	}*/

	public int getFrom() {
		return (int) (getMoveInt() & 0x0000003f);
	}

	public int getTosq() {
		return (int) ((getMoveInt() >> 6) & 0x0000003f);
	}

	public int getPiec() {
		return (int) ((getMoveInt() >> 12) & 0x0000000f);
	}

	public boolean isWhitemove() {
		return !isBlackmove();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (moveInt ^ (moveInt >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Move other = (Move) obj;
		if (moveInt != other.moveInt)
			return false;
		return true;
	}

	public boolean isBlackmove() {
		return (getMoveInt() & 0x00008000) == 0x00008000;
	}

	public boolean isRookmove() {
		return (getMoveInt() & 0x00007000) == 0x00006000;
	}


	

	/**
	 * 
	 * @return the move in the move notation used by the universal chess interface.
	 */
	public String toUCIMoveNotation() {

		if (isNullMove()) {
			return "0000";
		}

		int from = getFrom();
		char c0;
		switch (from % 8) {
		case 0:
			c0 = 'a';
			break;
		case 1:
			c0 = 'b';
			break;
		case 2:
			c0 = 'c';
			break;
		case 3:
			c0 = 'd';
			break;
		case 4:
			c0 = 'e';
			break;
		case 5:
			c0 = 'f';
			break;
		case 6:
			c0 = 'g';
			break;
		case 7:
			c0 = 'h';
			break;
		default:
			return null;
		}
		char c1;

		switch (from / 8) {
		case 0:
			c1 = '1';
			break;
		case 1:
			c1 = '2';
			break;
		case 2:
			c1 = '3';
			break;
		case 3:
			c1 = '4';
			break;
		case 4:
			c1 = '5';
			break;
		case 5:
			c1 = '6';
			break;
		case 6:
			c1 = '7';
			break;
		case 7:
			c1 = '8';
			break;
		default:
			return null;
		}

		int tosq = getTosq();
		char c2;

		switch (tosq % 8) {
		case 0:
			c2 = 'a';
			break;
		case 1:
			c2 = 'b';
			break;
		case 2:
			c2 = 'c';
			break;
		case 3:
			c2 = 'd';
			break;
		case 4:
			c2 = 'e';
			break;
		case 5:
			c2 = 'f';
			break;
		case 6:
			c2 = 'g';
			break;
		case 7:
			c2 = 'h';
			break;
		default:
			return null;
		}

		char c3;

		switch (tosq / 8) {
		case 0:
			c3 = '1';
			break;
		case 1:
			c3 = '2';
			break;
		case 2:
			c3 = '3';
			break;
		case 3:
			c3 = '4';
			break;
		case 4:
			c3 = '5';
			break;
		case 5:
			c3 = '6';
			break;
		case 6:
			c3 = '7';
			break;
		case 7:
			c3 = '8';
			break;
		default:
			return null;
		}

		return "" + c0 + c1 + c2 + c3;

	}

	public String toString() {

		if (isNullMove()) {
			return "--";
		}

		
		char c0;

		switch ((int) getPiec()) {
		
		case (int) BasicEngine.W_KNIGHT:
			c0 = 'S';
			break;
		case (int) BasicEngine.B_KNIGHT:
			c0 = 'S';
			break;
		case (int) BasicEngine.W_BISHOP:
			c0 = 'L';
			break;
		case (int) BasicEngine.B_BISHOP:
			c0 = 'L';
			break;
		case (int) BasicEngine.W_ROOK:
			c0 = 'T';
			break;
		case (int) BasicEngine.B_ROOK:
			c0 = 'T';
			break;
		default:
			return null;
		}

		int from = getFrom();
		char c1;
		switch (from % 8) {
		case 0:
			c1 = 'a';
			break;
		case 1:
			c1 = 'b';
			break;
		case 2:
			c1 = 'c';
			break;
		case 3:
			c1 = 'd';
			break;
		case 4:
			c1 = 'e';
			break;
		case 5:
			c1 = 'f';
			break;
		case 6:
			c1 = 'g';
			break;
		case 7:
			c1 = 'h';
			break;
		default:
			return null;
		}
		char c2;

		switch (from / 8) {
		case 0:
			c2 = '1';
			break;
		case 1:
			c2 = '2';
			break;
		case 2:
			c2 = '3';
			break;
		case 3:
			c2 = '4';
			break;
		case 4:
			c2 = '5';
			break;
		case 5:
			c2 = '6';
			break;
		case 6:
			c2 = '7';
			break;
		case 7:
			c2 = '8';
			break;
		default:
			return null;
		}
		char c3 = '-';

		int tosq = getTosq();
		char c4;

		switch (tosq % 8) {
		case 0:
			c4 = 'a';
			break;
		case 1:
			c4 = 'b';
			break;
		case 2:
			c4 = 'c';
			break;
		case 3:
			c4 = 'd';
			break;
		case 4:
			c4 = 'e';
			break;
		case 5:
			c4 = 'f';
			break;
		case 6:
			c4 = 'g';
			break;
		case 7:
			c4 = 'h';
			break;
		default:
			return null;
		}

		char c5;

		switch (tosq / 8) {
		case 0:
			c5 = '1';
			break;
		case 1:
			c5 = '2';
			break;
		case 2:
			c5 = '3';
			break;
		case 3:
			c5 = '4';
			break;
		case 4:
			c5 = '5';
			break;
		case 5:
			c5 = '6';
			break;
		case 6:
			c5 = '7';
			break;
		case 7:
			c5 = '8';
			break;
		default:
			return null;
		}

		return "" + c0 + c1 + c2 + c3 + c4 + c5;
	}

	public int getMoveInt() {
		return moveInt;
	}

	public void setMoveInt(int moveInt) {
		this.moveInt = moveInt;
	}

	public boolean isNullMove() {
		return moveInt == 0;
	}

};
