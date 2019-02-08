/**
 * 
 */
package fdi.ucm.server.importparser.umls;

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
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteLinkElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

/**
 * @author Joaquin Gayoso Cabada
 *
 */
public class LoadCollectionUMLSV1 extends LoadCollectionUMLS{

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoadCollectionUMLS LC=new LoadCollectionUMLSV1();
		LoadCollectionUMLS.consoleDebug=true;
		
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
	
		AA.add("salida_docs.json");
		AA.add("sample.txt");
		AA.add("salida.xml");
		AA.add("terminos_filtrados.txt");
		AA.add("openi_nlm_nih_gov.json");
	
			 Salida=LC.processCollecccion(AA);
	
			
		
		if (Salida!=null)
			{
			
			System.out.println("Correcto");
			
			for (String warning : Salida.getLogLines())
				System.err.println(warning);

			
			try {
				String FileIO = System.getProperty("user.home")+"/"+System.currentTimeMillis()+".clavy";
				
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

	
	
	

	
	
	protected CompleteCollectionAndLog processCollecccion(List<String> documentosList, List<String> documentosListText,
		   HashMap<String, HashMap<String, List<HashMap<String, HashSet<String>>>>> supertabla,
		   HashMap<String, HashMap<String, HashSet<String>>> supertablaSemPos, HashMap<String, String> TablaSemanticaTexto,
		   HashMap<String, HashSet<String>> imagenes_Tabla, CompleteCollectionAndLog Salida) {
	
	CompleteCollection C=new CompleteCollection("MetaMap ", "MetaMap  collection");
	Salida.setCollection(C);
		
	HashMap<CompleteGrammar,HashMap<String, CompleteElementType>> TablaElem=new HashMap<>();
	
	//Catalogue Entry
	
	CompleteGrammar Terminos=new CompleteGrammar("Catalogue Entry", "Entry Structure",Salida.getCollection());
	C.getMetamodelGrammar().add(Terminos);
	
	
	HashMap<String, CompleteElementType> TerrminosElem= new HashMap<String, CompleteElementType>();
	
	CompleteTextElementType clinical_Term=new CompleteTextElementType("Clinical Term",Terminos);
	Terminos.getSons().add(clinical_Term);
	
	TerrminosElem.put("Clinical Term", clinical_Term);
	
	CompleteTextElementType occurrence=new CompleteTextElementType("Occurrence",Terminos);
	Terminos.getSons().add(occurrence);
	
	TerrminosElem.put("Occurrence", occurrence);
	
	CompleteTextElementType Categoria=new CompleteTextElementType("Category",Terminos);
	Terminos.getSons().add(Categoria);
	Categoria.setBrowseable(true);
	
	TerrminosElem.put("Category", Categoria);
	
	CompleteTextElementType report=new CompleteTextElementType("Report*",Terminos);
	Terminos.getSons().add(report);
	report.setMultivalued(true);
	TerrminosElem.put("Report*", report);
	
	CompleteTextElementType Words=new CompleteTextElementType("Words",Terminos);
	report.getSons().add(Words);
	
	TerrminosElem.put("Words", Words);
	
	CompleteLinkElementType report_Link=new CompleteLinkElementType("Report Link",Terminos);
	report.getSons().add(report_Link);
	
	TerrminosElem.put("Report Link", report_Link);
	
	List<CompleteTextElementType> wordsList=new ArrayList<>();
	wordsList.add(Words);
	
	List<CompleteLinkElementType> reportList=new ArrayList<>();
	reportList.add(report_Link);
	
	TablaElem.put(Terminos,TerrminosElem);
	
	
	//Tabajamos los Informes
	
	
	CompleteGrammar ReportsG=new CompleteGrammar("Reports", "Reports Clasification",Salida.getCollection());
	C.getMetamodelGrammar().add(ReportsG);
	
	HashMap<String, CompleteElementType> ReportsElem= new HashMap<String, CompleteElementType>();
	
	CompleteTextElementType Description=new CompleteTextElementType("Description",ReportsG);
	ReportsG.getSons().add(Description);
	
	ReportsElem.put("Description", Description);
	
	CompleteLinkElementType cEntry=new CompleteLinkElementType("CEntry*",Terminos);
	ReportsG.getSons().add(cEntry);
	
	ReportsElem.put("CEntry*", cEntry);
	
	
	CompleteResourceElementType image=new CompleteResourceElementType("Image*",ReportsG);
	ReportsG.getSons().add(image);
	image.setMultivalued(true);
	
	ReportsElem.put("Image*", image);
	
	List<CompleteResourceElementType> ImagenesList=new ArrayList<>();
	ImagenesList.add(image);
	
	List<CompleteLinkElementType> cEntryList=new ArrayList<>();
	cEntryList.add(cEntry);
	
	TablaElem.put(ReportsG, ReportsElem);
	
	HashMap<String, HashMap<String, CompleteDocuments>> supertablaSemPos_Doc=new HashMap<>();
	
	String DocIcon="https://www.freeiconspng.com/uploads/document-icon-10.jpg";
//	String icon="https://www.freeiconspng.com/uploads/blank-price-tag-png-11.png"; 
	String iconEntry="https://www.freeiconspng.com/uploads/blank-price-tag-png-11.png"; 
	
	for (int i = 0; i < documentosList.size(); i++) {
	String Iden = documentosList.get(i);
		HashMap<String, List<HashMap<String, HashSet<String>>>> Table = supertabla.get(Iden);
		CompleteDocuments Doc=new CompleteDocuments(C,Iden , DocIcon);
		C.getEstructuras().add(Doc);
		
		
		CompleteTextElement DESC=new CompleteTextElement(Description, documentosListText.get(i) );
		Doc.getDescription().add(DESC);
		
		if (Table!=null)
		{
			for (Entry<String, List<HashMap<String, HashSet<String>>>> semantica_word_doc : Table.entrySet()) {
				
				String categoria = semantica_word_doc.getKey();
				
				//Positivos
				for (Entry<String, HashSet<String>> completeLinkElementType : semantica_word_doc.getValue().get(0).entrySet()) {
					
					
					HashMap<String, CompleteDocuments> term_Sem=supertablaSemPos_Doc.get(categoria);
					String cEntryTerm=completeLinkElementType.getKey();
					
					if (term_Sem==null)
						{
						term_Sem=new HashMap<>();
						supertablaSemPos_Doc.put(categoria,term_Sem);
						}
						
					CompleteDocuments WordInstance=term_Sem.get(cEntryTerm);
					
					if (WordInstance==null)
						{
							WordInstance=new CompleteDocuments(C, cEntryTerm + " (+)", iconEntry);
							C.getEstructuras().add(WordInstance);
							term_Sem.put(cEntryTerm, WordInstance);
						}
					
					
					
					
					
				}
				
				//Negativos
				for (Entry<String, HashSet<String>> completeLinkElementType : semantica_word_doc.getValue().get(1).entrySet()) {
					
					
				}
			}
		}
		
		
		
	}
	
	
	
	
	
	
	
	
	
	HashMap<CompleteDocuments, Integer> actualInforme=new HashMap<>();
	
	 HashMap<String, HashMap<String, CompleteDocuments>> supertablaSemPos_Doc=new HashMap<>();
		
	String icon="https://www.freeiconspng.com/uploads/blank-price-tag-png-11.png"; 
	 
	for (Entry<String, HashMap<String, HashSet<String>>> semantica_word : supertablaSemPos.entrySet()) {
			
		
		HashMap<String, CompleteDocuments> String_doc=new HashMap<>();
		
		for (Entry<String, HashSet<String>> word_wfound : semantica_word.getValue().entrySet()) {
				CompleteDocuments WordInstance=new CompleteDocuments(C, semantica_word.getKey()+"->"+word_wfound.getKey(), icon);
				C.getEstructuras().add(WordInstance);
				
				CompleteTextElement NameI=new CompleteTextElement(occurrence, word_wfound.getKey());
				WordInstance.getDescription().add(NameI);
				
				CompleteTextElement CateI=new CompleteTextElement(report, TablaSemanticaTexto.get(semantica_word.getKey()));
				WordInstance.getDescription().add(CateI);
				
				HashSet<String> ListaW = word_wfound.getValue();
				
				List<String> ListaWW=new LinkedList<>(ListaW);
				
				while (cEntryList.size()<ListaWW.size())
					{
					CompleteTextElementType WordsI=new CompleteTextElementType("Words",Terminos);
					Terminos.getSons().add(WordsI);
					WordsI.setMultivalued(true);
					WordsI.setClassOfIterator(cEntry);
					cEntryList.add(WordsI);
					}
				
				for (int i = 0; i < ListaWW.size(); i++) {
					String string = ListaWW.get(i);
					CompleteTextElement wordI=new CompleteTextElement(cEntryList.get(i), string);
					WordInstance.getDescription().add(wordI);
					
				}
				
				actualInforme.put(WordInstance, new Integer(0));
				
				String_doc.put(word_wfound.getKey(), WordInstance);
			}
		
		supertablaSemPos_Doc.put(semantica_word.getKey(), String_doc);
	}
	
	
	CompleteLinkElementType Reports=new CompleteLinkElementType("Reports",Terminos);
	Terminos.getSons().add(Reports);
	Reports.setMultivalued(true);
	
	TerrminosElem.put("Reports", Reports);
	
	List<CompleteLinkElementType> RepordList=new ArrayList<>();
	RepordList.add(Reports);
	
	TablaElem.put(Terminos,TerrminosElem);

	
	CompleteGrammar ReportsG=new CompleteGrammar("Reports", "Reports Clasification",Salida.getCollection());
	C.getMetamodelGrammar().add(ReportsG);
	
	//Tabajamos los Informes
	
HashMap<String, CompleteElementType> ReportsElem= new HashMap<String, CompleteElementType>();
	

CompleteTextElementType CODE=new CompleteTextElementType("CODE",ReportsG);
ReportsG.getSons().add(CODE);

TerrminosElem.put("CODE", CODE);

	CompleteTextElementType Description=new CompleteTextElementType("Description",ReportsG);
	ReportsG.getSons().add(Description);
	
	ReportsElem.put("Description", Description);
	
	
	CompleteResourceElementType Images=new CompleteResourceElementType("Images",ReportsG);
	ReportsG.getSons().add(Images);
	Images.setMultivalued(true);
	
	ReportsElem.put("Images", Images);
	
	List<CompleteResourceElementType> ImagenesList=new ArrayList<>();
	ImagenesList.add(Images);
	
	
	TablaElem.put(ReportsG, ReportsElem);
	
	
	
	String DocIcon="https://www.freeiconspng.com/uploads/document-icon-10.jpg";
	
	for (int i = 0; i < documentosList.size(); i++) {
	String Iden = documentosList.get(i);
		HashMap<String, List<HashMap<String, HashSet<String>>>> Table = supertabla.get(Iden);
		CompleteDocuments Doc=new CompleteDocuments(C,documentosListText.get(i) , DocIcon);
		C.getEstructuras().add(Doc);
		
		CompleteTextElement IDEN=new CompleteTextElement(CODE, Iden);
		Doc.getDescription().add(IDEN);
		
		CompleteTextElement DESC=new CompleteTextElement(Description, documentosListText.get(i) );
		Doc.getDescription().add(DESC);
		
		if (Table!=null)
		
		for (Entry<String, List<HashMap<String, HashSet<String>>>> semantica_word_doc : Table.entrySet()) {
			//TODO Es el 1 o el 2?
			for (Entry<String, HashSet<String>> completeLinkElementType : semantica_word_doc.getValue().get(0).entrySet()) {
					CompleteDocuments WordS=supertablaSemPos_Doc.get(semantica_word_doc.getKey()).get(completeLinkElementType.getKey());
					Integer Informes_Doc = actualInforme.get(WordS);
					
					if (Informes_Doc==null)
						System.out.println("AA");
					
					while (RepordList.size()<=Informes_Doc)
					{
						
					CompleteLinkElementType ReportsI=new CompleteLinkElementType("Reports",Terminos);
					Terminos.getSons().add(ReportsI);
					ReportsI.setMultivalued(true);
					ReportsI.setClassOfIterator(Reports);
					RepordList.add(ReportsI);
					
					}
					
					CompleteLinkElement CLE=new CompleteLinkElement(RepordList.get(Informes_Doc), Doc);
					WordS.getDescription().add(CLE);
					
					actualInforme.put(WordS, new Integer(Informes_Doc.intValue()+1));
					
			}
		}
		
		
		
		HashSet<String> imagenesLinks = new HashSet<String>(imagenes_Tabla.get(Iden)) ;
		
		if (imagenesLinks != null)
			{
			
			LinkedList<String> imagenesLinksList = new LinkedList<String>(imagenesLinks);
			
			while (ImagenesList.size()<imagenesLinksList.size())
			{
		
				CompleteResourceElementType ImagesI=new CompleteResourceElementType("Images",ReportsG);
				ReportsG.getSons().add(ImagesI);
				ImagesI.setMultivalued(true);
				ImagesI.setClassOfIterator(Images);
				ImagenesList.add(ImagesI);
			}
			
			
			for (int j = 0; j < imagenesLinks.size(); j++) {
				CompleteResourceElementURL CLE=new CompleteResourceElementURL(ImagenesList.get(j), imagenesLinksList.get(j));
				Doc.getDescription().add(CLE);
			}
			
			
			
			}
			
		
		
		//AQUI QUEdan las imagenes
	}
	
	return Salida;
}




	@Override
	public String getName() {
		return "UMLS XML Import V1";
	}


	

	
}
