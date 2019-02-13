/**
 * 
 */
package fdi.ucm.server.importparser.umls.semantic.categories;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import fdi.ucm.server.modelComplete.collection.CompleteCollectionAndLog;

/**
 * @author Joaquin Gayoso Cabada
 *
 */
public class LoadCollectionUMLS_SemCatV2 extends LoadCollectionUMLS_SemCatV1{

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoadCollectionUMLS_SemCatV2 LC=new LoadCollectionUMLS_SemCatV2();
		LoadCollectionUMLS_SemCatV2.consoleDebug=true;
		
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
	
		AA.add("semrelV2.json");

	
			 Salida=LC.processCollecccion(AA);
	
			
		
		if (Salida!=null)
			{
			
			System.out.println("Correcto");
			
			for (String warning : Salida.getLogLines())
				System.err.println(warning);

			
			try {
				String FileIO = "sem_"+System.currentTimeMillis()+".clavy";
				
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



	@Override
	public String getName() {
		return "UMLS Semantic Import V2";
	}


	

	

	

	
}
