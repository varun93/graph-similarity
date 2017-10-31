package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExtractTitle {

	final static String[] stopWords = {"(\\s|^)[Tt]he\\s", "(\\s|^)[Aa](n|nd|re)*\\s", "(\\s|^)[Tt]o\\s", "(\\s|^)[Ff](rom|or)\\s", "(\\s|^)[Ii](s|n)*\\s", "(\\s|^)[Oo](f|n)\\s",
	"(\\s|^)[Ww]h(om|ose|ere|o|ich)\\s"};
	static Set<String> words;

	public static void main(String args[]) throws IOException {
		Document doc;
		String newsChannel;
		int total_news_abc = 0 , total_news_bbc = 0, total_news_cnn = 0;
		long preprocessing_start = 0;

		storeStopWords();
		System.out.println("Stop Words: " + words.size());
		deletePreviousData("cnn");
		deletePreviousData("abc");
		deletePreviousData("bbc");
		deletePreviousData("cnn_swr");
		deletePreviousData("abc_swr");
		deletePreviousData("bbc_swr");
		deletePreviousData("cnn_stem");
		deletePreviousData("abc_stem");
		deletePreviousData("bbc_stem");
		deletePreviousData("abc-bbc");
		deletePreviousData("abc-cnn");
		deletePreviousData("bbc-cnn");

		try {
			doc = Jsoup.connect("http://rss.cnn.com/rss/edition.rss").get();

			Elements cnnTitles = doc.select("description");

			//int new_news_cnn = checkNewsContent(cnnTitles, "cnn");

			doc = null;

			doc = Jsoup.connect("http://feeds.abcnews.com/abcnews/topstories").get();

			Elements abcTitles = doc.select("description");


			//int new_news_abc = checkNewsContent(abcTitles, "abc");

			doc = null;

			doc = Jsoup.connect("http://feeds.bbci.co.uk/news/rss.xml?edition=int").get();	// http://feeds.nbcnews.com/feeds/topstories

			Elements bbcTitles = doc.select("description");

			//int new_news_bbc = checkNewsContent(bbcTitles, "bbc");
			
			//System.out.println("Line Check CNN: " + new_news_cnn);
			//System.out.println("Line Check ABC: " + new_news_cnn);
			//System.out.println("Line Check BBC: " + new_news_cnn);

			/*for (Element t: lastUpdated_abc) {
				System.out.println(t.text().toString());
				break;
			}

			for (Element t: lastUpdated_bbc) {
				System.out.println(t.text().toString());
				break;
			}

			for (Element t: lastUpdated_cnn) {
				System.out.println(t.text().toString());
				break;
			}*/

			// Beginning of pre-processing
			
			preprocessing_start = System.currentTimeMillis();
			
			newsChannel = "cnn";
			int i = 1;
			for (Element t: cnnTitles) {	
				//System.out.println(t.text());
				if (i > 2) {
					addToFile("cnn", t.text().toLowerCase(), true);
					removeStopWords(newsChannel, t.text().toLowerCase());
				}
				i++;
			}

			// Assigning the total news contents 
			total_news_cnn = i - 3;

			newsChannel = "abc";
			i = 1;
			for (Element t: abcTitles) {
				//System.out.println(t.text());
				if (i > 1) {
					addToFile("abc", t.text().toLowerCase(), true);
					removeStopWords(newsChannel, t.text().toLowerCase());
				}
				i++;
			}

			total_news_abc = i - 2;

			newsChannel = "bbc";
			i = 1;
			for (Element t: bbcTitles) {
				//System.out.println(t.text());
				if (i > 1) {
					addToFile("bbc", t.text().toLowerCase(), true);
					removeStopWords(newsChannel, t.text().toLowerCase());
				}
				i++;
			}

			total_news_bbc = i - 2;

		} catch(IOException e) {
			e.printStackTrace();
		}

		applyStemmer("cnn_swr.txt", "cnn");
		applyStemmer("abc_swr.txt", "abc");
		applyStemmer("bbc_swr.txt", "bbc");
		
		System.out.println("Preprocessing time: " + (System.currentTimeMillis() - preprocessing_start) + " ms");
		// End of pre-processing
		
		System.out.println("CNN: " + total_news_cnn + " ABC: " + total_news_abc + " BBC: " + total_news_bbc);

		long findTopNews_start = System.currentTimeMillis();
		
		DocumentDistance docDist = new DocumentDistance(total_news_abc, total_news_bbc, total_news_cnn);
		
		System.out.println("Results found in " + (System.currentTimeMillis() - findTopNews_start) + " ms");

	}


	public static void deletePreviousData(String fileName) {
		try {
			File file = new File(fileName + ".txt");

			if (file.delete()) {
				System.out.println(fileName + " is cleaned up.");
			}
			else {
				System.out.println(fileName + " cleanup failed.");
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void addToFile(String fileName, String data, boolean skip) {
		try{
			File file =new File(fileName + ".txt");

			//if file doesn't exists, then create it
			if(!file.exists()){
				file.createNewFile();
			}

			//true = append file
			FileWriter fileWritter = new FileWriter(file.getName(), true);
			BufferedWriter bufferWriter = new BufferedWriter(fileWritter);
			bufferWriter.write(data);
			if (skip)
				bufferWriter.newLine();
			bufferWriter.close();

		} catch(IOException e){
			e.printStackTrace();
		}
	}


	public static int checkNewsContent(Elements news, String newsChannel) {
		BufferedReader br = null;
		int line = -1;
		
		try {
			File file =new File(newsChannel + ".txt");
			FileReader fileReader = new FileReader(file.getName());

			br = new BufferedReader(fileReader);

			String firstNews = br.readLine();
			System.out.println(newsChannel + " : " + firstNews);
			
			line = 0;
			for (Element e: news) {
				if (e.text().toString().toLowerCase().equals(firstNews)) {
					break;
				}
				line++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return line;

	}

	public static  void storeStopWords() throws IOException
	{

		ArrayList<String> lines = new ArrayList<String>();
		FileInputStream fis = new FileInputStream(new File("stop words.txt"));
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		while((line = br.readLine())!=null)
		{
			lines.add(line);
		}


		words = new HashSet<String>();
		for(String l : lines)
		{
			String arr[] = l.split(",");
			for(int i=0;i<arr.length;i++)
			{
				String word = arr[i].trim();
				words.add(word);


			}
		}



	}


	public static void removeStopWords(String fileName, String s) {
		
		String[] line=null; 

		if(s.length()>0)
		{
			line = s.split(" ");

			//System.out.println(s);
			String newLine="";
			for(String word : line)
			{
				if(!words.contains(word))
				{

					newLine+=word+" ";
				}

			}



			fileName = fileName + "_swr";

			//System.out.println(s);

			addToFile(fileName,newLine, true);
		}
		//System.out.println();
	}

	public static void applyStemmer(String inputFile, String newsChannel) throws IOException {

		String singleLine="";
		String[] arr;
		Stemmer s = new Stemmer();
		FileInputStream fis = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		while((line = br.readLine())!=null)
		{

			arr = line.split("[\\W]");
			singleLine="";

			for(int i=0;i<arr.length;i++)
			{

				for(int j=0;j<arr[i].length();j++)
				{
					s.add(arr[i].charAt(j));
				}

				s.stem();
				singleLine+=s.toString() + " ";

			}

			addToFile(newsChannel + "_stem", singleLine, true);

		}




	}

}
