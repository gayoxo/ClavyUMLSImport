/**
 * 
 */
package fdi.ucm.server.importparser.umls;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import fdi.ucm.server.modelComplete.collection.CompleteCollectionAndLog;

/**
 * @author Joaquin Gayoso Cabada
 *
 */
public class LoadCollectionUMLSV1_4_JSON_No_Filter extends LoadCollectionUMLSV1_0{

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoadCollectionUMLSV1 LC=new LoadCollectionUMLSV1_4_JSON_No_Filter();
		LoadCollectionUMLSV1.consoleDebug=true;
		
		ArrayList<String> AA=new ArrayList<String>();
		
		CompleteCollectionAndLog Salida=null;
		
//		ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
//		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "List Documents json File"));
//		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "List Documents String txt File"));
//		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "UMLS output File"));
//		//ESTO ES MEJORABLE
//		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Filter categories txt",true));
//		//Futuro
//		//ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Filter categories cvs",true));
//		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Openi json File"));
	
		String Carpeta="";
//		if (args.length>0)
//			Carpeta=args[0];
//		if (!Carpeta.isEmpty()&&!Carpeta.endsWith(File.separator))	
//			Carpeta=Carpeta+File.separator;
//			
		AA.add(Carpeta+"salida_docs.json");
		AA.add(Carpeta+"sample.txt");
		AA.add(Carpeta+"salida.xml");
		AA.add(Carpeta+"");
		AA.add(Carpeta+"openi_nlm_nih_gov.json");
		AA.add(Carpeta+"reducido.csv");
	
			 Salida=LC.processCollecccion(AA);
	
			
		
		if (Salida!=null)
			{
			
			System.out.println("Correcto");
			
			for (String warning : Salida.getLogLines())
				System.err.println(warning);

			
			try {
				String FileIO = Carpeta+System.currentTimeMillis()+".clavy";
				System.out.println(FileIO);
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FileIO));

				oos.writeObject(Salida.getCollection());

				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			System.exit(0);
			
			}
		else
			{
			System.err.println("Error");
			System.exit(-1);
			}
	}


	
}
