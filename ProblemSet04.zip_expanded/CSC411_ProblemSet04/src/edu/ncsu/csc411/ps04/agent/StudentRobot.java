package edu.ncsu.csc411.ps04.agent;

import edu.ncsu.csc411.ps04.environment.Environment;
import edu.ncsu.csc411.ps04.environment.Position;
import edu.ncsu.csc411.ps04.environment.Status;

import java.util.ArrayList;

public class StudentRobot extends Robot {

	int count = 0;
	
	public StudentRobot(Environment env) {
		super(env);
	}

	/**
	 * Problem Set 04 - For this Problem Set you will design an agent that can play Connect Four. 
	 * The goal of Connect Four is to "connect" four (4) markers of the same color (role) 
	 * horizontally, vertically, or diagonally. In this exercise your getAction method should 
	 * return an integer between 0 and 6 (inclusive), representing the column you would like to 
	 * "drop" your marker. Unlike previous Problem Sets, in this environment, you will be alternating 
	 * turns with another agent.
	 * 
	 * There are multiple example agents found in the edu.ncsu.csc411.ps04.examples package.
	 * Each example agent provides a brief explanation on its decision process, as well as demonstrations
	 * on how to use the various methods from Environment. In order to pass this Problem Set, you must
	 * successfully beat RandomRobot, VerticalRobot, and HorizontalRobot 70% of the time as both the
	 * YELLOW and RED player. This is distributed across the first six (6) test cases. In addition,
	 * you have the chance to earn EXTRA CREDIT by beating GreedyRobot (test cases 07 and 08) 70% of
	 * the time (10% possible, 5% per test case). Finally, if you successfully pass the test cases,
	 * you are welcome to test your implementation against your classmates.
	 * 
	 * While Simple Reflex or Model-based agent may be able to succeed, consider exploring the Minimax
	 * search algorithm to maximize your chances of winning. While the first two will be easier, you may
	 * want to place priority on moves that prevent the adversary from winning.
	 */
	
	/**
	 * Replace this docstring comment with an explanation of your implementation.
	 */
	@Override
	public int getAction() {
        // Start the Minimax search with a depth limit (e.g., 6)
        int depth = 6;

        // If it's the first move of the game, return column 6
        if (count == 1) {
        	count++; 
        	return 6; // Rightmost column
        }
        
        count++;

        // Prioritize the center column in the early game
        if (isEarlyGame()) {
            int centerColumn = 3; // Center column index
            if (env.getValidActions().contains(centerColumn)) {
                return centerColumn;
            }
        }

        return minimaxDecision(depth);
    }

   

    private boolean isEarlyGame() {
        // Check if the board is mostly empty (e.g., less than 6 moves have been made)
        int totalMoves = 0;
        Position[][] positions = env.clonePositions();
        for (int row = 0; row < positions.length; row++) {
            for (int col = 0; col < positions[0].length; col++) {
                if (positions[row][col].getStatus() != Status.BLANK) {
                    totalMoves++;
                }
            }
        }
        return totalMoves < 6; // Adjust this threshold as needed
    }

    private int minimaxDecision(int depth) {
        int bestMove = -1;
        int bestValue = Integer.MIN_VALUE;
        ArrayList<Integer> validActions = env.getValidActions();

        for (int col : validActions) {
            Position[][] newPositions = simulateMove(col, this.getRole());
            int moveValue = minimax(newPositions, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            undoMove(col, newPositions);

            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = col;
            }
        }
        return bestMove;
    }

    private int minimax(Position[][] positions, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || isTerminal(positions)) {
            return evaluateBoard(positions, isMaximizing);
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col : getValidColumns(positions)) {
                Position[][] newPositions = simulateMove(col, this.getRole());
                int eval = minimax(newPositions, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col : getValidColumns(positions)) {
                Position[][] newPositions = simulateMove(col, getOpponentRole());
                int eval = minimax(newPositions, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private Position[][] simulateMove(int col, Status role) {
        Position[][] newPositions = env.clonePositions();
        for (int row = newPositions.length - 1; row >= 0; row--) {
            if (newPositions[row][col].getStatus() == Status.BLANK) {
                newPositions[row][col] = new Position(row, col, role);
                break;
            }
        }
        return newPositions;
    }

    private void undoMove(int col, Position[][] positions) {
        for (int row = 0; row < positions.length; row++) {
            if (positions[row][col].getStatus() != Status.BLANK) {
                positions[row][col] = new Position(row, col, Status.BLANK);
                break;
            }
        }
    }

    private int evaluateBoard(Position[][] positions, boolean isMaximizing) {
        int score = 0;
        Status playerRole = isMaximizing ? this.getRole() : getOpponentRole();
        Status opponentRole = isMaximizing ? getOpponentRole() : this.getRole();

        // Evaluate based on potential winning moves
        score += evaluateLines(positions, playerRole);
        score -= evaluateLines(positions, opponentRole);

        return score;
    }

    private int evaluateLines(Position[][] positions, Status role) {
        int score = 0;
        int rows = positions.length;
        int cols = positions[0].length;

        // Check horizontal, vertical, and diagonal lines
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (positions[row][col].getStatus() == role) {
                    // Horizontal
                    if (col + 3 < cols && positions[row][col + 1].getStatus() == role &&
                        positions[row][col + 2].getStatus() == role && positions[row][col + 3].getStatus() == role) {
                        score += 1000;
                    }
                    // Vertical
                    if (row + 3 < rows && positions[row + 1][col].getStatus() == role &&
                        positions[row + 2][col].getStatus() == role && positions[row + 3][col].getStatus() == role) {
                        score += 1000;
                    }
                    // Diagonal (bottom-right)
                    if (row + 3 < rows && col + 3 < cols && positions[row + 1][col + 1].getStatus() == role &&
                        positions[row + 2][col + 2].getStatus() == role && positions[row + 3][col + 3].getStatus() == role) {
                        score += 1000;
                    }
                    // Diagonal (top-right)
                    if (row - 3 >= 0 && col + 3 < cols && positions[row - 1][col + 1].getStatus() == role &&
                        positions[row - 2][col + 2].getStatus() == role && positions[row - 3][col + 3].getStatus() == role) {
                        score += 1000;
                    }
                }
            }
        }
        return score;
    }

    private boolean isTerminal(Position[][] positions) {
        // Check if the game is over (win or draw)
        return evaluateLines(positions, this.getRole()) >= 1000 || evaluateLines(positions, getOpponentRole()) >= 1000;
    }

    private Status getOpponentRole() {
        return (this.getRole() == Status.YELLOW) ? Status.RED : Status.YELLOW;
    }

    private ArrayList<Integer> getValidColumns(Position[][] positions) {
        ArrayList<Integer> validColumns = new ArrayList<>();
        for (int col = 0; col < positions[0].length; col++) {
            if (positions[0][col].getStatus() == Status.BLANK) {
                validColumns.add(col);
            }
        }
        return validColumns;
    }
}