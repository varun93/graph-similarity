package project;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class SimilarityGraph {



	HashMap<Integer, LinkedList<NewsContentNode>> adj;

	SimilarityGraph(int nodes) {

		adj = new HashMap<Integer, LinkedList<NewsContentNode>>();
		for (int i = 1; i <= nodes; ++i) {
			adj.put(i, new LinkedList<NewsContentNode>());
		}

	}

	public void addNeighbor(int v1, NewsContentNode n) {
		adj.get(v1).add(n);		
	}

	public LinkedList<NewsContentNode> getNeighbors(int v) {
		return adj.get(v);
	}

	public void displayGraph() {

		List<NewsContentNode> temp_news_details;

		for (int i=1; i <= adj.size(); i++) {
			System.out.print("Node " + i + ": ");
			temp_news_details = getNeighbors(i);

			for (NewsContentNode n: temp_news_details) {
				System.out.print(n.getNewsNumber() + "|" + n.getAngle() + "|" + n.getNewsChannel() + " --> ");
			}

			System.out.println();
		}
	}

	public LinkedList<CycleVertices> findCycles(SimilarityGraph abc_sg, SimilarityGraph bbc_sg, SimilarityGraph cnn_sg) {

		int vNewsNumber, testNewsNumber; 
		String vNewsChannel, testNewsChannel; 
		float vNewsAngle, testNewsAngle;
		LinkedList<NewsContentNode> testNodes, finalTestNodes;

		LinkedList<CycleVertices> cycles = new LinkedList<CycleVertices>();
		CycleVertices tempObj;

		for (int i = 1; i <= abc_sg.adj.size(); i++) {
			LinkedList<NewsContentNode> nc_node = abc_sg.getNeighbors(i);
			if (nc_node.size() != 0) {
				for (NewsContentNode nc : nc_node) {
					vNewsNumber = nc.getNewsNumber();
					vNewsChannel = nc.getNewsChannel();
					vNewsAngle = nc.getAngle();

					switch(vNewsChannel) {
					case "bbc":
						testNodes = bbc_sg.getNeighbors(vNewsNumber);
						for (NewsContentNode testNode : testNodes) {
							testNewsNumber = testNode.getNewsNumber();
							testNewsAngle = testNode.getAngle();
							
							if (testNode.getNewsChannel() != "abc") {
								finalTestNodes = cnn_sg.getNeighbors(testNewsNumber);
								for (NewsContentNode finalTest : finalTestNodes) {
									if (finalTest.getNewsNumber() == i && finalTest.getNewsChannel() != "bbc") {		// Cycle exists
										tempObj = new CycleVertices();

										tempObj.setVertices(i, vNewsNumber, testNewsNumber);
										//tempObj.setVertices(testNewsNumber, vNewsNumber, i);
										tempObj.setAverageAngle((finalTest.getAngle() + vNewsAngle + testNewsAngle) / 3);
										cycles.add(tempObj);
									}
								}
							}
						}
						break;

					case "cnn":
						testNodes = cnn_sg.getNeighbors(vNewsNumber);
						for (NewsContentNode testNode : testNodes) {
							testNewsNumber = testNode.getNewsNumber();
							testNewsAngle = testNode.getAngle();
							
							if (testNode.getNewsChannel() != "abc") {
								finalTestNodes = bbc_sg.getNeighbors(testNewsNumber);
								for (NewsContentNode finalTest : finalTestNodes) {
									if (finalTest.getNewsNumber() == i && finalTest.getNewsChannel() != "cnn") {		// Cycle exists
										tempObj = new CycleVertices();

										tempObj.setVertices(i,testNewsNumber, vNewsNumber);
										//tempObj.setVertices(vNewsNumber,testNewsNumber, i);
										
										// Mean
										tempObj.setAverageAngle((finalTest.getAngle() + vNewsAngle + testNewsAngle) / 3);
										cycles.add(tempObj);
									}
								}
							}
						}
						break;

					default:
						System.out.println("Wrong news content, mate!");
						break;
					}
				}
			}
		}

		return cycles;		// Returning the cycles found
	}

}


class NewsContentNode {			// For storing the news contents in the adjacency list 
	private int news_number;
	private float angle;
	private String news_channel;

	NewsContentNode() {
		news_number = -1;
		angle = -90;
		news_channel = "xyz";
	}

	void setNewsNumber(int news_number) {
		this.news_number = news_number;
	}

	void setAngle(float angle) {
		this.angle = angle;
	}

	void setNewsChannel(String news_channel) {
		this.news_channel = news_channel;
	}

	int getNewsNumber() {
		return news_number;
	}

	float getAngle() {
		return angle;
	}

	String getNewsChannel() {
		return news_channel;
	}
}

class CycleVertices {		// For storing the vertices which form a cycle
	int v1, v2, v3;
	float averageAngle;

	CycleVertices() {
		v1 = -1; v2 = -2; v3 = -3;
	}

	void setVertices(int v1, int v2, int v3) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	void setAverageAngle(float averageAngle) {
		this.averageAngle = averageAngle;
	}
	
	CycleVertices getVertices() {
		return this;
	}
	
	float getAverageAngle() {
		return averageAngle;
	}
	
	void displayCycles(LinkedList<CycleVertices> cycles) {
		CycleVertices dispObj;

		System.out.println("Cycles present in the graph");

		for (CycleVertices c : cycles) {
			dispObj = c.getVertices();
			System.out.println(dispObj.v1 + " " + dispObj.v2 + " " + dispObj.v3 + " " + dispObj.getAverageAngle());
		}
	}
}