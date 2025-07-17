
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class FindFiles {
	static FileWriter fileWriter;
	static ArrayList<String> data =new ArrayList<String>();

	public static void main(String[] args) throws ParseException, IOException {
		
		/*String s="/home/chaitu/mnt/ctpa_images1/Cteph_Sorted/501000020276069-Marlborough Medical Center/study_1.2.124.38368.20746863428970210671.73117176/series_1.2.124.38368.33582990490034705612.73117176";
           String[] col=s.split("/");
           for (int i = 0; i < col.length; i++) {
			if(col[i].indexOf("series_")!=-1){
				System.out.println(col[i].substring(7));
			}
			if(col[i].indexOf("study_")!=-1){
				System.out.println(col[i].substring(6));
			}
		}
*/
		File dicompath = new File(args[0]);
		fileWriter = new FileWriter(args[1]);
		fileWriter.append("\"path\",\"filepath\",\"studyuid\",\"seriesuid\"\n");

		System.out.println("counting Started");
		FindFiles t = new FindFiles();
		data=t.getList(args[2]);
		t.findFilesWithPath(dicompath);
		System.out.println("Finished............................");


		try {
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Error while flushing/closing fileWriter !!!");
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getList(String path){
		ArrayList al1 = new ArrayList();

		try {
			BufferedReader	f1 = new BufferedReader(new FileReader(path));
			String str1="";
			
			while ((str1 = f1.readLine()) != null) {
				
			//	String dcmname=str1.substring(str1.lastIndexOf("/"));
				al1.add(str1);

				
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		
		
		
		return al1;
		
		
	}
	
	
	public void findFiles(File folder) throws IOException {
		File[] files = folder.listFiles();
		int i = 0;
		int Count = 0;

		while (i < files.length) {
			File fileentry = files[i];
			if (fileentry.isDirectory()) {
				this.findFiles(fileentry);
			} else {
				if(folder.getCanonicalPath().indexOf("series_")!=-1){

				String[] col=folder.getCanonicalPath().split("/");
				 String Series="";
	      	   String Study="";
		           for (int j = 0; j < col.length; j++) {	        	  
					if(col[j].indexOf("series_")!=-1){
						Series=col[j].substring(7);
					}
					if(col[j].indexOf("study_")!=-1){
						Study=col[j].substring(6);
					}
				}
					//System.out.println(Study+"=="+Series+"===="+data.indexOf((Study+"_"+Series)));
				fileWriter.append("\"" + fileentry.getParent() + "\",\""+ fileentry.getCanonicalPath() + "\",\"" + Study +"\",\""+Series + "\"\n");
				Count++;
					}
			}
			++i;
		}
	/*	if(Count>0&&folder.getCanonicalPath().indexOf("series_")!=-1){
			String[] col=folder.getCanonicalPath().split("/");
			 String Series="";
      	   String Study="";
	           for (int j = 0; j < col.length; j++) {	        	  
				if(col[j].indexOf("series_")!=-1){
					Series=col[j].substring(7);
				}
				if(col[j].indexOf("study_")!=-1){
					Study=col[j].substring(6);
				}
			}
			fileWriter.append("\"" + folder.getCanonicalPath() + "\",\"" + Study +"\",\""+Series + "\",\"" +Count + "\"\n");

		}*/

	}


	public void findFilesWithPath(File folder) throws IOException {
		File[] files = folder.listFiles();
		int i = 0;
		int Count = 0;
		 String Series="";
  	     String Study="";
		if(folder.getCanonicalPath().indexOf("series_")!=-1){

			String[] col=folder.getCanonicalPath().split("/");
			
	           for (int j = 0; j < col.length; j++) {	        	  
				if(col[j].indexOf("series_")!=-1){
					Series=col[j].substring(7);
				}
				if(col[j].indexOf("study_")!=-1){
					Study=col[j].substring(6);
				}
			}
			}
		while (i < files.length) {
			File fileentry = files[i];
			if (fileentry.isDirectory()) {
				this.findFilesWithPath(fileentry);
			} else {
				if(folder.getCanonicalPath().indexOf("series_")!=-1){
					//System.out.println(Study+"=="+Series+"===="+data.indexOf((Study+"_"+Series)));
					if(!Series.equals("") && !Study.equals("") && data.indexOf((Study+"_"+Series))!=-1){
				fileWriter.append("\"" + fileentry.getParent() + "\",\""+ fileentry.getCanonicalPath() + "\",\"" + Study +"\",\""+Series + "\"\n");
				Count++;
					}
				}

			}
			++i;
		}
	/*	if(Count>0&&folder.getCanonicalPath().indexOf("series_")!=-1){
			String[] col=folder.getCanonicalPath().split("/");
			 String Series="";
      	   String Study="";
	           for (int j = 0; j < col.length; j++) {	        	  
				if(col[j].indexOf("series_")!=-1){
					Series=col[j].substring(7);
				}
				if(col[j].indexOf("study_")!=-1){
					Study=col[j].substring(6);
				}
			}
			fileWriter.append("\"" + folder.getCanonicalPath() + "\",\"" + Study +"\",\""+Series + "\",\"" +Count + "\"\n");

		}*/

	}
}
