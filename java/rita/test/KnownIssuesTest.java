package rita.test;

import static rita.support.QUnitStubs.deepEqual;
import static rita.support.QUnitStubs.equal;
import static rita.support.QUnitStubs.ok;
import static rita.support.QUnitStubs.setEqual;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import rita.RiGrammar;
import rita.RiLexicon;
import rita.RiString;
import rita.RiTa;
import rita.RiWordNet;
import rita.support.Constants;
import rita.support.JSONLexicon;
import rita.support.LetterToSound;

public class KnownIssuesTest implements Constants
{
  @Test
  public void testUntokenize()
  {
    String input[], output, expected;
    input = new String[] { "\"", "Oh", "God", ",", "\"", "he", "thought", "."};
    expected = "\"Oh God,\" he thought.";
    output = RiTa.untokenize(input);
    //System.out.println(expected);
    //System.out.println(output);
    deepEqual(output, expected);
  }
  
  @Test
  public void testSplitSentences()
  {
    //  Q. or A. at start of sentence 
    String input = "Q. Do I need a visa to visit Hong Kong? A. Visitors from most countries can enter Hong Kong without a visa for periods of seven to 180 days, depending on nationality.";
    String[] output = RiTa.splitSentences(input);
    System.out.println(Arrays.asList(output));
    String[] expected = new String[] { 
	"Q. Do I need a visa to visit Hong Kong?", 
	"A. Visitors from most countries can enter Hong Kong without a visa for periods of seven to 180 days, depending on nationality."};
    deepEqual(output, expected);
    
    // nextToken does not begin with an upper case
    input = "What did he buy? iPad or iPhone?";
    output = RiTa.splitSentences(input);
    expected = new String[] { 
	"What did he buy?",
	"iPad or iPhone?"};
    System.out.println(Arrays.asList(output));
    deepEqual(output, expected);
  }
  
  
  @Test
  public void testLTS() 
  {
    // TODO: these all fail -> CYRUS (create another array with the correct results) 
    String[] fails = { "be","bed","bled","break","bred","brooch","eyed","fed","fled","floors","great","guests","guise","he","keyed","led","me","noun","poured","purrs","red","rein","reined","rouge","rough","roughed","say","scares","scenes","scour","scoured","scours","seized","serge","she","shed","shred","sleight","slough","souls","sped","squared","squeak","stares","steak","suede","sure","tear","through","touch","touched","tough","toughs","we","wed","whoosh","yes","youth","youths" };

    RiLexicon.enabled = false; 
    String phones = RiTa.getPhonemes("youths");
    equal("y-uw-th-z", phones);
    RiLexicon.enabled = true;    
  }
  
  @SuppressWarnings("boxing")

  @Test
  public void testPosTagging()
  {
    String[] result = RiTa.getPosTags("fucking", false);
    System.out.println(Arrays.asList(result));
    String[] answer = new String[] { "jj" };
    deepEqual(result, answer);

    result = RiTa.getPosTags("shitting", false);
    System.out.println(Arrays.asList(result));
    answer = new String[] { "jj" };
    deepEqual(result, answer);

    result = RiTa.getPosTags("shitty", false);
    // System.out.println(Arrays.asList(result));
    answer = new String[] { "jj" };
    deepEqual(result, answer);

    result = RiTa.getPosTags("shitty", true);
    answer = new String[] { "a" };
    deepEqual(result, answer);
  }

  @Test
  public void testAnalyzeNums() // Cardinal numbers
  {
    // FROM RiStringTest
    Map features = new RiString("123.").analyze().features();
    ok(features);
    equal(features.get(PHONEMES), "w-ah-n-t-uw-th-r-iy");
    equal(features.get(SYLLABLES), "w-ah-n/t-uw/th-r-iy");
    equal(features.get(STRESSES), "0/0/0");

    features = new RiString("1 2 7").analyze().features();
    ok(features);
    System.out.println("Analyze: " + features);
    equal(features.get(PHONEMES), "w-ah-n t-uw s-eh-v-ax-n");
    equal(features.get(SYLLABLES), "w-ah-n t-uw s-eh-v/ax-n");
    equal(features.get(STRESSES), "1 1 1/0");

    /*
     * in RITAJS equal(features.get(SYLLABLES), "w-ah-n t-uw s-eh/v-ax-n");
     * equal(features.get(STRESSES), "0 0 1/0");
     */

    features = new RiString("123").analyze().features();
    ok(features);
    equal(features.get(PHONEMES), "w-ah-n-t-uw-th-r-iy");
    equal(features.get(SYLLABLES), "w-ah-n/t-uw/th-r-iy");
    equal(features.get(STRESSES), "0/0/0");

    /*
     * In RITAJS equal(features.get(PHONEMES), "w-ah-n-t-uw-th-r-iy");
     * equal(features.get(SYLLABLES), "w-ah-n/t-uw/th-r-iy");
     * equal(features.get(STRESSES), "0/0/0");
     */
  }

  @Test
  public void testAlliterations()
  {
    String[] result = new RiLexicon().alliterations("apples");
    //RiTa.out(result);
    ok(result.length > 0);
  }

  @Test
  public void testRandomIterator()
  {
    JSONLexicon lexicon = JSONLexicon.getInstance();
    long ts = System.currentTimeMillis();
    lexicon.randomPosIterator("nns");
    System.out.println("randomPosIterator in " + (System.currentTimeMillis() - ts) + "ms");
    equal("TODO:", "improve performance or remove!");
  }

  /*
   * Checks output of RiString.syllabify against syllabifications in rita_dict
   * Note(dch): added a hack to temp.fix bad output at end of RiString.syllabify, syllables like: l-ow-1
   */
  @Test
  public void testSyllabify() 
  {
    equal(1,1);
    RiLexicon lex = new RiLexicon();
    String[] failing = { "dog", "dragging", "mellow", "yoyo", "yo", "hawaii", "california",
	"elizabeth", "wikipedia" };
    for (int i = 0; i < failing.length; i++) {
      String phones = LetterToSound.getInstance().getPhones(failing[i]);
      String phones2 = lex.getRawPhones(failing[i], false);
      System.out.println(failing[i] + " -> "+phones + "[lts] ?= " + phones2
	  + " \t\t ["+phones.equals(phones2)+"]");
      //equal(phones, phones2);
    }
  }
  
    @Test
  public void testSyllabify2() 
  {
    String s = new RiLexicon().getRawPhones("yoyo",true);
    equal(s,"y-oy1 ow1"); // TODO: Is this correct? check in JS
  }
    
}
