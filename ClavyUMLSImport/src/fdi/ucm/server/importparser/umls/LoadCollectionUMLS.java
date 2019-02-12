/**
 * 
 */
package fdi.ucm.server.importparser.umls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import fdi.ucm.server.modelComplete.ImportExportDataEnum;
import fdi.ucm.server.modelComplete.ImportExportPair;
import fdi.ucm.server.modelComplete.LoadCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollectionAndLog;

/**
 * @author Joaquin Gayoso Cabada
 *
 */
public abstract class LoadCollectionUMLS extends LoadCollection{

	
	private static ArrayList<ImportExportPair> Parametros;
	public static boolean consoleDebug=false;
	
	
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		LoadCollectionUMLS LC=new LoadCollectionUMLS();
//		LoadCollectionUMLS.consoleDebug=true;
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
//		AA.add("salida_docs.json");
//		AA.add("sample.txt");
//		AA.add("salida.xml");
//		AA.add("terminos_filtrados.txt");
//		AA.add("openi_nlm_nih_gov.json");
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
//				String FileIO = System.getProperty("user.home")+"/"+System.currentTimeMillis()+".clavy";
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
		
		
		String Salida_Docs_File = dateEntrada.get(0);
		String Sample_File = dateEntrada.get(1);
		String Salida_File = dateEntrada.get(2);
		String Terminos_Filtrados_File = dateEntrada.get(3);
		String Imagenes_File = dateEntrada.get(4);
		String Reducido = dateEntrada.get(5);
		
		CompleteCollectionAndLog Salida= new CompleteCollectionAndLog();
		Salida.setLogLines(new ArrayList<String>());
		
		List<String> DocumentosList=new LinkedList<String>();
		List<String> DocumentosListText= new LinkedList<String>();
		
		HashMap<String, HashSet<String>> imagenes_Tabla=new HashMap<String, HashSet<String>>();
		
		HashMap<String, HashMap<String,List<HashMap<String,HashSet<String>>>>> Supertabla=new HashMap<String, HashMap<String,List<HashMap<String,HashSet<String>>>>>();
		HashMap<String,HashMap<String,HashSet<String>>> SupertablaSemPos=new HashMap<String,HashMap<String,HashSet<String>>>();
		HashMap<String,HashMap<String,HashSet<String>>> SupertablaSemNeg=new HashMap<String,HashMap<String,HashSet<String>>>();
		HashMap<String,String> TablaSemanticaTexto=new HashMap<String,String>();
		
		System.out.println("//Procesando el Sample");
		try {
			 JsonReader reader = new JsonReader(new FileReader(Salida_Docs_File));
				Gson gson = new Gson();
				List<String> T =  gson.fromJson(reader, List.class);

				DocumentosList.addAll(T);
				
			    String line = "";

			    try (BufferedReader br = new BufferedReader(new FileReader(Sample_File))) {

			        while ((line = br.readLine()) != null) {

			        	if (!line.isEmpty())
			        		DocumentosListText.add(line);
			        	// use comma as separator
			        }

			    } catch (IOException e) {
			        e.printStackTrace();
			        throw new RuntimeException("Error en la lectura del sample");
			    }
				
			    if (DocumentosList.size()!=DocumentosListText.size())
			    	throw new RuntimeException("Error en la lectura del sample");
		} catch (Exception e) {
			e.printStackTrace();
			Salida.getLogLines().add("Error en la lectura del sample");
			Salida.getLogLines().add("IRRECUPERABLE");
			return Salida;
		}
		
		System.out.println("//Procesando las imagenes");
		try {
			imagenes_Tabla= processImagenes(Imagenes_File);
		} catch (Exception e) {
			e.printStackTrace();
			Salida.getLogLines().add("El archivo de procesado de imagenes no esta, me lo intento descargar");
			Salida.getLogLines().add("->Se continuara sin imagenes");
		}
		
		System.out.println("//Procesando la salida");
		
		try {
			procesaSalida(Salida_File,Terminos_Filtrados_File,
					DocumentosList,DocumentosListText,Supertabla,
					SupertablaSemPos,SupertablaSemNeg,TablaSemanticaTexto,Salida,Reducido);
		} catch (Exception e) {
			e.printStackTrace();
			Salida.getLogLines().add("Error en la lectura del archivo de salida xml");
			Salida.getLogLines().add("IRRECUPERABLE");
			return Salida;
		}
		
		
		
		System.out.println("//Generando coleccion Clavy");
		
		processCollecccion(DocumentosList,DocumentosListText,Supertabla,SupertablaSemPos,TablaSemanticaTexto,imagenes_Tabla,Salida);
		
		return Salida;
	}

	
	
	protected abstract CompleteCollectionAndLog processCollecccion(List<String> documentosList, List<String> documentosListText,
			   HashMap<String, HashMap<String, List<HashMap<String, HashSet<String>>>>> supertabla,
			   HashMap<String, HashMap<String, HashSet<String>>> supertablaSemPos, HashMap<String, String> TablaSemanticaTexto,
			   HashMap<String, HashSet<String>> imagenes_Tabla, CompleteCollectionAndLog Salida);
	
	
//	private CompleteCollectionAndLog processCollecccion(List<String> documentosList, List<String> documentosListText,
//		   HashMap<String, HashMap<String, List<HashMap<String, HashSet<String>>>>> supertabla,
//		   HashMap<String, HashMap<String, HashSet<String>>> supertablaSemPos, HashMap<String, String> TablaSemanticaTexto,
//		   HashMap<String, HashSet<String>> imagenes_Tabla, CompleteCollectionAndLog Salida) {
//	
//	CompleteCollection C=new CompleteCollection("MetaMap ", "MetaMap  collection");
//	Salida.setCollection(C);
//	
//	
//	HashMap<CompleteGrammar,HashMap<String, CompleteElementType>> TablaElem=new HashMap<>();
//	
//	CompleteGrammar Terminos=new CompleteGrammar("Terms", "Terms Clasification",Salida.getCollection());
//	C.getMetamodelGrammar().add(Terminos);
//	
//	
//	//Tabajamos los terminos
//	
//	HashMap<String, CompleteElementType> TerrminosElem= new HashMap<String, CompleteElementType>();
//	
//	CompleteTextElementType Nombre=new CompleteTextElementType("Name",Terminos);
//	Terminos.getSons().add(Nombre);
//	
//	TerrminosElem.put("Name", Nombre);
//	
//	CompleteTextElementType Categoria=new CompleteTextElementType("Category",Terminos);
//	Terminos.getSons().add(Categoria);
//	Categoria.setBrowseable(true);
//	
//	TerrminosElem.put("Category", Categoria);
//	
//	CompleteTextElementType Words=new CompleteTextElementType("Words",Terminos);
//	Terminos.getSons().add(Words);
//	Words.setMultivalued(true);
//	
//	TerrminosElem.put("Words", Words);
//	
//	List<CompleteTextElementType> WordsList=new ArrayList<>();
//	WordsList.add(Words);
//	
//	
//	HashMap<CompleteDocuments, Integer> actualInforme=new HashMap<>();
//	
//	 HashMap<String, HashMap<String, CompleteDocuments>> supertablaSemPos_Doc=new HashMap<>();
//		
//	String icon="https://www.freeiconspng.com/uploads/blank-price-tag-png-11.png"; 
//	 
//	for (Entry<String, HashMap<String, HashSet<String>>> semantica_word : supertablaSemPos.entrySet()) {
//			
//		
//		HashMap<String, CompleteDocuments> String_doc=new HashMap<>();
//		
//		for (Entry<String, HashSet<String>> word_wfound : semantica_word.getValue().entrySet()) {
//				CompleteDocuments WordInstance=new CompleteDocuments(C, semantica_word.getKey()+"->"+word_wfound.getKey(), icon);
//				C.getEstructuras().add(WordInstance);
//				
//				CompleteTextElement NameI=new CompleteTextElement(Nombre, word_wfound.getKey());
//				WordInstance.getDescription().add(NameI);
//				
//				CompleteTextElement CateI=new CompleteTextElement(Categoria, TablaSemanticaTexto.get(semantica_word.getKey()));
//				WordInstance.getDescription().add(CateI);
//				
//				HashSet<String> ListaW = word_wfound.getValue();
//				
//				List<String> ListaWW=new LinkedList<>(ListaW);
//				
//				while (WordsList.size()<ListaWW.size())
//					{
//					CompleteTextElementType WordsI=new CompleteTextElementType("Words",Terminos);
//					Terminos.getSons().add(WordsI);
//					WordsI.setMultivalued(true);
//					WordsI.setClassOfIterator(Words);
//					WordsList.add(WordsI);
//					}
//				
//				for (int i = 0; i < ListaWW.size(); i++) {
//					String string = ListaWW.get(i);
//					CompleteTextElement wordI=new CompleteTextElement(WordsList.get(i), string);
//					WordInstance.getDescription().add(wordI);
//					
//				}
//				
//				actualInforme.put(WordInstance, new Integer(0));
//				
//				String_doc.put(word_wfound.getKey(), WordInstance);
//			}
//		
//		supertablaSemPos_Doc.put(semantica_word.getKey(), String_doc);
//	}
//	
//	
//	CompleteLinkElementType Reports=new CompleteLinkElementType("Reports",Terminos);
//	Terminos.getSons().add(Reports);
//	Reports.setMultivalued(true);
//	
//	TerrminosElem.put("Reports", Reports);
//	
//	List<CompleteLinkElementType> RepordList=new ArrayList<>();
//	RepordList.add(Reports);
//	
//	TablaElem.put(Terminos,TerrminosElem);
//
//	
//	CompleteGrammar ReportsG=new CompleteGrammar("Reports", "Reports Clasification",Salida.getCollection());
//	C.getMetamodelGrammar().add(ReportsG);
//	
//	//Tabajamos los Informes
//	
//HashMap<String, CompleteElementType> ReportsElem= new HashMap<String, CompleteElementType>();
//	
//
//CompleteTextElementType CODE=new CompleteTextElementType("CODE",ReportsG);
//ReportsG.getSons().add(CODE);
//
//TerrminosElem.put("CODE", CODE);
//
//	CompleteTextElementType Description=new CompleteTextElementType("Description",ReportsG);
//	ReportsG.getSons().add(Description);
//	
//	ReportsElem.put("Description", Description);
//	
//	
//	CompleteResourceElementType Images=new CompleteResourceElementType("Images",ReportsG);
//	ReportsG.getSons().add(Images);
//	Images.setMultivalued(true);
//	
//	ReportsElem.put("Images", Images);
//	
//	List<CompleteResourceElementType> ImagenesList=new ArrayList<>();
//	ImagenesList.add(Images);
//	
//	
//	TablaElem.put(ReportsG, ReportsElem);
//	
//	
//	
//	String DocIcon="https://www.freeiconspng.com/uploads/document-icon-10.jpg";
//	
//	for (int i = 0; i < documentosList.size(); i++) {
//	String Iden = documentosList.get(i);
//		HashMap<String, List<HashMap<String, HashSet<String>>>> Table = supertabla.get(Iden);
//		CompleteDocuments Doc=new CompleteDocuments(C,documentosListText.get(i) , DocIcon);
//		C.getEstructuras().add(Doc);
//		
//		CompleteTextElement IDEN=new CompleteTextElement(CODE, Iden);
//		Doc.getDescription().add(IDEN);
//		
//		CompleteTextElement DESC=new CompleteTextElement(Description, documentosListText.get(i) );
//		Doc.getDescription().add(DESC);
//		
//		if (Table!=null)
//		
//		for (Entry<String, List<HashMap<String, HashSet<String>>>> semantica_word_doc : Table.entrySet()) {
//			//TODO Es el 1 o el 2?
//			for (Entry<String, HashSet<String>> completeLinkElementType : semantica_word_doc.getValue().get(0).entrySet()) {
//					CompleteDocuments WordS=supertablaSemPos_Doc.get(semantica_word_doc.getKey()).get(completeLinkElementType.getKey());
//					Integer Informes_Doc = actualInforme.get(WordS);
//					
//					if (Informes_Doc==null)
//						System.out.println("AA");
//					
//					while (RepordList.size()<=Informes_Doc)
//					{
//						
//					CompleteLinkElementType ReportsI=new CompleteLinkElementType("Reports",Terminos);
//					Terminos.getSons().add(ReportsI);
//					ReportsI.setMultivalued(true);
//					ReportsI.setClassOfIterator(Reports);
//					RepordList.add(ReportsI);
//					
//					}
//					
//					CompleteLinkElement CLE=new CompleteLinkElement(RepordList.get(Informes_Doc), Doc);
//					WordS.getDescription().add(CLE);
//					
//					actualInforme.put(WordS, new Integer(Informes_Doc.intValue()+1));
//					
//			}
//		}
//		
//		
//		
//		HashSet<String> imagenesLinks = new HashSet<String>(imagenes_Tabla.get(Iden)) ;
//		
//		if (imagenesLinks != null)
//			{
//			
//			LinkedList<String> imagenesLinksList = new LinkedList<String>(imagenesLinks);
//			
//			while (ImagenesList.size()<imagenesLinksList.size())
//			{
//		
//				CompleteResourceElementType ImagesI=new CompleteResourceElementType("Images",ReportsG);
//				ReportsG.getSons().add(ImagesI);
//				ImagesI.setMultivalued(true);
//				ImagesI.setClassOfIterator(Images);
//				ImagenesList.add(ImagesI);
//			}
//			
//			
//			for (int j = 0; j < imagenesLinks.size(); j++) {
//				CompleteResourceElementURL CLE=new CompleteResourceElementURL(ImagenesList.get(j), imagenesLinksList.get(j));
//				Doc.getDescription().add(CLE);
//			}
//			
//			
//			
//			}
//			
//		
//		
//		//AQUI QUEdan las imagenes
//	}
//	
//	return Salida;
//}


	private static void procesaSalida(String filein, String terminos_Filtrados_File, List<String> Lista, List<String> documentosListTextIn,
			HashMap<String, HashMap<String, List<HashMap<String, HashSet<String>>>>> Supertabla,
			HashMap<String, HashMap<String, HashSet<String>>> SupertablaSemPos,
			HashMap<String, HashMap<String, HashSet<String>>> SupertablaSemNeg, HashMap<String,
			String> TablaSemanticaTexto, CompleteCollectionAndLog salida, String reducido) {
		
		
		
		
		
		HashMap<String,String> TablaSemanticaTextoInversa=new HashMap<>();
		HashMap<String,List<String>> TablaSemanticaTextoValidas=new HashMap<>();
		
		
		
		try {
			
			
			String csvFile = reducido;
	        String line = "";
	        String cvsSplitBy = ";";

	        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

	            while ((line = br.readLine()) != null) {

	                // use comma as separator
	                String[] semsepa = line.split(cvsSplitBy);

	               // System.out.println("Completos=> " + semsepa[0] + " , clave=>" + semsepa[1]);
	                TablaSemanticaTexto.put(semsepa[1], semsepa[0]);
	                TablaSemanticaTextoInversa.put(semsepa[0], semsepa[1]);
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
			
			
			
			
			
			String csvFile2 = terminos_Filtrados_File;
	        String line2 = "";

	        try (BufferedReader br2 = new BufferedReader(new FileReader(csvFile2))) {

	        	
	        	String SemanticaA="";
	            while ((line2 = br2.readLine()) != null) {

	            	if (line2.startsWith("Semantica="))
	            		{
	            		SemanticaA=line2.replace("Semantica=>", "");
	            		SemanticaA=TablaSemanticaTextoInversa.get(SemanticaA);
	            		}
			    	 else
				    	 {
			    		 
			    		 List<String> Valores = TablaSemanticaTextoValidas.get(SemanticaA);
			    		 if (Valores==null)
			    			 Valores= new LinkedList<>();
			    		 
			    		 String hijo=line2.trim().replace("++", "");
			    		 String[] hijos=hijo.split("\\[");
			    		 hijo=hijos[0];
			    		 
			    		 Valores.add(hijo);
			    		 
			    		 TablaSemanticaTextoValidas.put(SemanticaA, Valores);
				    	 }
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	            salida.getLogLines().add("Sin archivo de filtrado");
	            TablaSemanticaTextoValidas=new HashMap<>();
	        }
	        
	        
	        
	       
	        
		
		
	        System.out.println("//Procesando Resultado XML P1");
			File file=new File(filein);
			  DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			  	dbFactory.setValidating(false);
			  	dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(file);
				//Pillo los documentos
				NodeList nList = doc.getElementsByTagName("MMO");
//				Node ActualNode = doc.getFirstChild().getNextSibling().getFirstChild();
//				int i=0;
				//while (ActualNode!=null)
				for (int i = 0; nList.item(i)!=null; i++)
				{
					
					Node ActualNode=nList.item(i);
					if (ActualNode instanceof Element) {
					//	System.out.println("Documentos->"+i);
						Element eElement2 = ((Element)ActualNode);
						
						String Doc=Lista.get(i);
						
//						i++;
						
						//pillo los semanticos
						NodeList semanticos=eElement2.getElementsByTagName("Candidate");
							for (int j = 0; semanticos.item(j)!=null; j++) {
								Element Candidate = ((Element)semanticos.item(j));
								String CandidatePreferred=null;
								List<String> MatchedWords=new LinkedList<>();
								List<String> SemTypes=new LinkedList<>();
								boolean Negated = false;
								 NodeList hijos = Candidate.getChildNodes();
								for (int k = 0; hijos.item(k)!=null; k++) {
									Node ItemCan = hijos.item(k);
									if (ItemCan instanceof Element)
									{
										Element ElemItemCan=(Element)ItemCan;
										
										
										if (ElemItemCan.getTagName()=="CandidatePreferred")
											CandidatePreferred=ElemItemCan.getFirstChild().getTextContent();
										
										if (ElemItemCan.getTagName()=="MatchedWords")
											{
											NodeList Palabras=ElemItemCan.getElementsByTagName("MatchedWord");
											boolean primero=true;
											StringBuffer SB=new StringBuffer();
											for (int l = 0; Palabras.item(l)!=null; l++) 
												if (Palabras.item(l) instanceof Element){
													if (primero)
														primero=false;
													else
														SB.append("_");
													SB.append(Palabras.item(l).getFirstChild().getTextContent().toLowerCase());
												}
											
											
											MatchedWords.add(SB.toString());
											
											}
										
										
										if (ElemItemCan.getTagName()=="SemTypes")
										{
										NodeList TyposSem=ElemItemCan.getElementsByTagName("SemType");
										for (int l = 0; TyposSem.item(l)!=null; l++) {
											if (TyposSem.item(l) instanceof Element)
												SemTypes.add(TyposSem.item(l).getFirstChild().getTextContent());
											}
										}
										
										if (ElemItemCan.getTagName()=="Negated")
										{
										String intS=ElemItemCan.getFirstChild().getTextContent();
										if (intS.equals("1"))
											Negated=true;
										}
										
									}

								}
//								System.out.println(CandidatePreferred);
//								for (String word : MatchedWords) {
//									System.out.println(" +"+word);
//								}
//								for (String sem : SemTypes) {
//									System.out.println(" *"+sem);
//								}
								
								List<String> SemTypesFinal=new LinkedList<>();
								
								if (!TablaSemanticaTextoValidas.keySet().isEmpty())
								{	
									for (String string : SemTypes) {
										if (TablaSemanticaTextoValidas.containsKey(string))
											SemTypesFinal.add(string);
									}
								}
								else
									SemTypesFinal=new LinkedList<>(SemTypes);

								
								SemTypes=SemTypesFinal;
								
								HashSet<String> ValidCandidate=new HashSet<>();
								
								
								for (String string : SemTypes) {
									List<String> ListaWordVal = TablaSemanticaTextoValidas.get(string);
									
									
									if(ListaWordVal.contains(CandidatePreferred))
										ValidCandidate.add(string);
									

								}
								
								
								if (!SemTypes.isEmpty()&&!MatchedWords.isEmpty()&&!ValidCandidate.isEmpty())
								{
								
								HashMap<String, List<HashMap<String, HashSet<String>>>> ListaSemanticaHsh = Supertabla.get(Doc);
								if (ListaSemanticaHsh==null)
									ListaSemanticaHsh=new HashMap<>();
								
								for (String sem : SemTypes) {
									
									if (ValidCandidate.contains(sem))
									{
										
									
									List<HashMap<String, HashSet<String>>> SemCan = ListaSemanticaHsh.get(sem);
									if (SemCan==null)
										{
										SemCan=new LinkedList<>();
										SemCan.add(new HashMap<String, HashSet<String>>());
										SemCan.add(new HashMap<String, HashSet<String>>());
										}
									
									
									HashSet<String> ListW;
									
									if (!Negated)
										ListW = SemCan.get(0).get(CandidatePreferred);
									else
										ListW = SemCan.get(1).get(CandidatePreferred);
									
									
									if (ListW==null)
										ListW=new HashSet<>();
									
									for (String mword : MatchedWords) {
										ListW.add(mword);
									}
									
									if (!Negated)
										SemCan.get(0).put(CandidatePreferred, ListW);
									else
										SemCan.get(1).put(CandidatePreferred, ListW);
									
									
									ListaSemanticaHsh.put(sem, SemCan);
									
									
									HashMap<String, HashSet<String>> SemCanMiniTabla;
									if (!Negated)
										SemCanMiniTabla = SupertablaSemPos.get(sem);
									else
										SemCanMiniTabla = SupertablaSemNeg.get(sem);
									
									if (SemCanMiniTabla==null)
										SemCanMiniTabla=new HashMap<>();
									
									HashSet<String> ListWMiniTabla = SemCanMiniTabla.get(CandidatePreferred);
									if (ListWMiniTabla==null)
										ListWMiniTabla=new HashSet<>();
									
									for (String mword : MatchedWords) {
										ListWMiniTabla.add(mword);
									}
									
									SemCanMiniTabla.put(CandidatePreferred, ListWMiniTabla);
									
									if (!Negated)
										SupertablaSemPos.put(sem, SemCanMiniTabla);
									else
										SupertablaSemNeg.put(sem, SemCanMiniTabla);
									}
								}
								
								Supertabla.put(Doc, ListaSemanticaHsh);
								
							}
					}
						
						
					}
					
					
				
				}
				



		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			 
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	private static HashMap<String, HashSet<String>> processImagenes(String imagenes_File) {


		   
		   
		
		   JsonReader reader;
		try {
			reader = new JsonReader(new FileReader( imagenes_File));
			Gson gson = new Gson();
			HashMap<String, HashSet<String>> Imagenes_List =  gson.fromJson(reader, HashMap.class);
			
			System.out.println("Cargada");
			return Imagenes_List;
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.err.println("El archivo no esta, me lo intento descargar");
			throw new RuntimeException("El archivo no esta, me lo intento descargar");
		}
   
	}

	
	@Override
	public ArrayList<ImportExportPair> getConfiguracion() {
		if (Parametros==null)
		{
			ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "List Documents json File"));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "List Documents String txt File"));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "UMLS output File"));
			//ESTO ES MEJORABLE
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Filter categories txt",true));
			//Futuro
			//ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Filter categories cvs",true));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Openi json File"));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Resume Category csv"));
			Parametros=ListaCampos;
			return ListaCampos;
		}
		else return Parametros;
	}

	@Override
	public String getName() {
		return "UMLS XML Import";
	}

	@Override
	public boolean getCloneLocalFiles() {
		return false;
	}

	

	
}
