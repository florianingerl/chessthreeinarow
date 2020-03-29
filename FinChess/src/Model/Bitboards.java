package Model;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Contains various bitmasks used in the chess move generator.
 * 
 * @author Hermann
 *
 */
public class Bitboards {
	
	private static Logger logger = Logger.getLogger(Bitboards.class);

	public static long RANKMASK[] = new long[64];
	public static long FILEMASK[] = new long[64];
	public static long DIAGA8H1MASK[] = new long[64];
	public static long DIAGA1H8MASK[] = new long[64];
	public static long FILEMAGIC[] = new long[64];
	public static long DIAGA8H1MAGIC[] = new long[64];
	public static long DIAGA1H8MAGIC[] = new long[64];
	public static long RANK_ATTACKS[][] = new long[64][64];
	public static long FILE_ATTACKS[][] = new long[64][64];
	public static long DIAGA1H8_ATTACKS[][] = new long[64][64];
	public static long DIAGA8H1_ATTACKS[][] = new long[64][64];
	public static int RANKSHIFT[] = new int[64];
	public static long KNIGHT_ATTACKS[];
	public static long KING_ATTACKS[];
	public static long BITSET[] = new long[64];
	public static int RANKS[] = new int[64];
	public static int[][] BOARDINDEX = new int[9][9];
	public static long CENTRE;
	public static long ADVANDED_CENTRE;
	public static long EDGE_OF_BOARD;
	public static long BEST_WHITE_BISHOP;
	public static long BEST_BLACK_BISHOP;
	public static long[] ZOBRIST_HASH_RANDOMS = new long[1037];
	public static int HASH_SHORT_CASTLE_WHITE = 1024;
	public static int HASH_SHORT_CASTLE_BLACK = 1025;
	public static int HASH_LONG_CASTLE_WHITE = 1026;
	public static int HASH_LONG_CASTLE_BLACK = 1027;
	public static int HASH_BLACK_TO_MOVE = 1028;
	public static int HASH_EP_SQUARE = 1029;

	public static long[] ZOBRIST_HASH_RANDOMS2 = new long[1037];

	public static long BEST_BLACK_KING = -4107282860161892352L;
	public static long BEST_WHITE_KING = 199L;

	public static long FIRST_AND_SECOND_RANK = 65535L;
	public static long EIGHT_AND_SEVENTH_RANK = -281474976710656L;

	public static long FILE[] = new long[64];
	public static long RANK[] = new long[64];

	public static long maskEG[] = new long[2];
	public static long maskFG[] = new long[2];
	public static long maskBD[] = new long[2];
	public static long maskCE[] = new long[2];

	public static Move WHITE_OO_CASTL = new Move();
	public static Move BLACK_OO_CASTL = new Move();
	public static Move WHITE_OOO_CASTL = new Move();
	public static Move BLACK_OOO_CASTL = new Move();

	private Move move = new Move();

	private int CHARBITSET[] = new int[8];
	private int i, square, rank, file, slide, arank, afile, diaga1h8, diaga8h1, attackbit;
	private int state6Bit, state8Bit, attack8Bit;
	private long[] _DIAGA8H1MAGICSs = new long[15];
	private long[] _DIAGA1H8MAGICSs = new long[15];
	private int GEN_SLIDING_ATTACKS[][];
	private int FILESs[];

	static {
		new Bitboards();
	}

	private Bitboards() {
		initializeBitset();
		initializeBoardIndex();

		initializeDiaga8h1Magics();
		initializeDiaga1h8Magics();

		initializeRandAndFile();

		for (file = 1; file < 9; file++) {
			for (rank = 1; rank < 9; rank++) {
				initializeFileAndRankMask();

				diaga8h1 = file + rank;

				DIAGA8H1MAGIC[BOARDINDEX[file][rank]] = _DIAGA8H1MAGICSs[diaga8h1 - 2];

				DIAGA8H1MASK[BOARDINDEX[file][rank]] = 0L;
				if (diaga8h1 < 10) {
					for (square = 2; square < diaga8h1 - 1; square++) {
						DIAGA8H1MASK[BOARDINDEX[file][rank]] |= BITSET[BOARDINDEX[square][diaga8h1 - square]];
					}
				} else {
					for (square = 2; square < 17 - diaga8h1; square++) {
						DIAGA8H1MASK[BOARDINDEX[file][rank]] |= BITSET[BOARDINDEX[diaga8h1 + square - 9][9 - square]];
					}
				}

				diaga1h8 = file - rank;

				DIAGA1H8MAGIC[BOARDINDEX[file][rank]] = _DIAGA1H8MAGICSs[diaga1h8 + 7];

				DIAGA1H8MASK[BOARDINDEX[file][rank]] = 0L;
				if (diaga1h8 > -1) {
					for (square = 2; square < 8 - diaga1h8; square++) {
						DIAGA1H8MASK[BOARDINDEX[file][rank]] |= BITSET[BOARDINDEX[diaga1h8 + square][square]];
					}
				} else {
					for (square = 2; square < 8 + diaga1h8; square++) {
						DIAGA1H8MASK[BOARDINDEX[file][rank]] |= BITSET[BOARDINDEX[square][square - diaga1h8]];
					}
				}

				long _FILEMAGICS[] = new long[8];

				_FILEMAGICS[0] = 0x8040201008040200L;
				_FILEMAGICS[1] = 0x4020100804020100L;
				_FILEMAGICS[2] = 0x2010080402010080L;
				_FILEMAGICS[3] = 0x1008040201008040L;
				_FILEMAGICS[4] = 0x0804020100804020L;
				_FILEMAGICS[5] = 0x0402010080402010L;
				_FILEMAGICS[6] = 0x0201008040201008L;
				_FILEMAGICS[7] = 0x0100804020100804L;

				FILEMAGIC[BOARDINDEX[file][rank]] = _FILEMAGICS[file - 1];
			}
		}

		initializeCharBitSet();

		initializeGenSlidingAttacks();

		initializeFiles();

		for (square = 0; square < 64; square++) {

			RANKSHIFT[square] = (square / 8) * 8 + 1;

		}

		for (square = 0; square < 64; square++) {
			for (state6Bit = 0; state6Bit < 64; state6Bit++) {

				RANK_ATTACKS[square][state6Bit] = 0L;
				RANK_ATTACKS[square][state6Bit] |= ((long) (GEN_SLIDING_ATTACKS[FILESs[square]
						- 1][state6Bit]) << (RANKSHIFT[square] - 1));

			}
		}

		for (square = 0; square < 64; square++) {
			RANKS[square] = (square / 8) + 1;
		}

		for (square = 0; square < 64; square++) {
			for (state6Bit = 0; state6Bit < 64; state6Bit++) {
				FILE_ATTACKS[square][state6Bit] = 0L;

				for (attackbit = 0; attackbit < 8; attackbit++) {
					if ((GEN_SLIDING_ATTACKS[8 - RANKS[square]][state6Bit] & CHARBITSET[attackbit]) != 0) {
						file = FILESs[square];
						rank = 8 - attackbit;
						FILE_ATTACKS[square][state6Bit] |= (long) (BITSET[BOARDINDEX[file][rank]]);
					}

				}

			}
		}

		for (square = 0; square < 64; square++)

		{

			for (state6Bit = 0; state6Bit < 64; state6Bit++)

			{

				DIAGA8H1_ATTACKS[square][state6Bit] = 0x0;

				for (attackbit = 0; attackbit < 8; attackbit++) // from LSB to
																// MSB

				{

					int hilfsvariable;
					if ((8 - RANKS[square]) < (FILESs[square] - 1)) {
						hilfsvariable = (8 - RANKS[square]);
					} else {
						hilfsvariable = FILESs[square] - 1;
					}

					if ((GEN_SLIDING_ATTACKS[hilfsvariable][state6Bit] & CHARBITSET[attackbit]) != 0)

					{

						// the bit is set, so we need to update FILE_ATTACKS
						// accordingly:

						// conversion of square/attackbit to the corresponding
						// 64 board file and rank:

						diaga8h1 = FILESs[square] + RANKS[square]; // from 2 to
																	// 16,
																	// longest
																	// diagonal
																	// = 9

						if (diaga8h1 < 10)

						{

							file = attackbit + 1;

							rank = diaga8h1 - file;

						}

						else

						{

							rank = 8 - attackbit;

							file = diaga8h1 - rank;

						}

						if ((file > 0) && (file < 9) && (rank > 0) && (rank < 9))

						{

							DIAGA8H1_ATTACKS[square][state6Bit] |= BITSET[BOARDINDEX[file][rank]];

						}

					}

				}

			}

		}

		// WHITE_PAWN_ATTACKS

		// DIAGA1H8_ATTACKS attacks (BISHOPS and QUEENS):

		for (square = 0; square < 64; square++)

		{

			for (state6Bit = 0; state6Bit < 64; state6Bit++)

			{

				DIAGA1H8_ATTACKS[square][state6Bit] = 0x0;

				for (attackbit = 0; attackbit < 8; attackbit++) // from LSB to
																// MSB

				{

					int hilfsvariable;

					if ((RANKS[square] - 1) < (FILESs[square] - 1)) {
						hilfsvariable = RANKS[square] - 1;
					} else {
						hilfsvariable = FILESs[square] - 1;
					}

					if ((GEN_SLIDING_ATTACKS[hilfsvariable][state6Bit] & CHARBITSET[attackbit]) != 0)

					{

						// the bit is set, so we need to update FILE_ATTACKS
						// accordingly:

						// conversion of square/attackbit to the corresponding
						// 64 board file and rank:

						diaga1h8 = FILESs[square] - RANKS[square]; // from -7 to
																	// 7,
																	// longest
																	// diagonal
																	// = 0

						if (diaga1h8 < 0)

						{

							file = attackbit + 1;

							rank = file - diaga1h8;

						}

						else

						{

							rank = attackbit + 1;

							file = diaga1h8 + rank;

						}

						if ((file > 0) && (file < 9) && (rank > 0) && (rank < 9))

						{

							DIAGA1H8_ATTACKS[square][state6Bit] |= BITSET[BOARDINDEX[file][rank]];

						}

					}

				}

			}

		}

		// 1.5 10:12 Uhr alles richtig

		initializeKnightAttacks();

		// 31.05. 19:06 Uhr alles richtig
		initializeKingAttacks();

		initializeCastleMasks();

		// ===========================================================================

		// The 4 castling moves can be predefined:

		// ===========================================================================

		initializeWhitesCastleMoves();

		initializeBlacksCastleMoves();

		initializeCenterAndAdvancedCenter();

		initializeEdgeOfBoard();

		initializeZobrishHashRandoms();

	}// end constructor

	private void initializeFiles() {
		FILESs = new int[64];

		for (square = 0; square < 64; square++) {

			FILESs[square] = 1 + ((square) % 8);

		}
	}

	private void initializeGenSlidingAttacks() {
		GEN_SLIDING_ATTACKS = new int[8][64];

		for (square = 0; square <= 7; square++) {
			for (state6Bit = 0; state6Bit < 64; state6Bit++) {
				state8Bit = (state6Bit << 1);

				attack8Bit = 0;
				if (square < 7) {
					attack8Bit |= (CHARBITSET[square + 1]);
				}
				slide = square + 2;
				while (slide <= 7) {
					if (((~state8Bit) & (CHARBITSET[slide - 1])) != 0) {
						attack8Bit |= CHARBITSET[slide];
					} else {
						break;
					}
					slide++;
				}
				if (square > 0) {
					attack8Bit |= CHARBITSET[square - 1];
				}

				slide = square - 2;

				while (slide >= 0) {
					if (((~state8Bit) & (CHARBITSET[slide + 1])) != 0) {
						attack8Bit |= CHARBITSET[slide];
					} else {
						break;
					}
					slide--;
				}

				GEN_SLIDING_ATTACKS[square][state6Bit] = attack8Bit;
			}

		}
	}

	private void initializeCharBitSet() {
		CHARBITSET[0] = 1;
		for (square = 1; square <= 7; square++) {
			CHARBITSET[square] = (CHARBITSET[square - 1] << 1);
		}
	}

	private void initializeFileAndRankMask() {
		RANKMASK[BOARDINDEX[file][rank]] = BITSET[BOARDINDEX[2][rank]] | BITSET[BOARDINDEX[3][rank]]
				| BITSET[BOARDINDEX[4][rank]] | BITSET[BOARDINDEX[5][rank]] | BITSET[BOARDINDEX[6][rank]]
				| BITSET[BOARDINDEX[7][rank]];

		FILEMASK[BOARDINDEX[file][rank]] = BITSET[BOARDINDEX[file][2]] | BITSET[BOARDINDEX[file][3]]
				| BITSET[BOARDINDEX[file][4]] | BITSET[BOARDINDEX[file][5]] | BITSET[BOARDINDEX[file][6]]
				| BITSET[BOARDINDEX[file][7]];
	}

	private void initializeCastleMasks() {
		maskEG[0] = BITSET[4] | BITSET[5] | BITSET[6];

		maskEG[1] = BITSET[60] | BITSET[61] | BITSET[62];

		maskFG[0] = BITSET[5] | BITSET[6];

		maskFG[1] = BITSET[61] | BITSET[62];

		maskBD[0] = BITSET[1] | BITSET[2] | BITSET[3];

		maskBD[1] = BITSET[57] | BITSET[58] | BITSET[59];

		maskCE[0] = BITSET[2] | BITSET[3] | BITSET[4];

		maskCE[1] = BITSET[58] | BITSET[59] | BITSET[60];
	}

	private void initializeBitset() {
		BITSET[0] = 1L;
		for (i = 1; i < 64; i++) {
			BITSET[i] = BITSET[i - 1] << 1;
		}
	}

	private void initializeEdgeOfBoard() {
		EDGE_OF_BOARD = -35604928818740737L;
	}

	private void initializeCenterAndAdvancedCenter() {
		CENTRE = BITSET[27] | BITSET[28] | BITSET[35] | BITSET[36];
		ADVANDED_CENTRE = BITSET[18] | BITSET[19] | BITSET[20] | BITSET[21] | BITSET[26] | BITSET[29] | BITSET[34]
				| BITSET[37] | BITSET[42] | BITSET[43] | BITSET[44] | BITSET[45];
	}

	private void initializeBlacksCastleMoves() {
		move.clear();

		move.setCapt(BasicEngine.EMPTY);

		move.setPiec(BasicEngine.B_KING);

		move.setProm(BasicEngine.B_KING);

		move.setFrom(60);

		move.setTosq(62);

		BLACK_OO_CASTL.setMoveInt(move.getMoveInt());

		move.setTosq(58);

		BLACK_OOO_CASTL.setMoveInt(move.getMoveInt());
	}

	private void initializeWhitesCastleMoves() {
		move.clear();

		move.setCapt(BasicEngine.EMPTY);

		move.setPiec(BasicEngine.W_KING);

		move.setProm(BasicEngine.W_KING);

		move.setFrom(4);

		move.setTosq(6);

		WHITE_OO_CASTL.setMoveInt(move.getMoveInt());

		move.setTosq(2);

		WHITE_OOO_CASTL.setMoveInt(move.getMoveInt());
	}

	private void initializeZobrishHashRandoms() {
		try {
			DataInputStream in = new DataInputStream(getClass().getClassLoader()
					.getResourceAsStream("Resources/Zobrist_Hash_Randoms"));

			// pieces W_PAWN bis W_ROOK
			for (i = 64; i < 4 * 64; i++) {
				ZOBRIST_HASH_RANDOMS[i] = in.readLong();
				ZOBRIST_HASH_RANDOMS2[i] = in.readLong();
			}

			for (i = 5 * 64; i < 8 * 64; i++) {
				ZOBRIST_HASH_RANDOMS[i] = in.readLong();
				ZOBRIST_HASH_RANDOMS2[i] = in.readLong();
			}

			for (i = 9 * 64; i < 12 * 64; i++) {
				ZOBRIST_HASH_RANDOMS[i] = in.readLong();
				ZOBRIST_HASH_RANDOMS2[i] = in.readLong();
			}
			for (i = 13 * 64; i < 1037; i++) {
				ZOBRIST_HASH_RANDOMS[i] = in.readLong();
				ZOBRIST_HASH_RANDOMS2[i] = in.readLong();
			}
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			logger.error("Couln't read Zobrist_Hash_Randoms");
		}
	}

	private void initializeKingAttacks() {
		KING_ATTACKS = new long[] { 770L, 1797L, 1797L << 1, 1797L << 2, 1797L << 3, 1797L << 4, 1797L << 5, 49216L,
				197123L, 460039L, 460039 << 1, 460039 << 2, 460039 << 3, 460039 << 4, 460039 << 5, 12599488L,
				197123L << 8, 460039L << 8, 460039L << 9, 460039L << 10, 460039L << 11, 460039L << 12, 460039L << 13,
				12599488L << 8, 197123L << 16, 460039L << 16, 460039L << 17, 460039L << 18, 460039L << 19,
				460039L << 20, 460039L << 21, 12599488L << 16, 197123L << 24, 460039L << 24, 460039L << 25,
				460039L << 26, 460039L << 27, 460039L << 28, 460039L << 29, 12599488L << 24, 197123L << 32,
				460039L << 32, 460039L << 33, 460039L << 34, 460039L << 35, 460039L << 36, 460039L << 37,
				12599488L << 32, 197123L << 40, 460039L << 40, -2260560722335367168L >>> 4, -2260560722335367168L >>> 3,
				-2260560722335367168L >>> 2, -2260560722335367168L >>> 1, -2260560722335367168L, -4593460513685372928L,
				144959613005987840L, 362258295026614272L, 362258295026614272L << 1, 362258295026614272L << 2,
				362258295026614272L << 3, 362258295026614272L << 4, -6854478632857894912L, 4665729213955833856L };
	}

	private void initializeKnightAttacks() {
		KNIGHT_ATTACKS = new long[] { 132096L, 329728L, 659712L, 659712L << 1, 659712L << 2, 659712L << 3, 10489856L,
				4202496L, 33816580L, 84410376L, 168886289L, 168886289L << 1, 168886289L << 2, 168886289L << 3,
				2685403152L, 1075839008L, 8657044482L, 21609056261L, 43234889994L, 43234889994L << 1, 43234889994L << 2,
				43234889994L << 3, 687463207072L, 275414786112L, 8657044482L << 8, 21609056261L << 8, 43234889994L << 8,
				43234889994L << 9, 43234889994L << 10, 43234889994L << 11, 687463207072L << 8, 275414786112L << 8,
				8657044482L << 16, 21609056261L << 16, 43234889994L << 16, 43234889994L << 17, 43234889994L << 18,
				43234889994L << 19, 687463207072L << 16, 275414786112L << 16, 8657044482L << 24, 21609056261L << 24,
				43234889994L << 24, 43234889994L << 25, 43234889994L << 26, 43234889994L << 27, 687463207072L << 24,
				275414786112L << 24, 288234782788157440L, 576469569871282176L, 1224997833292120064L,
				1224997833292120064L << 1, 1224997833292120064L << 2, 1224997833292120064L << 3, 1152939783987658752L,
				2305878468463689728L, 1128098930098176L, 2257297371824128L, 4796069720358912L, 4796069720358912L << 1,
				4796069720358912L << 2, 4796069720358912L << 3, 4679521487814656L, 9077567998918656L };
	}

	private void initializeRandAndFile() {
		for (file = 1; file < 9; file++) {
			for (rank = 1; rank < 9; rank++) {
				RANK[BOARDINDEX[file][rank]] = BITSET[BOARDINDEX[1][rank]] | BITSET[BOARDINDEX[2][rank]]
						| BITSET[BOARDINDEX[3][rank]] | BITSET[BOARDINDEX[4][rank]] | BITSET[BOARDINDEX[5][rank]]
						| BITSET[BOARDINDEX[6][rank]] | BITSET[BOARDINDEX[7][rank]] | BITSET[BOARDINDEX[8][rank]];

				FILE[BOARDINDEX[file][rank]] = BITSET[BOARDINDEX[file][1]] | BITSET[BOARDINDEX[file][2]]
						| BITSET[BOARDINDEX[file][3]] | BITSET[BOARDINDEX[file][4]] | BITSET[BOARDINDEX[file][5]]
						| BITSET[BOARDINDEX[file][6]] | BITSET[BOARDINDEX[file][7]] | BITSET[BOARDINDEX[file][8]];
			}
		}
	}

	private void initializeDiaga1h8Magics() {
		_DIAGA1H8MAGICSs[0] = 0L;
		_DIAGA1H8MAGICSs[1] = 0L;
		_DIAGA1H8MAGICSs[2] = 0x0101010101010100L;
		_DIAGA1H8MAGICSs[3] = 0x0101010101010100L;
		_DIAGA1H8MAGICSs[4] = 0x0101010101010100L;
		_DIAGA1H8MAGICSs[5] = 0x0101010101010100L;
		_DIAGA1H8MAGICSs[6] = 0x0101010101010100L;
		_DIAGA1H8MAGICSs[7] = 0x0101010101010100L;
		_DIAGA1H8MAGICSs[8] = 0x8080808080808000L;

		_DIAGA1H8MAGICSs[9] = 0x4040404040400000L;

		_DIAGA1H8MAGICSs[10] = 0x2020202020000000L;

		_DIAGA1H8MAGICSs[11] = 0x1010101000000000L;
		_DIAGA1H8MAGICSs[12] = 0x0808080000000000L;

		_DIAGA1H8MAGICSs[13] = 0L;
		_DIAGA1H8MAGICSs[14] = 0L;
	}

	private void initializeDiaga8h1Magics() {
		_DIAGA8H1MAGICSs[0] = 0x0L;
		_DIAGA8H1MAGICSs[1] = 0x0L;
		_DIAGA8H1MAGICSs[2] = 0x0101010101010100L;
		_DIAGA8H1MAGICSs[3] = 0x0101010101010100L;
		_DIAGA8H1MAGICSs[4] = 0x0101010101010100L;
		_DIAGA8H1MAGICSs[5] = 0x0101010101010100L;
		_DIAGA8H1MAGICSs[6] = 0x0101010101010100L;
		_DIAGA8H1MAGICSs[7] = 0x0101010101010100L;
		_DIAGA8H1MAGICSs[8] = 0x0080808080808080L;
		_DIAGA8H1MAGICSs[9] = 0x0040404040404040L;
		_DIAGA8H1MAGICSs[10] = 0x0020202020202020L;
		_DIAGA8H1MAGICSs[11] = 0x0010101010101010L;
		_DIAGA8H1MAGICSs[12] = 0x0008080808080808L;
		_DIAGA8H1MAGICSs[13] = 0x0L;
		_DIAGA8H1MAGICSs[14] = 0x0L;
	}

	private void initializeBoardIndex() {
		// BOARDINDEX to translate [file][rank] to square
		// file and rank are from 1..8

		for (rank = 0; rank < 9; rank++) {
			for (file = 0; file < 9; file++) {
				BOARDINDEX[file][rank] = (rank - 1) * 8 + file - 1;
			}
		}
	}

} // end class
