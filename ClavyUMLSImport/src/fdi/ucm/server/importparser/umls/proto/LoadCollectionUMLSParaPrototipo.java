/**
 * 
 */
package fdi.ucm.server.importparser.umls.proto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollectionAndLog;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteOperationalValueType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

/**
 * @author Joaquin Gayoso Cabada
 *
 */
public class LoadCollectionUMLSParaPrototipo extends LoadCollectionUMLSVProtoBase{

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoadCollectionUMLSVProtoBase LC=new LoadCollectionUMLSParaPrototipo();
		LoadCollectionUMLSVProtoBase.consoleDebug=true;
		
		ArrayList<String> AA=new ArrayList<String>();
		
		CompleteCollectionAndLog Salida=null;
		

	
		String Carpeta="dos";
		if (args.length>0)
			Carpeta=args[0];
		if (!Carpeta.isEmpty()&&!Carpeta.endsWith(File.separator))	
			Carpeta=Carpeta+File.separator;
			
	
		AA.add("dos/sample.txt");
		AA.add("dos/salida.xml");
		AA.add("dos/terminos_filtrados.txt");
		AA.add("openi_nlm_nih_gov.json");
		AA.add("reducido.csv");
	
	
			 Salida=LC.processCollecccion(AA);
	
			
		
		if (Salida!=null)
			{
			
			System.out.println("Correcto");
			
			for (String warning : Salida.getLogLines())
				System.err.println(warning);

			
			try {
				String nombre=System.currentTimeMillis()+".clavy";
				
				System.out.println(nombre);
				
				String FileIO = nombre;
				
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
		   HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> supertablaUtt_list,
		   HashMap<String,HashMap<String,String>> Sem_Term_CUI) {
	
	CompleteCollection C=new CompleteCollection("MetaMap ", "MetaMap  collection");
	Salida.setCollection(C);
	
	CompleteGrammar Report=new CompleteGrammar("Report", "Report Data CARS",Salida.getCollection());
	C.getMetamodelGrammar().add(Report);
	
	
	//CONTEOTERMINOS POR DOCUMENTO
	
		int numero_terminos=1;
		
		for (Entry<String, HashMap<String, List<HashMap<String, HashSet<String>>>>> uterance_seman : supertabla.entrySet()) {
			HashSet<String> TermninosDoc=new HashSet<String>();
			for (Entry<String, List<HashMap<String, HashSet<String>>>> seman_terms : uterance_seman.getValue().entrySet())
				for (HashMap<String, HashSet<String>> string : seman_terms.getValue()) {
					for (String string2 : string.keySet()) {
						TermninosDoc.add(string2);
					}
				}
			
			if (TermninosDoc.size()>numero_terminos)
				numero_terminos=TermninosDoc.size();
		}
		
		
		int numero_terminos_posicion=1;
		int numero_terminos_seman=1;

		
		for (Entry<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> uterance_seman : supertablaUtt_list.entrySet()) { 
			HashMap<String, HashSet<String>> term_wordsco=new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> term_seman=new  HashMap<String, HashSet<String>>();
			for (Entry<String, HashMap<String, HashMap<String, HashSet<String>>>> seman_terms : uterance_seman.getValue().entrySet()) {
				for (Entry<String, HashMap<String, HashSet<String>>> seman_terms2 : seman_terms.getValue().entrySet()) 
					for (Entry<String, HashSet<String>> string : seman_terms2.getValue().entrySet()) {
						HashSet<String> valorcom=term_wordsco.get(string.getKey());
						if (valorcom==null)
							valorcom=new HashSet<String>();
						valorcom.addAll(string.getValue());
						term_wordsco.put(string.getKey(), valorcom);
						
						HashSet<String> valorcomsem=term_seman.get(string.getKey());
						
						if (valorcomsem==null)
							valorcomsem=new HashSet<String>();
						valorcomsem.add(seman_terms2.getKey());
						term_seman.put(string.getKey(), valorcomsem);
						
					}
				

					
				}
			
			
			
			for (Entry<String, HashSet<String>> interco : term_wordsco.entrySet()) {
				if (interco.getValue().size()>numero_terminos_posicion)
					numero_terminos_posicion=interco.getValue().size();
			}
			
			for (Entry<String, HashSet<String>> interco : term_seman.entrySet()) {
				if (interco.getValue().size()>numero_terminos_seman)
					numero_terminos_seman=interco.getValue().size();
			}
			
				

			
		}
		
		int numero_uters=1;
		
		for (Entry<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> uterance_seman : SupertablaUtt.entrySet()) {

			
			if (uterance_seman.getValue().keySet().size()>numero_uters)
				numero_uters=uterance_seman.getValue().keySet().size();
		}
		
		
		int numero_image=1;
		
		LinkedList<String> valore = new LinkedList<String>(imagenes_Tabla.keySet());
		
		for (int i = 0; i < valore.size(); i++) {
			ArrayList<String> tablaaa = new ArrayList<String>(imagenes_Tabla.get(valore.get(i)));

			if (tablaaa.size()>numero_image)
				numero_image=tablaaa.size();
		}
	
	List<CompleteTextElementType> clinical_TermList=new LinkedList<CompleteTextElementType>();
		
	CompleteTextElementType clinical_Term=new CompleteTextElementType("Clinical Term",Report);
	clinical_Term.setMultivalued(true);
	Report.getSons().add(clinical_Term);
	clinical_TermList.add(clinical_Term);
	
	for (int i = 0; i < numero_terminos; i++) {
		CompleteTextElementType clinical_TermH=new CompleteTextElementType("Clinical Term",Report);
		clinical_TermH.setMultivalued(true);
		clinical_TermH.setClassOfIterator(clinical_Term);
		Report.getSons().add(clinical_TermH);
		clinical_TermList.add(clinical_TermH);
	}
	
	for (CompleteTextElementType clinical_TermH : clinical_TermList) {
		clinical_TermH.getShows().add(new CompleteOperationalValueType("editor", "proto", "clavy"));
		clinical_TermH.getShows().add(new CompleteOperationalValueType("type", "term", "proto"));
		clinical_TermH.getShows().add(new CompleteOperationalValueType("source", "auto","proto" ));
	}
	
	List<CompleteTextElementType> utterancesList=new LinkedList<CompleteTextElementType>();
	
	CompleteTextElementType utterances=new CompleteTextElementType("Utterances",Report);
	utterances.setMultivalued(true);
	Report.getSons().add(utterances);
	utterances.getShows().add(new CompleteOperationalValueType("type", "utterance","proto" ));
	utterancesList.add(utterances);
	
	
	for (int i = 0; i < numero_uters; i++) {
		CompleteTextElementType utterancesH=new CompleteTextElementType("Utterances",Report);
		utterancesH.setMultivalued(true);
		utterancesH.setClassOfIterator(utterances);
		Report.getSons().add(utterancesH);
		utterancesList.add(utterancesH);
		utterancesH.getShows().add(new CompleteOperationalValueType("type", "utterance","proto" ));
	}
	
	
	List<CompleteResourceElementType> imagesList=new LinkedList<CompleteResourceElementType>();

	
	CompleteResourceElementType images=new CompleteResourceElementType("images",Report);
	images.setMultivalued(true);
	Report.getSons().add(images);
	
	images.getShows().add(new CompleteOperationalValueType("type", "image","proto" ));
	imagesList.add(images);
	
	for (int i = 0; i < numero_image; i++) {
		CompleteResourceElementType imagesH=new CompleteResourceElementType("images",Report);
		imagesH.setMultivalued(true);
		imagesH.setClassOfIterator(images);
		Report.getSons().add(imagesH);
		imagesList.add(imagesH);
		imagesH.getShows().add(new CompleteOperationalValueType("type", "image","proto" ));
	}
	
	
	CompleteElementType del=new CompleteElementType("delete",Report);
	del.setSelectable(true);
	Report.getSons().add(del);
	
	del.getShows().add(new CompleteOperationalValueType("type", "delete","proto" ));
	
	CompleteTextElementType Annotation=new CompleteTextElementType("Annotation",Report);
	Report.getSons().add( Annotation);
	
	Annotation.getShows().add(new CompleteOperationalValueType("type", "annotation","proto" ));
	

	HashMap<CompleteTextElementType,List<CompleteTextElementType>> PositionListHash=new HashMap<CompleteTextElementType,List<CompleteTextElementType>>();
	
	List<CompleteTextElementType> PositionList=new LinkedList<CompleteTextElementType>();
	
	CompleteTextElementType Position=new CompleteTextElementType("Position",clinical_Term,Report);
	Position.setMultivalued(true);
	clinical_Term.getSons().add(Position);
	
	Position.getShows().add(new CompleteOperationalValueType("type", "position","proto" ));
	
	PositionList.add(Position);
	
	for (int i = 0; i < numero_terminos_posicion-1; i++) {
		CompleteTextElementType PositionH=new CompleteTextElementType("Position",clinical_Term,Report);
		PositionH.setMultivalued(true);
		clinical_Term.getSons().add(PositionH);
		PositionH.setClassOfIterator(Position);
		PositionH.getShows().add(new CompleteOperationalValueType("type", "position","proto" ));
		PositionList.add(PositionH);
	}
	
	PositionListHash.put(clinical_Term, PositionList);
	
	
HashMap<CompleteTextElementType,List<CompleteTextElementType>> SemanticHash=new HashMap<CompleteTextElementType,List<CompleteTextElementType>>();
	
	List<CompleteTextElementType> SemanticList=new LinkedList<CompleteTextElementType>();
	
	CompleteTextElementType Semantic=new CompleteTextElementType("Semantic",clinical_Term,Report);
	Semantic.setMultivalued(true);
	clinical_Term.getSons().add(Semantic);
	
	Semantic.getShows().add(new CompleteOperationalValueType("type", "semantic","proto" ));
	
	SemanticList.add(Semantic);
	
	for (int i = 0; i < numero_terminos_seman-1; i++) {
		CompleteTextElementType SemanticH=new CompleteTextElementType("Semantic",clinical_Term,Report);
		SemanticH.setMultivalued(true);
		clinical_Term.getSons().add(SemanticH);
		SemanticH.setClassOfIterator(Semantic);
		SemanticH.getShows().add(new CompleteOperationalValueType("type", "semantic","proto" ));
		SemanticList.add(SemanticH);
	}
	
	SemanticHash.put(clinical_Term, SemanticList);
	
	
	HashMap<CompleteTextElementType,CompleteElementType> ty_delHash=new HashMap<CompleteTextElementType,CompleteElementType>();
	
	CompleteElementType ty_del=new CompleteElementType("delete",clinical_Term,Report);
	ty_del.setSelectable(true);
	clinical_Term.getSons().add(ty_del);
	
	ty_del.getShows().add(new CompleteOperationalValueType("type", "delete","proto" ));
	
	ty_delHash.put(clinical_Term, ty_del);
	
	
	HashMap<CompleteTextElementType,CompleteTextElementType> CUIHash=new HashMap<CompleteTextElementType,CompleteTextElementType>();
	
	CompleteTextElementType CUI=new CompleteTextElementType("cui",clinical_Term,Report);
	clinical_Term.getSons().add( CUI);
	
	CUI.getShows().add(new CompleteOperationalValueType("type", "cui","proto" ));
	
	CUIHash.put(clinical_Term, CUI);
	
	
	
	
	for (CompleteTextElementType ceteClini : clinical_TermList) 
		if (ceteClini!=clinical_Term)
		{
			
			List<CompleteTextElementType> ceteCliniList=new LinkedList<CompleteTextElementType>();
			
			for (int i = 0; i < numero_terminos_posicion; i++) {
				CompleteTextElementType PositionH=new CompleteTextElementType("Position",ceteClini,Report);
				PositionH.setMultivalued(true);
				ceteClini.getSons().add(PositionH);
				PositionH.setClassOfIterator(Position);
				PositionH.getShows().add(new CompleteOperationalValueType("type", "position","proto" ));
				ceteCliniList.add(PositionH);
			}
			
			PositionListHash.put(ceteClini, ceteCliniList);
			
			
			List<CompleteTextElementType> SemanticListList=new LinkedList<CompleteTextElementType>();
			
			for (int i = 0; i < numero_terminos_seman; i++) {
				CompleteTextElementType SemanticH=new CompleteTextElementType("Semantic",ceteClini,Report);
				SemanticH.setMultivalued(true);
				ceteClini.getSons().add(SemanticH);
				SemanticH.setClassOfIterator(Semantic);
				SemanticH.getShows().add(new CompleteOperationalValueType( "type", "semantic","proto" ));
				SemanticListList.add(SemanticH);
			}
			
			SemanticHash.put(ceteClini, SemanticListList);
			
			
			CompleteElementType ty_delH=new CompleteElementType("delete",ceteClini,Report);
			ty_delH.setSelectable(true);
			ceteClini.getSons().add(ty_delH);
			ty_delH.setClassOfIterator(ty_del);
			
			ty_delH.getShows().add(new CompleteOperationalValueType("type", "delete","proto" ));
			
			ty_delHash.put(ceteClini, ty_del);
			
			
			CompleteTextElementType CUIH=new CompleteTextElementType("cui",ceteClini,Report);
			ceteClini.getSons().add( CUIH);
			
			CUIH.setClassOfIterator(CUI);
			
			CUIH.getShows().add(new CompleteOperationalValueType("type", "cui","proto" ));
			
			CUIHash.put(ceteClini, CUIH);
		}
	
	String DocIcon="https://www.freeiconspng.com/uploads/document-icon-10.jpg";
//	String icon="https://www.freeiconspng.com/uploads/blank-price-tag-png-11.png"; 
//	String iconEntry="https://www.freeiconspng.com/uploads/blank-price-tag-png-11.png"; 
	
	HashMap<String, CompleteDocuments> iden_docum = new HashMap<>();
	HashMap<String, String> term_cui=new HashMap<String, String>();
	
	for (Entry<String, HashMap<String, String>> hashMap : Sem_Term_CUI.entrySet()) 
		for (Entry<String, String> hashMap2 : hashMap.getValue().entrySet()) 
			term_cui.put(hashMap2.getKey(), hashMap2.getValue());
	
	
	//DOCUMENTOS Y TERMINOS
	for (int i = 0; i < documentosList.size(); i++) {
	String Iden = documentosList.get(i);
		HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>> TableH = supertablaUtt_list.get(Iden);

		String[] utte=documentosListText.get(Iden).split("\\.");
		
		StringBuffer Desc=new StringBuffer();
		
		Desc.append(Iden);
		Desc.append(" - ");
		
		CompleteDocuments Doc=new CompleteDocuments(C,Desc.toString() , DocIcon);
		C.getEstructuras().add(Doc);
		iden_docum.put(Iden, Doc);
		
		HashMap<String, Integer> variacion=new HashMap<String, Integer>();
		
		int acumulado = 0;
		for (String string : utte) {
			variacion.put((string+".").trim(), new Integer(acumulado));
			String copia=new String(string);
			copia=copia.replace(",", "");
			copia=copia.replace(".", "");
			copia=copia.replace("  ", " ");
			String[] conteo = copia.split(" ");
			int conteoI = 0;
			for (String string2 : conteo) 
				if (!string2.trim().isEmpty())
					conteoI++;
			acumulado=acumulado+conteoI;
		}
		
		LinkedList<String> imagenes = new LinkedList<String>(imagenes_Tabla.get(Iden));
		
		if (imagenes!=null)
		{
		int contadorima = 0;
		for (String urlstring : imagenes) {
			CompleteResourceElementType imageType = imagesList.get(contadorima);
			CompleteResourceElementURL CRU=new CompleteResourceElementURL(imageType,  urlstring);
			Doc.getDescription().add(CRU);
			contadorima++;
		}
		}
		
		if (TableH!=null)
		{
			int cont=0;
			for (String uterancia : utte) {
				Desc.append(uterancia+".");
				CompleteTextElementType type = utterancesList.get(cont);
				CompleteTextElement CTU=new CompleteTextElement(type, uterancia+".");
				Doc.getDescription().add(CTU);
				cont++;
			}
			
			
			
			Doc.setDescriptionText(Desc.toString());
		}
		
		

		
			HashMap<String, HashSet<String>> term_wordsco=new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> term_seman=new  HashMap<String, HashSet<String>>();
			for (Entry<String, HashMap<String, HashMap<String, HashSet<String>>>> seman_terms : TableH.entrySet()) {
				for (Entry<String, HashMap<String, HashSet<String>>> seman_terms2 : seman_terms.getValue().entrySet()) 
					for (Entry<String, HashSet<String>> string : seman_terms2.getValue().entrySet()) {
						String completo=seman_terms.getKey().trim();
						if (!seman_terms.getKey().trim().endsWith("."))
							completo=completo+".";
						
						HashSet<String> valorcom=term_wordsco.get(string.getKey());
						if (valorcom==null)
							valorcom=new HashSet<String>();
						
						HashSet<String> variados=new HashSet<String>();
						for (String string2 : string.getValue()) {
							Integer varia=variacion.get(completo);
							if (varia==null)
							{
								Salida.getLogLines().add("Error, not found \""+ seman_terms.getKey() + "\" in "+ Arrays.toString(variacion.keySet().toArray()));
								varia=0;
								
							}
							variados.add(Integer.toString((Integer.parseInt(string2))+varia));
						}
						
						valorcom.addAll(variados);
						term_wordsco.put(string.getKey(), valorcom);
						
						HashSet<String> valorcomsem=term_seman.get(string.getKey());
						
						if (valorcomsem==null)
							valorcomsem=new HashSet<String>();
						valorcomsem.add(seman_terms2.getKey());
						term_seman.put(string.getKey(), valorcomsem);
						
					}
				

					
				}
			

			
			List<String> terms=new LinkedList<String>(term_wordsco.keySet());
			
			for (int j = 0; j < terms.size(); j++) {
				String Term=terms.get(j);
				CompleteTextElementType clinical_TermPos = clinical_TermList.get(j);
				
				HashSet<String> listaPos = term_wordsco.get(Term);
				if (listaPos==null)
					listaPos=new HashSet<String>();
				LinkedList<String> listaPosR = new LinkedList<String>(listaPos);
				
				HashSet<String> listaSem = term_seman.get(Term);
				if (listaSem==null)
					listaSem=new HashSet<String>();
				LinkedList<String> listaSemR = new LinkedList<String>(listaSem);
				
				
				List<CompleteTextElementType> listaPosCT = PositionListHash.get(clinical_TermPos);
				List<CompleteTextElementType> listaSemCT = SemanticHash.get(clinical_TermPos);
				CompleteTextElementType cuiT = CUIHash.get(clinical_TermPos);
				
				CompleteTextElement CTETErm=new CompleteTextElement(clinical_TermPos, Term);
				Doc.getDescription().add(CTETErm);
				
				for (int k = 0; k < listaPosR.size(); k++) {
						String position=listaPosR.get(k);
						CompleteTextElementType positioType=listaPosCT.get(k);
						CompleteTextElement CTEPos=new CompleteTextElement(positioType,position);
						Doc.getDescription().add(CTEPos);

					}

					for (int k = 0; k < listaSemR.size(); k++) {
						String semantica=listaSemR.get(k);
						CompleteTextElementType semanType=listaSemCT.get(k);
						CompleteTextElement CTESeman=new CompleteTextElement(semanType,semantica);
						Doc.getDescription().add(CTESeman);

					}
				
				String CuiVAlue = term_cui.get(Term);	
				if (CuiVAlue!=null)
				{
					CompleteTextElement CTECui=new CompleteTextElement(cuiT,CuiVAlue);
					Doc.getDescription().add(CTECui);
					
				}
				
			}
			
			
		
	}
	
	
//	//Por aqui
//	
//	
//	
//	
//	HashMap<CompleteGrammar,HashMap<String, CompleteElementType>> TablaElem=new HashMap<>();
//	HashMap<CompleteTextElementType, List<CompleteTextElementType>> Report_Words = new HashMap<>();
//	
//	//Catalogue Entry
//	
//	CompleteGrammar Terminos=new CompleteGrammar("Catalogue Entry", "Entry Structure",Salida.getCollection());
//	C.getMetamodelGrammar().add(Terminos);
//	
//	
//	HashMap<String, CompleteElementType> TerrminosElem= new HashMap<String, CompleteElementType>();
//	
//	CompleteTextElementType clinical_Term=new CompleteTextElementType("Clinical Term",Terminos);
//	Terminos.getSons().add(clinical_Term);
//	
//	TerrminosElem.put("Clinical Term", clinical_Term);
//	
//	CompleteTextElementType occurrence=new CompleteTextElementType("Occurrence",Terminos);
//	Terminos.getSons().add(occurrence);
//	
//	TerrminosElem.put("Occurrence", occurrence);
//	
//	CompleteTextElementType Categoria=new CompleteTextElementType("Category",Terminos);
//	Terminos.getSons().add(Categoria);
//	Categoria.setBrowseable(true);
//	
//	TerrminosElem.put("Category", Categoria);
//	
//	CompleteTextElementType report=new CompleteTextElementType("Report*",Terminos);
//	Terminos.getSons().add(report);
//	report.setMultivalued(true);
//	TerrminosElem.put("Report*", report);
//	
//	CompleteTextElementType Words=new CompleteTextElementType("Words",report,Terminos);
//	//report.getSons().add(Words);
//	Words.setMultivalued(true);
//	
//	LinkedList<CompleteTextElementType> Words_rep=new LinkedList<>();
//	Words_rep.add(Words);
//	
//	Report_Words.put(report, Words_rep);
//	
//	TerrminosElem.put("Words", Words);
//	
//	CompleteLinkElementType report_Link=new CompleteLinkElementType("Report Link",report,Terminos);
//	report.getSons().add(report_Link);
//	
//	TerrminosElem.put("Report Link", report_Link);
//	
//	List<CompleteTextElementType> reportList=new ArrayList<>();
//	reportList.add(report);
//	
//	
//	CompleteLinkElementType utterance_Link=new CompleteLinkElementType("Uterances*",Terminos);
//	utterance_Link.setMultivalued(true);
//	
//	TerrminosElem.put("Uterances*", utterance_Link);
//	
//	List<CompleteLinkElementType> utterance_Link_List=new ArrayList<>();
//	utterance_Link_List.add(utterance_Link);
//	
//	TablaElem.put(Terminos,TerrminosElem);
//	
//	//TABAJAMOS en utterancias
//	
//	HashMap<String, CompleteElementType> UtteElem= new HashMap<String, CompleteElementType>();
//	
//	
//	CompleteGrammar Uterancia=new CompleteGrammar("Utterances", "Utterance Structure",Salida.getCollection());
//	C.getMetamodelGrammar().add(Uterancia);
//	
//	
//	CompleteTextElementType NombreUtte=new CompleteTextElementType("Nombre",Terminos);
//	Uterancia.getSons().add(NombreUtte);
//	UtteElem.put("Nombre", NombreUtte);
	
	//Lo bueno es que lo puedo calcular

	/**
	
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
	
	TablaElem.put(Uterancia, UtteElem);
	
	//FALTA LA PARTE DE LOS DOCUMENTOS QUE ES IGUAL QUE LO DE ARRIBA
	
	//Tabajamos los Informes
	
	
	CompleteGrammar ReportsG=new CompleteGrammar("Reports", "Reports Clasification",Salida.getCollection());
	C.getMetamodelGrammar().add(ReportsG);
	
	HashMap<String, CompleteElementType> ReportsElem= new HashMap<String, CompleteElementType>();
	
	CompleteTextElementType Description=new CompleteTextElementType("Description",ReportsG);
	ReportsG.getSons().add(Description);
	
	ReportsElem.put("Description", Description);
	
	CompleteLinkElementType cEntry=new CompleteLinkElementType("CEntry*",ReportsG);
	cEntry.setMultivalued(true);
	ReportsElem.put("CEntry*", cEntry);
	
	
	CompleteResourceElementType image=new CompleteResourceElementType("Images*",ReportsG);
	image.setMultivalued(true);	
	ReportsElem.put("Images*", image);
	
	
	CompleteLinkElementType Uterancia_ele=new CompleteLinkElementType("Uterances*",ReportsG);
	Uterancia_ele.setMultivalued(true);
	ReportsElem.put("Uterances*", Uterancia_ele);
	
	List<CompleteLinkElementType> Uterancia_docs_List=new ArrayList<>();
	Uterancia_docs_List.add(Uterancia_ele);
	
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
	HashMap<String, CompleteDocuments> iden_docum = new HashMap<>();
	
	
	//DOCUMENTOS Y TERMINOS
	for (int i = 0; i < documentosList.size(); i++) {
	String Iden = documentosList.get(i);
		HashMap<String, List<HashMap<String, HashSet<String>>>> Table = supertabla.get(Iden);
		CompleteDocuments Doc=new CompleteDocuments(C,Iden , DocIcon);
		C.getEstructuras().add(Doc);
		iden_docum.put(Iden, Doc);
		
		CompleteTextElement DESC=new CompleteTextElement(Description, documentosListText.get(Iden) );
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
	
	
	//UTERANCIAS Y TERMINOS
	
	HashMap<String, HashSet<CompleteDocuments>> utte_doc=new HashMap<>();
	HashMap<String, HashSet<CompleteDocuments>> utte_entry=new HashMap<>();
	HashSet<String> total=new HashSet<>();
	
	for (Entry<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> ide_doc_utter : SupertablaUtt.entrySet()) {
		CompleteDocuments docI=iden_docum.get(ide_doc_utter.getKey());

			for (Entry<String, HashMap<String, HashMap<String, HashSet<String>>>> utter_sem : ide_doc_utter.getValue().entrySet()) {
				
				total.add(utter_sem.getKey());
				
				HashSet<CompleteDocuments> listaAsoc=utte_doc.get(utter_sem.getKey());
				if (listaAsoc==null)
					listaAsoc=new HashSet<>();
				
				if (docI!=null)
					listaAsoc.add(docI);
				else
					{
					System.err.println("documento " +docI+" no encontrado");
					Salida.getLogLines().add("documento " +docI+" no encontrado");
					}
				
				utte_doc.put(utter_sem.getKey(), listaAsoc);
				
				HashSet<CompleteDocuments> listaAsocEntry=utte_entry.get(utter_sem.getKey());
				if (listaAsocEntry==null)
					listaAsocEntry=new HashSet<>();
				
				for (Entry<String, HashMap<String, HashSet<String>>> sem_terms : utter_sem.getValue().entrySet()) {
					HashMap<String, CompleteDocuments> list_term_Doc = supertablaSemPos_Doc.get(sem_terms.getKey());
					if (list_term_Doc!=null)
					{
						for (Entry<String, HashSet<String>> term_words : sem_terms.getValue().entrySet()) {
							CompleteDocuments documentoTermino = list_term_Doc.get(term_words.getKey());
							if (documentoTermino!=null)
								listaAsocEntry.add(documentoTermino);
							else
							{
							System.err.println("termino " +term_words.getKey()+" no encontrado");
							Salida.getLogLines().add("termino " +term_words.getKey()+" no encontrado");
							}
						}
						
					}else
					{
					System.err.println("semantica " +sem_terms.getKey()+" no encontrado");
					Salida.getLogLines().add("semantica " +sem_terms.getKey()+" no encontrado");
					}
				}
				
				utte_entry.put(utter_sem.getKey(), listaAsocEntry);
				
			}
		
	}
	
		
	
	HashMap<String, CompleteDocuments> utte_docum_equi=new HashMap<>();
	
	String iconUtter="https://www.freeiconspng.com/uploads/maps-center-direction-icon-24.png"; 
	for (String utteruni : total) {
		CompleteDocuments uteruno=new CompleteDocuments(C, utteruni, iconUtter);
		C.getEstructuras().add(uteruno);
		
		utte_docum_equi.put(utteruni, uteruno);
		
		CompleteTextElement nameElem=new CompleteTextElement(NombreUtte, utteruni );
		uteruno.getDescription().add(nameElem);
		
		
		HashSet<CompleteDocuments> docum=utte_doc.get(utteruni);
		if (docum!=null)
			{
			List<CompleteDocuments> lista=new LinkedList<>(docum);
			for (int i = 0; i < lista.size(); i++) {
				CompleteDocuments docre = lista.get(i);
				CompleteLinkElementType cl=docUttList.get(i);
				
				CompleteLinkElement linksRef=new CompleteLinkElement(cl,docre);
				uteruno.getDescription().add(linksRef);
			}
			}
		
		HashSet<CompleteDocuments> entryel=utte_entry.get(utteruni);
		if (entryel!=null)
			{
			List<CompleteDocuments> lista=new LinkedList<>(entryel);
			for (int i = 0; i < lista.size(); i++) {
				CompleteDocuments docre = lista.get(i);
				CompleteLinkElementType cl=termUttList.get(i);
				
				CompleteLinkElement linksRef=new CompleteLinkElement(cl,docre);
				uteruno.getDescription().add(linksRef);
			}
			}
		
	}
	
	HashMap<CompleteDocuments, HashSet<CompleteDocuments>> utte_doc_inv=new HashMap<>();
	for (Entry<String, HashSet<CompleteDocuments>> ute_doc_uni : utte_doc.entrySet()) {
		for (CompleteDocuments doc_uni : ute_doc_uni.getValue()) {
			
			HashSet<CompleteDocuments> utters=utte_doc_inv.get(doc_uni);
			if (utters==null)
				utters=new HashSet<>();
			CompleteDocuments utedoc = utte_docum_equi.get(ute_doc_uni.getKey());
			if (utedoc!=null)
				utters.add(utedoc);
			else
				{
				System.err.println("uterancia " +ute_doc_uni.getKey()+" no encontrado");
				Salida.getLogLines().add("uterancia " +ute_doc_uni.getKey()+" no encontrado");
				}
			utte_doc_inv.put(doc_uni, utters);
		}
	}
	
	HashMap<CompleteDocuments, HashSet<CompleteDocuments>> utte_entry_inv=new HashMap<>();
	for (Entry<String, HashSet<CompleteDocuments>> ute_entry_uni : utte_entry.entrySet()) {
		for (CompleteDocuments entry_uni : ute_entry_uni.getValue()) {
			HashSet<CompleteDocuments> utters=utte_entry_inv.get(entry_uni);
			if (utters==null)
				utters=new HashSet<>();
			CompleteDocuments utedoc = utte_docum_equi.get(ute_entry_uni.getKey());
			if (utedoc!=null)
				utters.add(utedoc);
			else
				{
				System.err.println("uterancia " +ute_entry_uni.getKey()+" no encontrado");
				Salida.getLogLines().add("uterancia " +ute_entry_uni.getKey()+" no encontrado");
				}
			utte_entry_inv.put(entry_uni, utters);
		}
	}
	
	int max_uterances_doc=1;
	for (Entry<CompleteDocuments, HashSet<CompleteDocuments>> doc_utte : utte_doc_inv.entrySet())
		if (doc_utte.getValue().size()>max_uterances_doc)
			max_uterances_doc=doc_utte.getValue().size();
	
	int max_uterances_entry=1;
	for (Entry<CompleteDocuments, HashSet<CompleteDocuments>> term_utte : utte_entry_inv.entrySet())
		if (term_utte.getValue().size()>max_uterances_entry)
			max_uterances_entry=term_utte.getValue().size();
	
	
	//TODO FALTA LA DE LOS DOCUMENTOS
	
	for (int i = 1; i < max_uterances_entry; i++) {
		CompleteLinkElementType utterance_Link_multi=new CompleteLinkElementType("Uterances*",Terminos);
		utterance_Link_multi.setMultivalued(true);
		TerrminosElem.put("Uterances*", utterance_Link_multi);
		utterance_Link_multi.setClassOfIterator(utterance_Link);
		utterance_Link_List.add(utterance_Link_multi);
	}
	
	for (Entry<CompleteDocuments, HashSet<CompleteDocuments>> entry_utte : utte_entry_inv.entrySet())
	{
		List<CompleteDocuments> lista_docs=new LinkedList<>(entry_utte.getValue());
		for (int i = 0; i < lista_docs.size(); i++) {
			CompleteLinkElement linksRef=new CompleteLinkElement(utterance_Link_List.get(i),lista_docs.get(i));
			entry_utte.getKey().getDescription().add(linksRef);
		}
	}
	
	for (int i = 1; i < max_uterances_doc; i++) {
		CompleteLinkElementType Uterancia_ele_multi=new CompleteLinkElementType("Uterances*",ReportsG);
		Uterancia_ele_multi.setMultivalued(true);
		TerrminosElem.put("Uterances*", Uterancia_ele_multi);
		Uterancia_ele_multi.setClassOfIterator(Uterancia_ele);
		Uterancia_docs_List.add(Uterancia_ele_multi);
	}
	
	for (Entry<CompleteDocuments, HashSet<CompleteDocuments>> doc_utte : utte_doc_inv.entrySet())
	{
		List<CompleteDocuments> lista_docs=new LinkedList<>(doc_utte.getValue());
		for (int i = 0; i < lista_docs.size(); i++) {
			CompleteLinkElement linksRef=new CompleteLinkElement(Uterancia_docs_List.get(i),lista_docs.get(i));
			doc_utte.getKey().getDescription().add(linksRef);
		}
	}
	
	
	//DAle caña
//	CompleteLinkElementType utterance_Link=new CompleteLinkElementType("Uterances*",Terminos);
//	Terminos.getSons().add(utterance_Link);
//	
//	TerrminosElem.put("Uterances*", utterance_Link);
//	
//	List<CompleteLinkElementType> utterance_Link_List=new ArrayList<>();
//	utterance_Link_List.add(utterance_Link);
	
	
	for (CompleteTextElementType report_new : reportList) {
		Terminos.getSons().add(report_new);
	}
	
	for (CompleteLinkElementType uterance_link : utterance_Link_List) {
		Terminos.getSons().add(uterance_link);
	}
	
	
	for (CompleteLinkElementType cEntryele : cEntryList) {
		ReportsG.getSons().add(cEntryele);
	}
	
	for (CompleteResourceElementType imageele : ImagenesList) {
		ReportsG.getSons().add(imageele);
	}
	
	for (CompleteLinkElementType uterance_link : Uterancia_docs_List) {
		ReportsG.getSons().add(uterance_link);
	}
	
	for (Entry<CompleteTextElementType, List<CompleteTextElementType>> report_listW : Report_Words.entrySet()) {
		
		for (CompleteTextElementType worsd_for_report : report_listW.getValue()) {
			report_listW.getKey().getSons().add(worsd_for_report);
		}
		
	}
	
**/
	
		
	return Salida;
}




	@Override
	public String getName() {
		return "UMLS Import Proto V1";
	}


	

	
}
