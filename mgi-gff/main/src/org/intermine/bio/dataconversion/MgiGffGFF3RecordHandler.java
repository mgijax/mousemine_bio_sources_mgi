package org.intermine.bio.dataconversion;

import org.apache.log4j.Logger;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.intermine.bio.io.gff3.GFF3Record;
import org.intermine.metadata.Model;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.Reference;
import java.lang.RuntimeException;
import org.intermine.objectstore.ObjectStoreException;

/**
 * A converter/retriever for the MgiGff dataset via GFF files.
 */

public class MgiGffGFF3RecordHandler extends GFF3RecordHandler
{
    private static final Logger LOG = Logger.getLogger(MgiGffGFF3RecordHandler.class);

    private Map<String,String> features = new HashMap<String,String>();
    private Item musculusOrganism = null;

    /**
     * Create a new MgiGffGFF3RecordHandler for the given data model.
     * @param model the model for which items will be created
     */
    public MgiGffGFF3RecordHandler (Model model) {
        super(model);
	//
        refsAndCollections.put("Transcript", "gene");
	refsAndCollections.put("MRNA", "gene");
	refsAndCollections.put("LincRNA", "gene");
	refsAndCollections.put("TRNA", "gene");
	refsAndCollections.put("SenseOverlapNcRNA", "gene");
	refsAndCollections.put("MiRNA", "gene");
	refsAndCollections.put("AberrantProcessedTranscript", "gene");
	refsAndCollections.put("ProcessedTranscript", "gene");
	refsAndCollections.put("NMDTranscript", "gene");
	refsAndCollections.put("RRNA", "gene");
	refsAndCollections.put("SnoRNA", "gene");
	refsAndCollections.put("SnRNA", "gene");
        //refsAndCollections.put("Exon", "transcript");
        refsAndCollections.put("Exon", "transcripts");
        refsAndCollections.put("CDS", "transcript");
        refsAndCollections.put("UTR", "transcript");
        refsAndCollections.put("FivePrimeUTR", "transcript");
        refsAndCollections.put("ThreePrimeUTR", "transcript");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(GFF3Record record) {
        // This method is called for every line of GFF3 file(s) being read.  Features and their
        // locations are already created but not stored so you can make changes here.  Attributes
        // are from the last column of the file are available in a map with the attribute name as
        // the key.   For example:
        //
        //     Item feature = getFeature();
        //     String symbol = record.getAttributes().get("symbol");
        //     feature.setAttrinte("symbol", symbol);
        //
        // Any new Items created can be stored by calling addItem().  For example:
        // 
        //     String geneIdentifier = record.getAttributes().get("gene");
        //     gene = createItem("Gene");
        //     gene.setAttribute("primaryIdentifier", geneIdentifier);
        //     addItem(gene);
        //
        // You should make sure that new Items you create are unique, i.e. by storing in a map by
        // some identifier. 
	Map<String, List<String>> attrs = record.getAttributes();
	Item feature = getFeature();
	feature.setReference("strain", getSequence().getReference("strain").getRefId());
	if (attrs.containsKey("mgi_id")) {
	    feature.setReference("canonical", getCanonicalRef(attrs.get("mgi_id").get(0)));
	}
    }
    public String getCanonicalRef(String mgiid) {
	String featureRef = features.get(mgiid);
	if (featureRef == null) {
	    try {
		Item feature = converter.createItem("SequenceFeature");
		// canonical features are associated with 10090
		feature.setReference("organism", getMusculusOrganism());
		feature.setAttribute("primaryIdentifier", mgiid);
		featureRef = feature.getIdentifier();
		features.put(mgiid, featureRef);
		converter.store(feature);
	    } catch (ObjectStoreException e) {
		throw new RuntimeException("Got objectStore exception.", e);
	    }
	}
	return featureRef;
    }
    public Item getMusculusOrganism() throws ObjectStoreException {
	String MM = "10090";
        if (musculusOrganism == null) {
	    Item sourceOrganism = getOrganism();
	    if (sourceOrganism.getAttribute("taxonId").getValue().equals(MM)) {
	        musculusOrganism = sourceOrganism ;
	    }
	    else {
		musculusOrganism = converter.createItem("Organism");
		musculusOrganism.setAttribute("taxonId", MM);
		converter.store(musculusOrganism);
	    }
        }
        return musculusOrganism;
    }
}
