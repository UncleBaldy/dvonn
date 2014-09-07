

public class DvonnGame {
	static final int RANKS = 5;
	static final int FILES = 11;
	static final int[][] INVALID_SPACES = {{0,9}, {0,10}, {1,10}, {3,0}, {4,0}, {4,1}};
	static final int SPACES = RANKS * FILES - INVALID_SPACES.length;
	static final int HEIGHT_MASK = 0x3F;
	static final int BLACK_BIT = 0x40;
	static final int DVONN_BIT = 0x80;

	private byte[][] board;
	private int moveNumber;

	public DvonnGame() {
		reset();
	}

	public void reset() {
		moveNumber = 1;
		board = new byte[RANKS][FILES];
		for (int[] space : INVALID_SPACES) {
			board[space[0]][space[1]] = -1;
		}
	}

	public boolean isLegalMove(int ... args) {
		switch (args.length) {
			case 0:
				return isLegalPass();

			case 2:
				return isLegalPlacement(args[0], args[1]);

			case 4:
				return isLegalJump(args[0], args[1], args[2], args[3]);

			default:
				return false;
		}
	}

	private boolean isLegalPass() {
		if (moveNumber <= SPACES) { // must place remaining pieces
			return false;
		}

		for (int fromRank = 0; fromRank < RANKS; fromRank++) {
			for (int fromFile = 0; fromFile < FILES; fromFile++) {
				for (int toRank = 0; toRank < RANKS; toRank++) {
					for (int toFile = 0; toFile < FILES; toFile++) {
						if (isLegalJump(fromRank, fromFile, toRank, toFile)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	private boolean isLegalPlacement(int rank, int file) {
		if (moveNumber > SPACES) {
			return false; // no more pieces to place
		}

		if (rank < 0 || rank >= RANKS || file < 0 || file >= FILES) {
			return false;
		}

		if (board[rank][file] != 0) {
			return false;
		}

		return true;
	}

	private boolean isLegalJump(int fromRank, int fromFile, int toRank, int toFile) {
		if (moveNumber <= SPACES) {
			return false; // must place remaining pieces
		}

		if (fromRank < 0 || fromRank >= RANKS || toRank < 0 || toRank >= RANKS
		 || fromFile < 0 || fromFile >= FILES || toFile < 0 || toFile >= FILES) {
			return false;
		}

		int dRank = toRank - fromRank;
		int dFile = toFile - fromFile;

		if (dRank != 0 && dFile != 0 && dRank != dFile) {
			return false;
		}

		byte stack = board[fromRank][fromFile];
		int height = stack & HEIGHT_MASK;

		if (stack == 0 || stack == -1 || (stack < 0 && height == 1)
		 || board[toRank][toFile] == 0 || board[toRank][toFile] == -1) {
			return false;
		}

		if (Math.abs(dRank) != height && Math.abs(dFile) != height) {
			return false;
		}

		boolean blackToMove = (moveNumber % 2) != 0;
		boolean isBlack = (stack & BLACK_BIT) != 0;
		if (blackToMove != isBlack) {
			return false;
		}

		if (isSurrounded(fromRank, fromFile)) {
			return false;
		}

		return true;
	}

	private boolean isSurrounded(int rank, int file) {
		if (rank == 0 || rank == RANKS-1 || file == 0 || file == FILES-1) {
			return false;
		}
		if (board[rank-1][file] == 0 || board[rank-1][file] == -1
		 || board[rank+1][file] == 0 || board[rank+1][file] == -1
		 || board[rank][file-1] == 0 || board[rank][file-1] == -1
		 || board[rank][file+1] == 0 || board[rank][file+1] == -1
		 || board[rank-1][file-1] == 0 || board[rank-1][file-1] == -1
		 || board[rank+1][file+1] == 0 || board[rank+1][file+1] == -1) {
		 	return false;
		 }

		 return true;
	}

	public boolean move(int ... args) {
		if (!isLegalMove(args)) {
			return false;
		}

		if (args.length == 2) {
			int rank = args[0], file = args[1];

			board[rank][file] = 1;

			if (moveNumber <= 3) { // first place the three DVONN pieces
				board[rank][file] |= DVONN_BIT;
			}
			else if (moveNumber % 2 == 0) {
				board[rank][file] |= BLACK_BIT;
			}
		}
		else if (args.length == 4) {
			int fromRank = args[0], fromFile = args[1], toRank = args[2], toFile = args[3];
			byte stack = board[toRank][toFile];
			board[toRank][toFile] = board[fromRank][fromFile];
			board[toRank][toFile] |= stack & DVONN_BIT;
			board[toRank][toFile] += stack & HEIGHT_MASK;
			board[fromRank][fromFile] = 0;
			removeDisconnectedStacks();
		}

		moveNumber++;
		return true;
	}

	private void removeDisconnectedStacks() {
		boolean[][] connected = new boolean[RANKS][FILES];
		int[] qRank = new int[RANKS * FILES];
		int[] qFile = new int[RANKS * FILES];
		int qSize = 0;

		for (int rank = 0; rank < RANKS; rank++) {
			for (int file = 0; file < FILES; file++) {
				if (board[rank][file] < -1) {
					connected[rank][file] = true;
					qRank[qSize] = rank;
					qFile[qSize] = file;
					qSize++;
				}
			}
		}

		for (int i = 0; i < qSize; i++) {
			int rank = qRank[i], file = qFile[i];
			if (rank > 0 && !connected[rank-1][file] && board[rank-1][file] > 0) {
				qRank[qSize] = rank - 1;
				qFile[qSize] = file;
				qSize++;
				connected[rank-1][file] = true;
			}
			if (rank+1 < RANKS && !connected[rank+1][file] && board[rank+1][file] > 0) {
				qRank[qSize] = rank + 1;
				qFile[qSize] = file;
				qSize++;
				connected[rank+1][file] = true;
			}
			if (file > 0 && !connected[rank][file-1] && board[rank][file-1] > 0) {
				qRank[qSize] = rank;
				qFile[qSize] = file - 1;
				qSize++;
				connected[rank][file-1] = true;
			}
			if (file+1 < FILES && !connected[rank][file+1] && board[rank][file+1] > 0) {
				qRank[qSize] = rank;
				qFile[qSize] = file + 1;
				qSize++;
				connected[rank][file+1] = true;
			}
			if (rank > 0 && file > 0 && !connected[rank-1][file-1] && board[rank-1][file-1] > 0) {
				qRank[qSize] = rank - 1;
				qFile[qSize] = file - 1;
				qSize++;
				connected[rank-1][file-1] = true;
			}
			if (rank+1 < RANKS && file+1 < FILES && !connected[rank+1][file+1] && board[rank+1][file+1] > 0) {
				qRank[qSize] = rank + 1;
				qFile[qSize] = file + 1;
				qSize++;
				connected[rank+1][file+1] = true;
			}
		}

		for (int rank = 0; rank < RANKS; rank++) {
			for (int file = 0; file < FILES; file++) {
				if (!connected[rank][file] && board[rank][file] > 0) {
					board[rank][file] = 0;
				}
			}
		}
	}

	public String toString() {
		String rank0 = "", rank1 = "", rank2 = "", rank3 = "", rank4 = "";
		for (int file = 0; file < FILES; file++) {
			rank0 += " " + toString(board[0][file]);
			rank1 += " " + toString(board[1][file]);
			rank2 += " " + toString(board[2][file]);
			rank3 += " " + toString(board[3][file]);
			rank4 += " " + toString(board[4][file]);
		}
		return ("    " + rank0 + "\n"
		      + "  " + rank1 + "\n"
		      + rank2 + "\n"
		      + "  " + rank3 + "\n"
		      + "    " + rank4
		).replace(" [63>", "");
	}

	private String toString(byte stack) {
		int height = stack & HEIGHT_MASK;
		boolean isBlack = (stack & BLACK_BIT) != 0;

		if (height == 0) {
			return "   ";
		}

		if (stack < 0) { // contains a DVONN piece
			if (height == 1) return "<1>";
			return (isBlack ? "[" : "(") + height + ">";
		}

		return isBlack ? "["+height+"]" : "("+height+")";
	}
}
