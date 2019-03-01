/**
 * 
 */
package fdi.ucm.server.importparser.umls.exp;

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
public class LoadCollectionUMLSV3_0 extends LoadCollectionUMLSV3{

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoadCollectionUMLSV3 LC=new LoadCollectionUMLSV3_0();
		LoadCollectionUMLSV3.consoleDebug=true;
		
		ArrayList<String> AA=new ArrayList<String>();
		
		CompleteCollectionAndLog Salida=null;
		

	
		AA.add("sample.txt");
		AA.add("salida.xml");
		AA.add("terminos_filtrados.txt");
		AA.add("openi_nlm_nih_gov.json");
		AA.add("reducido.csv");
	
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

	
	
	

	
	
	protected CompleteCollectionAndLog processCollecccion(List<String> documentosList, HashMap<String,String> documentosListText,
		   HashMap<String, HashMap<String, List<HashMap<String, HashSet<String>>>>> supertabla,
		   HashMap<String, HashMap<String, HashSet<String>>> supertablaSemPos, HashMap<String, String> TablaSemanticaTexto,
		   HashMap<String, HashSet<String>> imagenes_Tabla, CompleteCollectionAndLog Salida,
		   HashMap<String, HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>>> SupertablaUtt,
			HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>> Utter_Text,
			HashMap<String, List<String>> Utter_do) {
	
	CompleteCollection C=new CompleteCollection("MetaMap ", "MetaMap  collection");
	Salida.setCollection(C);
		
	HashMap<CompleteGrammar,HashMap<String, CompleteElementType>> TablaElem=new HashMap<>();
	HashMap<CompleteTextElementType, List<CompleteTextElementType>> Report_Words = new HashMap<>();
	
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
	
	CompleteTextElementType Words=new CompleteTextElementType("Words",report,Terminos);
	//report.getSons().add(Words);
	Words.setMultivalued(true);
	
	LinkedList<CompleteTextElementType> Words_rep=new LinkedList<>();
	Words_rep.add(Words);
	
	Report_Words.put(report, Words_rep);
	
	TerrminosElem.put("Words", Words);
	
	CompleteLinkElementType report_Link=new CompleteLinkElementType("Report Link",report,Terminos);
	report.getSons().add(report_Link);
	
	TerrminosElem.put("Report Link", report_Link);
	
	List<CompleteTextElementType> reportList=new ArrayList<>();
	reportList.add(report);
	
	TablaElem.put(Terminos,TerrminosElem);
	
	//TABAJAMOS en utterancias
	
	HashMap<String, CompleteElementType> UtteElem= new HashMap<String, CompleteElementType>();
	
	
	CompleteGrammar Uterancia=new CompleteGrammar("Utterances", "Utterance Structure",Salida.getCollection());
	C.getMetamodelGrammar().add(Uterancia);
	
	//Lo bueno es que lo puedo calcular
	
	int numero_terminos=1;
	
	for (Entry<String, HashMap<String, HashMap<String, HashSet<String>>>> uterance_seman : Utter_Text.entrySet()) {
		int acumulativo=0;
		for (Entry<String, HashMap<String, HashSet<String>>> seman_terms : uterance_seman.getValue().entrySet())
			acumulativo=acumulativo+seman_terms.getValue().keySet().size();
		
		if (acumulativo>numero_terminos)
			numero_terminos=acumulativo;
	}
	
	
	int numero_docs=1;
	
	HashMap<String, List<String>> Utter_do_inver=new HashMap<>();
	for (Entry<String, List<String>> do_to_utter : Utter_do.entrySet()) {
		for (String utter : do_to_utter.getValue()) {
			List<String> listDo = Utter_do_inver.get(utter);
			if (listDo==null)
				listDo=new LinkedList<>();
			listDo.add(do_to_utter.getKey());
			Utter_do_inver.put(utter, listDo);
		}
		
	}
	
	for (Entry<String, List<String>> utter_do : Utter_do_inver.entrySet()) 
		if (utter_do.getValue().size()>numero_docs)
			numero_docs=utter_do.getValue().size();
	
	
	List<CompleteLinkElementType> termUttList=new LinkedList<>();
	CompleteLinkElementType term_lin=new CompleteLinkElementType("Entry*",Terminos);
	Uterancia.getSons().add(term_lin);
	term_lin.setMultivalued(true);
	UtteElem.put("Entry*", term_lin);
	termUttList.add(term_lin);
	
	for (int i = 1; i < numero_terminos; i++) {
		CompleteLinkElementType term_lin2=new CompleteLinkElementType("Entry*",Terminos);
		Uterancia.getSons().add(term_lin2);
		term_lin2.setMultivalued(true);
		term_lin2.setClassOfIterator(term_lin);
		termUttList.add(term_lin2);
	}
	
	
	List<CompleteLinkElementType> docUttList=new LinkedList<>();
	CompleteLinkElementType doc_lin=new CompleteLinkElementType("Report*",Terminos);
	Uterancia.getSons().add(doc_lin);
	doc_lin.setMultivalued(true);
	UtteElem.put("Report*", doc_lin);
	docUttList.add(doc_lin);
	
	for (int i = 1; i < numero_terminos; i++) {
		CompleteLinkElementType doc_lin2=new CompleteLinkElementType("Report*",Terminos);
		Uterancia.getSons().add(doc_lin2);
		doc_lin2.setMultivalued(true);
		doc_lin2.setClassOfIterator(doc_lin);
		docUttList.add(doc_lin2);
	}
	
	//FALTA LA PARTE DE LOS DOCUMENTOS QUE ES IGUAL QUE LO DE ARRIBA
	
	//Tabajamos los Informes
	
	
	CompleteGrammar ReportsG=new CompleteGrammar("Reports", "Reports Clasification",Salida.getCollection());
	C.getMetamodelGrammar().add(ReportsG);
	
	HashMap<String, CompleteElementType> ReportsElem= new HashMap<String, CompleteElementType>();
	
	CompleteTextElementType Description=new CompleteTextElementType("Description",ReportsG);
	ReportsG.getSons().add(Description);
	
	ReportsElem.put("Description", Description);
	
	CompleteLinkElementType cEntry=new CompleteLinkElementType("CEntry*",Terminos);
	cEntry.setMultivalued(true);
	ReportsElem.put("CEntry*", cEntry);
	
	
	CompleteResourceElementType image=new CompleteResourceElementType("Images*",ReportsG);
	image.setMultivalued(true);
	
	ReportsElem.put("Images*", image);
	
	List<CompleteResourceElementType> ImagenesList=new ArrayList<>();
	ImagenesList.add(image);
	
	List<CompleteLinkElementType> cEntryList=new ArrayList<>();
	cEntryList.add(cEntry);
	
	TablaElem.put(ReportsG, ReportsElem);
	
	HashMap<String, HashMap<String, CompleteDocuments>> supertablaSemPos_Doc=new HashMap<>();
	HashMap<String, HashMap<String, CompleteDocuments>> supertablaSemNeg_Doc=new HashMap<>();
	
	
	
	String DocIcon="https://www.freeiconspng.com/uploads/document-icon-10.jpg";
//	String icon="https://www.freeiconspng.com/uploads/blank-price-tag-png-11.png"; 
	String iconEntry="https://www.freeiconspng.com/uploads/blank-price-tag-png-11.png"; 
	
	HashMap<CompleteDocuments, Integer> entry_docum_cnt = new HashMap<>();
	HashMap<CompleteDocuments, Integer> docum_entry_cnt = new HashMap<>();
	
	
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
				
				for (int j = 0; j < semantica_word_doc.getValue().size(); j++) {
					for (Entry<String, HashSet<String>> completeLinkElementType : semantica_word_doc.getValue().get(j).entrySet()) {
						
						
						HashMap<String, CompleteDocuments> term_Sem;
						String cEntryTerm;
						
						if (j==0)
						{
							term_Sem=supertablaSemPos_Doc.get(categoria);
							cEntryTerm=completeLinkElementType.getKey();
							
							if (term_Sem==null)
								{
								term_Sem=new HashMap<>();
								supertablaSemPos_Doc.put(categoria,term_Sem);
								}
						
						}else
						{
							term_Sem=supertablaSemNeg_Doc.get(categoria);
							cEntryTerm=completeLinkElementType.getKey();
							
							if (term_Sem==null)
								{
								term_Sem=new HashMap<>();
								supertablaSemNeg_Doc.put(categoria,term_Sem);
								}
						}
						
						CompleteDocuments WordInstance=term_Sem.get(cEntryTerm);
						
						if (WordInstance==null)
							{
							
								String ocurrenceValue="";
								
								if (j==0)
									ocurrenceValue="(+)";
								else
									ocurrenceValue="(-)";
									
								WordInstance=new CompleteDocuments(C, cEntryTerm + " "+ocurrenceValue, iconEntry);
								C.getEstructuras().add(WordInstance);
								term_Sem.put(cEntryTerm, WordInstance);
								
								CompleteTextElement clinical_Term_value=new CompleteTextElement(clinical_Term, cEntryTerm );
								WordInstance.getDescription().add(clinical_Term_value);
								
								CompleteTextElement ocurrence_value=new CompleteTextElement(occurrence, ocurrenceValue );
								WordInstance.getDescription().add(ocurrence_value);
								
								CompleteTextElement categoria_value=new CompleteTextElement(Categoria, TablaSemanticaTexto.get(categoria) );
								WordInstance.getDescription().add(categoria_value);
								
							}
						
						if (j==0)
							supertablaSemPos_Doc.put(categoria,term_Sem);
						else
							supertablaSemNeg_Doc.put(categoria,term_Sem);
						//TPDP
						
						Integer actualDocsAsoc=entry_docum_cnt.get(WordInstance);
						
						if (actualDocsAsoc==null)
							actualDocsAsoc=new Integer(-1);
						
						actualDocsAsoc=new Integer(actualDocsAsoc.intValue()+1);
						
						entry_docum_cnt.put(WordInstance, actualDocsAsoc);
						
						if (reportList.size()<=actualDocsAsoc)
						{
							CompleteTextElementType report_new=new CompleteTextElementType("Report*",Terminos);
//							Terminos.getSons().add(report_new);
							report_new.setMultivalued(true);
							report_new.setClassOfIterator(report);
							reportList.add(report_new);
							
							CompleteTextElementType Words_new=new CompleteTextElementType("Words",report_new,Terminos);
//							report_new.getSons().add(Words_new);
							Words_new.setClassOfIterator(Words);
							Words_new.setMultivalued(true);

							LinkedList<CompleteTextElementType> Words_rep2=new LinkedList<>();
							Words_rep2.add(Words_new);
							
							Report_Words.put(report_new, Words_rep2);
							
							CompleteLinkElementType report_Link_new=new CompleteLinkElementType("Report Link",report_new,Terminos);
							report_new.getSons().add(report_Link_new);
							report_Link_new.setClassOfIterator(report_Link);
							
						}
						
						CompleteTextElementType reportAc=reportList.get(actualDocsAsoc);
						
						List<CompleteTextElementType> lista_wordT = Report_Words.get(reportAc);
						
						HashSet<String> ListWords_value = completeLinkElementType.getValue();
						
						if (lista_wordT.size()<=ListWords_value.size())
						{
							CompleteTextElementType Words_new=new CompleteTextElementType("Words",reportAc,Terminos);
//							report_new.getSons().add(Words_new);
							Words_new.setClassOfIterator(Words);
							Words_new.setMultivalued(true);
							lista_wordT.add(Words_new);
						}
						
						Report_Words.put(reportAc, lista_wordT);

						int l = 0;
						for (String string : ListWords_value) {
							CompleteTextElement wordsa=new CompleteTextElement(lista_wordT.get(l),
									string);
							l++;
							WordInstance.getDescription().add(wordsa);
						}
						
						
						
						CompleteLinkElement linksRef=new CompleteLinkElement((CompleteLinkElementType)reportAc.getSons().get(0),Doc );
						WordInstance.getDescription().add(linksRef);
						
						Integer actualEntryAsoc=docum_entry_cnt.get(Doc);
						
						if (actualEntryAsoc==null)
							actualEntryAsoc=new Integer(-1);
						
						actualEntryAsoc=new Integer(actualEntryAsoc.intValue()+1);
						
						docum_entry_cnt.put(Doc, actualEntryAsoc);
						
						if (cEntryList.size()<=actualEntryAsoc)
						{
							CompleteLinkElementType cEntry_new=new CompleteLinkElementType("CEntry*",Terminos);
							cEntry_new.setMultivalued(true);
							cEntry_new.setClassOfIterator(cEntry);
							cEntryList.add(cEntry_new);
						}
						
						CompleteLinkElementType cEntryListElem=cEntryList.get(actualEntryAsoc);
						
						CompleteLinkElement linksCentry=new CompleteLinkElement(cEntryListElem,WordInstance );
						Doc.getDescription().add(linksCentry);
						
						
					}
				}
				
			}
		}
		
		if (imagenes_Tabla.get(Iden) != null)
		{
		
		HashSet<String> imagenesLinks = new HashSet<String>(imagenes_Tabla.get(Iden)) ;
		
		
			
			LinkedList<String> imagenesLinksList = new LinkedList<String>(imagenesLinks);
			
			while (ImagenesList.size()<imagenesLinksList.size())
			{
		
				CompleteResourceElementType ImagesI=new CompleteResourceElementType("Images*",ReportsG);
				ImagesI.setMultivalued(true);
				ImagesI.setClassOfIterator(image);
				ImagenesList.add(ImagesI);
			}
			
			
			for (int j = 0; j < imagenesLinks.size(); j++) {
				CompleteResourceElementURL CLE=new CompleteResourceElementURL(ImagenesList.get(j), imagenesLinksList.get(j));
				Doc.getDescription().add(CLE);
			}
			
			
			
			}
		
		
	}
	
	for (CompleteTextElementType report_new : reportList) {
		Terminos.getSons().add(report_new);
	}
	
	
	for (CompleteLinkElementType cEntryele : cEntryList) {
		ReportsG.getSons().add(cEntryele);
	}
	
	for (CompleteResourceElementType imageele : ImagenesList) {
		ReportsG.getSons().add(imageele);
	}
	
	
	for (Entry<CompleteTextElementType, List<CompleteTextElementType>> report_listW : Report_Words.entrySet()) {
		
		for (CompleteTextElementType worsd_for_report : report_listW.getValue()) {
			report_listW.getKey().getSons().add(worsd_for_report);
		}
		
	}
	
	
	
		
	return Salida;
}




	@Override
	public String getName() {
		return "UMLS Import V1";
	}


	

	
}
