package IR;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PositionalIndex {
    public static void main(String[] args) throws IOException {
    	Map<String, Map<String, Integer>> termFrequency = GenerateTF();
    	Map<String, Map<String, Double>> tfWeights = GenerateTFWeight(termFrequency);
    	Map<String, Double> df = GenerateDF(termFrequency);
    	Map<String, Double> idf = GenerateIDF(termFrequency);
    	Map<String, Map<String, Double>> tf_idf = GenerateTF_IDF(tfWeights, idf);
    	Map<String, Double> doclengths = GenerateDocLength(tf_idf);
    	Map<String, Map<String, Double>> normalizedTfIdfMatrix = GenerateNormlized(tf_idf, doclengths);
    	
 
      	
    	
    	Scanner scan = new Scanner(System.in);  

    	while (true) {
    	    System.out.println("==============\n  IR Project\n==============\n");
    	    System.out.println("1) Show TF And TF Weight\n\n2) Show DF And IDF\n\n3) Show TF-IDF, Doc Length And Normalized TF-IDF\n\n4) Scan Query\n\n5) Exit\n\n\n");
   	    
    	    int choice = scan.nextInt();  
    	    scan.nextLine();  

    	    if (choice == 1) {
    	        ShowTF(termFrequency);
    	        ShowTFWeight(tfWeights);
    	    } 
    	    else if (choice == 2) {
    	        ShowDFAndIDF(idf, df);
    	    } 
    	    else if (choice == 3) {
    	        ShowTF_IDF(tf_idf);
    	        ShowDocLength(doclengths);
    	        ShowNormalizedTF_IDF(normalizedTfIdfMatrix);
    	    } 
    	    else if (choice == 4) {
    	    	
    	    	boolean isBoolQuery = false;
    	    	boolean isANDNOTBoolQuery = false;
    			System.out.println("Enter your query:");
    	        String query = scan.nextLine();
    	        if(query.contains("AND")) isBoolQuery = true;
    	        
    	        String processedQuery = "";
    	        String excludedPart = "";
    	        if(isBoolQuery) {
   	            
    	            if (query.contains("AND NOT")) {
    	            	isANDNOTBoolQuery = true;
    	                int index = query.indexOf("AND NOT");
    	                processedQuery = query.substring(0, index).replace("AND ", "").trim();
    	                excludedPart = query.substring(index + 7).trim(); // Skip "AND NOT" and trim the rest
    	            } else {
    	                
    	                processedQuery = query.replace("AND ", "").trim();
    	            }          
    	        }else processedQuery = query;
    	        
    	        //norm the query
    	        String[] words = processedQuery.split(" ");
    	        String[] excludeWords = excludedPart.split(" ");
    	        String normQuery = "";
    	        String normExcludeQuery = "";
    	        for(String word : words) {
    	        	word = normalizeTerm(word);
    	        	normQuery += word + " ";
    	        }
    	        for(String word : excludeWords) {
    	        	word = normalizeTerm(word);
    	        	normExcludeQuery += word + " ";
    	        }
    	        normQuery = normQuery.trim();
    	        normExcludeQuery = normExcludeQuery.trim();
    	        
    	        
    	        normQuery = normQuery.trim();  
    	        Map<String, Double> normalizedValues = ProcessQuery(normQuery, idf, termFrequency, isANDNOTBoolQuery, normExcludeQuery);
    	        Map<String, Double> simDocs = GenerateDocTables(normalizedValues, normQuery, getSimilarDocs(normQuery, termFrequency, isANDNOTBoolQuery, normExcludeQuery), termFrequency, idf, doclengths, isANDNOTBoolQuery, normExcludeQuery);
    	        printSortedDocs(simDocs);
    	    } 
    	    else {
    	        break;  
    	    }

    	    System.out.println("\n\n <<Click Enter To Back>>");
    	    scan.nextLine();  
    	}
    }
    
    
    
    //TASK (1)
    
    static Map<String, Map<String, Integer>> GenerateTF() throws IOException {
    	String fileName = "C:\\Users\\TM\\eclipse-workspace\\IRProject\\src\\IR\\PositionalIndexOutput.txt";

        Map<String, Map<String, Integer>> termFrequency = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;

        while ((line = br.readLine()) != null) {
        	// line = angel	 9.txt:0; 8.txt:0; 7.txt:0;
            String[] parts = line.split("\t");
            String term = parts[0];
            String[] docs = parts[1].split("; ");

            // docs = [9.txt:0, 8.txt:0, 7.txt:0]
            
            Map<String, Integer> docFrequency = new HashMap<>();
            for (String doc : docs) {
            	
            	// doc = 9.txt:0
                String[] docParts = doc.split(":");
                String docId = docParts[0];
                int freq = docParts[1].split(",").length; // Positions count frequency
                docFrequency.put(docId, freq);
            }
            termFrequency.put(term, docFrequency);
        }

        br.close();
        return termFrequency;
    }
    
    static void ShowTF(Map<String, Map<String, Integer>> termFrequency) throws IOException {
        
        int docColumnWidth = 8;

        
        int maxTermLength = termFrequency.keySet().stream()
                                          .mapToInt(String::length)
                                          .max()
                                          .orElse(0);

        
        int termColumnWidth = maxTermLength + 2;

        System.out.println("\nTerm Frequency (TF):");

        printTableBorder(termColumnWidth, docColumnWidth);

        System.out.print("|");
        System.out.print(String.format(" %-"+termColumnWidth+"s |", "Term"));
        for (int i = 1; i <= 10; i++) {
            System.out.print(String.format("  %d.txt |  ", i));
        }
        System.out.println();

        printTableBorder(termColumnWidth, docColumnWidth);

        for (String term : termFrequency.keySet()) {
            System.out.print("|");
            System.out.print(String.format(" %-"+termColumnWidth+"s |", term));  
            for (int i = 1; i <= 10; i++) {
                String docId = i + ".txt";
                int tfValue = termFrequency.getOrDefault(term, new HashMap<>()).getOrDefault(docId, 0);
                System.out.print(String.format(" %-"+docColumnWidth+"d |", tfValue));  
            }
            System.out.println();
            printTableBorder(termColumnWidth, docColumnWidth);
        }
    }

    static void printTableBorder(int termColumnWidth, int docColumnWidth) {
        System.out.print("-");
        System.out.print("-".repeat(termColumnWidth + 2));  
        for (int i = 1; i <= 10; i++) {
            System.out.print("-".repeat(docColumnWidth + 2));  
        }
        System.out.println("-");
    }

    static Map<String, Map<String, Double>> GenerateTFWeight(Map<String, Map<String, Integer>> termFrequency) {
        Map<String, Map<String, Double>> tfWeights = new HashMap<>();

        for (String term : termFrequency.keySet()) {
            Map<String, Double> tfWeightForTerm = new HashMap<>();
            for (String doc : termFrequency.get(term).keySet()) {
                int tf = termFrequency.get(term).get(doc);
                // 1 + log10(tf)
                double tfWeight = 1 + Math.log10(tf);  
                tfWeightForTerm.put(doc, tfWeight);
            }
            tfWeights.put(term, tfWeightForTerm);
        }
        return tfWeights;
    }
    
    static void ShowTFWeight(Map<String, Map<String, Double>> tfWeights) {

    
        System.out.println("\nTF Weight (1 + log10(tf))");
        
        String header = String.format("| %-15s |", "Term");
        for (int i = 1; i <= 10; i++) {
            header += String.format(" %d.txt |", i);
        }
        System.out.println("-------------------" + "-".repeat(12 * 10) + "-");
        System.out.println(header);
        System.out.println("-------------------" + "-".repeat(12 * 10) + "-");

        for (String term : tfWeights.keySet()) {
            String row = String.format("| %-15s |", term);
            for (int i = 1; i <= 10; i++) {
                String docId = i + ".txt";
                double tfWeightValue = tfWeights.getOrDefault(term, new HashMap<>()).getOrDefault(docId, 0.0);
                row += String.format(" %.2f  |", tfWeightValue);
            }
            System.out.println(row);
            System.out.println("-------------------" + "-".repeat(12 * 10) + "-");
        }
    }

    
    
    //TASK (2)
    
    static  Map<String, Double> GenerateDF(Map<String, Map<String, Integer>> termFrequency) {
    	// df : count num of appaering in docs
    	Map<String, Double> df = new HashMap<>();

        for (String term : termFrequency.keySet()) {
            double docCount = termFrequency.get(term).size();
            df.put(term, docCount);
        }
        return df;
    }
       
    static  Map<String, Double> GenerateIDF(Map<String, Map<String, Integer>> termFrequency) {
    	Map<String, Double> idf = new HashMap<>();
        int totalDocs = 10; // عدد الملفات

        for (String term : termFrequency.keySet()) {
            int docCount = termFrequency.get(term).size();
            double idfValue = Math.log10((double) totalDocs / docCount);
            idf.put(term, idfValue);
        }
        return idf;
    }
    
    static void ShowDFAndIDF(Map<String, Double> idf, Map<String, Double> df) {

        System.out.println("---------------------------------------------");
        System.out.println("| Term            | DF         | IDF        |");
        System.out.println("---------------------------------------------");

        for (String term : idf.keySet()) {
            int dfValue = df.getOrDefault(term, 0.0).intValue();  
            
            System.out.printf("| %-15s | %-10d | %.5f     |\n", term, dfValue, idf.get(term));
        }

        System.out.println("---------------------------------------------");
    }


    
    //TASK (3)
    
    
    static Map<String, Map<String, Double>> GenerateTF_IDF(Map<String, Map<String, Double>> tfweight, Map<String, Double> idf){
        Map<String, Map<String, Double>> tfIdfMatrix = new HashMap<>();

        for (String term : tfweight.keySet()) {
            Map<String, Double> tfIdfForTerm = new HashMap<>();
            for (String doc : tfweight.get(term).keySet()) {
                double tfw = tfweight.get(term).get(doc);
                double idfValue = idf.getOrDefault(term, 0.0); 
                
                double tfIdf = tfw * idfValue;
                
                tfIdfForTerm.put(doc, tfIdf);
            }
            tfIdfMatrix.put(term, tfIdfForTerm);
        }
        return tfIdfMatrix;
    }
      
    static void ShowTF_IDF(Map<String, Map<String, Double>> tfIdfMatrix) {
        
        System.out.println("\nTF-IDF");
        
        String header = String.format("| %-15s |", "Term");
        for (int i = 1; i <= 10; i++) {
            header += String.format(" %d.txt  | ", i);
        }
        System.out.println("-------------------" + "-".repeat(12 * 10) + "-");
        System.out.println(header);
        System.out.println("-------------------" + "-".repeat(12 * 10) + "-");

        for (String term : tfIdfMatrix.keySet()) {
            String row = String.format("| %-15s |", term);
            for (int i = 1; i <= 10; i++) {
                String docId = i + ".txt";
                double tfIdfValue = tfIdfMatrix.getOrDefault(term, new HashMap<>()).getOrDefault(docId, 0.0);
                row += String.format(" %.4f  |", tfIdfValue);//2
            }
            System.out.println(row);
            System.out.println("--------------------" + "-".repeat(12 * 10) + "-");
        }
    }
    
    static Map<String, Double> GenerateDocLength(Map<String, Map<String, Double>> tfIdfMatrix) {
    	Map<String, Double> docSquaredSums = new HashMap<>();
        for (String term : tfIdfMatrix.keySet()) {
            for (String doc : tfIdfMatrix.get(term).keySet()) {
                double tfIdfValue = tfIdfMatrix.get(term).get(doc);
                docSquaredSums.put(doc, docSquaredSums.getOrDefault(doc, 0.0) + Math.pow(tfIdfValue, 2));
            }
        }

        Map<String, Double> docLengths = new HashMap<>();
        for (String docId : docSquaredSums.keySet()) {
            double sumOfSquares = docSquaredSums.get(docId);
            double length = Math.sqrt(sumOfSquares);  
            docLengths.put(docId, length);
        }
        return docLengths;
    }
    
    static void ShowDocLength(Map<String, Double> docLengths) {

        System.out.println("\nDocId and Length");
        System.out.println("---------------------------------");
        System.out.println("| DocId      | Length           |");
        System.out.println("---------------------------------");

        for (String docId : docLengths.keySet()) {
            System.out.printf("| %-10s | %.6f         |\n", docId, docLengths.get(docId));
        }

        System.out.println("---------------------------------");
    }
    
    static Map<String, Map<String, Double>> GenerateNormlized(Map<String, Map<String, Double>> tfIdfMatrix, Map<String, Double> docLengths){
    	Map<String, Map<String, Double>> normalizedTfIdfMatrix = new HashMap<>();

        for (String term : tfIdfMatrix.keySet()) {
            Map<String, Double> normalizedTfIdfForTerm = new HashMap<>();
            for (String doc : tfIdfMatrix.get(term).keySet()) {
                double tfIdfValue = tfIdfMatrix.get(term).get(doc);
                double docLength = docLengths.getOrDefault(doc, 1.0); // if not found then the default is 1.0 to avoid division by zero error
                double normalizedTfIdf = tfIdfValue / docLength;
                normalizedTfIdfForTerm.put(doc, normalizedTfIdf);
            }
            normalizedTfIdfMatrix.put(term, normalizedTfIdfForTerm);
        }
        return normalizedTfIdfMatrix;
    }
    
    static void ShowNormalizedTF_IDF(Map<String, Map<String, Double>> normalizedTfIdfMatrix) {

        System.out.println("\nNormalized TF-IDF");
        

        String header = String.format("| %-15s |", "Term");
        for (int i = 1; i <= 10; i++) {
            header += String.format(" %d.txt   |", + i);
        }
        System.out.println("-------------------" + "-".repeat(12 * 10) + "-");
        System.out.println(header);
        System.out.println("-------------------" + "-".repeat(12 * 10) + "-");


        for (String term : normalizedTfIdfMatrix.keySet()) {
            String row = String.format("| %-15s |", term);
            for (int i = 1; i <= 10; i++) {
                String docId = i + ".txt";
                double normalizedTfIdfValue = normalizedTfIdfMatrix.getOrDefault(term, new HashMap<>()).getOrDefault(docId, 0.0);
                
                row += String.format(" %.4f  |", normalizedTfIdfValue);
            }
            System.out.println(row);
            System.out.println("-------------------" + "-".repeat(12 * 10) + "+");
        }
    }

    
    //TASK (4)
    
    public static Map<String, Map<String, Integer>> getPositions(
            String filePath, List<String> docNames, List<String> terms) {
        // Map to store the final result
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Initialize the result map with the terms
            for (String term : terms) {
                result.put(term, new LinkedHashMap<>());
            }

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) continue;

                String term = parts[0];
                String occurrences = parts[1];

                if (terms.contains(term)) {
                    String[] docOccurrences = occurrences.split(";\s*");
                    for (String docOccurrence : docOccurrences) {
                        String[] docAndPos = docOccurrence.split(":");
                        if (docAndPos.length < 2) continue;

                        String docName = docAndPos[0];
                        int position;
                        try {
                            position = Integer.parseInt(docAndPos[1].trim());
                        } catch (NumberFormatException e) {
                            continue; // Skip invalid positions
                        }

                        if (docNames.contains(docName)) {
                            result.get(term).put(docName, position);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static List<String> findDocsWithSequentialPositions(
            Map<String, Map<String, Integer>> positions, List<String> terms) {
        List<String> validDocs = new ArrayList<>();

        // Check if there are enough terms to process
        if (terms.isEmpty()) {
            return validDocs; // No terms provided
        }

        // Get the first term and its document positions
        String firstTerm = terms.get(0);
        Map<String, Integer> firstTermDocs = positions.getOrDefault(firstTerm, new HashMap<>());

        // Iterate through each document in the first term's map
        for (Map.Entry<String, Integer> entry : firstTermDocs.entrySet()) {
            String docName = entry.getKey();
            int currentPosition = entry.getValue();
            boolean isSequential = true;

            // Check the positions for subsequent terms in the same document
            for (int i = 1; i < terms.size(); i++) {
                String term = terms.get(i);
                Map<String, Integer> termDocs = positions.getOrDefault(term, new HashMap<>());

                // Get the position of the current term in the same document
                Integer termPosition = termDocs.get(docName);

                // If the term is not in the same document or not sequential, mark as invalid
                if (termPosition == null || termPosition != currentPosition + 1) {
                    isSequential = false;
                    break;
                }

                // Move to the next expected position
                currentPosition = termPosition;
            }

            // If all terms have sequential positions, add the document to the valid list
            if (isSequential) {
                validDocs.add(docName);
            }
        }

        return validDocs;
    }

    
    public static Map<String, Double> ProcessQuery(String query, Map<String, Double> idf, 
        Map<String, Map<String, Integer>> tfTable, boolean isANDNOTBoolQuery, String excludeQuery) {
		String[] words = query.split(" ");
		String normQuery = "";
		for (String word : words) {
			word = normalizeTerm(word);
			normQuery += word + " ";
		}
		normQuery = normQuery.trim();  
		
		String[] terms = normQuery.split("\\s+");
		Map<String, Integer> tfRawMap = new HashMap<>();
		Map<String, Double> tfIdfMap = new HashMap<>();
		
		for (String term : terms) {
			tfRawMap.put(term, tfRawMap.getOrDefault(term, 0) + 1);
		}
		
		for (String term : tfRawMap.keySet()) {
			int tfRaw = tfRawMap.get(term);
			double tfLog = 1 + Math.log10(tfRaw); 
			double idfValue = idf.getOrDefault(term, 0.0); 
			double tfIdf = tfLog * idfValue; 
			tfIdfMap.put(term, tfIdf);
		}
		
		double queryLength = calculateQueryLength(tfIdfMap);
		
		Map<String, Double> normalizedValues = new HashMap<>();
		
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.println("| Term            | TF-Raw  | 1 + Log(TF-Raw)   | IDF     | TF*IDF     | Normalized |");
		System.out.println("-------------------------------------------------------------------------------------");
		
		for (String term : tfRawMap.keySet()) {
			int tfRawQuery = tfRawMap.get(term);
			
			double tfLogQuery = 1 + Math.log10(tfRawQuery);
			
			double idfValue = idf.getOrDefault(term, 0.0);
			
			double tfIdfQuery = tfLogQuery * idfValue;
			
			double normalizedQuery = tfIdfQuery / queryLength;
			
			normalizedValues.put(term, normalizedQuery);
			
			System.out.printf("| %-15s | %-7d | %-17.2f | %-7.3f | %-10.6f | %-10.6f |\n", 
			term, tfRawQuery, tfLogQuery, idfValue, tfIdfQuery, normalizedQuery);
		}
		
		System.out.println("-------------------------------------------------------------------------------------");
		
		System.out.printf("\nQuery Length = %.6f\n", queryLength);
		
		List<String> matchingDocs = getSimilarDocs(normQuery, tfTable, isANDNOTBoolQuery, excludeQuery);
		
        String filePath = "C:\\Users\\TM\\eclipse-workspace\\IRProject\\src\\IR\\PositionalIndexOutput.txt";
        List<String> term = Arrays.asList(normQuery.split("\\s+"));
        Map<String,Map<String,Integer>>results = getPositions(filePath, matchingDocs, term);
        List<String> out = findDocsWithSequentialPositions(results, term);
        matchingDocs = out;

        
		System.out.print("\nMatching Documents:");
        if(matchingDocs.isEmpty()) 
            System.out.print(" Not Found!\n");
        System.out.println();
		for (String doc : matchingDocs) {
			System.out.println(doc);
		}
		
		return normalizedValues;
	}
  
    public static Map<String, Double> GenerateDocTables(Map<String, Double> normalizedQueryValues, String query, 
            List<String> similarDocs, Map<String, Map<String, Integer>> tfMap, 
            Map<String, Double> idf, Map<String, Double> docLengths, 
            boolean isANDNOTBoolQuery, String excludeQuery) {
		
    	Map<String, Double> docSimilarities = new HashMap<>();
		
		String[] terms = query.split(" ");
		String normQuery = "";
		for (String word : terms) {
			word = normalizeTerm(word);
			normQuery += word + " ";
		}
		normQuery = normQuery.trim();  // Create a normalized query string
		String[] normalizedTerms = normQuery.split("\\s+");
		////////
        String filePath = "C:\\Users\\TM\\eclipse-workspace\\IRProject\\src\\IR\\PositionalIndexOutput.txt";
        List<String> termp = Arrays.asList(normQuery.split("\\s+"));
        Map<String,Map<String,Integer>>results = getPositions(filePath, similarDocs, termp);
        List<String> out = findDocsWithSequentialPositions(results, termp);
//        System.out.println("\n\n\nfunction(2)");
//        for(String n : out) System.out.println(n);
//        System.out.println("\n\n\n");
        similarDocs = out;
		
		
		////////
		for (String doc : similarDocs) {
			double docLength = docLengths.getOrDefault(doc, 1.0); // Default to 1.0 if not found
			
			System.out.println("\nDocument: " + doc);
			System.out.println("-------------------------------");
			System.out.println("| Term            | Product   |");
			System.out.println("-------------------------------");
			
			double productSum = 0.0; 
			
			for (int i = 0; i < normalizedTerms.length; i++) {
				String term = normalizedTerms[i];
				
				int tfRaw = tfMap.getOrDefault(term, new HashMap<>()).getOrDefault(doc, 0);
				
				double tfLog = (tfRaw > 0) ? 1 + Math.log10(tfRaw) : 0.0;
				
				double idfValue = idf.getOrDefault(term, 0.0);
				
				double tfIdf = tfLog * idfValue;
				
				double normalizedDoc = (docLength > 0) ? tfIdf / docLength : 0.0;
				
				double normalizedQueryValue = normalizedQueryValues.getOrDefault(term, 0.0);
				
				double product = normalizedDoc * normalizedQueryValue;
				productSum += product;
				
				System.out.printf("| %-15s | %-10.6f|\n", term, product);
			}
			
			System.out.println("-------------------------------");
			
			System.out.printf("Sum = %.6f\n", productSum);
			System.out.printf("Similarity (q, %s) = %.6f\n\n", doc, productSum);
			
			docSimilarities.put(doc, productSum);
		}
		
		return docSimilarities;
	}
    
    public static void printSortedDocs(Map<String, Double> docSimilarities) {
        List<Map.Entry<String, Double>> sortedDocs = new ArrayList<>(docSimilarities.entrySet());

        // bubble sort
        for (int i = 0; i < sortedDocs.size(); i++) {
            for (int j = 0; j < sortedDocs.size() - i - 1; j++) {
                if (sortedDocs.get(j).getValue() < sortedDocs.get(j + 1).getValue()) {
                    Map.Entry<String, Double> temp = sortedDocs.get(j);
                    sortedDocs.set(j, sortedDocs.get(j + 1));
                    sortedDocs.set(j + 1, temp);
                }
            }
        }

        System.out.print("Documents sorted by similarity:");
        if(sortedDocs.isEmpty()) 
        System.out.print(" Not Found!\n");
        System.out.println();
        for (Map.Entry<String, Double> entry : sortedDocs) {
            System.out.println(entry.getKey()); 
        }
    }

   
    public static double calculateQueryLength(Map<String, Double> tfIdfMap) {
        double queryLengthSquared = 0.0;

        for (double tfIdf : tfIdfMap.values()) {
            queryLengthSquared += Math.pow(tfIdf, 2);
        }

        return Math.sqrt(queryLengthSquared);
    }
    

    public static List<String> getSimilarDocs(String query, Map<String, Map<String, Integer>> tfMap, 
            boolean isANDNOTBoolQuery, String excludeQuery) {
    	
		String[] queryWords = query.toLowerCase().split("\\s+");
		
		List<Set<String>> docLists = new ArrayList<>();
		
		// find all documents that contain that word
		for (String word : queryWords) {
			word = normalizeTerm(word); 
			if (tfMap.containsKey(word)) {
				Map<String, Integer> wordDocs = tfMap.get(word);
				docLists.add(new HashSet<>(wordDocs.keySet()));
			} else {
				return new ArrayList<>();
			}
		}
		
		//Find intersection 
		Set<String> commonDocs = new HashSet<>(docLists.get(0)); // Start with the first word's document set
		for (int i = 1; i < docLists.size(); i++) {
			commonDocs.retainAll(docLists.get(i)); // Retain only common documents
		}
		
		//  If AND-NOT then exclude documents
		if (isANDNOTBoolQuery) {
			String[] excludeWords = excludeQuery.toLowerCase().split("\\s+");
			List<String> ex = new ArrayList<String>();
			for(String n : excludeWords) ex.add(n);
			List<Set<String>> excludeDocLists = new ArrayList<>();

			for (String word : excludeWords) {
				word = normalizeTerm(word); 
				if (tfMap.containsKey(word)) {
					Map<String, Integer> wordDocs = tfMap.get(word);
					excludeDocLists.add(new HashSet<>(wordDocs.keySet()));
				} else {
					excludeDocLists.add(new HashSet<>()); 
				}
			}
			
			// Find the intersection of documents for excludeQuery
			Set<String> excludeDocs = new HashSet<>(excludeDocLists.get(0)); 
			List<String> excludeDocL = new ArrayList<>(excludeDocs);
			for (int i = 1; i < excludeDocLists.size(); i++) {
				excludeDocs.retainAll(excludeDocLists.get(i)); // Retain only common documents
			}	
			
			Map<String,Map<String,Integer>> r = getPositions("C:\\Users\\TM\\eclipse-workspace\\IRProject\\src\\IR\\PositionalIndexOutput.txt", excludeDocL, ex);
			List<String> out = findDocsWithSequentialPositions(r, ex);
			Set<String> fu = new HashSet<>(out);
			commonDocs.removeAll(fu);
		}
		
		return new ArrayList<>(commonDocs);
	}

    
    private static String normalizeTerm(String term) {
        term = term.toLowerCase();

        if(term.equals("worser") || term.equals("worsen") || term.equals("worsened") || term.equals("worsening")) term = "worse";
        if(term.equals("rushes")) term = "rush";
        if(term.equals("mercies") || term.equals("merciful")) term = "mercy";
        if (term.endsWith("s") && !term.equals("brutus")) {
            term = term.substring(0, term.length() - 1);
        }
        else if (term.endsWith("ing")) {
            term = term.substring(0, term.length() - 3);
        }
        else if (term.endsWith("ed")) {
            term = term.substring(0, term.length() - 2);
        }
        else if (term.endsWith("er") && !term.equals("caeser")) {
            term = term.substring(0, term.length() - 2);
        }
        
        return term;
    }

}