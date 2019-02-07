/**
 * 
 */
package fdi.ucm.server.importparser.mesh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fdi.ucm.server.modelComplete.ImportExportDataEnum;
import fdi.ucm.server.modelComplete.ImportExportPair;
import fdi.ucm.server.modelComplete.LoadCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.CompleteCollectionAndLog;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteResourceElementURL;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteResourceElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;

/**
 * @author Joaquin Gayoso Cabada
 *
 */
public class LoadCollectionMesH extends LoadCollection{

	
	private static ArrayList<ImportExportPair> Parametros;
	private CompleteCollection CC;
	public static boolean consoleDebug=false;
	private CompleteElementType MayorPadre=null;
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoadCollectionMesH LC=new LoadCollectionMesH();
		LoadCollectionMesH.consoleDebug=true;
		
		ArrayList<String> AA=new ArrayList<String>();
		
		CompleteCollectionAndLog Salida=null;
		
		if (args.length==0)
			{
			AA.add("MesH.zip");
			AA.add(System.getProperty("user.home"));
			 Salida=LC.processCollecccion(AA);
			}
		else
			{
			String carpeta = args[0];
			System.out.println(carpeta);
			LinkedList<File> Archivos=new LinkedList<>();
			 File destDir=new File(carpeta);
			 if (destDir.exists())
				 listFilesForFolder(destDir,Archivos);

			Salida=LC.internalProcess(Archivos);
			
			}
			
		
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

	
	public static void listFilesForFolder(File folder, LinkedList<File> archivos) {
	    for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry,archivos);
	        } else {
	        	 if (fileEntry.getAbsolutePath().endsWith(".xml"))
	        	 	archivos.add(fileEntry);

	        }
	    }
	}
	
	
	
	@Override
	public CompleteCollectionAndLog processCollecccion(ArrayList<String> dateEntrada) {
		
		LinkedList<File> Archivos=new LinkedList<>();
		
		try {
			String fileZip = dateEntrada.get(0);
	        File destDir = new File(dateEntrada.get(1)+"/"+System.nanoTime()+"/");
	        destDir.mkdirs();
	        byte[] buffer = new byte[1024];
	        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
	        ZipEntry zipEntry = zis.getNextEntry();
	        while (zipEntry != null) {
	            File newFile = newFile(destDir, zipEntry);
	            FileOutputStream fos = new FileOutputStream(newFile);
	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	                fos.write(buffer, 0, len);
	            }
	            fos.close();
	            zipEntry = zis.getNextEntry();
	            if (newFile.getAbsolutePath().endsWith(".xml"))
	            	Archivos.add(newFile);
	           
	        }
	        zis.closeEntry();
	        zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return internalProcess(Archivos);
		
		
		
		
	}

	

	private CompleteCollectionAndLog internalProcess(LinkedList<File> Archivos) {
		CompleteCollectionAndLog Salida=new CompleteCollectionAndLog();
		CC=new CompleteCollection("MESH IMPORT", new Date()+"");
		Salida.setCollection(CC);
		Salida.setLogLines(new ArrayList<String>());
		
		CompleteGrammar ECGEN=new CompleteGrammar("ecgen", "ecgen", CC);
		CC.getMetamodelGrammar().add(ECGEN);
		
		CompleteTextElementType uId=new CompleteTextElementType("uId", ECGEN);
		ECGEN.getSons().add(uId);
		
		CompleteTextElementType publisher=new CompleteTextElementType("publisher", ECGEN);
		ECGEN.getSons().add(publisher);
		
		CompleteTextElementType note=new CompleteTextElementType("note", ECGEN);
		ECGEN.getSons().add(note);
		
		CompleteTextElementType specialty=new CompleteTextElementType("specialty", ECGEN);
		specialty.setBrowseable(true);
		ECGEN.getSons().add(specialty);
		
		
		CompleteGrammar MeSH=new CompleteGrammar("MeSH", "MeSH", CC);
		CC.getMetamodelGrammar().add(MeSH);
		
		LinkedList<CompleteTextElementType> automaticL = new LinkedList<>();
		
		CompleteTextElementType automatic = new CompleteTextElementType("Automatica", MeSH);
		automatic.setMultivalued(true);
		automatic.setBrowseable(true);
		automaticL.add(automatic);
		
		
		
		LinkedList<CompleteTextElementType> captionLis = new LinkedList<>();
		
		CompleteTextElementType caption = new CompleteTextElementType("Caption", MeSH);
		caption.setMultivalued(true);
		captionLis.add(caption);
		
		
LinkedList<CompleteResourceElementType> imageURLLis = new LinkedList<>();
		
CompleteResourceElementType imageURL = new CompleteResourceElementType("Image", MeSH);
		imageURL.setMultivalued(true);
		imageURLLis.add(imageURL);
		caption.setFather(imageURL);
		imageURL.getSons().add(caption);
		
		
		HashMap<String, CompleteElementType> Mayor=new HashMap<>();
			
		for (File file : Archivos) {
			 System.out.println(file.getAbsolutePath());
			 
			 try {
				 ProcessXML(file,CC,uId,publisher,note,specialty,MeSH,automaticL,automatic,Mayor,captionLis,caption,imageURLLis,imageURL);
			} catch (Exception e) {
				e.printStackTrace();
			}
			 
			 
			
			 
		}
		
		
		for (CompleteTextElementType automaticEle : automaticL) {
			MeSH.getSons().add(automaticEle);
		}
		
		for (CompleteResourceElementType automaticEle : imageURLLis) {
			MeSH.getSons().add(automaticEle);
		}
		
		
		return Salida;
		
	}



	private void ProcessXML(File file, CompleteCollection cC2, CompleteTextElementType uId, CompleteTextElementType publisher, CompleteTextElementType note, CompleteTextElementType specialty, 
			CompleteGrammar meSH, LinkedList<CompleteTextElementType> automaticL, CompleteTextElementType automatic,
			HashMap<String, CompleteElementType> mayor, LinkedList<CompleteTextElementType> captionLis, CompleteTextElementType caption,
			LinkedList<CompleteResourceElementType> imageURLLis, CompleteResourceElementType imageURL) throws ParserConfigurationException, SAXException, IOException {
		CompleteDocuments D=new CompleteDocuments(cC2, "", "https://meshb.nlm.nih.gov/public/img/meshLogo.jpg");
		cC2.getEstructuras().add(D);		
				
		 DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			
//			NodeList title = doc.getElementsByTagName("title");
//			if (title.getLength()>0)
//			{
//				String Desc = title.item(0).getTextContent();
//				D.setDescriptionText(Desc);
//			}
			
			NodeList nList = doc.getElementsByTagName("uId");
			if (nList.getLength()>0)
				{
				Element eElement2 = ((Element)nList.item(0));
				String GA=eElement2.getAttribute("id");
				if (GA!=null&&!GA.isEmpty())
					{
					CompleteTextElement Uid=new CompleteTextElement(uId, GA);
					D.getDescription().add(Uid);
					D.setDescriptionText(GA);
					}
				}
			
			
			
			NodeList publisherN = doc.getElementsByTagName("publisher");
			if (publisherN.getLength()>0)
			{
				String publisherD = publisherN.item(0).getTextContent();
				
				if (publisherD!=null&&!publisherD.isEmpty())
				{
				CompleteTextElement publisherE=new CompleteTextElement(publisher, publisherD);
				D.getDescription().add(publisherE);
				}
			}
			
			
			NodeList noteN = doc.getElementsByTagName("note");
			if (noteN.getLength()>0)
			{
				String noteD = noteN.item(0).getTextContent();
				
				if (noteD!=null&&!noteD.isEmpty())
				{
				CompleteTextElement noteE=new CompleteTextElement(note, noteD);
				D.getDescription().add(noteE);
				}
			}
			
			
			NodeList specialtyN = doc.getElementsByTagName("specialty");
			if (specialtyN.getLength()>0)
			{
				String specialtyD = specialtyN.item(0).getTextContent();
				
				if (specialtyD!=null&&!specialtyD.isEmpty())
				{
				CompleteTextElement specialtyE=new CompleteTextElement(specialty, specialtyD);
				D.getDescription().add(specialtyE);
				}
			}
			
			
			
			NodeList MeSHN = doc.getElementsByTagName("MeSH");
			if (MeSHN.getLength()>0)
			{
				NodeList MeSHNH = ((Element)MeSHN.item(0)).getElementsByTagName("automatic");
				for (int temp2 = 0; temp2 < MeSHNH.getLength(); temp2++)
				{
					Node nNodeH = MeSHNH.item(temp2);
					String noteD = nNodeH.getTextContent();
					if (!noteD.isEmpty())
						{
						while (automaticL.size()<=temp2)
							{
							CompleteTextElementType automatic2 = new CompleteTextElementType(automatic.getName(), meSH);
							automatic2.setClassOfIterator(automatic);
							automaticL.add(automatic2);
							}
						
						CompleteTextElementType mio=automaticL.get(temp2);
						
						CompleteTextElement automa=new CompleteTextElement(mio, noteD);
						D.getDescription().add(automa);
						
						}
					
				}
				
				
				
				NodeList majorH = ((Element)MeSHN.item(0)).getElementsByTagName("major");
				for (int temp2 = 0; temp2 < majorH.getLength(); temp2++)
				{
					Node nNodeH = majorH.item(temp2);
					String noteD = nNodeH.getTextContent();
					if (!noteD.isEmpty())
						{
						
						String[] tags=noteD.split("/");
						String Acu="";
						for (String string : tags) {
							String AcuN=Acu+"/"+string.toLowerCase();
							
							CompleteElementType Previo = mayor.get(AcuN);
							if (Previo==null)
							{
							//NO ESTA
								Previo = new CompleteElementType(string, meSH);
								Previo.setSelectable(true);
								Previo.setBrowseable(true);
								
								if (Acu.isEmpty())
									{
									if (MayorPadre==null)
										{
										MayorPadre=new CompleteElementType("Manual", meSH);
										MayorPadre.setBrowseable(true);
										meSH.getSons().add(MayorPadre);
										}

									//SOBRE PAPA
									MayorPadre.getSons().add(Previo);
									}
								else
								{	
									CompleteElementType PrevioPA = mayor.get(Acu);
									PrevioPA.getSons().add(Previo);
									Previo.setFather(PrevioPA);
									//TENGO PAPA Ele
								}
								
								mayor.put(AcuN, Previo);
								
								
								
							}
							
							
							CompleteElement automa=new CompleteElement(Previo);
							D.getDescription().add(automa);
							
							
							Acu=AcuN;
						}
						

						
						}
					
				}
				
				
			}
			
			
			NodeList parentImage = doc.getElementsByTagName("parentImage");
			for (int temp2 = 0; temp2 < parentImage.getLength(); temp2++)
			{
				NodeList parenIma = ((Element)parentImage.item(0)).getElementsByTagName("caption");
				NodeList urlI = ((Element)parentImage.item(0)).getElementsByTagName("url");
				
				if (urlI.getLength()!=0)
				{
				while (imageURLLis.size()<=temp2)
					{
					CompleteResourceElementType imageURL2 = new CompleteResourceElementType(imageURL.getName(), meSH);
					imageURL2.setClassOfIterator(imageURL);
					imageURLLis.add(imageURL2);
					
					CompleteTextElementType caption2 = new CompleteTextElementType(caption.getName(), meSH);
					caption2.setClassOfIterator(caption);
					captionLis.add(caption2);
					
					imageURL2.getSons().add(caption2);
					caption2.setFather(imageURL2);

					}
				
				CompleteResourceElementType mio=imageURLLis.get(temp2);
				CompleteTextElementType mioC=captionLis.get(temp2);
				
				if (parenIma.getLength()>0)
					{
					CompleteTextElement automa=new CompleteTextElement(mioC, parenIma.item(0).getTextContent());
					D.getDescription().add(automa);
					}
				
					CompleteResourceElementURL automa=new CompleteResourceElementURL(mio, urlI.item(0).getTextContent());
					D.getDescription().add(automa);
				
				}
				
			}
			
	/**		NodeList Hijos=((Element)nList.item(0)).getChildNodes();
			for (int temp2 = 0; temp2 < Hijos.getLength(); temp2++)
			{
				Node nNodeH = Hijos.item(temp2);
				if (nNodeH.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement2 = (Element) nNodeH;
					
					
					
				}
			}
			**/

	}



	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
         
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
         
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
         
        return destFile;
    }
	

	@Override
	public ArrayList<ImportExportPair> getConfiguracion() {
		if (Parametros==null)
		{
			ArrayList<ImportExportPair> ListaCampos=new ArrayList<ImportExportPair>();
			ListaCampos.add(new ImportExportPair(ImportExportDataEnum.File, "XML zip File"));
			Parametros=ListaCampos;
			return ListaCampos;
		}
		else return Parametros;
	}

	@Override
	public String getName() {
		return "MESH XML Import";
	}

	@Override
	public boolean getCloneLocalFiles() {
		return false;
	}

	
	/**
	
	
	
	
	
	try {
		
		HashMap<String,CompleteDocuments> tablaEqui=new HashMap<String,CompleteDocuments>();
		HashMap<CompleteLinkElement, String> tablaLink=new HashMap<CompleteLinkElement,String>();
		HashMap<CompleteLinkElement, CompleteDocuments> tablaLinkPadre=new HashMap<CompleteLinkElement,CompleteDocuments>();
		List<CompleteLinkElement> posProcessLink=new ArrayList<CompleteLinkElement>();
		
		HashMap<CompleteGrammar,HashMap<String,CompleteElementType>> ListaElem=new HashMap<CompleteGrammar,HashMap<String,CompleteElementType>>();
		HashMap<String,CompleteGrammar> ListaGram=new HashMap<String,CompleteGrammar>();
		
		CompleteCollectionAndLog Salida=new CompleteCollectionAndLog();
		CC=new CompleteCollection("XML IMPORT", new Date()+"");
		Salida.setCollection(CC);
		Salida.setLogLines(new ArrayList<String>());
		
		String FileS = dateEntrada.get(0);
		File XMLD=new File(FileS);
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(XMLD);
		
		NodeList nList = doc.getElementsByTagName("records");
		nList = ((Element)nList.item(0)).getElementsByTagName("record");
		
		
		for (int temp = 0; temp < nList.getLength(); temp++)
		{
			
			try {
				Node nNode = nList.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Element eElement = (Element) nNode;
					
					
					String descript = eElement.getElementsByTagName("description").item(0).getTextContent();
					String icon = eElement.getElementsByTagName("icon").item(0).getTextContent();
					CompleteDocuments CD=new CompleteDocuments(CC, descript, icon);
					String Id=eElement.getAttribute("id");
					if (!(Id.isEmpty()||Id==null))
						tablaEqui.put(Id, CD);

					CC.getEstructuras().add(CD);
					
					try {
						NodeList Resto=eElement.getChildNodes();
						for (int temp2 = 0; temp2 < Resto.getLength(); temp2++)
						{
							
							try {
								Node nNodeH = Resto.item(temp2);
								
								if (nNodeH.getNodeType() == Node.ELEMENT_NODE) {
									
									
									
									Element eElement2 = (Element) nNodeH;
									if (!(eElement2.getTagName().equals("description")||(eElement2.getTagName().equals("icon"))))
											{
										String GA=eElement2.getAttribute("gram");
										if (GA.isEmpty()||GA==null)
											GA="Ungrammar";
										
										CompleteGrammar Grama=ListaGram.get(GA);
										
										if (Grama==null)
											{
											Grama=new CompleteGrammar(GA, GA, CC);
											CC.getMetamodelGrammar().add(Grama);
											ListaGram.put(GA, Grama);
											ListaElem.put(Grama, new HashMap<String,CompleteElementType>());
											Salida.getLogLines().add("Creada la Gramatica-> " +GA);
											}
											
										String ElemTy=eElement2.getTagName();
										
										HashMap<String, CompleteElementType> ListaElemTy = ListaElem.get(Grama);
										CompleteElementType ElementoTy=ListaElemTy.get(ElemTy);
										
										if (ElementoTy==null)
										{
										
											String rara=eElement2.getAttribute("relation");	
											if (rara!=null&&rara.equals("true"))
											{
												ElementoTy=new CompleteLinkElementType(ElemTy, Grama);
												Grama.getSons().add(ElementoTy);
												ListaElemTy.put(ElemTy, ElementoTy);
												ListaElem.put(Grama, ListaElemTy);
												Salida.getLogLines().add("Creada la Gramatica-> " +ElemTy);
											}
											else
											{	
											ElementoTy=new CompleteTextElementType(ElemTy, Grama);
											Grama.getSons().add(ElementoTy);
											ListaElemTy.put(ElemTy, ElementoTy);
											ListaElem.put(Grama, ListaElemTy);
											Salida.getLogLines().add("Creada la Gramatica-> " +ElemTy);
											}
										}
										
										String Valor=eElement2.getTextContent();
										
										if (ElementoTy instanceof CompleteTextElementType)
										{
										CompleteTextElement EE=new CompleteTextElement((CompleteTextElementType) ElementoTy,Valor);
										CD.getDescription().add(EE);
										}else
											if (ElementoTy instanceof CompleteLinkElementType)
											{
												CompleteLinkElement EE=new CompleteLinkElement((CompleteLinkElementType) ElementoTy,null);
												CD.getDescription().add(EE);
												posProcessLink.add(EE);
												tablaLink.put(EE, Valor);
												tablaLinkPadre.put(EE, CD);
											}
											}
									
									
									
								
								}
							} catch (Exception e) {
								Salida.getLogLines().add("Error in record "+temp);
							}
							
							
							
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
					
					
				
				}
			} catch (Exception e) {
				Salida.getLogLines().add("Error in record "+temp);
			}
			
			
			
		}
		
//		CompleteCollectionAndLog Salida=new CompleteCollectionAndLog();
//		CC=new CompleteCollection("MedPix", new Date()+"");
//		Salida.setCollection(CC);
//		Logs=new ArrayList<String>();
//		Salida.setLogLines(Logs);
//		encounterID=new HashMap<String,CompleteDocuments>();
//		topicID=new HashMap<String,List<CompleteDocuments>>();
//		ListImageEncounter=new ArrayList<CompleteElementTypeencounterIDImage>();
//		ListImageEncounterTopics=new ArrayList<CompleteElementTypeencounterIDImage>();
//		ListTopicID=new ArrayList<CompleteElementTypetopicIDTC>();
//		
//		ProcesaCasos();
//		ProcesaCasoID();
//		ProcesaTopics();
//		//AQUI se puede trabajar
		
		for (CompleteLinkElement completeLinkElement : posProcessLink) {
			String valor = tablaLink.get(completeLinkElement);
			CompleteDocuments dd=tablaEqui.get(valor);
			if (dd==null)
			tablaLinkPadre.get(completeLinkElement).getDescription().remove(completeLinkElement);
			else	
			completeLinkElement.setValue(dd);
		}
		
		
		
		return Salida;
	} catch (Exception e) {
		e.printStackTrace();
		return null;
	}
	**/
	
}
