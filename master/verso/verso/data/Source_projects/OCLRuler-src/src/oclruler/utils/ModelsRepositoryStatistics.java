package oclruler.utils;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import oclruler.metamodel.ExampleSet;
import oclruler.metamodel.InvalidModelException;
import oclruler.metamodel.Metamodel;
import oclruler.metamodel.Model;
import partitioner.partition.PartitionModel;

public class ModelsRepositoryStatistics {
	public final static Logger LOGGER = Logger.getLogger(ModelsRepositoryStatistics.class.getName());
	
	
	public static void main(String[] args) throws Exception{
		LOGGER.info("");
		ToolBox.init();	

		String res = buildAndWriteModelRepositoryStatistics();
		
		System.out.println(res);
		
		System.out.println("Done !");
	}
	
	static char[] al = "qwertyuiopasdfghjklzxcvbnQWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
	public static String genChars(int length){
		String res = "";
		for (int i = 0; i < length; i++) {
			res += al[ToolBox.getRandomInt(al.length)];			
		}
		return res;
	}
	
	/**
	 * Compute statistics over the examples repository and store it in a file name MM_NAME_RepositoryStatistics.log in the example base directory.<br/>
	 * Directory considered is DIR_EXAMPLE/METAMODEL_NAME
	 * @return What's written : metamodel, classes and properties' statistics.
	 * @throws IOException 
	 */
	public static String buildAndWriteModelRepositoryStatistics() {
		return buildAndWriteModelRepositoryStatistics(
				new File(Config.DIR_EXAMPLES+Config.METAMODEL_NAME)
			);
	}
	
	/**
	 * Directory considered is DIR_EXAMPLE/METAMODEL_NAME
	 * @param target
	 * @param targetShort
	 * @return
	 */
	public static String buildAndWriteModelRepositoryStatistics(File sourceRepository) {
		return buildAndWriteModelRepositoryStatistics(
				new File(sourceRepository.getAbsolutePath()+File.separator+"ExamplesStatistics_extended.log"), 
				new File(sourceRepository.getAbsolutePath()+File.separator+"ExamplesStatistics.log"),  
				sourceRepository
			);
	}
	
	private static boolean shortOnly = false; //Testing purpose
	public static String buildAndWriteModelRepositoryStatistics(File target, File targetShort, File sourceRepository) {
		LOGGER.config("Building repository statistics file for '"+sourceRepository+"'.");
		File fDatas = target;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fDatas));
			if(!shortOnly) {
				if(!fDatas.exists())
						fDatas.createNewFile();
				else {
//					LOGGER.warning("File '"+fDatas.getAbsolutePath()+"' will be overriden.");
					fDatas.delete();
					fDatas.createNewFile();
				}
				bw.append("file;nbClasses;nbProperties\n");
			}
			
			File[] instanceFiles = ToolBox.listXMIFiles(sourceRepository).clone();
			int nbInstanceFiles = instanceFiles.length;
	
			int sumC = 0, sumP = 0;
			double avgC = 0, avgP = 0;
			double varC = 0, varP = 0;
			int maxC = 0, maxP =0;
			int minC = Integer.MAX_VALUE, minP = Integer.MAX_VALUE;
	
			int iPercent = 0;
			Model m = null;
	
	
			int[][] knowledge = new int[2][nbInstanceFiles];
			int CLASSES = 0;
			int PROPERTIES = 1;
			
			File files[] = Arrays.copyOf(instanceFiles, nbInstanceFiles);
			if(!shortOnly)
				for (int i = 0;  i < nbInstanceFiles+0; i++) {
					File f = files[i-0];
					if( !f.isDirectory()){
						try {
							m = ExampleSet.loadModel(f.getAbsolutePath());
						} catch (InvalidModelException e) {
							e.printStackTrace();
						}
						if(m != null){
							int classes = m.getNbClasses();
							int nbProperties = m.getNbProperties();
			
							knowledge[CLASSES][i] = classes;
							knowledge[PROPERTIES][i] = nbProperties;
							
							
							bw.append(m.getFileName()+";"+classes+";"+nbProperties+"\n");
			
							if(classes > maxC) maxC = classes;
							if(classes < minC) minC = classes;
							sumC += classes;
			
							if(nbProperties > maxP) maxP = nbProperties;
							if(nbProperties < minP) minP = nbProperties;
							sumP += nbProperties;
							m=null;
						}
					}
					if( i != 0 && (((i*1.0)/nbInstanceFiles)*100)>=iPercent){
						avgC = sumC*1.0/(i+1);
						avgP = sumP*1.0/(i+1);
						LOGGER.config(iPercent+++"% - "+i+" files processed : "+minC+"/"+ToolBox.format2Decimals((float)avgC)+"/"+maxC+"  -  "+minP+"/"+ToolBox.format2Decimals((float)avgP)+"/"+maxP);
					}
				}
			bw.close();
			avgC = sumC*1.0/nbInstanceFiles;
			avgP = sumP*1.0/nbInstanceFiles;
			iPercent = 0;
			for (int i = 0; i < nbInstanceFiles+0; i++) {
				varC += Math.pow(knowledge[CLASSES][i]-avgC, 2);
				varP += Math.pow(knowledge[PROPERTIES][i]-avgP, 2);
			}
			varC = varC/nbInstanceFiles;
			varP = varP/nbInstanceFiles;
			double sigmaC = Math.sqrt(varC);
			double sigmaP = Math.sqrt(varP);
			
			
			
			PartitionModel partitionModel;
			partitionModel = new PartitionModel();
			
			partitionModel.extractPartition();
			
			int nbPackages = 0;
			int nbClasses = 0;
			float nbReferences = 0;
			float nbAttributes = 0;
			int nbPartitions = partitionModel.getPartitions().size();
			
			for (Iterator<EObject> it = Metamodel.metamodelResource.getAllContents(); it.hasNext();) {
				EObject eObject = (EObject) it.next();
				if (eObject instanceof EPackage) {
					Metamodel.ePackages().add((EPackage) eObject);
					nbPackages++;
					for (Iterator<EObject> it2 = ((EPackage)eObject).eAllContents(); it2.hasNext();){
						EObject eo = it2.next();
						if(eo instanceof EClass){
							EClass ec = (EClass)eo;
							nbClasses++;
							nbReferences += ec.getEAllReferences().size();
							nbAttributes += ec.getEAllAttributes().size();
						}
					}
				}
			}
			
			nbReferences /= (float)nbClasses;
			nbAttributes /= (float)nbClasses;
			
			
			String res = "Repository:  "+sourceRepository.getAbsolutePath()+"\n";
			res +=       "         Metamodel: "+Config.METAMODEL_NAME+"\n";
			res +=       "        # packages: "+nbPackages+"\n";
			res +=       "         # classes: "+nbClasses+"\n";
			res +=       "  avg # references: "+nbReferences+"\n";
			res +=       "  avg # attributes: "+nbAttributes+"\n";
			res +=       "      # partitions: "+nbPartitions+"\n";
			
			res +=       "\n          # models: "+nbInstanceFiles+"\n";
	
			String resC = " * Classes *"+"\n";
			resC += "  Avg size: "+avgC+"\n";
			resC += "     Sigma: "+sigmaC+"\n";
			resC += "  Variance: "+varC+"\n";
			resC += "       Min: "+minC+"\n";
			resC += "       Max: "+maxC+"\n";
			resC += "     Total: "+sumC+"\n";
	
			String resP = " * Properties *"+"\n";
			resP += "  Avg size: "+avgP+"\n";
			resP += "     Sigma: "+sigmaP+"\n";
			resP += "  Variance: "+varP+"\n";
			resP += "       Min: "+minP+"\n";
			resP += "       Max: "+maxP+"\n";
			resP += "     Total: "+sumP+"\n";
	
	
			File fStats = targetShort;
			BufferedWriter bwF = new BufferedWriter(new FileWriter(fStats));
			try {
				LOGGER.fine(res + "\n"+resC+"\n"+resP);
				bwF.write(res+"\n"+resC+"\n"+resP);
				bwF.flush();
			} catch (Exception e) {
				LOGGER.severe("Couldn't write failed resources in file '"+fStats.getAbsolutePath()+"'");
				e.printStackTrace();
			} finally {
				bwF.close();
			}
			LOGGER.config("Data written in '"+ fDatas.getAbsolutePath()+"', statistics in '"+fStats.getAbsolutePath()+"'");
			
			return res+"\n"+resC+"\n"+resP;
		} catch (IOException e) {
			e.printStackTrace();
			return "IO Exception while writing file '"+fDatas+"'";
		}

	}
}
