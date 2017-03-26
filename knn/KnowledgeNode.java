package knn;

import tags.Fact;
import tags.Tag;

/**
 * Represents a Knowledge Node in the Knowledge Node Network.
 */
public class KnowledgeNode { // (inputTag -> outputTags)
    Tag inputTag;
    int activation = 0; // int starts at 0 goes to 1 (can be sigmoid, or jump to 1). Increases when sees tag.
    int threshold = 1; // limit: When activation > threshold : fires output tags (outputTags array). These tags can be lists of rules or facts.
    int age = 0; // When a node is newly formed it has an age of zero.
    // When the node’s age increases to a value greater than or equal to K the node is then deleted.
    // The age parameter ages in a particular way.  It ages only if it is not used.  Every time a node is used the age is reset to zero.
    // If the node is not used after a tau amount of time it will age.
    // Ages linearly or using sigmoid.
    int strength; // TODO?: Which strength approach to take?
    int confidence; // TODO: Implement confidence.
    Tag[] outputTags;

    /**
     * Constructor
     * @param inputTag Input tag.
     * @param outputTags Output tags.
     */
    public KnowledgeNode(Tag inputTag, Tag[] outputTags) { // TODO?: Which fields should be parameters to the constructor?
        this.inputTag = inputTag;
        this.outputTags = outputTags;
    }

    /**
     * Creates a Knowledge Node (for debugging purposes). Assumes all tags are facts.
     * @param inputTag the input Tag of the Knowledge Node.
     * @param outputTags the output Tag of the Knowldge Node.
     */
    public KnowledgeNode(String inputTag, String[] outputTags) {
        this.inputTag = new Fact(inputTag);
        int n = outputTags.length;
        this.outputTags = new Tag[n];
        for (int i = 0; i < n; i++) {
            this.outputTags[i] = new Fact(outputTags[i]);
        }
    }

    /**
     * Ages the current Knowledge Node.
     * TODO: System will have (daily) timestamp, nodes will have timestamp updated at every firing. Look at difference between the two before deciding to fire
     */
    public void age() {
        age++;
    }
}