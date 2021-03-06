/**
 * 
 */
package fdi.ucm.server.importparser.umls.proto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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
public abstract class LoadCollectionUMLSVProtoBase extends LoadCollection{


	
	
	
	
	private static boolean DEBUG = false;
	private static ArrayList<ImportExportPair> Parametros;
	public static boolean consoleDebug=false;
	
	

	@Override
	public CompleteCollectionAndLog processCollecccion(ArrayList<String> dateEntrada) {
		
		
		String Sample_File = dateEntrada.get(0);
		String Salida_File = dateEntrada.get(1);
		String Terminos_Filtrados_File = dateEntrada.get(2);
		String Imagenes_File = dateEntrada.get(3);
		String Reducido = dateEntrada.get(4);
		
		CompleteCollectionAndLog Salida= new CompleteCollectionAndLog();
		Salida.setLogLines(new ArrayList<String>());
		
		List<String> DocumentosList=new LinkedList<String>();
		HashMap<String,String> DocumentosListText= new HashMap<String,String>();
		
		HashMap<String, HashSet<String>> imagenes_Tabla=new HashMap<String, HashSet<String>>();
		
		HashMap<String, HashMap<String,List<HashMap<String,HashSet<String>>>>> Supertabla=new HashMap<String, HashMap<String,List<HashMap<String,HashSet<String>>>>>();
		HashMap<String,HashMap<String,HashSet<String>>> SupertablaSemPos=new HashMap<String,HashMap<String,HashSet<String>>>();
		HashMap<String,HashMap<String,HashSet<String>>> SupertablaSemNeg=new HashMap<String,HashMap<String,HashSet<String>>>();
		HashMap<String,String> TablaSemanticaTexto=new HashMap<String,String>();
		HashMap<String, HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>>> SupertablaUtt=new HashMap<>();
		HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> supertablaUtt_list=new HashMap<>();
		HashMap<String,HashMap<String,String>> Sem_Term_CUI=new HashMap<String,HashMap<String,String>>();
		
		System.out.println("//Procesando el Sample");
		
		
	    String line = "";

	    try (BufferedReader br = new BufferedReader(new FileReader(Sample_File))) {

	        while ((line = br.readLine()) != null) {

	        	if (!line.isEmpty())
	        		{
	        		String[] LineT=line.split("\\|");
	        		StringBuffer Texto=new StringBuffer();
	        		String NombreArhivo = LineT[0];
	        		DocumentosList.add(NombreArhivo);
	        		for (int i = 1; i < LineT.length; i++) {
	        			if (i!=1)
	        				Texto.append("\\|");
	        			Texto.append(LineT[i]);
					}
	        		
	        		DocumentosListText.put(NombreArhivo,Texto.toString());
	        		}
	        	// use comma as separator
	        }

	    } catch (IOException e) {
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
					SupertablaSemPos,SupertablaSemNeg,TablaSemanticaTexto,
					SupertablaUtt,supertablaUtt_list,Sem_Term_CUI,
					Salida,Reducido);
		} catch (Exception e) {
			e.printStackTrace();
			Salida.getLogLines().add("Error en la lectura del archivo de salida xml");
			Salida.getLogLines().add("IRRECUPERABLE");
			return Salida;
		}
		
		
		
		System.out.println("//Generando coleccion Clavy");
		
		processCollecccion(DocumentosList,DocumentosListText,Supertabla,SupertablaSemPos,TablaSemanticaTexto,imagenes_Tabla,Salida,SupertablaUtt,supertablaUtt_list,Sem_Term_CUI);
		
		return Salida;
	}

	
	
	protected abstract CompleteCollectionAndLog processCollecccion(List<String> documentosList, HashMap<String,String> documentosListText,
			   HashMap<String, HashMap<String, List<HashMap<String, HashSet<String>>>>> supertabla,
			   HashMap<String, HashMap<String, HashSet<String>>> supertablaSemPos, HashMap<String, String> TablaSemanticaTexto,
			   HashMap<String, HashSet<String>> imagenes_Tabla, CompleteCollectionAndLog Salida,
			   HashMap<String, HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>>> SupertablaUtt,
			   HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> supertablaUtt_list,
			   HashMap<String,HashMap<String,String>> Sem_Term_CUI);
	
//	private static void procesaSalida(String filein,String filteredTerms, List<String> Lista,
//	HashMap<String,String> documentosListTextIn,
//	HashMap<String, HashMap<String,List<HashMap<String,HashSet<String>>>>> Supertabla,
//	HashMap<String,HashMap<String,HashSet<String>>> SupertablaSemPos,
//	HashMap<String,HashMap<String,HashSet<String>>> SupertablaSemNeg,
//	HashMap<String,String> TablaSemanticaTexto,
//	HashMap<String, HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>>> SupertablaUtt,
//	HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>> Utter_Text,
//	HashMap<String, List<String>> Utter_doc,
//	CompleteCollectionAndLog Salida,
//	String Reducido) throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {


	private static void procesaSalida(String filein,String filteredTerms,List<String> Lista,
			HashMap<String,String> documentosListTextIn,
			HashMap<String, HashMap<String, List<HashMap<String, HashSet<String>>>>> Supertabla,
			HashMap<String, HashMap<String, HashSet<String>>> SupertablaSemPos,
			HashMap<String, HashMap<String, HashSet<String>>> SupertablaSemNeg, 
			HashMap<String,String> TablaSemanticaTexto,
			HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> SupertablaUtt,
			HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> supertablaUtt_list,
			HashMap<String,HashMap<String,String>> Sem_Term_CUI,
			CompleteCollectionAndLog Salida,
			String Reducido)
					throws XMLStreamException, FactoryConfigurationError, IOException {
		

		HashMap<String, String> TablaSemanticaTextoInversa = new HashMap<String,String>();
		HashMap<String,List<String>> TablaSemanticaTextoValidas=new HashMap<String,List<String>>();
		
		
//		try {
//			
			
			String csvFile = Reducido;
	        String line = "";
	        String cvsSplitBy = ";";

	        try {
				
			
	        BufferedReader br = new BufferedReader(new FileReader(csvFile));

	            while ((line = br.readLine()) != null) {

	                // use comma as separator
	                String[] semsepa = line.split(cvsSplitBy);

	               // System.out.println("Completos=> " + semsepa[0] + " , clave=>" + semsepa[1]);
	                TablaSemanticaTexto.put(semsepa[1], semsepa[0]);
	                TablaSemanticaTextoInversa.put(semsepa[0], semsepa[1]);
	            }
	            br.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
						
			String csvFile2 = filteredTerms;
	        String line2 = "";
	        
	        if ((new File(csvFile2).exists()))
			{
	        try {
	        BufferedReader br2 = new BufferedReader(new FileReader(csvFile2));

	        	
	        	String SemanticaA="";
	            while ((line2 = br2.readLine()) != null) {

	            	if (line2.startsWith("Semantica="))
	            		{
	            		SemanticaA=line2.replace("Semantica=>", "");
	            		SemanticaA=TablaSemanticaTextoInversa.get(SemanticaA);
	            		TablaSemanticaTextoValidas.put(SemanticaA, new LinkedList<String>());
	            		}
			    	 else
				    	 {
			    		 
			    		 List<String> Valores = TablaSemanticaTextoValidas.get(SemanticaA);
			    		 if (Valores==null)
			    			 Valores= new LinkedList<String>();
			    		 
			    		 String hijo=line2.trim().replace("++", "");
			    		 String[] hijos=hijo.split("\\[");
			    		 hijo=hijos[0];
			    		 
			    		 Valores.add(hijo);
			    		 
			    		 TablaSemanticaTextoValidas.put(SemanticaA, Valores);
				    	 }
	            }
	            br2.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	            System.err.println("Error con el archvo se calcularan todas");
	            Logger.getLogger(LoadCollectionUMLSVProtoBase.class.getName()).log(Level.INFO, "Error con el archvo se calcularan todas");
	            TablaSemanticaTextoValidas=new HashMap<String,List<String>>();
	        }            
			}else
	        	
				if (DEBUG){
				System.out.println("Trabajaremos con todas las gramaticas");
				Logger.getLogger(LoadCollectionUMLSVProtoBase.class.getName()).log(Level.INFO, "Trabajaremos con todas las gramaticas");
	        	}
	        
	        if (DEBUG)
	        {
	        System.out.println("//Procesando Resultado XML P1");
	        Logger.getLogger(LoadCollectionUMLSVProtoBase.class.getName()).log(Level.INFO, "//Procesando Resultado XML P1");
	        }

	        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

	        
			File file=new File(filein);

			
			if (file.exists())	
			{	
				
			int doc_ind=0;
			
//			File file2=new File("salida.xml");
//			
//			copyFileUsingStream(file,new File("salida.xml"));
//			
			XMLInputFactory instaance = XMLInputFactory.newInstance();
			instaance.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			XMLStreamReader in = instaance.createXMLStreamReader(new FileInputStream(file));
			while (! (in.isEndElement() && in.getLocalName().equals("MMOs"))) {
	            if (in.isStartElement() && in.getLocalName().equals("MMO")) {
	            	doc_ind++;
	            	
	            	if (DEBUG){
	            		System.out.println("Documentos->"+doc_ind);
	            		 Logger.getLogger(LoadCollectionUMLSVProtoBase.class.getName()).log(Level.INFO, "Documentos->"+doc_ind);
	            	}
	            	
	            	String Doc="unknown";
	            	String Utterance="";
	            	
	            	while(! (in.isEndElement() && in.getLocalName().equals("MMO"))) {
	            		
	            		 if(in.isStartElement() && in.getLocalName().equals("PMID")) {
	            			 Doc=in.getElementText();
	            			 while (! (in.isEndElement() && in.getLocalName().equals("PMID")))
	                			 {
	            				 in.next();
	                			 }
	                       }
	            		 
	            		//ID
	            		 
	            		 
	            		 if(in.isStartElement() && in.getLocalName().equals("UttText")) {
	            			 Utterance=in.getElementText().trim();
	            			 while (! (in.isEndElement() && in.getLocalName().equals("UttText")))
	                			 {
	            				 in.next();
	                			 }
	                       }
	            		 
	            		 //TODO La TextUterance esta a esta altura
	            		
	                    if(in.isStartElement() && in.getLocalName().equals("Candidate")) {
	                    		String CandidatePreferred=null;
	                    		String CandidateCUI=null;
								List<String> MatchedWords=new LinkedList<String>();
								List<String> SemTypes=new LinkedList<String>();
								boolean Negated = false;
								List<String> Posiciones=new LinkedList<String>();
	                    	while (! (in.isEndElement() && in.getLocalName().equals("Candidate")))
	                    	{
	                    		
	                    		if(in.isStartElement() && in.getLocalName().equals("CandidatePreferred")) {
	                    			CandidatePreferred=in.getElementText();
	                   			 while (! (in.isEndElement() && in.getLocalName().equals("CandidatePreferred")))
	                       			 {
	                   				 in.next();
	                       			 }
	                              }
	                    		
	                    		if(in.isStartElement() && in.getLocalName().equals("CandidateCUI")) {
	                    			CandidateCUI=in.getElementText();
	                   			 while (! (in.isEndElement() && in.getLocalName().equals("CandidateCUI")))
	                       			 {
	                   				 in.next();
	                       			 }
	                              }
	                    		
	                    		//PREFFER
	                    		
	                    		
	                    		if(in.isStartElement() && in.getLocalName().equals("MatchedWords")) {
	                    		
	                    		StringBuffer SB=new StringBuffer();
	                    		boolean primero=true;
	                   			 while (! (in.isEndElement() && in.getLocalName().equals("MatchedWords")))
	                   			 	{
	                   				if(in.isStartElement() && in.getLocalName().equals("MatchedWord")) {
	                   					
	                   					if (primero)
											primero=false;
										else
											SB.append("_");
	                   					
	                   					SB.append(in.getElementText());
	                       			 while (! (in.isEndElement() && in.getLocalName().equals("MatchedWord")))
	                           			 {
	                       				 in.next();
	                           			 }
	                                  }
	                   				in.next();
	                   			 	}

	                   			 	 MatchedWords.add(SB.toString());
	                              }

	                    			//MECHED
	                    		
	                    		
	                    		if(in.isStartElement() && in.getLocalName().equals("SemTypes")) {
	                   			 while (! (in.isEndElement() && in.getLocalName().equals("SemTypes")))
	                       			 {
	                   				if(in.isStartElement() && in.getLocalName().equals("SemType")) {
	                   					SemTypes.add(in.getElementText());
	                          			 while (! (in.isEndElement() && in.getLocalName().equals("SemType")))
	                          				 {
	                          				 in.next();
	                          				 }
	                              			 
	                                     }
	                   				
	                   				 in.next();
	                       			 }
	                              }
	                    		
	                    		//MAtCHEDWORDS 
	                    		
	                    		if(in.isStartElement() && in.getLocalName().equals("MatchMaps")) {
	                    			
	                    			
	                      			 while (! (in.isEndElement() && in.getLocalName().equals("MatchMaps")))
	                      				 {
	                      				 //ME FALTA ALGO AQUI
	                      				if(in.isStartElement() && in.getLocalName().equals("MatchMap")) {
	                      					int PositionSTA = -1;
	                      					int PositionEND = -1;
	   	                      			 while (! (in.isEndElement() && in.getLocalName().equals("MatchMap")))
	   	                      				 {
	   	                      				 	
			   	                      			if(in.isStartElement() && in.getLocalName().equals("TextMatchStart")) 
			   	                      				{
			   	                      				try {
			   	                      				PositionSTA=Integer.parseInt(in.getElementText());
													} catch (Exception e) {
														e.printStackTrace();
													}
			   	                      				
				   	                      			 while (! (in.isEndElement() && in.getLocalName().equals("TextMatchStart")))
				   	                      				 {
				   	                      				 in.next();
				   	                      				 }
			   	                      				}
			   	                      			
				   	                      		if(in.isStartElement() && in.getLocalName().equals("TextMatchEnd")) 
			   	                      				{
			   	                      				try {
			   	                      				PositionEND=Integer.parseInt(in.getElementText());
													} catch (Exception e) {
														e.printStackTrace();
													}
			   	                      				
				   	                      			 while (! (in.isEndElement() && in.getLocalName().equals("TextMatchEnd")))
				   	                      				 {
				   	                      				 in.next();
				   	                      				 }
		   	                      				}
				   	                      	
	   	                      				 in.next();
	   	                      				 }
	   	                      			 
	   	                      			 
	   	                      			 if (PositionSTA!=-1&&PositionEND!=-1)
	   	                      			 {
	   	                      				 if (PositionSTA==PositionEND)
	   	                      					 Posiciones.add(Integer.toString(PositionSTA));
	   	                      				 else
	   	                      					 for (int i = PositionSTA; i <= PositionEND; i++) {
	   	                      						 Posiciones.add(Integer.toString(i));
												}
	   	                      			 }
	   	                      			
	   	                          			 
	   	                                 }
	                      				 
	                      				 in.next();
	                      				 }
	                          			 
	                                 }
	                    		
	                    		//NEGATED
	                    		
	                    		if(in.isStartElement() && in.getLocalName().equals("Negated")) {
	                    			String intS=in.getElementText();
	                    			if (intS.equals("1"))
	    								Negated=true;
	                      			 while (! (in.isEndElement() && in.getLocalName().equals("Negated")))
	                      				 {
	                      				 in.next();
	                      				 }
	                          			 
	                                 }
								in.next();
	                    	}
	                    	
	                    	List<String> SemTypesFinal=new LinkedList<String>();
							
							if (!TablaSemanticaTextoValidas.keySet().isEmpty())
							{
							for (String string : SemTypes) {
								if (TablaSemanticaTextoValidas.containsKey(string))
									SemTypesFinal.add(string);
							}
							}else
								SemTypesFinal.addAll(SemTypes);
							
							
							SemTypes=SemTypesFinal;
							
							HashSet<String> ValidCandidate=new HashSet<String>();
							
							
							if (!TablaSemanticaTextoValidas.keySet().isEmpty())
							{
							
							for (String string : SemTypes) {
								List<String> ListaWordVal = TablaSemanticaTextoValidas.get(string);
								
								
								if (ListaWordVal==null||ListaWordVal.isEmpty())
									ValidCandidate.add(string);
								else
									if(ListaWordVal.contains(CandidatePreferred))
										ValidCandidate.add(string);
								

							}
							}else
								ValidCandidate.addAll(SemTypes);
							
							
							if (!SemTypes.isEmpty()&&!MatchedWords.isEmpty()&&!ValidCandidate.isEmpty())
							{
							
							HashMap<String, List<HashMap<String, HashSet<String>>>> ListaSemanticaHsh = Supertabla.get(Doc);
							HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>> ListaSemanticaHshUtt = SupertablaUtt.get(Doc);
							HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>> ListaSemanticaHshUtt_List = supertablaUtt_list.get(Doc);
							
							if (ListaSemanticaHsh==null)
								ListaSemanticaHsh=new HashMap<String, List<HashMap<String, HashSet<String>>>>();
							
							if (ListaSemanticaHshUtt==null)
								ListaSemanticaHshUtt=new HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>>();
							
							if (ListaSemanticaHshUtt_List==null)
								ListaSemanticaHshUtt_List=new HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>>();
							
							HashMap<String, HashMap<String, HashSet<String>>> ListaSemanticaHsSem = ListaSemanticaHshUtt.get(Utterance);
							if (ListaSemanticaHsSem==null)
								ListaSemanticaHsSem=new HashMap<String, HashMap<String, HashSet<String>>>();	
							
							HashMap<String, HashMap<String, HashSet<String>>> ListaSemanticaHsSem_list = ListaSemanticaHshUtt_List.get(Utterance);
							if (ListaSemanticaHsSem_list==null)
								ListaSemanticaHsSem_list=new HashMap<String, HashMap<String, HashSet<String>>>();	
							
							for (String sem : SemTypes) {
								
								if (ValidCandidate.contains(sem))
								{
									
								
								List<HashMap<String, HashSet<String>>> SemCan = ListaSemanticaHsh.get(sem);
								if (SemCan==null)
									{
									SemCan=new LinkedList<HashMap<String, HashSet<String>>>();
									SemCan.add(new HashMap<String, HashSet<String>>());
									SemCan.add(new HashMap<String, HashSet<String>>());
									}
								
								HashMap<String, HashSet<String>> SemCanUtt = ListaSemanticaHsSem.get(sem);
								if (SemCanUtt==null)
									SemCanUtt=new HashMap<String, HashSet<String>>();
								
								HashMap<String, HashSet<String>> SemCanUtt_list = ListaSemanticaHsSem_list.get(sem);
								if (SemCanUtt_list==null)
									SemCanUtt_list=new HashMap<String, HashSet<String>>();
								
								
								HashSet<String> ListW;
								
								if (!Negated)
									ListW = SemCan.get(0).get(CandidatePreferred);
								else
									ListW = SemCan.get(1).get(CandidatePreferred);
								
								HashSet<String> ListWUtte=null;
								
								if (!Negated)
									ListWUtte = SemCanUtt.get(CandidatePreferred);
								else
									; 
								
								HashSet<String> ListWUtte_list=null;
								
								if (!Negated)
									ListWUtte_list = SemCanUtt_list.get(CandidatePreferred);
								else
									; 
								
								if (ListW==null)
									ListW=new HashSet<String>();
								
								if (ListWUtte==null)
									ListWUtte=new HashSet<String>();
								
								if (ListWUtte_list==null)
									ListWUtte_list=new HashSet<String>();
								
								for (String mword : MatchedWords) {
									ListW.add(mword);
									ListWUtte.add(mword);
									
								}
								
								for (String posi : Posiciones) {
									ListWUtte_list.add(posi);
								}
								
								if (!Negated)
									SemCan.get(0).put(CandidatePreferred, ListW);
								else
									SemCan.get(1).put(CandidatePreferred, ListW);
								
								if (!Negated)
									SemCanUtt.put(CandidatePreferred, ListWUtte);
								else
									;
								
								if (!Negated)
									SemCanUtt_list.put(CandidatePreferred, ListWUtte_list);
								else
									;
								
								ListaSemanticaHsh.put(sem, SemCan);
								ListaSemanticaHsSem.put(sem, SemCanUtt);
								ListaSemanticaHsSem_list.put(sem, SemCanUtt_list);
								
								if (CandidateCUI!=null)
								{
								HashMap<String, String> Term_CUI = Sem_Term_CUI.get(sem);
								if (Term_CUI==null)
									Term_CUI=new HashMap<String, String>();
								
								Term_CUI.put(CandidatePreferred, CandidateCUI);
								Sem_Term_CUI.put(sem, Term_CUI);
								}
								
								HashMap<String, HashSet<String>> SemCanMiniTabla;
								if (!Negated)
									SemCanMiniTabla = SupertablaSemPos.get(sem);
								else
									SemCanMiniTabla = SupertablaSemNeg.get(sem);
								
								if (SemCanMiniTabla==null)
									SemCanMiniTabla=new HashMap<String, HashSet<String>>();
								
								HashSet<String> ListWMiniTabla = SemCanMiniTabla.get(CandidatePreferred);
								if (ListWMiniTabla==null)
									ListWMiniTabla=new HashSet<String>();
								
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
							
							ListaSemanticaHshUtt.put(Utterance, ListaSemanticaHsSem);
							ListaSemanticaHshUtt_List.put(Utterance, ListaSemanticaHsSem_list);
							Supertabla.put(Doc, ListaSemanticaHsh);
							SupertablaUtt.put(Doc, ListaSemanticaHshUtt);
							supertablaUtt_list.put(Doc, ListaSemanticaHshUtt_List);
							
						}
	               			 
	                    	
	                    }
	                    in.next();
	                  }
	            	

	            }
	            in.next();
	        }   
			}else
				{
				for (String name : Lista) {
					supertablaUtt_list.put(name, new HashMap<String, HashMap<String,HashMap<String,HashSet<String>>>>());
					SupertablaUtt.put(name,new HashMap<String, HashMap<String,HashMap<String,HashSet<String>>>>());
				}
				System.out.println("No tengo archivo de proceso, por lo que empieza en blanco");
				}
			

		
		
	}
	

//TODO YO QUE SE
//	private static void procesaSalida(String filein,String filteredTerms, List<String> Lista,
//			HashMap<String,String> documentosListTextIn,
//			HashMap<String, HashMap<String,List<HashMap<String,HashSet<String>>>>> Supertabla,
//			HashMap<String,HashMap<String,HashSet<String>>> SupertablaSemPos,
//			HashMap<String,HashMap<String,HashSet<String>>> SupertablaSemNeg,
//			HashMap<String,String> TablaSemanticaTexto,
//			HashMap<String, HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>>> SupertablaUtt,
//			HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>> Utter_Text,
//			HashMap<String, List<String>> Utter_doc,
//			CompleteCollectionAndLog Salida,
//			String Reducido) throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
//		
//		
//		
//		HashMap<String,String> TablaSemanticaTextoInversa=new HashMap<>();
//		HashMap<String,List<String>> TablaSemanticaTextoValidas=new HashMap<>();
//
//
//		
////		try {
////			
//			
//			String csvFile = Reducido;
//	        String line = "";
//	        String cvsSplitBy = ";";
//
//	        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
//
//	            while ((line = br.readLine()) != null) {
//
//	                // use comma as separator
//	                String[] semsepa = line.split(cvsSplitBy);
//
//	               // System.out.println("Completos=> " + semsepa[0] + " , clave=>" + semsepa[1]);
//	                TablaSemanticaTexto.put(semsepa[1], semsepa[0]);
//	                TablaSemanticaTextoInversa.put(semsepa[0], semsepa[1]);
//	            }
//
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	            Salida.getLogLines().add("Error en la carga del archivo de mapeado de acronimos de semanticas");
//	            Salida.getLogLines().add("IRRECUPERABLE");
//				return ;
//	        }
//			
//			
//			
//			
//			
//			if (!filteredTerms.isEmpty())
//			{
//			String csvFile2 = filteredTerms;
//	        String line2 = "";
//
//	        try (BufferedReader br2 = new BufferedReader(new FileReader(csvFile2))) {
//
//	        	
//	        	String SemanticaA="";
//	            while ((line2 = br2.readLine()) != null) {
//
//	            	if (line2.startsWith("Semantica="))
//	            		{
//	            		SemanticaA=line2.replace("Semantica=>", "");
//	            		SemanticaA=TablaSemanticaTextoInversa.get(SemanticaA);
//	            		TablaSemanticaTextoValidas.put(SemanticaA, new LinkedList<String>());
//	            		}
//			    	 else
//				    	 {
//			    		 
//			    		 List<String> Valores = TablaSemanticaTextoValidas.get(SemanticaA);
//			    		 if (Valores==null)
//			    			 Valores= new LinkedList<>();
//			    		 
//			    		 String hijo=line2.trim().replace("++", "");
//			    		 String[] hijos=hijo.split("\\[");
//			    		 hijo=hijos[0];
//			    		 
//			    		 Valores.add(hijo);
//			    		 
//			    		 TablaSemanticaTextoValidas.put(SemanticaA, Valores);
//				    	 }
//	            }
//
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	            Salida.getLogLines().add("Error con el archvo de filtrado, se calcularan todas");
//	            TablaSemanticaTextoValidas=new HashMap<>();
//	        }            
//			}else
//	        	{
//				Salida.getLogLines().add("Error con el archvo de filtrado, se calcularan todas");
//	            TablaSemanticaTextoValidas=new HashMap<>();
//	        	}
//	        
//		
//		
//	        System.out.println("//Procesando Resultado XML P1");
//		
//			File file=new File(filein);
//			//NEW WAY 
//			int doc_ind=0;
//			XMLStreamReader in = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(file));
//			while (! (in.isEndElement() && in.getLocalName().equals("MMOs"))) {
//	            if (in.isStartElement() && in.getLocalName().equals("MMO")) {
//	            	doc_ind++;
//	            	System.out.println("Documentos->"+doc_ind);
//	            	String Doc="unknown";
//	            	String Utterance="";
//	            	while(! (in.isEndElement() && in.getLocalName().equals("MMO"))) {
//	            		
//	            		 if(in.isStartElement() && in.getLocalName().equals("PMID")) {
//	            			 Doc=in.getElementText();
//	            			 while (! (in.isEndElement() && in.getLocalName().equals("PMID")))
//	                			 in.next();
//	                       }
//	            		 
//	            		//ID
//	            		 
//	            		 if(in.isStartElement() && in.getLocalName().equals("UttText")) {
//	            			 Utterance=in.getElementText().trim();
//	            			 while (! (in.isEndElement() && in.getLocalName().equals("UttText")))
//	                			 in.next();
//	                       }
//	            		 //TODO La TextUterance esta a esta altura
//	            		
//	                    if(in.isStartElement() && in.getLocalName().equals("Candidate")) {
//	                    		String CandidatePreferred=null;
//								List<String> MatchedWords=new LinkedList<>();
//								List<String> SemTypes=new LinkedList<>();
//								boolean Negated = false;
//	                    	while (! (in.isEndElement() && in.getLocalName().equals("Candidate")))
//	                    	{
//	                    		
//	                    		if(in.isStartElement() && in.getLocalName().equals("CandidatePreferred")) {
//	                    			CandidatePreferred=in.getElementText();
//	                   			 while (! (in.isEndElement() && in.getLocalName().equals("CandidatePreferred")))
//	                       			 in.next();
//	                              }
//	                    		
//	                    		
//	                    		
//	                    		//PREFFER
//	                    		
//	                    		
//	                    		if(in.isStartElement() && in.getLocalName().equals("MatchedWords")) {
//	                    		
//	                    		StringBuffer SB=new StringBuffer();
//	                    		boolean primero=true;
//	                   			 while (! (in.isEndElement() && in.getLocalName().equals("MatchedWords")))
//	                   			 	{
//	                   				if(in.isStartElement() && in.getLocalName().equals("MatchedWord")) {
//	                   					
//	                   					if (primero)
//											primero=false;
//										else
//											SB.append("_");
//	                   					
//	                   					SB.append(in.getElementText());
//	                       			 while (! (in.isEndElement() && in.getLocalName().equals("MatchedWord")))
//	                           			 in.next();
//	                                  }
//	                   				in.next();
//	                   			 	}
//
//	                   			 	 MatchedWords.add(SB.toString());
//	                              }
//
//	                    			//MECHED
//	                    		
//	                    		
//	                    		if(in.isStartElement() && in.getLocalName().equals("SemTypes")) {
//	                   			 while (! (in.isEndElement() && in.getLocalName().equals("SemTypes")))
//	                       			 {
//	                   				if(in.isStartElement() && in.getLocalName().equals("SemType")) {
//	                   					SemTypes.add(in.getElementText());
//	                          			 while (! (in.isEndElement() && in.getLocalName().equals("SemType")))
//	                          				 in.next();
//	                              			 
//	                                     }
//	                   				 
//	                   				 in.next();
//	                       			 }
//	                              }
//	                    		
//	                    		
////	                    		if (ElemItemCan.getTagName()=="Negated")
////								{
////								String intS=ElemItemCan.getFirstChild().getTextContent();
////								if (intS.equals("1"))
////									Negated=true;
////								}
//	                    		
//	                    		if(in.isStartElement() && in.getLocalName().equals("Negated")) {
//	                    			String intS=in.getElementText();
//	                    			if (intS.equals("1"))
//	    								Negated=true;
//	                      			 while (! (in.isEndElement() && in.getLocalName().equals("Negated")))
//	                      				 in.next();
//	                          			 
//	                                 }
//	                    		
//								in.next();
//	                    	}
//	                    	
//	                    	List<String> SemTypesFinal=new LinkedList<>();
//							
//							if (!TablaSemanticaTextoValidas.keySet().isEmpty())
//							{
//							for (String string : SemTypes) {
//								if (TablaSemanticaTextoValidas.containsKey(string))
//									SemTypesFinal.add(string);
//							}
//							}else
//								SemTypesFinal.addAll(SemTypes);
//							
//							
//							SemTypes=SemTypesFinal;
//							
//							HashSet<String> ValidCandidate=new HashSet<>();
//							
//							
//							if (!TablaSemanticaTextoValidas.keySet().isEmpty())
//							{
//							
//							for (String string : SemTypes) {
//								List<String> ListaWordVal = TablaSemanticaTextoValidas.get(string);
//								
//								
//								if (ListaWordVal==null||ListaWordVal.isEmpty())
//									ValidCandidate.add(string);
//								else
//									if(ListaWordVal.contains(CandidatePreferred))
//										ValidCandidate.add(string);
//								
//
//							}
//							}else
//								ValidCandidate.addAll(SemTypes);
//							
//							
//							if (!SemTypes.isEmpty()&&!MatchedWords.isEmpty()&&!ValidCandidate.isEmpty())
//							{
//							
//							HashMap<String, List<HashMap<String, HashSet<String>>>> ListaSemanticaHsh = Supertabla.get(Doc);
//							HashMap<String, HashMap<String, HashMap<String,HashSet<String>>>> ListaSemanticaHshUtt = SupertablaUtt.get(Doc);
//							if (ListaSemanticaHsh==null)
//								ListaSemanticaHsh=new HashMap<>();
//							
//							if (ListaSemanticaHshUtt==null)
//								ListaSemanticaHshUtt=new HashMap<>();
//							
//							HashMap<String, HashMap<String, HashSet<String>>> ListaSemanticaHsSem = ListaSemanticaHshUtt.get(Utterance);
//							if (ListaSemanticaHsSem==null)
//								ListaSemanticaHsSem=new HashMap<>();
//							
//							for (String sem : SemTypes) {
//								
//								if (ValidCandidate.contains(sem))
//								{
//									
//								
//								List<HashMap<String, HashSet<String>>> SemCan = ListaSemanticaHsh.get(sem);
//								if (SemCan==null)
//									{
//									SemCan=new LinkedList<>();
//									SemCan.add(new HashMap<String, HashSet<String>>());
//									SemCan.add(new HashMap<String, HashSet<String>>());
//									}
//								
//								HashMap<String, HashSet<String>> SemCanUtt = ListaSemanticaHsSem.get(sem);
//								if (SemCanUtt==null)
//									SemCanUtt=new HashMap<>();
//								
//								HashSet<String> ListW;
//								
//								if (!Negated)
//									ListW = SemCan.get(0).get(CandidatePreferred);
//								else
//									ListW = SemCan.get(1).get(CandidatePreferred);
//								
//								HashSet<String> ListWUtte=null;
//								
//								if (!Negated)
//									ListWUtte = SemCanUtt.get(CandidatePreferred);
//								else
//									; 
//								
//								if (ListW==null)
//									ListW=new HashSet<>();
//								
//								if (ListWUtte==null)
//									ListWUtte=new HashSet<>();
//								
//								for (String mword : MatchedWords) {
//									ListW.add(mword);
//									ListWUtte.add(mword);
//								}
//								
//								if (!Negated)
//									SemCanUtt.put(CandidatePreferred, ListWUtte);
//								else
//									;
//								
//								if (!Negated)
//									SemCan.get(0).put(CandidatePreferred, ListW);
//								else
//									SemCan.get(1).put(CandidatePreferred, ListW);
//								
//								
//								ListaSemanticaHsh.put(sem, SemCan);
//								ListaSemanticaHsSem.put(sem, SemCanUtt);
//								
//								HashMap<String, HashSet<String>> SemCanMiniTabla;
//								if (!Negated)
//									SemCanMiniTabla = SupertablaSemPos.get(sem);
//								else
//									SemCanMiniTabla = SupertablaSemNeg.get(sem);
//								
//								if (SemCanMiniTabla==null)
//									SemCanMiniTabla=new HashMap<>();
//								
//								HashSet<String> ListWMiniTabla = SemCanMiniTabla.get(CandidatePreferred);
//								if (ListWMiniTabla==null)
//									ListWMiniTabla=new HashSet<>();
//								
//								for (String mword : MatchedWords) {
//									ListWMiniTabla.add(mword);
//								}
//								
//								SemCanMiniTabla.put(CandidatePreferred, ListWMiniTabla);
//								
//								if (!Negated)
//									SupertablaSemPos.put(sem, SemCanMiniTabla);
//								else
//									SupertablaSemNeg.put(sem, SemCanMiniTabla);
//								}
//							}
//							
//							Supertabla.put(Doc, ListaSemanticaHsh);
//							
//							ListaSemanticaHshUtt.put(Utterance, ListaSemanticaHsSem);
//							SupertablaUtt.put(Doc, ListaSemanticaHshUtt);
//							
//						}
//	               			 
//	                    	
//	                    }
//	                   
//	                    in.next();
//	                  }
//	            	
//
//	            }
//	            in.next();
//	        }   
//			 
//			 
//			
//			
//			
//			for (Entry<String, HashMap<String, HashMap<String, HashMap<String, HashSet<String>>>>> valueporDoc : SupertablaUtt.entrySet()) {
//				
//				List<String> ListaUtterMax = Utter_doc.get(valueporDoc.getKey());
//				if (ListaUtterMax==null)
//					ListaUtterMax=new LinkedList<String>();
//				
//				for (Entry<String, HashMap<String, HashMap<String, HashSet<String>>>> utter_Values : valueporDoc.getValue().entrySet()) {
//					
//					ListaUtterMax.add(utter_Values.getKey());
//					
//					
//					HashMap<String, HashMap<String, HashSet<String>>> UtterTextG = Utter_Text.get(utter_Values.getKey());
//					if (UtterTextG==null)
//						UtterTextG=new HashMap<>();
//					for (Entry<String, HashMap<String, HashSet<String>>> sem_terms : utter_Values.getValue().entrySet()) {
//						HashMap<String, HashSet<String>> UtterTextSem = UtterTextG.get(sem_terms.getKey());
//						if (UtterTextSem==null)
//							UtterTextSem=new HashMap<>();
//						for (Entry<String, HashSet<String>> terms_words : sem_terms.getValue().entrySet()) {
//							HashSet<String> UtterTextWords = UtterTextSem.get(terms_words.getKey());
//							if (UtterTextWords==null)
//								UtterTextWords=new HashSet<>();
//							for (String words : terms_words.getValue())
//								UtterTextWords.add(words);
//							UtterTextSem.put(terms_words.getKey(),UtterTextWords);
//						}
//						UtterTextG.put(sem_terms.getKey(),UtterTextSem);				
//					}
//					Utter_Text.put(utter_Values.getKey(),UtterTextG);
//				}
//				
//				
//				Utter_doc.put(valueporDoc.getKey(), ListaUtterMax);
//				
//			}
//
//
//		
//		
//	}
//
//	
	
	
	
	
	
	
	
	
	
	
	
	

	private static HashMap<String, HashSet<String>> processImagenes(String imagenes_File) {


		   
		   
		
		   JsonReader reader;
		try {
			reader = new JsonReader(new FileReader( imagenes_File));
			Gson gson = new Gson();
			HashMap<String, HashSet<String>> Imagenes_List =  gson.fromJson(reader, HashMap.class);
			return Imagenes_List;
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			throw new RuntimeException("El archivo no esta");
		}
   
	}

	
	@Override
	public ArrayList<ImportExportPair> getConfiguracion() {
		if (Parametros==null)
		{
			ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "List Documents findings strings txt File"));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "UMLS output xml File"));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Filter categories (txt or json) File",true));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Openi images equivalence json File"));
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "Short to complete semantic category names equivalence cvs File "));
			Parametros=ListaCampos;
			return ListaCampos;
		}
		else return Parametros;
	}

	@Override
	public String getName() {
		return "UMLS Import";
	}

	@Override
	public boolean getCloneLocalFiles() {
		return false;
	}

	

	
}
