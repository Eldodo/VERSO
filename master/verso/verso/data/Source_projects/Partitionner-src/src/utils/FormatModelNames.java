package utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

public class FormatModelNames {
	public final static Logger LOGGER = Logger.getLogger(FormatModelNames.class.getName());
	public static void main(String[] args) throws Exception{
		LOGGER.info("");
	
		
		Utils.init();	
		
		File instancesDirectory = new File(Config.DIR_INSTANCES+Config.METAMODEL_NAME);
		
		
		
		String newDir = instancesDirectory.getAbsolutePath()+File.separator+"jsh"+Utils.getRandomInt(1000)+"dhJK";
		File newDirFile = new File(newDir);
		if(newDirFile.exists())
			newDirFile.delete();
		
		newDirFile.mkdir();
		int nbFiles = instancesDirectory.listFiles().length;
		System.out.println("Files to move : "+nbFiles);
		System.out.println("Source repository : "+instancesDirectory.getAbsolutePath());
		
	    System.out.println("Allright ? o/n");
	    String on =  new BufferedReader(new InputStreamReader(System.in)).readLine();
	    if(on.trim().equalsIgnoreCase("o")){
	    	System.out.println("First number : ");
		    int first =  Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
	    	
		    System.out.println("C'est parti... ");
	    	File flist[] = Arrays.copyOf(instancesDirectory.listFiles(), instancesDirectory.listFiles().length);
	    	System.out.println("Ordering files by size...");
	    	
	    	Arrays.sort(flist, new Comparator<File>() {
				@Override
				public int compare(File arg0, File arg1) {
					if(arg0.isDirectory())
						return Integer.MAX_VALUE;
					if(arg1.isDirectory())
						return Integer.MIN_VALUE;
					return (int)(arg0.length() - arg1.length());
				}
			});
	    	
	    	
	    	System.out.println("Copying files to tmp folder...");
	    	int percent10 = 0;
			for (int i = 0; i < flist.length; i++) {
				File f = flist[i-0];
				if(!f.isDirectory()){
					
					int ii = i+first;
					String newName = "model_"+(((ii<10)?"0000":(ii<100)?"000":(ii<1000)?"00":(ii<10000)?"0":"") +ii) + ".xmi";

					Path source = Paths.get(f.getAbsolutePath());
					Path cible = Paths.get(newDir+File.separator+newName);
					try {
//						System.out.println("From "+source+" to "+cible);
						Files.move(source, cible);
					} catch (Exception e) {
						e.printStackTrace();
						
					}
					f.delete();
				}
				if(i%(nbFiles/10)==0 && i!= 0)
					System.out.println(++percent10 + "0% processed.");
			}
			
			
			
			File flist2[] = Arrays.copyOf(newDirFile.listFiles(), newDirFile.listFiles().length);
			System.out.println("Copying files back to instance directory...");
			for (int i = 0; i < flist2.length; i++) {
				File f = flist2[i];
				
				if(f != null ){
					Path source = Paths.get(f.getAbsolutePath());
					Path cible = Paths.get(instancesDirectory.getAbsolutePath()+File.separator+f.getName());
					try {
//						System.out.println("From "+source+" to "+cible);
						Files.move(source, cible, StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			newDirFile.delete();
			System.out.println("Done !");
	    } else 
	    	System.out.println("Exit.");
	}

}
