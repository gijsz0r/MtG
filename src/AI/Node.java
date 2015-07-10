package AI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Node {

	private ArrayList<Integer> move;
	private int stage;
	private Node parent;
	private ArrayList<Integer> scores;
	private double uct;
	private double c = Math.sqrt(2);
	private ArrayList<Node> children;
	private Random random;

	public Node(ArrayList<Integer> move, int stage, Node parent, ArrayList<Integer> scores, double uct) {
		this.stage = stage;
		this.parent = parent;
		this.scores = scores;
		this.uct = uct;
		this.move = move;
		this.children = new ArrayList<Node>();
		this.random = new Random();

	}
	
	@Override
	public String toString() {
		return Arrays.toString(move.toArray()) + " , and move is : "  + stage;
	}

	public ArrayList<Integer> getMove() {
		return move;
	}

	public int getStage() {
		return stage;
	}

	public void addScore(int score) {
		scores.add(score);
	}

	public double getUCT() {
		return uct;
	}

	public void updateUCT(int totalVisitCounter) {
		int avg = 0;
		for (int i = 0; i < scores.size(); i++) {
			avg += scores.get(i);
		}
		avg = avg / scores.size();

		uct = avg + c * Math.sqrt((Math.log(totalVisitCounter) / scores.size()));
	}

	public Object getParent() {
		return this.parent;
	}

	public void setChildren(ArrayList<Node> children) {
		this.children = children;
	}
	public void addChild(Node child) {
		this.children.add(child);
	}

	public boolean hasChildren() {
		if (this.children.size() > 0) {
			return true;
		} else
			return false;
	}

	public void calculateUCT(int p) {
		double avg = 0;
		double c = Math.sqrt(2);
		int total = 0;
		double lnp = Math.log(p);
		for (int i = 0; i < scores.size(); i++) {
			total += scores.get(i);
		}
		avg = total / scores.size();
		uct = avg + c * Math.sqrt(lnp / scores.size());
	}

	public Node selectBestChild(int currentPlayer) {
		
		// Pick a random child if we haven't gone through the node X times
		if(scores.size() < children.size() * 20)
			return children.get(random.nextInt(children.size()));
		
		Node bestChild = null;
		if (currentPlayer == 0) {
			double bestUCT = Double.MIN_VALUE;
			for (int i = 0; i < children.size(); i++) {
				double epsilon = random.nextDouble()*0.00001;
				if (bestUCT < children.get(i).getUCT() + epsilon) {
					bestUCT = children.get(i).getUCT() + epsilon;
					bestChild = children.get(i);
				}
			}
		}
		if (currentPlayer == 1) {
			double bestUCT = Double.MAX_VALUE;
			for (int i=0; i<children.size(); i++) {
				double epsilon = random.nextDouble()*0.00001;
				if (bestUCT > children.size() + epsilon) {
					bestUCT = children.get(i).getUCT() + epsilon;
					bestChild = children.get(i);
				}
			}
		}
		return bestChild;
	}
	
	public boolean hasParent() {
		if (this.parent != null) {
			return true;
		}
		return false;
	}
}
