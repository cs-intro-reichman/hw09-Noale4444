import java.util.HashMap;
import java.util.ListIterator;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In in = new In(fileName);
        String text = in.readAll(); 
        in.close();
        text.trim(); 
        for(int i = 0; i < text.length() - windowLength; i++){ 
            boolean hasNext = i+windowLength < text.length(); 

        while(hasNext) { 
         String window = text.substring(i, i+windowLength);
          List probs = new List();
             CharDataMap.put(window,probs); 
                 probs = CharDataMap.get(window);
                   probs.update(text.charAt(i+windowLength));
            }
        } 
            for(List probs: CharDataMap.values()) { 
                calculateProbabilities(probs);
            }
            
        }
       
    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {	
    int totalCount = 0;    			
	CharData[] dataArray = probs.toArray(); 
    for(CharData data : dataArray) {   
        totalCount+= data.count;
    }
    for(CharData data: dataArray) { 
        probs.update(data.chr);
    }
    double probability = 0.0;
    double commulativeProbability = 0.0;
    for(CharData data: dataArray) { 
        probability = (double)(data.count/totalCount);
        commulativeProbability+=probability;
        data.p = probability;
        data.cp = commulativeProbability;
       }     
    }

      
	

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double random = randomGenerator.nextDouble();
        ListIterator itr = new ListIterator(probs.getFirstNode());
        CharData start = probs.getFirst();
        if(random < start.cp) 
        return start.chr; 

        while(itr.hasNext()) { 
            CharData current = itr.next().cp; 
            if( random < current.cp) 
            return current.chr;
        }
        return ' ';
            
    }

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        if(initialText.length() < windowLength) 
        return initialText; 

       StringBuilder generateText = new StringBuilder(initialText);
       String window = initialText.substring(initialText.length()-windowLength);

       while(generateText.length() < textLength) { 

         if(CharDataMap.get(window) != null ){ 
            List probs = CharDataMap.get(window); 
            char nextChar = getRandomChar(probs); 
            generateText.append(nextChar); 
            window = window.substring(1) + nextChar;
         } 
         else 
           break;
        }
        return generateText.toString();
    }
   
    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]); 
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]); 
        boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];

        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
  }	
    }

