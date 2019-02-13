/**
 * 
 */
package fdi.ucm.server.importparser.umls.semantic.categories;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollectionAndLog;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteLinkElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteLinkElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

/**
 * @author Joaquin Gayoso Cabada
 *
 */
public class LoadCollectionUMLS_SemCatV1 extends LoadCollectionUMLS_SemCat{

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoadCollectionUMLS_SemCatV1 LC=new LoadCollectionUMLS_SemCatV1();
		LoadCollectionUMLS_SemCatV1.consoleDebug=true;
		
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
	
		AA.add("semrel.json");

	
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
		return "UMLS Semantic Import V1";
	}


	@Override
	public void processCollecccion(HashMap<String, HashMap<String, List<String>>> tablaSeman,
			CompleteCollection completeCollection) {
		HashSet<String> Semanticas=new HashSet<>();
		HashMap<String, CompleteDocuments> string_doc_table=new HashMap<>();
		HashSet<String> Actions=new HashSet<>();
		HashMap<String, CompleteElementType> string_act_table=new HashMap<>();
		HashMap<CompleteElementType, List<CompleteElementType>> act_listMultiAct=new HashMap<>();
		
		CompleteGrammar SemanticaG=new CompleteGrammar("Semantica","Gramatica que describe la semantica", completeCollection);
		completeCollection.getMetamodelGrammar().add(SemanticaG);
		
		CompleteTextElementType Name=new CompleteTextElementType("Name", SemanticaG);
		SemanticaG.getSons().add(Name);
		
		for (Entry<String, HashMap<String, List<String>>> sem_tabla : tablaSeman.entrySet())
			{
			Semanticas.add(sem_tabla.getKey());
			for (Entry<String, List<String>> act_des : sem_tabla.getValue().entrySet())
				{
				Actions.add(act_des.getKey());
				Semanticas.addAll(act_des.getValue());
				}
			}
		
		String DocIcon="https://www.freeiconspng.com/uploads/brain-education-gears-idea-knowledge-power-solution-icon-22.png";

		for (String sem_tabla : Semanticas) {
			CompleteDocuments Doc=new CompleteDocuments(completeCollection, sem_tabla, DocIcon);
			CompleteTextElement CT=new CompleteTextElement(Name, sem_tabla);
			Doc.getDescription().add(CT);
			string_doc_table.put(sem_tabla, Doc);
		}
		
		
		for (String sem_tabla : Actions) {
			CompleteElementType RelationType=new CompleteLinkElementType(sem_tabla, SemanticaG);
			RelationType.setMultivalued(true);
			RelationType.setClassOfIterator(RelationType);
			string_act_table.put(sem_tabla, RelationType);
			List<CompleteElementType> listaT=new LinkedList<>();
			listaT.add(RelationType);
			act_listMultiAct.put(RelationType, listaT);
		}
		
		
		for (Entry<String, HashMap<String, List<String>>> sem_tabla : tablaSeman.entrySet())
		{
			CompleteDocuments SemanticaDoc = string_doc_table.get(sem_tabla.getKey());
			HashMap<String, List<String>> Acciones = sem_tabla.getValue();
			for (Entry<String, List<String>> act_destlist : Acciones.entrySet()) {
				CompleteElementType representante = string_act_table.get(act_destlist.getKey());
				List<CompleteElementType> total=act_listMultiAct.get(representante);
				while (total.size()<act_destlist.getValue().size())
					{
					CompleteElementType RelationType_Nuevo=new CompleteLinkElementType(representante.getName(), SemanticaG);
					RelationType_Nuevo.setMultivalued(true);
					RelationType_Nuevo.setClassOfIterator(representante);
					total.add(RelationType_Nuevo);
					}
				act_listMultiAct.put(representante, total);
				
				for (int i = 0; i < act_destlist.getValue().size(); i++) {
					CompleteLinkElement LE=new CompleteLinkElement((CompleteLinkElementType)total.get(i),string_doc_table.get(act_destlist.getValue().get(i)));
					SemanticaDoc.getDescription().add(LE);
				}
				
			
			}
			
			completeCollection.getEstructuras().add(SemanticaDoc);
		}
		
		
		
		
		
		for (Entry<CompleteElementType, List<CompleteElementType>> eReltype : act_listMultiAct.entrySet()) {
			//TODO MEter aqui las categorias
			SemanticaG.getSons().add(eReltype.getKey());
			for (CompleteElementType hermanodeeReltype : eReltype.getValue()) 
				if (hermanodeeReltype!=eReltype)
					SemanticaG.getSons().add(hermanodeeReltype);
			
		}
		
		
		
	}

	

	

	
}
