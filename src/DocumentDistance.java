package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class DocumentDistance {

	NewsContentNode news_node;
	SimilarityGraph abc_sg, bbc_sg, cnn_sg, sg_obj;
	LinkedList<CycleVertices> cyclesList; 
	CycleVertices tempCycles = new CycleVertices();

	public DocumentDistance(int total_news_abc, int total_news_bbc, int total_news_cnn)
	{

		abc_sg = new SimilarityGraph(total_news_abc);
		bbc_sg = new SimilarityGraph(total_news_bbc);
		cnn_sg = new SimilarityGraph(total_news_cnn);
		
		ArrayList<OutputStructure> results1 = computeResults(new File("abc_swr.txt"), new File("cnn_swr.txt"));
		addToFile(results1,new File("abc-cnn.txt"), abc_sg, cnn_sg, "abc", "cnn");
		

		ArrayList<OutputStructure> results2 = computeResults(new File("bbc_swr.txt"), new File("cnn_swr.txt"));
		addToFile(results2,new File("bbc-cnn.txt"), bbc_sg, cnn_sg, "bbc", "cnn");

		ArrayList<OutputStructure> results3 = computeResults(new File("abc_swr.txt"), new File("bbc_swr.txt"));
		addToFile(results3,new File("abc-bbc.txt"), abc_sg, bbc_sg, "abc", "bbc");
		
		System.out.println("Similarity Graph: ABC News");
		abc_sg.displayGraph();
		System.out.println("Similarity Graph: BBC News");
		bbc_sg.displayGraph();
		System.out.println("Similarity Graph: CNN News");
		cnn_sg.displayGraph();
		
		sg_obj = new SimilarityGraph(0);
		cyclesList = sg_obj.findCycles(abc_sg, bbc_sg, cnn_sg);
		tempCycles.displayCycles(cyclesList);
	}

	private ArrayList<OutputStructure> computeResults(File news1,File news2)
	{

		ArrayList<String> site1 = null,site2 = null;
		try {
			site1 = getList(news1);
			site2 = getList(news2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		ArrayList<OutputStructure> results = new ArrayList<OutputStructure>();
		int outer = 1;
		int inner;
		for(String high : site1)
		{
			inner = 1;
			for(String low : site2)
			{

                 
				if (low.length() != 0 && high.length() != 0) {
				results.add(new OutputStructure(outer, inner, computerAngle(high, low)));

				//System.out.println(outer + ":" + inner + "=" + computerAngle(high, low));
				}
				inner++;
				
			}

			outer++;
		}

		Collections.sort(results, new Comparator<OutputStructure>() {


			public int compare(OutputStructure o1, OutputStructure o2) {

				if(o1.getAngle() > o2.getAngle())
					return 1;
				else if(o1.getAngle() < o2.getAngle())
					return -1;

				return 0;
			}
		});
		


		return results;

	}




	public void addToFile(ArrayList<OutputStructure> results,File file, SimilarityGraph sg_1, SimilarityGraph sg_2, String news_channel_1, String news_channel_2)
	{
		FileWriter fstream ;
		BufferedWriter out = null;
		
		try {

			fstream = new FileWriter(file,true);
			out	 = new BufferedWriter(fstream);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			out.write("Results from " + file.toString());
			out.newLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(OutputStructure result : results )
		{
			
			try {
				if (result.getAngle() <= 80.0) {
					out.write("Line: " + result.getLine1().toString()+ " - ");
					out.write(result.getLine2().toString()+ " = " + "  ");
					out.write(result.getAngle().toString() + " degrees");
					out.newLine();
					
					news_node = new NewsContentNode();
					
					news_node.setNewsNumber(result.getLine2());
					news_node.setAngle(result.getAngle());
					news_node.setNewsChannel(news_channel_2);
					
					//System.out.println(news_node.getNewsNumber() + " " + news_node.getAngle() + " " + news_node.getNewsChannel());
					//System.out.println(result.getLine1() + " " + result.getLine2() + " " + news_channel_1);
					// Add to the adjacency list of news_content_2
					sg_1.addNeighbor(result.getLine1(), news_node);
					
					news_node = new NewsContentNode();
					
					news_node.setNewsNumber(result.getLine1());
					news_node.setAngle(result.getAngle());
					news_node.setNewsChannel(news_channel_1);
					
					// Add to the adjacency list of news_content_2
					sg_2.addNeighbor(result.getLine2(), news_node);
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private ArrayList<String> getList(File file) throws IOException
	{
		ArrayList<String> lines = new ArrayList<String>();
		FileInputStream fis = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		while((line = br.readLine())!=null)
		{
			lines.add(line);	
		}

		return lines;
	}

	private float computerAngle(String one,String two)
	{


		HashMap<String, Integer> map1 = getFrequency(one);
		HashMap<String, Integer> map2 = getFrequency(two);

		double num = computeInnerProduct(map1, map2);
		double denom =  Math.sqrt(computeInnerProduct(map1, map1)*computeInnerProduct(map2, map2));



		float angle = (float) Math.acos(num/denom);


		//System.out.println(radiansToDegrees(angle));

		return radiansToDegrees(angle);

	}

	private HashMap<String, Integer> getFrequency(String string)
	{
		HashMap<String, Integer> map = new HashMap<String,Integer>();
		StringTokenizer token = new StringTokenizer(string, " ");//regex split method better than this

		while(token.hasMoreElements())
		{
			String a = (String) token.nextElement();
			if(map.containsKey(a))
			{
				int count = map.get(a);
				map.put(a, ++count);
			}
			else
				map.put(a, 1);

		}

		return map;

	}


	float radiansToDegrees(float angle)
	{

		return (float) (angle*180/Math.PI);

	}



	int computeInnerProduct(Map<String, Integer> map1,Map<String, Integer> map2)
	{
		int match=0;
		Iterator iter1 = map1.entrySet().iterator();

		while(iter1.hasNext())
		{

			Map.Entry entry = (Entry) iter1.next();

			if(map2.containsKey(entry.getKey()))
			{

				int val1 = map1.get(entry.getKey());

				int val2 = map2.get(entry.getKey());

				match+=val1*val2;

			}

		}


		return match;
	}


	

	public static void main(String args[])
	{

		DocumentDistance vector = new DocumentDistance(30, 64, 107);

	}


}

class OutputStructure 
{
	Integer line1,line2;
	Float angle;

	public OutputStructure(Integer line1, Integer line2, Float angle) {

		this.line1 = line1;
		this.line2 = line2;
		this.angle = angle;
	}

	public Integer getLine1() {
		return line1;
	}

	public Integer getLine2() {
		return line2;
	}

	public Float getAngle() {
		return angle;
	}



}
