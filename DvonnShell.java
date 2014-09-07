import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;



public class DvonnShell {
	static DvonnGame game = new DvonnGame();

	public static void main(String[] args) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String cmd = "";

		while (!cmd.equals("exit")) {
			System.out.println(game);
			System.out.print("#> ");

			cmd = in.readLine();

			if (cmd.length() == 0) {
				game.move();
			}
			else if (cmd.matches("^\\d+( \\d+)*$")) {
				String[] tok = cmd.split(" ");
				int[] params = new int[tok.length];
				for (int i = 0; i < tok.length; i++) {
					params[i] = Integer.parseInt(tok[i]);
				}
				boolean isLegal = game.move(params);
				if (!isLegal) {
					System.out.println("illegal move");
				}
			}
			else if (cmd.equals("reset")) {
				game.reset();
			}
			else if (cmd.startsWith("load ")) {
				loadGame(cmd.split(" ")[1]);
			}
			else if (cmd.equals("exit")) {
				System.out.println("bye.");
			}
			else {
				System.out.println("illegal command");
			}
		}
	}

	static void loadGame(String path) throws Exception {
		String pgn = new String(Files.readAllBytes(Paths.get(path)));
		String[] moves = pgn.split("\\s+1[.]\\s+")[1].split("\\s+\\d+[.]\\s+");
		int moveNumber = 0;
		while (moveNumber < 24) {
			String[] m = moves[moveNumber].split("\\s+");
			int file = m[0].charAt(0) - 'a';
			int rank = m[0].charAt(1) - '1';
			game.move(rank, file);
			System.out.println(" > " + m[0] + "\n" + game);
			file = m[1].charAt(0) - 'a';
			rank = m[1].charAt(1) - '1';
			game.move(rank, file);
			System.out.println(" > " + m[1] + "\n" + game);
			moveNumber++;
		}
		for (int rank = 0; rank < 5; rank++) {
			for (int file = 0; file < 11; file++) {
				game.move(rank, file);
			}
		}
		while (moveNumber < moves.length) {
			String[] m = (moves[moveNumber]+" pass").split("\\s+");
			int fromFile = m[0].charAt(0) - 'a';
			int fromRank = m[0].charAt(1) - '1';
			int toFile = m[0].charAt(2) - 'a';
			int toRank = m[0].charAt(3) - '1';
			if (!game.move()) {
				game.move(fromRank, fromFile, toRank, toFile);
			}
			System.out.println(" > " + m[0] + "\n" + game);
			fromFile = m[1].charAt(0) - 'a';
			fromRank = m[1].charAt(1) - '1';
			toFile = m[1].charAt(2) - 'a';
			toRank = m[1].charAt(3) - '1';
			if (!game.move()) {
				game.move(fromRank, fromFile, toRank, toFile);
			}
			System.out.println(" > " + m[1] + "\n" + game);
			moveNumber++;
		}
	}
}
