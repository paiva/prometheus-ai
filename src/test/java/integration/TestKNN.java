package integration;

import com.google.inject.Guice;
import knn.api.KnowledgeNode;
import knn.api.KnowledgeNodeNetwork;
import knn.api.Tuple;
import knn.guice.KnowledgeNodeNetworkModule;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tags.Fact;
import tags.Recommendation;
import tags.Rule;
import tags.Tag;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Knowledge Node Network Unit Tests
 */
public class TestKNN {
    private KnowledgeNodeNetwork relation;
    private ArrayList<KnowledgeNode> animal = new ArrayList<>();

    @BeforeTest
    public void setup() {
        relation = Guice.createInjector(new KnowledgeNodeNetworkModule()).getInstance(KnowledgeNodeNetwork.class);
    }

    public void setupKNN(){
        relation.resetEmpty();
        try{
            BufferedReader br = new BufferedReader(new FileReader("./petData")); //change the directory for the integration file to run
            String line;
            while( (line = br.readLine()) != null){
                String[] info = line.split(";\\s+");
                KnowledgeNode kn = new KnowledgeNode(info);
                animal.add(kn);
            }
            br.close();
        }
        catch(Exception e){
            System.out.println(e);
        }

        for(int i=0; i<animal.size(); i++){
            relation.addKN(animal.get(i));
            //System.out.println(animal.get(i).toString());
            //System.out.println(" ");
        }
    }

    @Test
    public void testResetEmpty() throws Exception {
        relation.resetEmpty();
        Assert.assertTrue(relation.getActiveTags().isEmpty());
    }

    @Test
    public void fireTesting(){
        setupKNN();
        for(KnowledgeNode kn : animal){
            Tag t = kn.typeChecker();
            if(t.type == Tag.TagType.FACT){
                Fact f = (Fact)t;
                if(f.getPredicateName().equals("dog"))
                {
                    kn.setObjectTruth(100);
                    relation.fire(kn);
                }
                else if(f.getPredicateName().equals("cat")){
                    kn.setObjectTruth(100);
                    relation.fire(kn);
                }
            }
        }
        HashMap<Tag, Double> expectedActiveTags = new HashMap<>();
        expectedActiveTags.put(new Fact("animal(multicellular,vertebrate,invertebrate)"), 82.5);
        expectedActiveTags.put(new Fact("pet(dog>100,cat>80)"), 75.0);
        System.out.println("[fire Test] KNs to fire: dog(wow, carnivore) : 100.0, cat(meow, carnivore) : 100.0");
        System.out.println("[fire Test] Active Tags: " + relation.getActiveTags().toString());
        Assert.assertEquals(relation.getActiveTags(), expectedActiveTags);

        for(KnowledgeNode kn : animal){
            Tag t = kn.typeChecker();
            if(t.type == Tag.TagType.FACT){
                Fact f = (Fact)t;
                if(f.getPredicateName().equals("dog"))
                {
                    kn.setObjectTruth(85);
                    relation.updateConfidence(kn);
                }
            }
        }
        expectedActiveTags.put(new Fact("animal(multicellular,vertebrate,invertebrate)"), 75.75);
        expectedActiveTags.put(new Fact("pet(dog>100,cat>80)"), 69.0);
        System.out.println("[fire Test] KN with an updated confidence: dog(wow, carnivore) : 85.0");
        System.out.println("[fire Test] updated active Tags: " + relation.getActiveTags().toString());
        Assert.assertEquals(relation.getActiveTags(), expectedActiveTags);
        System.out.println("");
    }

    @Test
    public void exciteTest(){
        setupKNN();
        for(KnowledgeNode kn : animal){
            Tag t = kn.typeChecker();
            if(t.type == Tag.TagType.FACT ){
                Fact f = (Fact)t;
                if(f.getPredicateName().equals("dog")){
                    relation.excite(kn, 10);
                }
            }
        }
        HashMap<Tag, Double> expectedActiveTags = new HashMap<>();
        expectedActiveTags.put(new Fact("animal(multicellular,vertebrate,invertebrate)"), 90.0);
        expectedActiveTags.put(new Fact("dog(wow, carnivore)"), 100.0);
        expectedActiveTags.put(new Fact("pet(dog>100,cat>80)"), 80.0);
        System.out.println("[excite integration] Tags to excite 1st: dog(wow, carnivore) : 10");
        System.out.println("[excite integration] Active Tags: " + relation.getActiveTags().toString());
        Assert.assertEquals(relation.getActiveTags(), expectedActiveTags);

        for(KnowledgeNode kn : animal){
            Tag t = kn.typeChecker();
            if(t.type == Tag.TagType.FACT){
                Fact f = (Fact)t;
                if(f.getPredicateName().equals("husky")){
                    relation.excite(kn, 10);
                }
                else if(f.getPredicateName().equals("cat")){
                    relation.excite(kn, 10);
                }
            }
        }
        System.out.println("[excite integration] Tags to excite 2nd: husky(Ranger,male,length>58,weight=26) : 10, cat(meow, carnivore) : 10");
        expectedActiveTags.put(new Fact("cat(meow, carnivore)"), 100.0);
        expectedActiveTags.put(new Fact("pet(dog>100,cat>80)"), 69.0);
        expectedActiveTags.put(new Fact("husky(Ranger,male,length>58,weight=26)"), 100.0);
        expectedActiveTags.put(new Fact("animal(multicellular,vertebrate,invertebrate)"), 75.75);
        expectedActiveTags.put(new Fact("dog(wow, carnivore)"), 85.0);
        System.out.println("[excite integration] Active Tags: " + relation.getActiveTags().toString());
        Assert.assertEquals(relation.getActiveTags(), expectedActiveTags);
        System.out.println("");
    }

    @Test
    public void CreateKNFromTupleTest(){
        relation.resetEmpty();
        Tuple tp1 = new Tuple("monkey(intelligent,length>50,weight>3)", 10);
        Tuple tp2 = new Tuple("@isAnimal(calm,bark)", 10);
        Tuple tp3 = new Tuple("friend(nice,kind) -> @meet(community,people>2)", 10);
        Tuple tp4 = new Tuple("chair", 10);

        relation.createKNfromTuple(tp1);
        relation.createKNfromTuple(tp2);
        relation.createKNfromTuple(tp3);
        relation.createKNfromTuple(tp4);
        HashMap<Tag, Double> expectedInputTags = new HashMap<>();
        expectedInputTags.put(new Fact("monkey(intelligent,length>50,weight>3)"), 0.0);
        expectedInputTags.put(new Recommendation("@isAnimal(calm,bark)"), 0.0);
        expectedInputTags.put(new Rule("friend(nice,kind) -> @meet(community,people>2)"), 0.0);
        expectedInputTags.put(new Fact("chair()"), 0.0);
        System.out.println("[CreateKNFromTuple integration] Input Tags: " + relation.getInputTags().toString());
        Assert.assertEquals(relation.getInputTags(), expectedInputTags);
        Assert.assertTrue(relation.getActiveTags().isEmpty());
        System.out.println("");
    }

    @Test
    public void getInputForForwardSearchTest(){
        relation.resetEmpty();
        Tuple tp1 = new Tuple("monkey", 10);
        Tuple tp2 = new Tuple("isAnimal", 10);
        Tuple tp3 = new Tuple("{ [[friend([nice, kind]) 100.0% ]]=>[[@meet([community, people > 2]) 100.0% ]]100.0% }", 10);
        Tuple tp4 = new Tuple("banana", 10);
        ArrayList<Tuple> NNoutputs = new ArrayList<>();
        NNoutputs.add(tp1);
        NNoutputs.add(tp2);
        NNoutputs.add(tp3);
        NNoutputs.add(tp4);

        String[] info1 = {"monkey(intelligent,length>50,weight>3)", "100"};
        String[] info2 = {"@isAnimal(calm,bark)", "100"};
        String[] info3 = {"friend(nice,kind) -> @meet(community,people>2)", "100"};
        relation.addKN(new KnowledgeNode(info1));
        relation.addKN(new KnowledgeNode(info2));
        relation.addKN(new KnowledgeNode(info3));

        relation.getInputForForwardSearch(NNoutputs);
        HashMap<Tag, Double> expectedInputTags = new HashMap<>();
        expectedInputTags.put(new Fact("monkey(intelligent,length>50,weight>3)"), 100.0);
        expectedInputTags.put(new Recommendation("@isAnimal(calm,bark)"), 100.0);
        expectedInputTags.put(new Rule("friend(nice,kind) -> @meet(community,people>2)"), 100.0);
        expectedInputTags.put(new Fact("banana()"), 0.0);
        System.out.println("[getInputForForwardSearchTest] Output from NN: monkey, @isAnimal");
        System.out.println("[getInputForForwardSearchTest] A wanted rule from the Meta: friend(nice,kind) -> @meet(community,people>2)");
        System.out.println("[getInputForForwardSearchTest] Input Tags: " + relation.getInputTags().toString());
        Assert.assertEquals(relation.getInputTags(), expectedInputTags);
        System.out.println("");
    }

    @Test
    public void getInputForBackwardSearchTest(){
        Tuple tp1 = new Tuple("Tiger", 10);
        Tuple tp2 = new Tuple("isTiger", 10);
        Tuple tp3 = new Tuple("{ [[friend([nice, kind]) 100.0% ]]=>[[@meet([community, people > 2]) 100.0% ]]100.0% }", 10);
        Tuple tp4 = new Tuple("apple", 10);
        ArrayList<Tuple> NNoutputs = new ArrayList<>();
        NNoutputs.add(tp1);
        NNoutputs.add(tp2);
        NNoutputs.add(tp3);
        NNoutputs.add(tp4);

        String[] info1 = {"Tiger(carnivore,length>50,weight>90)", "100"};
        String[] info2 = {"@isTiger(danger,run)", "100"};
        String[] info3 = {"friend(nice,kind) -> @meet(community,people>2)", "100"};
        relation.addKN(new KnowledgeNode(info1));
        relation.addKN(new KnowledgeNode(info2));
        relation.addKN(new KnowledgeNode(info3));

        relation.getInputForBackwardSearch(NNoutputs);
        HashMap<Tag, Double> expectedInputTags = new HashMap<>();
        expectedInputTags.put(new Fact("Tiger(carnivore,length>50,weight>90)"), 100.0);
        expectedInputTags.put(new Recommendation("@isTiger(danger,run)"), 100.0);
        expectedInputTags.put(new Rule("friend(nice,kind) -> @meet(community,people>2)"), 100.0);
        expectedInputTags.put(new Fact("apple()"), 0.0);
        System.out.println("[getInputForBackwardSearchTest] Output from NN: monkey, @isAnimal");
        System.out.println("[getInputForBackwardSearchTest] A wanted rule from the Meta: friend(nice,kind) -> @meet(community,people>2)");
        System.out.println("[getInputForBackwardSearchTest] Input Tags: " + relation.getInputTags().toString());
        Assert.assertEquals(relation.getInputTags(), expectedInputTags);
        System.out.println("");
    }

    @Test
    public void KNtoStringTest(){
        String[] info1 = {"Tiger(carnivore,length>50,weight>90)", "100", "monkey(intelligent,length>50,weight>3)", "100"};
        KnowledgeNode kn1 = new KnowledgeNode(info1);
        String[] info2 = {"@isTiger(danger,run)", "100"};
        KnowledgeNode kn2 = new KnowledgeNode(info2);
        String[] info3 = {"friend(nice,kind) -> @meet(community,people>2)", "100"};
        KnowledgeNode kn3 = new KnowledgeNode(info3);
        System.out.println("[KNtoStringTest]: "+ kn1.toString());
        System.out.println("[KNtoStringTest]: "+ kn2.toString());
        System.out.println("[KNtoStringTest]: "+ kn3.toString());
    }
}