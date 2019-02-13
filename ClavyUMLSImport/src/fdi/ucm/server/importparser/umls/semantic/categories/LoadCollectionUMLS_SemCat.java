/**
 * 
 */
package fdi.ucm.server.importparser.umls.semantic.categories;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;

import fdi.ucm.server.modelComplete.ImportExportDataEnum;
import fdi.ucm.server.modelComplete.ImportExportPair;
import fdi.ucm.server.modelComplete.LoadCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollectionAndLog;

/**
 * @author Joaquin Gayoso Cabada
 *
 */
public abstract class LoadCollectionUMLS_SemCat extends LoadCollection{


	
	private static ArrayList<ImportExportPair> Parametros;
	public static boolean consoleDebug=false;
	
	
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		LoadCollectionUMLS_SemCat LC=new LoadCollectionUMLS_SemCat();
//		LoadCollectionUMLS_SemCat.consoleDebug=true;
//		
//		ArrayList<String> AA=new ArrayList<String>();
//		
//		CompleteCollectionAndLog Salida=null;
//		
////		ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
////		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "List Documents json File"));
////		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "List Documents String txt File"));
////		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "UMLS output File"));
////		//ESTO ES MEJORABLE
////		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Filter categories txt",true));
////		//Futuro
////		//ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Filter categories cvs",true));
////		ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Openi json File"));
//	
//		AA.add("semrel.json");
//
//	
//			 Salida=LC.processCollecccion(AA);
//	
//			
//		
//		if (Salida!=null)
//			{
//			
//			System.out.println("Correcto");
//			
//			for (String warning : Salida.getLogLines())
//				System.err.println(warning);
//
//			
//			try {
//				String FileIO = System.currentTimeMillis()+".clavy";
//				
//				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FileIO));
//
//				oos.writeObject(Salida.getCollection());
//
//				oos.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//			
//			System.exit(0);
//			
//			}
//		else
//			{
//			System.err.println("Error");
//			System.exit(-1);
//			}
//	}

	
	
	
	
	@Override
	public CompleteCollectionAndLog processCollecccion(ArrayList<String> dateEntrada) {
		
		CompleteCollectionAndLog Salida = new CompleteCollectionAndLog();
		Salida.setCollection(new CompleteCollection("UMLS SemRel", "Relaciones semanticas entre UMLS"));
		Salida.setLogLines(new ArrayList<String>());
		
		   HashMap<String, HashMap<String,List<String>>> TablaSeman=new HashMap<>();
		
		try {
			System.out.println("//Procesando la Entrada");
			
			String Relation = dateEntrada.get(0);
			
			 JsonReader reader = new JsonReader(new FileReader(Relation));
				Gson gson = new Gson();
				@SuppressWarnings("rawtypes")
				List<LinkedTreeMap> SemanticaLi =  gson.fromJson(reader, List.class);
			
				for (@SuppressWarnings("rawtypes") LinkedTreeMap semantica2 : SemanticaLi) 
				{
				try {
					String name=(String)semantica2.get("name");
					@SuppressWarnings({ "rawtypes" })
					LinkedTreeMap valores=(LinkedTreeMap) semantica2.get("act");
					if (valores!=null)
						{
						HashMap<String, List<String>> act_des=new HashMap<>();
						for (Object Claves : valores.keySet()) {
							if (Claves instanceof String)
								{
								
								@SuppressWarnings("unchecked")
								List<String> destino=(List<String>) valores.get(Claves);
								
								
								act_des.put((String) Claves, destino);
								
								
								}
						}
						
						TablaSeman.put(name,act_des);
						
						}
					System.out.println(name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			
				} 	
		} catch (Exception e) {
			e.printStackTrace();
			Salida.getLogLines().add("Error en la carga del archivo");
		}
		
		System.out.println("//Generando coleccion Clavy");
		
		processCollecccion(TablaSeman,Salida.getCollection());
		
		return Salida;
	}

	
	
	
	public abstract void processCollecccion(HashMap<String, HashMap<String, List<String>>> tablaSeman, CompleteCollection completeCollection);





	@Override
	public ArrayList<ImportExportPair> getConfiguracion() {
		if (Parametros==null)
		{
			ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Semantica Relations in JSON"));
			return ListaCampos;
		}
		else return Parametros;
	}

	@Override
	public String getName() {
		return "UMLS Semantics Import";
	}

	@Override
	public boolean getCloneLocalFiles() {
		return false;
	}

	

	
}
